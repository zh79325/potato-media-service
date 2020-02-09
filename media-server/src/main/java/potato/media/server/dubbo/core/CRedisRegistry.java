package potato.media.server.dubbo.core;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.URLBuilder;
import org.apache.dubbo.common.constants.RemotingConstants;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.*;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.rpc.RpcException;
import potato.media.storage.redis.RedisFactory;
import potato.media.storage.redis.RedisProvider;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.common.constants.RegistryConstants.*;
import static org.apache.dubbo.registry.Constants.*;

/**
 * CRedisRegistry
 */
public class CRedisRegistry extends FailbackRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CRedisRegistry.class);

    private static final int DEFAULT_REDIS_PORT = 6379;

    private final static String DEFAULT_ROOT = "potato_media2";

    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryExpireTimer", true));

    private final ScheduledFuture<?> expireFuture;

    private final String root;

    private final RedisProvider jedisPools;

    private final ConcurrentMap<String, Notifier> notifiers = new ConcurrentHashMap<>();

    private final int reconnectPeriod;

    private final int expirePeriod;

    private volatile boolean admin = false;

    private boolean replicate;

    public CRedisRegistry(String redisCluster, URL url) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setTestOnBorrow(url.getParameter("test.on.borrow", true));
        config.setTestOnReturn(url.getParameter("test.on.return", false));
        config.setTestWhileIdle(url.getParameter("test.while.idle", false));
        if (url.getParameter("max.idle", 0) > 0) {
            config.setMaxIdle(url.getParameter("max.idle", 0));
        }
        if (url.getParameter("min.idle", 0) > 0) {
            config.setMinIdle(url.getParameter("min.idle", 0));
        }
        if (url.getParameter("max.active", 0) > 0) {
            config.setMaxTotal(url.getParameter("max.active", 0));
        }
        if (url.getParameter("max.total", 0) > 0) {
            config.setMaxTotal(url.getParameter("max.total", 0));
        }
        if (url.getParameter("max.wait", url.getParameter("timeout", 0)) > 0) {
            config.setMaxWaitMillis(url.getParameter("max.wait", url.getParameter("timeout", 0)));
        }
        if (url.getParameter("num.tests.per.eviction.run", 0) > 0) {
            config.setNumTestsPerEvictionRun(url.getParameter("num.tests.per.eviction.run", 0));
        }
        if (url.getParameter("time.between.eviction.runs.millis", 0) > 0) {
            config.setTimeBetweenEvictionRunsMillis(url.getParameter("time.between.eviction.runs.millis", 0));
        }
        if (url.getParameter("min.evictable.idle.time.millis", 0) > 0) {
            config.setMinEvictableIdleTimeMillis(url.getParameter("min.evictable.idle.time.millis", 0));
        }

        String cluster = url.getParameter("cluster", "failover");
        if (!"ipfailover".equals(cluster) && !"replicate".equals(cluster)) {
            throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
        }
        replicate = "replicate".equals(cluster);
        this.jedisPools = RedisFactory.GetProvider(redisCluster);
        List<String> addresses = new ArrayList<>();
        addresses.add(url.getAddress());
        String[] backups = url.getParameter(RemotingConstants.BACKUP_KEY, new String[0]);
        if (ArrayUtils.isNotEmpty(backups)) {
            addresses.addAll(Arrays.asList(backups));
        }


        this.reconnectPeriod = url.getParameter(REGISTRY_RECONNECT_PERIOD_KEY, DEFAULT_REGISTRY_RECONNECT_PERIOD);
        String group = url.getParameter(GROUP_KEY, DEFAULT_ROOT);
        if (!group.startsWith(PATH_SEPARATOR)) {
            group = PATH_SEPARATOR + group;
        }
        if (!group.endsWith(PATH_SEPARATOR)) {
            group = group + PATH_SEPARATOR;
        }
        this.root = group;

        this.expirePeriod = url.getParameter(SESSION_TIMEOUT_KEY, DEFAULT_SESSION_TIMEOUT);
        this.expireFuture = expireExecutor.scheduleWithFixedDelay(() -> {
            try {
                deferExpired(); // Extend the expiration time
            } catch (Throwable t) { // Defensive fault tolerance
                logger.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
            }
        }, expirePeriod / 2, expirePeriod / 2, TimeUnit.MILLISECONDS);
    }

    private void deferExpired() {
        try {
            for (URL url : new HashSet<>(getRegistered())) {
                if (url.getParameter(DYNAMIC_KEY, true)) {
                    String key = toCategoryPath(url);
                    if (jedisPools.hset(key, url.toFullString(), String.valueOf(System.currentTimeMillis() + expirePeriod))==1) {
                        publishKeyValue(key, jedisPools, REGISTER);
                    }
                }
            }
            if (admin) {
                clean(jedisPools);
            }
            if (!replicate) {
                return;//  If the server side has synchronized data, just write a single machine
            }
        } catch (Throwable t) {
            logger.warn("Failed to write provider heartbeat to redis registry. registry: , cause: " + t.getMessage(), t);
        }
    }

    static final String ALL_KEYS = DEFAULT_ROOT+":all:keys";

    private Long publishKeyValue(String key, RedisProvider jedisPools, String register) {
        jedisPools.sadd(ALL_KEYS, key);
        return jedisPools.publish(key, register);
    }

    // The monitoring center is responsible for deleting outdated dirty data
    private void clean(RedisProvider jedis) {
        Set<String> keys = getAllKeys(root + ANY_VALUE, jedis);
        if (CollectionUtils.isNotEmpty(keys)) {
            for (String key : keys) {
                Map<String, String> values = jedis.hgetAll(key);
                if (CollectionUtils.isNotEmptyMap(values)) {
                    boolean delete = false;
                    long now = System.currentTimeMillis();
                    for (Map.Entry<String, String> entry : values.entrySet()) {
                        URL url = URL.valueOf(entry.getKey());
                        if (url.getParameter(DYNAMIC_KEY, true)) {
                            long expire = Long.parseLong(entry.getValue());
                            if (expire < now) {
                                jedis.hdel(key, entry.getKey());
                                delete = true;
                                if (logger.isWarnEnabled()) {
                                    logger.warn("Delete expired key: " + key + " -> value: " + entry.getKey() + ", expire: " + new Date(expire) + ", now: " + new Date(now));
                                }
                            }
                        }
                    }
                    if (delete) {
                        publishKeyValue(key, jedis, UNREGISTER);
                    }
                }
            }
        }
    }

    @Override
    public boolean isAvailable() {

        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            expireFuture.cancel(true);
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        try {
            for (Notifier notifier : notifiers.values()) {
                notifier.shutdown();
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }

        ExecutorUtil.gracefulShutdown(expireExecutor, expirePeriod);
    }

    @Override
    public void doRegister(URL url) {
        String key = toCategoryPath(url);
        String value = url.toFullString();
        String expire = String.valueOf(System.currentTimeMillis() + expirePeriod);
        boolean success = false;
        RpcException exception = null;
        try {
            jedisPools.hset(key, value, expire);
            publishKeyValue(key, jedisPools, REGISTER);
            success = true;
        } catch (Throwable t) {
            exception = new RpcException("Failed to register service to redis registry. registry: , service: " + url + ", cause: " + t.getMessage(), t);
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void doUnregister(URL url) {
        String key = toCategoryPath(url);
        String value = url.toFullString();
        RpcException exception = null;
        boolean success = false;
        try {
            jedisPools.hdel(key, value);
            publishKeyValue(key, jedisPools, UNREGISTER);
            success = true;
        } catch (Throwable t) {
            exception = new RpcException("Failed to unregister service to redis registry. registry: , service: " + url + ", cause: " + t.getMessage(), t);
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        String service = toServicePath(url);
        Notifier notifier = notifiers.get(service);
        if (notifier == null) {
            Notifier newNotifier = new Notifier(service);
            notifiers.putIfAbsent(service, newNotifier);
            notifier = notifiers.get(service);
            if (notifier == newNotifier) {
                notifier.start();
            }
        }
        boolean success = false;
        RpcException exception = null;
        try {
            if (service.endsWith(ANY_VALUE)) {
                admin = true;
                Set<String> keys = getAllKeys(service, jedisPools);
                if (CollectionUtils.isNotEmpty(keys)) {
                    Map<String, Set<String>> serviceKeys = new HashMap<>();
                    for (String key : keys) {
                        String serviceKey = toServicePath(key);
                        Set<String> sk = serviceKeys.computeIfAbsent(serviceKey, k -> new HashSet<>());
                        sk.add(key);
                    }
                    for (Set<String> sk : serviceKeys.values()) {
                        doNotify(jedisPools, sk, url, Collections.singletonList(listener));
                    }
                }
            } else {
                doNotify(jedisPools, getAllKeys(service + PATH_SEPARATOR + ANY_VALUE, jedisPools), url, Collections.singletonList(listener));
            }
            success = true;
        } catch (Throwable t) { // Try the next server
            exception = new RpcException("Failed to subscribe service from redis registry. registry: , service: " + url + ", cause: " + t.getMessage(), t);
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    private Set<String> getAllKeys(String pattern, RedisProvider jedisPools) {
        if (pattern.endsWith("*")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        Set<String> keys = jedisPools.smembers(ALL_KEYS);
        Set<String> result = new HashSet<>();
        for (String key : keys) {
            if (key.startsWith(pattern)) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
    }

    private void doNotify(RedisProvider jedis, String key) {
        for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<>(getSubscribed()).entrySet()) {
            doNotify(jedis, Collections.singletonList(key), entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    private void doNotify(RedisProvider jedis, Collection<String> keys, URL url, Collection<NotifyListener> listeners) {
        if (keys == null || keys.isEmpty()
                || listeners == null || listeners.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        List<URL> result = new ArrayList<>();
        List<String> categories = Arrays.asList(url.getParameter(CATEGORY_KEY, new String[0]));
        String consumerService = url.getServiceInterface();
        for (String key : keys) {
            if (!ANY_VALUE.equals(consumerService)) {
                String providerService = toServiceName(key);
                if (!providerService.equals(consumerService)) {
                    continue;
                }
            }
            String category = toCategoryName(key);
            if (!categories.contains(ANY_VALUE) && !categories.contains(category)) {
                continue;
            }
            List<URL> urls = new ArrayList<>();
            Map<String, String> values = jedis.hgetAll(key);
            if (CollectionUtils.isNotEmptyMap(values)) {
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    URL u = URL.valueOf(entry.getKey());
                    if (!u.getParameter(DYNAMIC_KEY, true)
                            || Long.parseLong(entry.getValue()) >= now) {
                        if (UrlUtils.isMatch(url, u)) {
                            urls.add(u);
                        }
                    }
                }
            }
            if (urls.isEmpty()) {
                urls.add(URLBuilder.from(url)
                        .setProtocol(EMPTY_PROTOCOL)
                        .setAddress(ANYHOST_VALUE)
                        .setPath(toServiceName(key))
                        .addParameter(CATEGORY_KEY, category)
                        .build());
            }
            result.addAll(urls);
            if (logger.isInfoEnabled()) {
                logger.info("redis notify: " + key + " = " + urls);
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        for (NotifyListener listener : listeners) {
            notify(url, listener, result);
        }
    }

    private String toServiceName(String categoryPath) {
        String servicePath = toServicePath(categoryPath);
        return servicePath.startsWith(root) ? servicePath.substring(root.length()) : servicePath;
    }

    private String toCategoryName(String categoryPath) {
        int i = categoryPath.lastIndexOf(PATH_SEPARATOR);
        return i > 0 ? categoryPath.substring(i + 1) : categoryPath;
    }

    private String toServicePath(String categoryPath) {
        int i;
        if (categoryPath.startsWith(root)) {
            i = categoryPath.indexOf(PATH_SEPARATOR, root.length());
        } else {
            i = categoryPath.indexOf(PATH_SEPARATOR);
        }
        return i > 0 ? categoryPath.substring(0, i) : categoryPath;
    }

    private String toServicePath(URL url) {
        return root + url.getServiceInterface();
    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + PATH_SEPARATOR + url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
    }

    private class NotifySub extends JedisPubSub {

        private final RedisProvider jedisPool;

        public NotifySub(RedisProvider jedisPool) {
            this.jedisPool = jedisPool;
        }

        @Override
        public void onMessage(String key, String msg) {
            if (logger.isInfoEnabled()) {
                logger.info("redis event: " + key + " = " + msg);
            }
            if (msg.equals(REGISTER)
                    || msg.equals(UNREGISTER)) {
                try {
                    try {
                        doNotify(jedisPool, key);
                    } finally {

                    }
                } catch (Throwable t) { // TODO Notification failure does not restore mechanism guarantee
                    logger.error(t.getMessage(), t);
                }
            }
        }

        @Override
        public void onPMessage(String pattern, String key, String msg) {
            onMessage(key, msg);
        }

        @Override
        public void onSubscribe(String key, int num) {
        }

        @Override
        public void onPSubscribe(String pattern, int num) {
        }

        @Override
        public void onUnsubscribe(String key, int num) {
        }

        @Override
        public void onPUnsubscribe(String pattern, int num) {
        }

    }

    private class Notifier extends Thread {

        private final String service;
        private final AtomicInteger connectSkip = new AtomicInteger();
        private final AtomicInteger connectSkipped = new AtomicInteger();

        private volatile boolean first = true;
        private volatile boolean running = true;
        private volatile int connectRandom;

        public Notifier(String service) {
            super.setDaemon(true);
            super.setName("DubboRedisSubscribe");
            this.service = service;
        }

        private void resetSkip() {
            connectSkip.set(0);
            connectSkipped.set(0);
            connectRandom = 0;
        }

        private boolean isSkip() {
            int skip = connectSkip.get(); // Growth of skipping times
            if (skip >= 10) { // If the number of skipping times increases by more than 10, take the random number
                if (connectRandom == 0) {
                    connectRandom = ThreadLocalRandom.current().nextInt(10);
                }
                skip = 10 + connectRandom;
            }
            if (connectSkipped.getAndIncrement() < skip) { // Check the number of skipping times
                return true;
            }
            connectSkip.incrementAndGet();
            connectSkipped.set(0);
            connectRandom = 0;
            return false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    if (!isSkip()) {
                        try {

                            if (service.endsWith(ANY_VALUE)) {
                                if (first) {
                                    first = false;
                                    Set<String> keys = getAllKeys(service, jedisPools);
                                    if (CollectionUtils.isNotEmpty(keys)) {
                                        for (String s : keys) {
                                            doNotify(jedisPools, s);
                                        }
                                    }
                                    resetSkip();
                                }
                                jedisPools.psubscribe(new NotifySub(jedisPools), service); // blocking
                            } else {
                                if (first) {
                                    first = false;
                                    doNotify(jedisPools, service);
                                    resetSkip();
                                }
                                jedisPools.psubscribe(new NotifySub(jedisPools), service + PATH_SEPARATOR + ANY_VALUE); // blocking
                            }

                        } catch (Throwable t) {
                            logger.error(t.getMessage(), t);
                            sleep(reconnectPeriod);
                        }
                    }
                } catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }
            }
        }

        public void shutdown() {
            try {
                running = false;
            } catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }
        }

    }

}
