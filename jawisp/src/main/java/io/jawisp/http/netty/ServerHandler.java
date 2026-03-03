package io.jawisp.http.netty;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Context;
import io.jawisp.http.HttpMethod;
import io.jawisp.http.Route;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final List<Route> routes;

    public ServerHandler(List<Route> routes) {
        this.routes = routes;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // Run BEFORE filter
        executeFilters(ctx, request, HttpMethod.BEFORE_FILTER);

        var route = ServerHandlerUtils.findRoute(request, routes);
        Context context = new Context(request, route.orElse(null));
        if (route.isPresent()) {
            // Run main handler
            route.get().getHandler().handle(context);
        } else {
            // 404 result
            context.result("404 Not Found").status(404);
        }

        response(ctx, context);

        // Always run AFTER filter
        executeFilters(ctx, request, HttpMethod.AFTER_FILTER);
    }

    private void executeFilters(ChannelHandlerContext ctx, FullHttpRequest request, HttpMethod filterType) {
        ServerHandlerUtils.findFilter(filterType, routes).ifPresent(route -> {
            Context context = new Context(request, route);
            route.getHandler().handle(context);
        });
    }

    private static void response(ChannelHandlerContext ctx, Context context) {
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

}
