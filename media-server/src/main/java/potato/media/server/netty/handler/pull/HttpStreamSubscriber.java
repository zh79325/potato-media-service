package potato.media.server.netty.handler.pull;

import io.netty.channel.Channel;
import potato.media.common.pull.StreamSubscriber;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:17
 * Copyright [2020] [zh_zhou]
 */
public class HttpStreamSubscriber extends StreamSubscriber {
    private Channel channel;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
