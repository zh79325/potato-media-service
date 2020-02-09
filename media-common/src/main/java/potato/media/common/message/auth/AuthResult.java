package potato.media.common.message.auth;

import java.io.Serializable;

/**
 * @author zh_zhou
 * created at 2020/02/09 22:20
 * Copyright [2020] [zh_zhou]
 */
public class AuthResult implements Serializable {
    boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
