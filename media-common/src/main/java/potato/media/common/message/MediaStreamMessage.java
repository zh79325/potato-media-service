package potato.media.common.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import potato.media.common.util.MessageUtil;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:34
 * Copyright [2020] [zh_zhou]
 */
public class MediaStreamMessage implements Serializable {


    MediaStreamHead head;
    byte[] data;

    public byte[] encode() {
        ByteBuf buf = Unpooled.buffer();
        byte[] headBytes = head.encode();

        int totalLength = headBytes.length;
        if(data!=null){
            totalLength+=data.length;
        }
        buf.writeInt(totalLength);
        buf.writeBytes(headBytes);
        if(data!=null){
            buf.writeBytes(data);
        }
        return MessageUtil.getBytes(buf);
    }

    public static MediaStreamMessage decode(ByteBuf buf) {
        MediaStreamHead head = new MediaStreamHead();
        head.decode(buf);

        MediaStreamMessage message = new MediaStreamMessage();
        message.setHead(head);
        if(buf.readableBytes()>0){
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            message.setData(data);
        }
        return message;
    }


    public MediaStreamHead getHead() {
        return head;
    }

    public void setHead(MediaStreamHead head) {
        this.head = head;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
