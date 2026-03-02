package io.jawisp.http;

public class Route {
    private final String method;
    private final String path;
    private final Handler handler;

    public Route(String method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Handler getHandler() {
        return handler;
    }
}