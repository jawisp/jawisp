package io.jawisp.http.netty;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * Utility class containing methods for handling Netty {@link FullHttpRequest}.
 *
 * @author reftch
 * @version 1.0.0
 */
public class Utils {

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

}