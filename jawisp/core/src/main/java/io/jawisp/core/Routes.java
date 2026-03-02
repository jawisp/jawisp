package io.jawisp.core;

import java.util.ArrayList;
import java.util.List;
import io.netty.handler.codec.http.HttpMethod;

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

    public List<Route> getRoutes() {
        return routes;
    }

}