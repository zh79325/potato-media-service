package potato.media.common.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import potato.media.common.util.MessageUtil;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:53
 * Copyright [2020] [zh_zhou]
 */
public class MediaStreamHead {
    MediaStreamType type;
    String mid;
    String parentId;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public MediaStreamType getType() {
        return type;
    }

    public void setType(MediaStreamType type) {
        this.type = type;
    }

    public byte[] encode() {
        ByteBuf buf= Unpooled.buffer();
        byte[]bytes= MessageUtil.toBytes(type.toString());
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        byte[]idBytes=MessageUtil.toBytes(mid);
        buf.writeInt(idBytes.length);
        buf.writeBytes(idBytes);
        if(StringUtils.isEmpty(parentId)){
            buf.writeInt(0);
        }else{
            byte[]pidBytes=MessageUtil.toBytes(parentId);
            buf.writeInt(pidBytes.length);
            buf.writeBytes(pidBytes);
        }
        return MessageUtil.wrap(buf);
    }

    public void decode(ByteBuf buf) {
        int n=buf.readInt();
        byte[]typeBytes=new byte[n];
        buf.readBytes(typeBytes);
        String typeName=MessageUtil.toString(typeBytes);
        type=MediaStreamType.getByName(typeName);
        n=buf.readInt();
        byte[]idBytes=new byte[n];
        buf.readBytes(idBytes);
        mid=MessageUtil.toString(idBytes);
        n=buf.readInt();
        if(n>0){
            byte[]pidBytes=new byte[n];
            buf.readBytes(pidBytes);
            parentId=MessageUtil.toString(pidBytes);
        }
    }

    public void build() {
        mid=System.currentTimeMillis()+ RandomStringUtils.random(10,true,true);
    }
}
