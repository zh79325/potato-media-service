package potato.media.server.netty.handler.push;

import io.netty.channel.ChannelHandlerContext;
import potato.media.common.message.MediaStreamMessage;
import potato.media.server.netty.handler.AbstractChannelHandler;

/**
 * @author zh_zhou
 * created at 2020/02/09 21:45
 * Copyright [2020] [zh_zhou]
 */
public class ClientPushStreamHandler extends AbstractChannelHandler {
    @Override
    protected void messageRead(ChannelHandlerContext ctx, MediaStreamMessage msg) {

    }
}
