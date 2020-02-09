package potato.media.common.pull;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:14
 * Copyright [2020] [zh_zhou]
 */
public class StreamSubscriber {
    private String channelId;
    private String host;
    private String target;
    private String owner;
    private String id;



    public void buildId() {
        id= System.currentTimeMillis()+RandomStringUtils.random(10,true,true);
    }


    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
