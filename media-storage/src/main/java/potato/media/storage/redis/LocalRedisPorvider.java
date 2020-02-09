package potato.media.storage.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.Set;

/**
 * @author zh_zhou
 * created at 2020/02/09 16:00
 * Copyright [2020] [zh_zhou]
 */
public class LocalRedisPorvider implements RedisProvider {
    Jedis jedis;
    Jedis subjedis;
    Jedis pubjedis;

    public LocalRedisPorvider(String host, int port) {
        jedis = new Jedis(host, port);
        subjedis=new Jedis(host, port);
        pubjedis=new Jedis(host,port);
    }

    @Override
    public Long hset(String key, String hKey, String hValue) {
        return jedis.hset(key, hKey, hValue);
    }

    @Override
    public Long sadd(String key, String... value) {
        return jedis.sadd(key, value);
    }

    @Override
    public Long publish(String key, String value) {
        return pubjedis.publish(key, value);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return jedis.hgetAll(key);
    }

    @Override
    public Long hdel(String key, String... fields) {
        return jedis.hdel(key, fields);
    }

    @Override
    public Set<String> smembers(String key) {
        return jedis.smembers(key);
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        subjedis.psubscribe(jedisPubSub,patterns);
    }
}
