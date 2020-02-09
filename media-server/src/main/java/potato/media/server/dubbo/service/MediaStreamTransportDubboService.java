package potato.media.server.dubbo.service;


import org.apache.dubbo.config.annotation.Service;
import potato.media.server.dubbo.command.HelloCommand;
import potato.media.server.dubbo.service.StreamTransportService;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:40
 * Copyright [2020] [zh_zhou]
 */
@Service
public class MediaStreamTransportDubboService implements StreamTransportService {
    @Override
    public String sayHello(HelloCommand command) {
        System.out.println(getClass().getName()+"-"+command.getName());
        return null;
    }
}
