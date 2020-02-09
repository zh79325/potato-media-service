package potato.media.server.akka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * @author zh_zhou
 * created at 2020/02/09 23:13
 * Copyright [2020] [zh_zhou]
 */
public class AkkaStreamActor extends AbstractBehavior<AkkaMessage> {
    public AkkaStreamActor(ActorContext<AkkaMessage> context) {
        super(context);
    }

    public static Behavior<AkkaMessage> create() {
        return Behaviors.setup(AkkaStreamActor::new);
    }

    @Override
    public Receive<AkkaMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(AkkaStreamMessage.class, this::onMessage)
                .onMessage(AkkaStopMessage.class, this::close)
                .onMessage(AkkaCustomMessage.class,this::porcessCustom)
                .build();
    }

    private Behavior<AkkaMessage> onMessage(AkkaStreamMessage message) {
        message.getHandler().pushStream(message.getContext(),message.getData());
        return this;
    }

    private Behavior<AkkaMessage> porcessCustom(AkkaCustomMessage message){
        message.getHandler().porcessCustom(message.getContext(),message.getType(),message.getHead(),message.getData());
        return this;
    }

    private Behavior<AkkaMessage> close(AkkaStopMessage akkaStopMessage) {
        return Behaviors.stopped();
    }
}
