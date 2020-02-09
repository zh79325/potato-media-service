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
    Map<Long, ClientStream> streamMap = new HashMap<>();
    Map<Long, Map<String, StreamSubscriber>> subMap = new ConcurrentHashMap<>();

    @Override
    public ClientStream getStream(long streamId) {
        return streamMap.get(streamId);
    }

    @Override
    public void saveSubscriber(long streamId, StreamSubscriber subscriber) {
        if (subMap.containsKey(streamId)) {
            subMap.put(streamId, new ConcurrentHashMap<>());
        }
        subMap.get(streamId).put(subscriber.getId(), subscriber);
    }

    @Override
    public void removeSubscriber(long streamId, StreamSubscriber subscriber) {
        if (!subMap.containsKey(streamId)) {
            return;
        }
        Map<String, StreamSubscriber> map = subMap.get(streamId);
        map.remove(subscriber.getId());
        if (map.isEmpty()) {
            subMap.remove(streamId);
        }
    }
}
