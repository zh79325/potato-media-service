package potato.media.server.dubbo.core;

import potato.media.server.util.NettyUtil;

import java.io.Serializable;

/**
 * @author zh_zhou
 * created at 2020/02/08 22:39
 * Copyright [2020] [zh_zhou]
 */
public abstract class IpCommand implements Serializable {
    String hostIp;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public <T> T build(){
        hostIp= NettyUtil.getIpAddress();
        return (T) this;
    }
}
