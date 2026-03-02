package io.jawisp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Route;
import io.jawisp.http.Routes;
import io.jawisp.http.HttpServer;
import io.jawisp.http.netty.NettyServer;

public class Jawisp {
    private static final Logger logger = LoggerFactory.getLogger(Jawisp.class);

    private Jawisp(Config config) {
        logger.info("Starting Web JAWISP v2.0.0 ...");

        AtomicInteger index = new AtomicInteger(1);
        config.routes.stream()
                .forEach(route -> logger.info("Route[{}]: {} {}",
                        index.getAndIncrement(),
                        route.getMethod().name(),
                        route.getPath()));

         try {
            HttpServer server = new NettyServer(config);
            server.start();
        } catch (Exception e) {
            logger.error("Error during starting server {}", e.getMessage());
        }
    }

    public static Config create() {
        return new Config();
    }

    public static class Config {
        private int port = 8080;

        private final List<Route> routes = new ArrayList<>();

        public Config configure(Consumer<Config> config) {
            config.accept(this);
            return this;
        }

        public Config port(int port) {
            this.port = port;
            return this;
        }

        public Config routes(Consumer<Routes> routesConfig) {
            Routes routing = new Routes();
            routesConfig.accept(routing);
            this.routes.addAll(routing.getRoutes());
            return this;
        }
        
        public Jawisp start() {
            return new Jawisp(this);
        }

        public Jawisp start(Consumer<Config> config) {
            return new Jawisp(configure(config));
        }

        public int getPort() {
            return port;
        }

        public List<Route> getRoutes() {
            return routes;
        }
    }
}