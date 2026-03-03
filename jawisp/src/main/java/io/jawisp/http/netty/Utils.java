package io.jawisp.http.netty;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

public class Utils {
    
    public static boolean isJson(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return contentType != null &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/*") ||
                        contentType.contains("*/*"));
    }

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
