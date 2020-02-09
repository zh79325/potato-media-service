package potato.media.server.netty.handler;

import akka.actor.typed.ActorRef;
import io.netty.channel.ChannelHandlerContext;
import potato.media.common.message.MediaStreamHead;
import potato.media.common.message.MediaStreamMessage;
import potato.media.common.message.MediaStreamType;
import potato.media.common.message.StreamMessageUtil;
import potato.media.common.message.info.InfoType;
import potato.media.common.message.subscribe.SubscribeMessage;
import potato.media.common.pull.StreamSubscriber;
import potato.media.common.util.MessageUtil;
import potato.media.server.MediaMainServer;
import potato.media.server.akka.AkkaCustomMessage;
import potato.media.server.akka.AkkaMessage;
import potato.media.server.akka.AkkaStopMessage;
import potato.media.server.akka.AkkaStreamMessage;
import potato.media.server.dubbo.MediaStreamTransportLocalService;
import potato.media.server.dubbo.command.SendStreamCommand;
import potato.media.server.util.NettyUtil;
import potato.media.storage.MediaStreamService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zh_zhou
 * created at 2020/02/09 21:45
 * Copyright [2020] [zh_zhou]
 */
public class ClientStreamHandler extends AbstractChannelHandler {

    static Map<String,ClientStreamHandler> localMap=new ConcurrentHashMap<>();
    MediaStreamService streamService;
    MediaStreamTransportLocalService transportLocalService;
    Map<String, StreamSubscriber> partners=new HashMap<>();

    ActorRef<AkkaMessage> actorRef;
    ChannelHandlerContext context;

    public static ClientStreamHandler getHandler(String channel) {
        return localMap.get(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String id= NettyUtil.getId(ctx);
        streamService= MediaMainServer.getBean(MediaStreamService.class);
        transportLocalService=MediaMainServer.getBean(MediaStreamTransportLocalService.class);
        actorRef= MediaMainServer.createActor(id);
        this.context=ctx;
        localMap.put(id,this);
    }



    @Override
    protected void messageRead(ChannelHandlerContext ctx, MediaStreamType type, MediaStreamHead head, MediaStreamMessage msg) {
        switch (type){
            case Binary:
                AkkaStreamMessage akkaStreamMessage =new AkkaStreamMessage();
                akkaStreamMessage.setContext(ctx);
                akkaStreamMessage.setData(msg.getData());
                akkaStreamMessage.setHandler(this);
                actorRef.tell(akkaStreamMessage);
                return;
            default:
                AkkaCustomMessage customMessage=new AkkaCustomMessage();
                customMessage.setContext(ctx);
                customMessage.setData(msg);
                customMessage.setHandler(this);
                customMessage.setType(type);
                customMessage.setHead(head);
                actorRef.tell(customMessage);
                return;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if(actorRef!=null){
            actorRef.tell(new AkkaStopMessage());
            actorRef=null;
        }
        String id= NettyUtil.getId(ctx);
        localMap.remove(id);
    }


    public void pushStream(ChannelHandlerContext context, byte[] data) {
        Map<String, StreamSubscriber> subscribers = streamService.getSubscribers(userId);
        if (subscribers == null) {
            return;
        }
        subscribers.values().parallelStream().forEach(
                s -> {
                    SendStreamCommand command=new SendStreamCommand();
                    command.setHostIp(s.getHost());
                    command.setChannel(s.getChannelId());
                    command.setData(data);
                    transportLocalService.sendStreamData(command);
                }
        );
    }

    public void porcessCustom(ChannelHandlerContext context, MediaStreamType type, MediaStreamHead head, MediaStreamMessage data) {
        switch (type){
            case Subscribe:
                SubscribeMessage subscribeMessage= StreamMessageUtil.parse(data, SubscribeMessage.class);
                String channelId=NettyUtil.getId(context);
                StreamSubscriber subscriber=new StreamSubscriber();
                subscriber.setChannelId(channelId);
                subscriber.setHost(NettyUtil.getIpAddress());
                subscriber.setOwner(userId);
                subscriber.setTarget(subscribeMessage.getTarget());
                subscriber.buildId();
                streamService.subscribe(userId,subscriber);
                partners.put(subscribeMessage.getTarget(),subscriber);
                break;
            case Unsubscribe:
                SubscribeMessage unsubscribeMessage= StreamMessageUtil.parse(data, SubscribeMessage.class);
                streamService.unsubscribe(userId,unsubscribeMessage.getTarget());
                partners.remove(unsubscribeMessage.getTarget());
                break;
            case Reset:
                for (Map.Entry<String, StreamSubscriber> entry : partners.entrySet()) {
                    String targetUid=entry.getKey();
                    streamService.unsubscribe(userId,targetUid);
                }
                partners.clear();
                break;
            default:
                return;
        }
    }

    public void deliverData(byte[] data) {
        MediaStreamMessage msg = StreamMessageUtil.createBinary(data);
        context.writeAndFlush(msg);
    }
}
