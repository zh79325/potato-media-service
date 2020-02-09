package potato.media.common.message.control;

import java.io.Serializable;

/**
 * @author zh_zhou
 * created at 2020/02/09 21:51
 * Copyright [2020] [zh_zhou]
 */
public class ControlMessage implements Serializable {
    ControlType type;

    public ControlType getType() {
        return type;
    }

    public void setType(ControlType type) {
        this.type = type;
    }
}
