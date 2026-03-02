package io.jawisp.core;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    // private final Routes routes;
    private final List<Route> routes;


    public ServerHandler(List<Route> routes) {
        this.routes = routes;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        HttpMethod method = request.method();

        Route route = findRoute(method.name(), uri);
        if (route != null) {
            Context context = new Context(request);
            route.getHandler().handle(context);
            response(ctx, context);
        } else {
            response(ctx, "404 Not Found", HttpResponseStatus.NOT_FOUND, false);
        }
    }

    public static void response(ChannelHandlerContext ctx, String content) {
        response(ctx, content, HttpResponseStatus.OK, true);
    }

    public static void response(ChannelHandlerContext ctx, Context context) {
        var content = Unpooled.copiedBuffer(context.getResult(), CharsetUtil.UTF_8);
        var status = HttpResponseStatus.valueOf(context.getStatus());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, context.getContentType());
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        if (context.isKeepAlive()) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void response(ChannelHandlerContext ctx, String body, HttpResponseStatus status, boolean keepAlive) {
        var content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException && 
            cause.getMessage().contains("Connection reset")) {
            // Normal client disconnect - ignore
            ctx.close();
            return;
        }
        logger.error(cause.getMessage());
        ctx.close();
    }

    private Route findRoute(String method, String path) {
        return routes.stream()
                .filter(r -> r.getMethod().equals(method) && matchPath(r.getPath(), path))
                .findFirst()
                .orElse(null);
    }

    private boolean matchPath(String pattern, String path) {
        // Simple :param matching
        if (pattern.equals(path))
            return true;
        Pattern p = Pattern.compile(pattern.replaceAll(":[^/]+", "([^/]+)"));
        Matcher m = p.matcher(path);
        return m.matches();
    }

}
