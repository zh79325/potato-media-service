package potato.media.server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.web.HttpRequestHandler;
import potato.media.common.message.MediaStreamHead;
import potato.media.common.message.MediaStreamMessage;
import potato.media.common.message.MediaStreamType;
import potato.media.server.netty.handler.pull.ClientPullStreamHandler;
import potato.media.server.netty.handler.pull.HttpFileServerHandler;
import potato.media.server.netty.handler.push.ClientPushStreamHandler;

import java.util.List;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:20
 * Copyright [2020] [zh_zhou]
 */
public class SocketChooseHandle extends SimpleChannelInboundHandler<MediaStreamMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MediaStreamMessage msg) throws Exception {
        MediaStreamHead head=msg.getHead();
        if(head==null){
            close(ctx);
            return;
        }
        MediaStreamType type=head.getType();
        if(type==null){
            close(ctx);
            return;
        }
        switch (type){
            case PullClientInit:
                registerPullHandler(ctx);
                break;
            case PushClientInit:
                registerPushHandler(ctx);
                break;
            default:
                close(ctx);
                break;
        }
        ctx.pipeline().remove(this.getClass());
    }

    private void registerPushHandler(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast("client-handler",
                new ClientPushStreamHandler());
    }

    private void registerPullHandler(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast("client-handler",
                new ClientPullStreamHandler());
    }

    private void close(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }
}