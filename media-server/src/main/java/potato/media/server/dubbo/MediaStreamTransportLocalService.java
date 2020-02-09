package potato.media.server.dubbo;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import potato.media.server.dubbo.command.HelloCommand;
import potato.media.server.dubbo.service.MediaStreamTransportDubboService;
import potato.media.server.dubbo.service.StreamTransportService;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:42
 * Copyright [2020] [zh_zhou]
 */
@Service
public class MediaStreamTransportLocalService implements StreamTransportService {
    @Reference(id = "dubboTransportServiceClient",loadbalance = "ip",cluster = "ipfailover",init=false, check=false)
    private StreamTransportService dubboClient;

    @Autowired
    MediaStreamTransportDubboService dubboService;

    @Override
    public String sayHello(HelloCommand command) {
        System.out.println(getClass().getName()+"-"+ command.getName());
        dubboClient.sayHello(new HelloCommand("a").build());
        dubboService.sayHello(new HelloCommand("b").build());
        return null;
    }
}
