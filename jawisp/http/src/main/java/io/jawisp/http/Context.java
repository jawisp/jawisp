package io.jawisp.http;

import java.nio.charset.StandardCharsets;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;

public class Context {
    private final String path;
    private final FullHttpRequest request;
    private final StringBuilder result = new StringBuilder();
    private final Route route;

    private int status = 200;
    private String contentType = "text/plain; charset=UTF-8";
    private boolean keepAlive = true;

    public Context(FullHttpRequest request, Route route) {
        this.path = request.uri();
        this.request = request;
        this.route = route;
        this.keepAlive = HttpUtil.isKeepAlive(request);
    }

    public Context result(String result) {
        this.result.append(result);
        return this;
    }

    public Context status(int status) {
        this.status = status;
        return this;
    }

    public Context json(String json) {
        this.contentType = "application/json; charset=UTF-8";
        this.result.append(json);
        return this;
    }

    public String body() {
        return request.content().toString(StandardCharsets.UTF_8);
    }

    public String pathParam(String name) {
        String[] parts = path.split("/");
        String[] patternParts = route.getPath().split("/");
        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].startsWith(":") &&
                    patternParts[i].substring(1).equals(name) &&
                    i < parts.length) {
                return parts[i];
            }
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public StringBuilder getResult() {
        return result;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

}
