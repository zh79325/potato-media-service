package potato.media.storage;

import potato.media.common.pull.StreamSubscriber;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:28
 * Copyright [2020] [zh_zhou]
 */
public interface SubscriberStorage {
    void saveSubscriber(long streamId, StreamSubscriber subscriber);

    void removeSubscriber(long streamId, StreamSubscriber subscriber);
}
