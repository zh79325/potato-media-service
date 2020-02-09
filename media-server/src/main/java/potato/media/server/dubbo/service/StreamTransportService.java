package potato.media.server.dubbo.service;

import potato.media.server.dubbo.command.HelloCommand;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:43
 * Copyright [2020] [zh_zhou]
 */
public interface StreamTransportService {
    String sayHello(HelloCommand command);
}
