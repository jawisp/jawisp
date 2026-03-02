package io.jawisp.http;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Config<T extends Object> {
    private int port = 8080;
    private final List<Route> routes = new ArrayList<>();

    public Config<T> configure(Consumer<Config<T>> config) {
        config.accept(this);
        return this;
    }

    public Config<T> port(int port) {
        this.port = port;
        return this;
    }

    public Config<T> routes(Consumer<Routes> routesConfig) {
        Routes routing = new Routes();
        routesConfig.accept(routing);
        this.routes.addAll(routing.getRoutes());
        return this;
    }

    public T start() {
        return new T(this);
    }

    public T start(Consumer<Config<T>> config) {
        return new T(configure(config));
    }

    public int getPort() {
        return port;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
