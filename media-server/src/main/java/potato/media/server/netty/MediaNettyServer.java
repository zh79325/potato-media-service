package potato.media.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import potato.media.server.netty.handler.SocketChooseHandle;
import potato.media.server.netty.handler.StreamMessageDecoder;
import potato.media.server.netty.handler.StreamMessageEncoder;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:12
 * Copyright [2020] [zh_zhou]
 */
@Component
public class MediaNettyServer {
    static Logger logger= LoggerFactory.getLogger(MediaNettyServer.class);
    @Value("${netty.server.port}")
    int serverPort;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
    private ServerBootstrap bootstrap = null;

    @PostConstruct
    void startNettyServer(){
        System.out.println(serverPort);
    }

    public void start() {
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 20000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024 * 50, 0, 4, 0, 4));
                            p.addLast("message-decode", new StreamMessageDecoder());
                            p.addLast("message-encode", new StreamMessageEncoder());
                            p.addLast("choose-handle", new SocketChooseHandle());
                            p.addLast("idleStateHandler", new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
                        }
                    });

            bootstrap.bind(serverPort)
                    .sync().channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("start netty server failed",e);
        } finally {
        }
    }
}

