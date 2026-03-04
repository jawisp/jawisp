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

/**
 * The ServerHandler class extends SimpleChannelInboundHandler and is used to handle incoming HTTP requests
 * in the Netty server. It processes the requests, routes them to the appropriate handlers, and sends back the responses.
 *
 * @author reftch
 * @version 1.0.0
 */
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private final List<Route> routes;

    /**
     * Constructs a new ServerHandler instance with the given list of routes.
     *
     * @param routes the list of routes to handle incoming requests
     */
    public ServerHandler(List<Route> routes) {
        this.routes = routes;
    }

    /**
     * Handles the incoming HTTP request by routing it to the appropriate handler and sending back the response.
     *
     * @param ctx the ChannelHandlerContext for the request
     * @param request the FullHttpRequest object representing the HTTP request
     * @throws Exception if an error occurs during request processing
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // Run BEFORE filter
        executeFilters(ctx, request, HttpMethod.BEFORE_FILTER);

        var route = ServerHandlerUtils.findRoute(request, routes);
        Context context = new NettyContext(ctx, request, route.orElse(null));
        if (route.isPresent()) {
            // Run main handler
            route.get().getHandler().handle(context);
        } else {
            // 404 result
            context.text("404 Not Found").status(404);
        }

        response(ctx, context);

        // Always run AFTER filter
        executeFilters(ctx, request, HttpMethod.AFTER_FILTER);
    }

    /**
     * Executes the filters of the specified type for the given request.
     *
     * @param ctx the ChannelHandlerContext for the request
     * @param request the FullHttpRequest object representing the HTTP request
     * @param filterType the type of the filter to execute
     */
    private void executeFilters(ChannelHandlerContext ctx, FullHttpRequest request, HttpMethod filterType) {
        ServerHandlerUtils.findFilter(filterType, routes).ifPresent(route -> {
            Context context = new NettyContext(ctx, request, route);
            route.getHandler().handle(context);
        });
    }

    /**
     * Sends back the response to the client.
     *
     * @param ctx the ChannelHandlerContext for the request
     * @param context the Context object representing the HTTP request and response
     */
    private static void response(ChannelHandlerContext ctx, Context context) {
        var content = Unpooled.copiedBuffer(context.result(), CharsetUtil.UTF_8);
        var status = HttpResponseStatus.valueOf(context.status());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, context.contentType());
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        if (context.isKeepAlive()) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Handles exceptions that occur during request processing.
     * Closes the connection if an exception is caught.
     *
     * @param ctx the ChannelHandlerContext for the request
     * @param cause the Throwable that caused the exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException &&
                cause.getMessage().contains("Connection reset")) {
            // Normal client disconnect - ignore
            ctx.close();
            return;
        }
        // cause.printStackTrace();
        logger.error(cause.getLocalizedMessage());
        ctx.close();
    }

}