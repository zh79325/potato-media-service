package potato.media.common.message;

import com.alibaba.fastjson.JSON;
import potato.media.common.message.info.InfoMessage;
import potato.media.common.message.info.InfoType;
import potato.media.common.util.MessageUtil;

/**
 * @author zh_zhou
 * created at 2020/02/09 21:55
 * Copyright [2020] [zh_zhou]
 */
public class StreamMessageUtil {
    public static MediaStreamMessage createInfo(InfoType type, String message) {
        InfoMessage infoMessage = new InfoMessage();
        infoMessage.setMessage(message);
        infoMessage.setType(type);
        return create(MediaStreamType.Message, infoMessage);

    }

    public static <T> T parse(MediaStreamMessage message, Class<T> tClass) {
        byte[] data = message.getData();
        MediaStreamType type = message.getHead().getType();
        if (data == null) {
            return null;
        }
        if (type == MediaStreamType.Binary) {
            return (T) data;
        }
        String json = MessageUtil.toString(data);
        return JSON.parseObject(json, tClass);
    }

    public static <T> MediaStreamMessage create(MediaStreamType type, T data) {
        MediaStreamMessage message = new MediaStreamMessage();
        MediaStreamHead head = new MediaStreamHead();
        head.setType(type);
        head.build();
        message.setHead(head);
        if (type == MediaStreamType.Binary) {
            message.setData((byte[]) data);
        } else if (data != null) {
            String json = JSON.toJSONString(data);
            byte[] bytes = MessageUtil.toBytes(json);
            message.setData(bytes);
        }
        return message;
    }

    public static MediaStreamMessage createBinary(byte[] data) {
        return create(MediaStreamType.Binary,data);
    }
}
