package potato.media.storage.impl;

import org.springframework.stereotype.Component;
import potato.media.common.pull.StreamSubscriber;
import potato.media.common.stream.ClientStream;
import potato.media.storage.StreamStorage;
import potato.media.storage.SubscriberStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:51
 * Copyright [2020] [zh_zhou]
 */
@Component
public class MemStorage implements StreamStorage, SubscriberStorage {
    Map<String, ClientStream> streamMap = new HashMap<>();
    Map<String, Map<String, StreamSubscriber>> subMap = new ConcurrentHashMap<>();

    @Override
    public ClientStream getStream(long streamId) {
        return streamMap.get(streamId);
    }


    @Override
    public void subscribe(String owner, StreamSubscriber subscriber) {

    }

    @Override
    public StreamSubscriber unsubscribe(String owner, String targetUid) {
        Map<String, StreamSubscriber> subscriberMap=subMap.get(targetUid);
        if(subscriberMap==null){
            return null;
        }
        StreamSubscriber subscriber= subscriberMap.remove(owner);
        return subscriber;
    }

    @Override
    public Map<String, StreamSubscriber> getSubscribers(String userId) {
        return subMap.get(userId);
    }


}
