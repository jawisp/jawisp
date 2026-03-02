package io.jawisp.core;

import io.netty.handler.codec.http.HttpMethod;

public class Route {
    private final HttpMethod method;
    private final String path;
    private final Handler handler;

    public Route(HttpMethod method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Handler getHandler() {
        return handler;
    }
}