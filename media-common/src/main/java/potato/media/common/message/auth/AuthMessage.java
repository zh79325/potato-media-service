package potato.media.common.message.auth;

import java.io.Serializable;

/**
 * @author zh_zhou
 * created at 2020/02/09 22:19
 * Copyright [2020] [zh_zhou]
 */
public class AuthMessage implements Serializable {
    String uid;
    String token;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
