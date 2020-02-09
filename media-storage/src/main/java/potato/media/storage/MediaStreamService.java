package potato.media.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import potato.media.common.pull.StreamSubscriber;
import potato.media.common.stream.ClientStream;

import java.util.Map;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:35
 * Copyright [2020] [zh_zhou]
 */
@Service
public class MediaStreamService {

    StreamStorage streamStorage;

    SubscriberStorage subscriberStorage;

    @Autowired


    public void setStreamStorage(StreamStorage streamStorage) {
        this.streamStorage = streamStorage;
    }

    public void setSubscriberStorage(SubscriberStorage subscriberStorage) {
        this.subscriberStorage = subscriberStorage;
    }


    public StreamSubscriber unsubscribe(String userId, String targetUid) {
        return subscriberStorage.unsubscribe(userId, targetUid);
    }

    public void subscribe(String userId, StreamSubscriber subscriber) {
        subscriberStorage.subscribe(userId, subscriber);
    }



    public Map<String, StreamSubscriber> getSubscribers(String userId) {
        Map<String, StreamSubscriber> subscribers = subscriberStorage.getSubscribers(userId);
        return subscribers;
    }
}
