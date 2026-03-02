package io.jawisp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.jawisp.http.Routes;
import io.jawisp.http.Route;

public class Config {
    private int port = 8080;
    private String contextPath = "/";

    private final List<Route> routes = new ArrayList<>();

    public Config configure(Consumer<Config> config) {
        config.accept(this);
        return this;
    }

    public Config port(int port) {
        this.port = port;
        return this;
    }

    public Config contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }
    
    public Config routes(Consumer<Routes> routesConfig) {
        Routes routing = new Routes(contextPath);
        routesConfig.accept(routing);
        this.routes.addAll(routing.getRoutes());
        return this;
    }

    public int getPort() {
        return port;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public String getContextPath() {
        return contextPath;
    }

}
