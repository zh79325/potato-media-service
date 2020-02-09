package potato.media.server.netty.handler.pull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import potato.media.common.stream.ClientStream;
import potato.media.server.MediaMainServer;
import potato.media.server.netty.ConnectSubscriberPool;
import potato.media.server.util.NettyUtil;
import potato.media.storage.MediaStreamService;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author zh_zhou
 * created at 2020/02/07 22:25
 * Copyright [2020] [zh_zhou]
 */
public class HttpFileServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(HttpFileServerHandler.class);

    static final Pattern urlPattern = Pattern.compile("media/(\\d+)");
    long streamId;
    HttpStreamSubscriber subscriber;

    MediaStreamService streamService;

    public HttpFileServerHandler() {
        streamService = MediaMainServer.getBean(MediaStreamService.class);
    }

    /**
     * process request of http://server/media/${streamId}
     *
     * @param ctx
     * @param request
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        if (request.method() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }
        String uri = request.uri();

        Matcher matcher = urlPattern.matcher(uri);
        if (!matcher.find()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        String id = matcher.group(1);
        try {
            streamId = Long.parseLong(id);
        } catch (Exception e) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        if (streamId <= 0) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        ClientStream stream = streamService.getStream(streamId);
        if (stream == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }


        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set("Access-Control-Allow-Credentials", true);
        response.headers().set("Content-Type", "video/x-mp4");
        response.headers().set("Expires", -1);
        response.headers().set("Server", "netty");
        response.headers().set("Transfer-Encoding", "chunked");
        response.headers().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.headers().set("Access-Control-Allow-Headers", "DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization,range");
        // write response header
        ctx.writeAndFlush(response);

        byte[] head = stream.getHead();
        ctx.channel().write(Unpooled.wrappedBuffer(head));

        String channelId = NettyUtil.getId(ctx);
        String ip = NettyUtil.getIpAddress();
        subscriber = new HttpStreamSubscriber();
        subscriber.buildId();
        subscriber.setChannelId(channelId);
        subscriber.setHost(ip);
        subscriber.setChannel(ctx.channel());
        subscriber.setStreamId(streamId);
        streamService.registerSubscribe(subscriber);
        ConnectSubscriberPool.add(subscriber);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        streamService.unregister(subscriber);
        ConnectSubscriberPool.remove(subscriber);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");


    private static final Pattern ALLOWED_FILE_NAME = Pattern
            .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }



}
