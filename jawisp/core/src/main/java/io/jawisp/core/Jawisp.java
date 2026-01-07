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
    private final ConfigurationService config = ConfigurationService.getInstance();

    private Jawisp() {
        logger.info("Starting JAWISP v1.0.0 ...");

        var routes = new DependencyInjector().getRoutes();
        var server = Server.create(new HttpHandler(routes));
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
