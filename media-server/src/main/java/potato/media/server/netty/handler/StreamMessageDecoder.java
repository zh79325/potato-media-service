package potato.media.server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import potato.media.common.message.MediaStreamMessage;

import java.util.List;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:44
 * Copyright [2020] [zh_zhou]
 */
public class StreamMessageDecoder  extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        MediaStreamMessage m=MediaStreamMessage.decode(byteBuf);
        list.add(m);
    }
}
