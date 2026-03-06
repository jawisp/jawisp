package io.jawisp.http.netty;

import java.io.IOException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.config.Config;
import io.jawisp.http.Context;
import io.jawisp.http.HttpMethod;
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
import io.netty.util.CharsetUtil;

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
     * Constructs a new ServerHandler instance with the given list of routes and a
     * template engine.
     *
     * @param routes         the list of routes to handle incoming requests
     * @param templateEngine the template engine to use for rendering templates
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
        // static resources
        boolean matches = Utils.containsAny(config.staticResources(), request);
        if (matches) {
            resourceHandler.get().response(ctx, request);
            return;
        }

        var route = ServerHandlerUtils.findRoute(request, config.routes());
        Context context = new NettyContext(ctx, request, route.orElse(null), config.templateEngine());

        // Run BEFORE filter
        executeFilters(ctx, context, HttpMethod.BEFORE_FILTER);
        if (route.isPresent()) {
            // Run main handler
            route.get().getHandler().handle(context);
        } else {
            // 404 result
            context.text("404 Not Found").status(404);
        }

        // check on error handlers
        executeErrors(ctx, context);

        // Always run AFTER filter
        executeFilters(ctx, context, HttpMethod.AFTER_FILTER);

        response(ctx, context);
    }

    /**
     * Executes the error handlers for the given request context.
     *
     * @param ctx     the ChannelHandlerContext for the request
     * @param context the Context object representing the request and response
     *                context
     */
    private void executeErrors(ChannelHandlerContext ctx, Context context) {
        ServerHandlerUtils.findFilter(HttpMethod.ERROR, config.routes()).ifPresent(route -> {
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
        ByteBuf content = ctx.alloc().buffer();
        content.writeCharSequence(context.result(), CharsetUtil.UTF_8);

        // return already created response object
        var response = context.response();
        // set current status
        response.setStatus(HttpResponseStatus.valueOf(context.status()));

        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE, context.contentType());
        headers.setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

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
}