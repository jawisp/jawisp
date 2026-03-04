package io.jawisp.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.HttpServer;
import io.jawisp.http.netty.NettyServer;

public class Jawisp {
    private static final Logger logger = LoggerFactory.getLogger(Jawisp.class);

    private long start = System.nanoTime();

    private Jawisp(Config config) {
        logger.info("Starting Web JAWISP v2.0.0 ...");

        AtomicInteger index = new AtomicInteger(1);
        config.getRoutes().stream()
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
        
        long end = System.nanoTime();
        long elapsedMs = (end - start) / 1_000_000;
        logger.info("Server started on {}:{}/ in {} ms", "http://localhost",
                String.valueOf(config.getPort()), elapsedMs);
    }

    // Default config
    public static Jawisp run() {
        return run(config -> {
        });
    }

    // Custom config
    public static Jawisp run(Consumer<Config> config) {
        Config cfg = new Config();
        config.accept(cfg);
        return new Jawisp(cfg);
    }
}