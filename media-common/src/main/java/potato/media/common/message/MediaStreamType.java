package potato.media.common.message;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:38
 * Copyright [2020] [zh_zhou]
 */
public enum MediaStreamType {
    Auth,
    AuthResp(true),
    Subscribe,
    Unsubscribe,
    Reset,
    Binary,
    Message,
    Ping,
    Pong;
    MediaStreamType(){
        this(false);
    }
    MediaStreamType(boolean serverIgnore) {
        this.serverIgnore = serverIgnore;
    }

    final boolean serverIgnore;

    public boolean isServerIgnore() {
        return serverIgnore;
    }

    public static MediaStreamType getByName(String name) {
        for (MediaStreamType value : MediaStreamType.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
