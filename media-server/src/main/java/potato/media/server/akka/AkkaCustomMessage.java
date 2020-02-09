package potato.media.server.akka;

import io.netty.channel.ChannelHandlerContext;
import potato.media.common.message.MediaStreamHead;
import potato.media.common.message.MediaStreamMessage;
import potato.media.common.message.MediaStreamType;
import potato.media.server.netty.handler.ClientStreamHandler;

/**
 * @author zh_zhou
 * created at 2020/02/09 23:47
 * Copyright [2020] [zh_zhou]
 */
public class AkkaCustomMessage extends AkkaMessage {

    private ChannelHandlerContext context;
    private MediaStreamMessage data;
    private ClientStreamHandler handler;
    private MediaStreamType type;
    private MediaStreamHead head;

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setData(MediaStreamMessage data) {
        this.data = data;
    }

    public MediaStreamMessage getData() {
        return data;
    }

    public void setHandler(ClientStreamHandler handler) {
        this.handler = handler;
    }

    public ClientStreamHandler getHandler() {
        return handler;
    }

    public void setType(MediaStreamType type) {
        this.type = type;
    }

    public MediaStreamType getType() {
        return type;
    }

    public void setHead(MediaStreamHead head) {
        this.head = head;
    }

    public MediaStreamHead getHead() {
        return head;
    }
}
