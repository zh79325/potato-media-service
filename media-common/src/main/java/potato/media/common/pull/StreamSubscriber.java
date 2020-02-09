package potato.media.common.pull;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:14
 * Copyright [2020] [zh_zhou]
 */
public class StreamSubscriber {
    private String channelId;
    private String host;
    private long streamId;
    private String id;

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setStreamId(long streamId) {
        this.streamId = streamId;
    }

    public long getStreamId() {
        return streamId;
    }

    public void buildId() {
        id= System.currentTimeMillis()+RandomStringUtils.random(10,true,true);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
