package io.jawisp.http;

import java.util.ArrayList;
import java.util.List;

public class Routes {
    private final List<Route> routes = new ArrayList<>();

    public void get(String path, Handler handler) {
        routes.add(new Route(HttpMethod.GET, path, handler));
    }

    public void post(String path, Handler handler) {
        routes.add(new Route(HttpMethod.POST, path, handler));
    }

    public void put(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PUT, path, handler));
    }

    public void patch(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PATCH, path, handler));
    }

    public void delete(String path, Handler handler) {
        routes.add(new Route(HttpMethod.DELETE, path, handler));
    }

    public void head(String path, Handler handler) {
        routes.add(new Route(HttpMethod.HEAD, path, handler));
    }

    public void options(String path, Handler handler) {
        routes.add(new Route(HttpMethod.OPTIONS, path, handler));
    }

    public void trace(String path, Handler handler) {
        routes.add(new Route(HttpMethod.TRACE, path, handler));
    }

    public void connect(String path, Handler handler) {
        routes.add(new Route(HttpMethod.CONNECT, path, handler));
    }

    public void before(String path, Handler handler) {
        routes.add(new Route(HttpMethod.BEFORE_FILTER, path, handler));
    }

    public void after(String path, Handler handler) {
        routes.add(new Route(HttpMethod.AFTER_FILTER, path, handler));
    }

    public List<Route> getRoutes() {
        return routes;
    }

}