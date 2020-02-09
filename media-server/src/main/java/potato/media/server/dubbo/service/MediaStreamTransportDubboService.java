package potato.media.server.dubbo.service;


import org.apache.dubbo.config.annotation.Service;
import potato.media.server.dubbo.command.HelloCommand;
import potato.media.server.dubbo.command.SendStreamCommand;
import potato.media.server.dubbo.service.StreamTransportService;
import potato.media.server.netty.handler.ClientStreamHandler;

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

    @Override
    public boolean sendStreamData(SendStreamCommand command) {
        ClientStreamHandler handler=ClientStreamHandler.getHandler(command.getChannel());
        if(handler==null){
            return false;
        }
        handler.deliverData(command.getData());
        return false;
    }
}
