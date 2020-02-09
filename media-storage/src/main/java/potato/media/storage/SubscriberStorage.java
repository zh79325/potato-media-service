package potato.media.storage;

import potato.media.common.pull.StreamSubscriber;

import java.util.Map;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:28
 * Copyright [2020] [zh_zhou]
 */
public interface SubscriberStorage {
    void subscribe(String owner, StreamSubscriber subscriber);


    StreamSubscriber unsubscribe(String owner, String targetUid);

    Map<String, StreamSubscriber> getSubscribers(String userId);
}
