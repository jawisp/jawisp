package io.jawisp.http.netty;

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
}
