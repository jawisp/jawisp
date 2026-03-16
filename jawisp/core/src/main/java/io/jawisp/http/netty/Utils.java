package io.jawisp.http.netty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.jawisp.config.cors.CorsSettingsBuilder;
import io.jawisp.http.HttpMethod;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Utility class containing methods for handling Netty {@link FullHttpRequest}.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class Utils {

    /**
     * Default constructor for the Utils class.
     */
    public Utils() {
    }

    /**
     * Checks if the content type of the request is JSON.
     *
     * @param request the {@link FullHttpRequest} object
     * @return true if the content type is JSON or a wildcard that includes JSON,
     *         false otherwise
     */
    public static boolean isJson(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return contentType != null &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/*") ||
                        contentType.contains("*/*"));
    }

    /**
     * Extracts path parameters from the request path and route pattern.
     *
     * @param requestPath the request path
     * @param routePath   the route path containing parameter placeholders (e.g.,
     *                    /users/:id)
     * @return a map of path parameters where the key is the parameter name and the
     *         value is the parameter value
     */
    public static Map<String, String> pathParamMap(String requestPath, String routePath) {
        String[] parts = requestPath.split("/");
        String[] patternParts = routePath.split("/");

        return IntStream.range(0, patternParts.length)
                .filter(i -> i < parts.length && patternParts[i].startsWith(":"))
                .boxed()
                .collect(Collectors.toMap(
                        i -> patternParts[i].substring(1),
                        i -> parts[i]));
    }

    /**
     * Checks if the request URI contains any of the specified static resource
     * paths.
     *
     * @param staticResources the list of static resource paths to check against
     * @param request         the incoming HTTP request
     * @return true if the request URI contains any of the static resource paths,
     *         false otherwise
     */
    public static boolean containsAny(List<String> staticResources, FullHttpRequest request) {
        var resource = request.uri();
        if (staticResources == null || resource == null) {
            return false;
        }
        return staticResources.stream()
                .anyMatch(resource::contains);
    }

    /**
     * Sends an error response to the client.
     *
     * @param ctx    the ChannelHandlerContext for the current channel
     * @param status the HttpResponseStatus indicating the error
     */
    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        DefaultFullHttpResponse errorResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer((status.code() + " " + status.reasonPhrase()).getBytes()));
        errorResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(errorResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Converts a list of {@link io.jawisp.http.HttpMethod} to a list of
     * {@link io.netty.handler.codec.http.HttpMethod}.
     *
     * @param methods the list of Jawisp HTTP methods to convert
     * @return a list of Netty HTTP methods corresponding to the provided Jawisp
     *         HTTP methods
     */
    public static List<io.netty.handler.codec.http.HttpMethod> methods(List<HttpMethod> methods) {
        List<io.netty.handler.codec.http.HttpMethod> nettyMethods = new ArrayList<>();

        for (io.jawisp.http.HttpMethod jawisp : methods) {
            switch (jawisp) {
                case GET -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.GET);
                case POST -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.POST);
                case PUT -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.PUT);
                case DELETE -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.DELETE);
                case PATCH -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.PATCH);
                case HEAD -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.HEAD);
                case OPTIONS -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.OPTIONS);
                case TRACE -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.TRACE);
                case CONNECT -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.CONNECT);
                // skip BEFORE_FILTER, AFTER_FILTER, ERROR - they're Jawisp internals
                default -> {
                }
            }
        }

        return nettyMethods;
    }

}