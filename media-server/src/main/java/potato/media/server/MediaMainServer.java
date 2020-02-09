package potato.media.server;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import potato.media.server.akka.AkkaMessage;
import potato.media.server.akka.AkkaStreamActor;
import potato.media.server.dubbo.MediaStreamTransportLocalService;
import potato.media.server.dubbo.command.HelloCommand;
import potato.media.server.util.NettyUtil;
import potato.media.storage.MediaStreamService;
import potato.media.storage.impl.MemStorage;

import static org.apache.dubbo.common.constants.CommonConstants.DUBBO_IP_TO_BIND;
import static org.apache.dubbo.config.Constants.DUBBO_IP_TO_REGISTRY;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:00
 * Copyright [2020] [zh_zhou]
 */
@ComponentScan(basePackages = {"potato.media"})
@SpringBootApplication()
@EnableScheduling
public class MediaMainServer {
    static ApplicationContext context;
    public static ActorSystem<AkkaMessage> akkaMain ;

    public static void main(String[] args) {
        String local = NettyUtil.getIpAddress();
        System.setProperty(DUBBO_IP_TO_REGISTRY, local);
        System.setProperty(DUBBO_IP_TO_BIND, local);
        context=SpringApplication.run(MediaMainServer.class, args);
        MediaStreamService streamService=getBean(MediaStreamService.class);
        streamService.setStreamStorage(getBean(MemStorage.class));
        streamService.setSubscriberStorage(getBean(MemStorage.class));
        akkaMain = ActorSystem.create(AkkaStreamActor.create(), "im3_akka");
        startDubbo();
        new Thread(()->{
            context.getBean(MediaStreamTransportLocalService.class).sayHello(new HelloCommand("abcd").build());
        }).start();
    }

    private static void startDubbo() {
        if (1 > 0) {
            return;
        }

    }


    public static <T>  T getBean(Class<T> tClass){
        return context.getBean(tClass);
    }

    public static ActorRef<AkkaMessage> createActor(String id) {
        return akkaMain.systemActorOf(AkkaStreamActor.create(),id,akkaMain.systemActorOf$default$3());
    }

}
