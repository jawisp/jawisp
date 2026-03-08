package io.jawisp.http.netty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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


}