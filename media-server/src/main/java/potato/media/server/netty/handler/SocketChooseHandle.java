package potato.media.server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.web.HttpRequestHandler;
import potato.media.server.netty.handler.pull.HttpFileServerHandler;

import java.util.List;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:20
 * Copyright [2020] [zh_zhou]
 */
public class SocketChooseHandle extends ByteToMessageDecoder {
    /**
     * 默认暗号长度为23
     */
    private static final int MAX_LENGTH = 23;
    /**
     * WebSocket握手的协议前缀
     */
    private static final String WEBSOCKET_PREFIX = "GET /";


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String protocol = getBufStart(in);
        if (protocol.startsWith(WEBSOCKET_PREFIX)) {
            registerPushStreamHandler(ctx);
        } else {
            registerPullMediaStreamHandler(ctx);
        }

        in.resetReaderIndex();
        ctx.pipeline().remove(this.getClass());

    }

    private void registerPushStreamHandler(ChannelHandlerContext ctx) {

    }

    private void registerPullMediaStreamHandler(ChannelHandlerContext ch) {
        ch.pipeline().addLast("http-decoder",
                new HttpRequestDecoder());
        ch.pipeline().addLast("http-aggregator",
                new HttpObjectAggregator(65536));
        ch.pipeline().addLast("http-encoder",
                new HttpResponseEncoder());
        ch.pipeline().addLast("http-chunked",
                new ChunkedWriteHandler());
        ch.pipeline().addLast("fileServerHandler",
                new HttpFileServerHandler());
    }

    private String getBufStart(ByteBuf in) {
        int length = in.readableBytes();
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }

        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        return new String(content);
    }
}