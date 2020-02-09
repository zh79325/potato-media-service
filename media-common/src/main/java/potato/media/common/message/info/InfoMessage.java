package potato.media.common.message.info;

import java.io.Serializable;

/**
 * @author zh_zhou
 * created at 2020/02/09 21:53
 * Copyright [2020] [zh_zhou]
 */
public class InfoMessage implements Serializable {
    InfoType type;
    String message;

    public InfoType getType() {
        return type;
    }

    public void setType(InfoType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
