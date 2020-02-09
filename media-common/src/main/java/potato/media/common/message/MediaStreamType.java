package potato.media.common.message;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:38
 * Copyright [2020] [zh_zhou]
 */
public enum MediaStreamType {
    PullClientInit,
    PushClientInit,
    Ping,
    Pong;

    public static MediaStreamType getByName(String name) {
        for (MediaStreamType value : MediaStreamType.values()) {
            if(value.name().equalsIgnoreCase(name)){
                return value;
            }
        }
        return null;
    }
}
