package potato.media.server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import potato.media.common.message.MediaStreamMessage;

import java.util.List;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:44
 * Copyright [2020] [zh_zhou]
 */
public class StreamMessageEncoder extends MessageToMessageEncoder<MediaStreamMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MediaStreamMessage message, List<Object> list) throws Exception {
        byte[]bytes=message.encode();
        ByteBuf byteBuf= Unpooled.buffer();
        byteBuf.writeBytes(bytes);
        list.add(byteBuf);
    }
}
