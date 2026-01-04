package io.jawisp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.core.config.ConfigurationService;
import io.jawisp.core.inject.DependencyInjector;
import io.jawisp.http.HttpHandler;
import io.jawisp.http.Server;;

public class Jawisp {

    private final static Logger logger = LoggerFactory.getLogger(Jawisp.class);

    private static volatile Jawisp instance;
    // private final Server server;
    private final ConfigurationService config = ConfigurationService.getInstance();

    private Jawisp() {
        logger.info("Starting Jawisp ...");

        var injector = new DependencyInjector();
        var handler = new HttpHandler(injector.getRoutes());

        var server = Server.create("netty", handler);
        // server = Server.create("netty", (req, res) -> {
        //     res.body = switch (req.path) {
        //         case "/" -> "<h2>Hello Jawisp Netty!</h2>".getBytes();
        //         case "/api" -> "<h1>API OK</h1>".getBytes();
        //         default -> "<h1>404 Not Found</h1>".getBytes();
        //     };
        // });

        try {
            server.start(config.getInt("server.port", 8080));
        } catch (Exception e) {
            logger.error("Error during starting server {}", e.getMessage());
        }
    }

    public static Jawisp run() {
        if (instance == null) {
            synchronized (Jawisp.class) {
                if (instance == null) {
                    instance = new Jawisp();
                }
            }
        }
        return instance;
    }
}
