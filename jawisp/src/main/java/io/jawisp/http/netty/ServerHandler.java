package io.jawisp.http.netty;

import java.io.IOException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.config.Config;
import io.jawisp.http.Context;
import io.jawisp.http.HttpMethod;
import io.jawisp.http.Route;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * The ServerHandler class extends SimpleChannelInboundHandler and is used to
 * handle incoming HTTP requests
 * in the Netty server. It processes the requests, routes them to the
 * appropriate handlers, and sends back the responses.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Config config;
    private final Supplier<ResourceHandler> resourceHandler = () -> ResourceHandler.getInstance();

    /**
     * Constructs a new ServerHandler instance with the given configuration.
     *
     * @param config the configuration object containing routes and other settings
     */
    public ServerHandler(Config config) {
        this.config = config;
    }

    /**
     * Handles the incoming HTTP request by routing it to the appropriate handler
     * and sending back the response.
     *
     * @param ctx     the ChannelHandlerContext for the request
     * @param request the FullHttpRequest object representing the HTTP request
     * @throws Exception if an error occurs during request processing
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        NettyContext context = new NettyContext(ctx, request, config.templateEngine());
        // Run BEFORE filter
        executeFilters(ctx, context, HttpMethod.BEFORE_FILTER);

        // static resources
        boolean matches = Utils.containsAny(config.staticResources(), request);
        if (matches) {
            resourceHandler.get().response(ctx, request, context);
        } else {
            // generic handlers
            ServerHandlerUtils.findRoute(request, config.routes())
                    .ifPresentOrElse(
                            r -> handleMatchingRoute(r, context),
                            () -> handleNotFound(context));
        }

        // check on error handlers
        executeErrors(ctx, context);

        // Run AFTER filter
        executeFilters(ctx, context, HttpMethod.AFTER_FILTER);

        response(ctx, context);
    }

    /**
     * Required by Netty - flush pending writes
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * Handles exceptions that occur during request processing.
     * Closes the connection if an exception is caught.
     *
     * @param ctx   the ChannelHandlerContext for the request
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

    /**
     * Handles the routing for a matching route.
     *
     * @param route   the route that matches the incoming request
     * @param context the context of the Netty request
     */
    private void handleMatchingRoute(Route route, NettyContext context) {
        context.route(route);
        route.getHandler().handle(context);
    }

    /**
     * Handles the case where no route matches the incoming request.
     *
     * @param context the context of the request
     */
    private void handleNotFound(Context context) {
        context.text("404 Not Found").status(404);
    }

    /**
     * Executes the error handlers for the given request context.
     *
     * @param ctx     the ChannelHandlerContext for the request
     * @param context the Context object representing the request and response
     *                context
     */
    private void executeErrors(ChannelHandlerContext ctx, Context context) {
        ServerHandlerUtils.findFilters(HttpMethod.ERROR, config.routes())
                .forEach(route -> {
                    if (context.status() == route.status()) {
                        route.getHandler().handle(context);
                    }
                });
    }

    /**
     * Executes the filters of the specified type for the given request.
     *
     * @param ctx        the ChannelHandlerContext for the request
     * @param request    the FullHttpRequest object representing the HTTP request
     * @param filterType the type of the filter to execute
     */
    private void executeFilters(ChannelHandlerContext ctx, Context context, HttpMethod filterType) {
        ServerHandlerUtils.findFilter(filterType, config.routes()).ifPresent(route -> {
            route.getHandler().handle(context);
        });
    }

    /**
     * Sends back the response to the client.
     *
     * @param ctx     the ChannelHandlerContext for the request
     * @param context the Context object representing the HTTP request and response
     */
    private static void response(ChannelHandlerContext ctx, Context context) {
        ByteBuf content = ctx.alloc().buffer().writeBytes(context.result());

        // return already created response object
        var response = context.response();
        // set current status
        response.setStatus(HttpResponseStatus.valueOf(context.status()));

        HttpHeaders headers = response.headers();
        headers.setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        if (!headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, context.contentType());
        }

        if (context.isKeepAlive()) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // send headers
        ctx.write(response);
        // send body + end
        ChannelFuture future = ctx.writeAndFlush(new DefaultLastHttpContent(content));
        if (!context.isKeepAlive()) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}