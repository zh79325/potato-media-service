package potato.media.server.netty.handler.pull;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import potato.media.common.message.MediaStreamMessage;
import potato.media.server.netty.handler.AbstractChannelHandler;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:28
 * Copyright [2020] [zh_zhou]
 */
public class ClientPullStreamHandler extends AbstractChannelHandler  {


    @Override
    protected void messageRead(ChannelHandlerContext ctx, MediaStreamMessage msg) {

    }
}
