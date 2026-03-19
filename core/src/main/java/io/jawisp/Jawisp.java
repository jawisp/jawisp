package io.jawisp;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.config.Config;
import io.jawisp.config.PropertyReader;
import io.jawisp.http.HttpServer;
import io.jawisp.http.netty.NettyServer;

/**
 * The Jawisp class is the main entry point for the JAWISP application.
 * It initializes the HTTP server and manages its lifecycle.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class Jawisp {
    private static final Logger log = LoggerFactory.getLogger(Jawisp.class);

    private final Supplier<PropertyReader> property = () -> PropertyReader.getInstance();
    private final Config config;
    private final HttpServer server;

    private final AtomicReference<HotReloader> hotReloader = new AtomicReference<>();

    private long startTime = System.nanoTime();

    /**
     * Constructs a new instance of Jawisp with the provided configuration.
     *
     * @param config the configuration for the application
     */
    private Jawisp(Config config) {
        this.config = config;

        log.info("JAWISP v1.0.22 starting ...");

        // Template engine logging
        var templateEngine = config.templateEngine();
        if (templateEngine != null) {
            log.info("Plugins: '{}' template rendering engine", templateEngine);
        }

        // Routes logging
        var index = new java.util.concurrent.atomic.AtomicInteger(1);
        config.getRoutes().forEach(route -> log.info("Route[{}]: {} {}", index.getAndIncrement(),
                route.getMethod().name(), route.getPath()));

        this.server = new NettyServer(config);
    }

    /**
     * Builds a Jawisp instance with default configuration.
     *
     * @return a new Jawisp instance
     */
    public static Jawisp build() {
        return build(config -> {
        });
    }

    /**
     * Builds a Jawisp instance with custom configuration.
     *
     * @param config a consumer to configure the application
     * @return a new Jawisp instance
     */
    public static Jawisp build(Consumer<Config> config) {
        Config cfg = new Config();
        config.accept(cfg);
        return new Jawisp(cfg);
    }

    /**
     * Starts the server and enters the development loop if in dev mode.
     *
     * @return this Jawisp instance
     */
    public Jawisp start() {
        startServer();

        var isDevelopmentMode = property.get()
            .get("jawisp.devtools.livereload.enabled")
            .asBoolean()
            .orElse(false);
        if (isDevelopmentMode) {
            startHotReload();
        }

        return this;
    }

    /**
     * Starts the HTTP server.
     */
    private void startServer() {
        try {
            server.start();
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Server started on http://localhost:{}/ in {} ms", config.port(), elapsedMs);
        } catch (Exception e) {
            log.error("Server start failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void startHotReload() {
        HotReloader reloader = new HotReloader(server);
        if (hotReloader.compareAndSet(null, reloader)) {
            reloader.start();
            log.info("HotReloader started in background");
        }
    }

    /**
     * Stops the HTTP server.
     */
    public void stop() {
        if (hotReloader.get() != null) {
            hotReloader.get().stop();
        }
        try {
            server.stop();
        } catch (Exception e) {
            log.error("Error during server stop: {}", e.getMessage());
        }
    }

    /**
     * Returns the configuration of the application.
     *
     * @return the configuration
     */
    public Config config() {
        return config;
    }

    /**
     * Returns the HTTP server instance.
     *
     * @return the HTTP server
     */
    public HttpServer server() {
        return server;
    }
}
