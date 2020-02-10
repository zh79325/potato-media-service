package potato.media.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:49
 * Copyright [2020] [zh_zhou]
 */
public class MessageUtil {
    static final Charset UTF8=Charset.forName("UTF8");
    public static byte[] toBytes(String msg){
        return msg.getBytes(UTF8);
    }
    public static String toString(byte[] bytes){
        return new String(bytes,UTF8);
    }

    public static byte[] wrap(ByteBuf buf) {
        buf.markReaderIndex();
        ByteBuf r= Unpooled.buffer();
        r.writeInt(buf.readableBytes());
        r.writeBytes(buf);
        buf.resetReaderIndex();
        byte[] result=new byte[r.readableBytes()];
        r.readBytes(result);
        return result;
    }
}
