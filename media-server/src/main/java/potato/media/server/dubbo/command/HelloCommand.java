package potato.media.server.dubbo.command;

import potato.media.server.dubbo.core.IpCommand;

/**
 * @author zh_zhou
 * created at 2020/02/09 18:43
 * Copyright [2020] [zh_zhou]
 */
public class HelloCommand extends IpCommand {
    public HelloCommand() {
    }

    public HelloCommand(String name) {
        this.name = name;
    }

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
