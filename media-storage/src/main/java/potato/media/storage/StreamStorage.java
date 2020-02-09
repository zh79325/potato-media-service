package potato.media.storage;

import potato.media.common.pull.StreamSubscriber;
import potato.media.common.stream.ClientStream;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:50
 * Copyright [2020] [zh_zhou]
 */
public interface StreamStorage {
    ClientStream getStream(long streamId);


}
