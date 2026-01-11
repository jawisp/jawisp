package io.jawisp.netty;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.handler.Handler;
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
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

public class HttpRequestProcessor extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestProcessor.class);

    private final Handler handler;

    public HttpRequestProcessor(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            // // 1. Convert Netty → Jawisp Request
            Request jawispRequest = nettyToCore(request);

            // 2. Call your framework Handler
            Response jawispResponse = new Response(200, "text/html", "OK".getBytes());
            handler.handle(jawispRequest, jawispResponse);

            // 3. Convert Jawisp → Netty Response
            FullHttpResponse response = coreToNetty(jawispResponse, request.protocolVersion());

            // 4. Send response
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
            } else {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            logger.error("Handler error {}", e.getMessage());
            FullHttpResponse errorResponse = new DefaultFullHttpResponse(
                    request.protocolVersion(),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8));
            errorResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            ctx.writeAndFlush(errorResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof java.net.SocketException &&
                "Connection reset".equals(cause.getMessage())) {
            return;
        }
        cause.printStackTrace();
        ctx.close();
    }

    private Request nettyToCore(FullHttpRequest req) {
        String body = req.content().toString(CharsetUtil.UTF_8);
        QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());

        Map<String, String> params = new HashMap<>();
        queryDecoder.parameters().forEach((k, v) -> params.put(k, v.get(0)));

        Map<String, String> headers = new HashMap<>();
        req.headers().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));

        return new Request(
                req.method().name(),
                queryDecoder.path(),
                body,
                headers,
                params);
    }

    private FullHttpResponse coreToNetty(Response res, HttpVersion version) {
        var content = Unpooled.copiedBuffer(res.getBody());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(res.getStatus()), Unpooled.copiedBuffer(res.getBody()));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, res.getContentType());
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        res.getHeaders().forEach((k, v) -> response.headers().set(k, v));

        return response;
    }
}