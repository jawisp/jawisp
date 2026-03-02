package io.jawisp.http;

import java.util.ArrayList;
import java.util.List;

public class Routes {
    private final String contextPath;
    private final List<Route> routes = new ArrayList<>();

    public Routes(String contextPath) {
        // FORCE empty to be truly empty
        this.contextPath = (contextPath == null || contextPath.trim().isEmpty()) ? ""
                : contextPath.trim().replaceAll("^/+", "/").replaceAll("/+$", "");
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void get(String path, Handler handler) {
        routes.add(new Route(HttpMethod.GET, forceCleanPath(path), handler));
    }

    public void post(String path, Handler handler) {
        routes.add(new Route(HttpMethod.POST, forceCleanPath(path), handler));
    }

    public void put(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PUT, forceCleanPath(path), handler));
    }

    public void patch(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PATCH, forceCleanPath(path), handler));
    }

    public void delete(String path, Handler handler) {
        routes.add(new Route(HttpMethod.DELETE, forceCleanPath(path), handler));
    }

    public void head(String path, Handler handler) {
        routes.add(new Route(HttpMethod.HEAD, forceCleanPath(path), handler));
    }

    public void options(String path, Handler handler) {
        routes.add(new Route(HttpMethod.OPTIONS, forceCleanPath(path), handler));
    }

    public void trace(String path, Handler handler) {
        routes.add(new Route(HttpMethod.TRACE, forceCleanPath(path), handler));
    }

    public void connect(String path, Handler handler) {
        routes.add(new Route(HttpMethod.CONNECT, forceCleanPath(path), handler));
    }

    public void before(String path, Handler handler) {
        routes.add(new Route(HttpMethod.BEFORE_FILTER, forceCleanPath(path), handler));
    }

    public void after(String path, Handler handler) {
        routes.add(new Route(HttpMethod.AFTER_FILTER, forceCleanPath(path), handler));
    }

    private String forceCleanPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "/";
        }

        // Remove ALL multiple slashes
        String cleanPath = path.replaceAll("/+", "/");

        // If no context path, return clean path directly
        if (this.contextPath.isEmpty()) {
            return cleanPath;
        }
        if (cleanPath.equals("/")) {
            return this.contextPath;
        }

        // Context path exists - strip leading slash from route path
        String route = cleanPath.startsWith("/") ? cleanPath.substring(1) : cleanPath;
        String prefix = this.contextPath;

        return prefix + "/" + route;
    }

}
