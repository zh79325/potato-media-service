package potato.media.common.message.subscribe;

import java.io.Serializable;

/**
 * @author zh_zhou
 * created at 2020/02/09 22:53
 * Copyright [2020] [zh_zhou]
 */
public class SubscribeMessage implements Serializable {
    String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
