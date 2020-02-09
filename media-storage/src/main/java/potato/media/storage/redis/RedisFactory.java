package potato.media.storage.redis;



import java.io.File;

/**
 * @author zh_zhou
 * created at 2020/02/09 15:41
 * Copyright [2020] [zh_zhou]
 */
public class RedisFactory {
    public static RedisProvider GetProvider(String cluster) {
        int port=6379;
        return new LocalRedisPorvider("localhost",port);
    }
}
