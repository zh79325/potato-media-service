package potato.media.server.akka;

import io.netty.channel.ChannelHandlerContext;
import potato.media.common.message.MediaStreamHead;
import potato.media.common.message.MediaStreamMessage;
import potato.media.common.message.MediaStreamType;
import potato.media.server.netty.handler.ClientStreamHandler;

/**
 * @author zh_zhou
 * created at 2020/02/09 23:28
 * Copyright [2020] [zh_zhou]
 */
public class AkkaStreamMessage extends AkkaMessage {
    ChannelHandlerContext context;
    byte[]data;
    ClientStreamHandler handler;

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ClientStreamHandler getHandler() {
        return handler;
    }

    public void setHandler(ClientStreamHandler handler) {
        this.handler = handler;
    }
}
