package potato.media.common.stream;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:48
 * Copyright [2020] [zh_zhou]
 */
public class ClientStream {
    long streamId;
    byte[]head;

    public long getStreamId() {
        return streamId;
    }

    public void setStreamId(long streamId) {
        this.streamId = streamId;
    }

    public byte[] getHead() {
        return head;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }
}
