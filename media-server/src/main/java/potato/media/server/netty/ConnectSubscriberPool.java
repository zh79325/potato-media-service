package potato.media.server.netty;

import potato.media.server.netty.handler.pull.HttpStreamSubscriber;

import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:21
 * Copyright [2020] [zh_zhou]
 */
public class ConnectSubscriberPool {
    static Map<String, HttpStreamSubscriber> subscriberMap = new ConcurrentHashMap<>();

    public static void add(HttpStreamSubscriber subscriber) {
        subscriberMap.put(subscriber.getChannelId(),subscriber);
    }

    public static void remove(HttpStreamSubscriber subscriber) {
        if(subscriber==null){
            return;
        }
        subscriberMap.remove(subscriber.getChannelId());
    }

    public static HttpStreamSubscriber get(String channelId){
        return subscriberMap.get(channelId);
    }
}
