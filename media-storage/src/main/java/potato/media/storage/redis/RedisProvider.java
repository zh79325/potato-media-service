package potato.media.storage.redis;

import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.Set;

/**
 * @author zh_zhou
 * created at 2020/02/09 15:41
 * Copyright [2020] [zh_zhou]
 */
public interface RedisProvider {
    Long hset(String key, String hKey, String hValue);

    Long sadd(String key, String... value);

    Long publish(String key, String value);

    Map<String, String> hgetAll(String key);

    Long hdel(String key, String ...fields);

    Set<String> smembers(String key);

    void psubscribe(final JedisPubSub jedisPubSub, final String... patterns);
}
