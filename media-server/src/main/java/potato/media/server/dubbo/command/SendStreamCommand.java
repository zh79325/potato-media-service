package potato.media.server.dubbo.command;

import potato.media.server.dubbo.core.IpCommand;

/**
 * @author zh_zhou
 * created at 2020/02/10 00:11
 * Copyright [2020] [zh_zhou]
 */
public class SendStreamCommand extends IpCommand {
    private String channel;
    private byte[] data;

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
