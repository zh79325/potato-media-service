package potato.media.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import potato.media.common.pull.StreamSubscriber;
import potato.media.common.stream.ClientStream;

import javax.xml.ws.ServiceMode;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:35
 * Copyright [2020] [zh_zhou]
 */
@Service
public class MediaStreamService {

    StreamStorage streamStorage;

    SubscriberStorage subscriberStorage;

    public void setStreamStorage(StreamStorage streamStorage) {
        this.streamStorage = streamStorage;
    }

    public void setSubscriberStorage(SubscriberStorage subscriberStorage) {
        this.subscriberStorage = subscriberStorage;
    }

    public ClientStream getStream(long streamId) {
        return streamStorage.getStream(streamId);
    }

    public void registerSubscribe(StreamSubscriber subscriber) {
        subscriberStorage.saveSubscriber(subscriber.getStreamId(), subscriber);
    }

    public void unregister(StreamSubscriber subscriber) {
        if (subscriber == null) {
            return;
        }
        subscriberStorage.removeSubscriber(subscriber.getStreamId(), subscriber);
    }
}
