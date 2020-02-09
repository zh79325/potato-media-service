package potato.media.server.netty.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import potato.media.common.message.MediaStreamHead;
import potato.media.common.message.MediaStreamMessage;
import potato.media.common.message.MediaStreamType;
import potato.media.common.message.StreamMessageUtil;
import potato.media.common.message.auth.AuthMessage;
import potato.media.common.message.auth.AuthResult;
import potato.media.common.message.info.InfoType;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static potato.media.common.message.MediaStreamType.Ping;
import static potato.media.common.message.MediaStreamType.Pong;

/**
 * @author zh_zhou
 * created at 2020/02/09 20:33
 * Copyright [2020] [zh_zhou]
 */
public abstract class AbstractChannelHandler extends SimpleChannelInboundHandler<MediaStreamMessage> implements ChannelOutboundHandler {
    final int MAX_IDLE_NUM = 3;
    public static final int HEARTBEAT_DELAY = 30;
    int heartBeatFailNum = 0;
    private ScheduledFuture<?> heartbeatScheduledFuture;
    Set<String> pingArray = new HashSet<>();

    protected String userId;

    boolean authed;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        init(ctx);
    }

    private void init(ChannelHandlerContext ctx) {
        heartbeatScheduledFuture = schedule(ctx, new HeartbeatTask(ctx),
                HEARTBEAT_DELAY, TimeUnit.SECONDS);
    }

    class HeartbeatTask implements Runnable {

        final ChannelHandlerContext ctx;

        HeartbeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (pingArray.size() >= MAX_IDLE_NUM) {
                ctx.fireUserEventTriggered(new HeartbeatTimoutEvent());
            } else {
                heartbeatScheduledFuture = schedule(ctx, this, HEARTBEAT_DELAY, TimeUnit.SECONDS);
            }
        }
    }

    ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        destory();
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }


    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
        resetIdle();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MediaStreamMessage msg) throws Exception {
        MediaStreamHead head = msg.getHead();
        if (head == null) {
            return;
        }
        MediaStreamType type = head.getType();
        if (type == null) {
            return;
        }
        if (type.isServerIgnore()) {
            return;
        }
        switch (type) {
            case Ping:
                MediaStreamMessage pong = StreamMessageUtil.create(Pong, null);
                pong.getHead().setParentId(head.getMid());
                ctx.channel().write(pong);
                break;
            case Pong:
                String pid = msg.getHead().getParentId();
                pingArray.remove(pid);
                break;
            case Auth:
                AuthMessage authMessage = StreamMessageUtil.parse(msg, AuthMessage.class);
                validateAuth(ctx, authMessage);
                break;
            default:
                if(authed){
                    sendError(ctx,"not authed");
                }else {
                    messageRead(ctx, type,head,msg);
                }
                break;

        }

    }


    private void sendError(ChannelHandlerContext ctx, String message) {
        MediaStreamMessage msg = StreamMessageUtil.createInfo(InfoType.Error, message);
        ctx.write(msg);
        ctx.close();
        destory();
    }

    private void validateAuth(ChannelHandlerContext ctx, AuthMessage authMessage) {
        userId = authMessage.getUid();
        AuthResult result = new AuthResult();
        if (StringUtils.isEmpty(userId)) {
            result.setSuccess(false);
        } else {
            result.setSuccess(true);
            authed=true;
        }
        MediaStreamMessage message = StreamMessageUtil.create(MediaStreamType.AuthResp, result);
        ctx.channel().write(message);
        if (!result.isSuccess()) {
            sendError(ctx,"auth failed");
        }
    }

    protected abstract void messageRead(ChannelHandlerContext ctx, MediaStreamType type, MediaStreamHead head, MediaStreamMessage msg);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }


    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    void resetIdle() {
        heartBeatFailNum = 0;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            MediaStreamMessage ping = StreamMessageUtil.create(Ping, null);
            pingArray.add(ping.getHead().getMid());
            ctx.channel().write(ping)
                    .addListener(f -> {
                        if (!f.isSuccess()) {
                            heartBeatFailNum++;
                            if (heartBeatFailNum > MAX_IDLE_NUM) {
                                ctx.close();
                                destory();
                            }
                        }
                    });
        } else if (evt instanceof HeartbeatTimoutEvent) {
            ctx.close();
            destory();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    void destory() {
        if (heartbeatScheduledFuture != null) {
            heartbeatScheduledFuture.cancel(false);
            heartbeatScheduledFuture = null;
        }
    }
}
