package io.jawisp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Config;
import io.jawisp.http.HttpServer;
import io.jawisp.http.Route;
import io.jawisp.http.Routes;

public class Jawisp {
    private static final Logger logger = LoggerFactory.getLogger(Jawisp.class);

    // private Channel serverChannel;
    // private final EventLoopGroup bossGroup;
    // private final EventLoopGroup workerGroup;

    private Jawisp(Config config) {
        logger.info("Starting Web JAWISP v2.0.0 ...");

        AtomicInteger index = new AtomicInteger(1);
        config.getRoutes().stream()
                .forEach(route -> logger.info("Route[{}]: {} {}",
                        index.getAndIncrement(),
                        route.getMethod(),
                        route.getPath()));

        // bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        // workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        // startServer(config);
        try {
            // server.start(config.getInt("server.port", 8080));
            HttpServer.create(config).start();
        } catch (Exception e) {
            logger.error("Error during starting server {}", e.getMessage());
        }
    }

    // private void startServer(Config config) {
    //     long start = System.nanoTime();

    //     ServerBootstrap b = new ServerBootstrap();
    //     b.group(bossGroup, workerGroup)
    //             .channel(NioServerSocketChannel.class)
    //             .option(ChannelOption.SO_BACKLOG, 4096)
    //             .childOption(ChannelOption.SO_KEEPALIVE, true)
    //             .childOption(ChannelOption.TCP_NODELAY, true) // Enable TCP_NODELAY to disable Nagle's algorithm
    //             .childHandler(new ChannelInitializer<SocketChannel>() {
    //                 @Override
    //                 protected void initChannel(SocketChannel ch) {
    //                     ch.pipeline().addLast(
    //                             new HttpServerCodec(),
    //                             new HttpObjectAggregator(65536),
    //                             new ServerHandler(config.routes));
    //                 }
    //             });

    //     try {
    //         serverChannel = b.bind(config.port).sync().channel();
    //         long end = System.nanoTime();
    //         long elapsedMs = (end - start) / 1_000_000;
    //         logger.info("Server started on {}:{}/ in {} ms", "http://localhost", String.valueOf(config.port), elapsedMs);
    //     } catch (InterruptedException e) {
    //         throw new RuntimeException(e);
    //     }
    // }

    // public void stop() {
    //     if (serverChannel != null) {
    //         serverChannel.close();
    //     }
    //     bossGroup.shutdownGracefully();
    //     workerGroup.shutdownGracefully();
    // }

    public static Config create() {
        return new Config();
    }

    // public static class Config {
    //     private int port = 8080;
    //     private final List<Route> routes = new ArrayList<>();

    //     public Config configure(Consumer<Config> config) {
    //         config.accept(this);
    //         return this;
    //     }

    //     public Config port(int port) {
    //         this.port = port;
    //         return this;
    //     }

    //     public Config routes(Consumer<Routes> routesConfig) {
    //         Routes routing = new Routes();
    //         routesConfig.accept(routing);
    //         this.routes.addAll(routing.getRoutes());
    //         return this;
    //     }

    //     public Jawisp start() {
    //         return new Jawisp(this);
    //     }

    //     public Jawisp start(Consumer<Config> config) {
    //         return new Jawisp(configure(config));
    //     }

    //     public int getPort() {
    //         return port;
    //     }

    //     public List<Route> getRoutes() {
    //         return routes;
    //     }
    // }
}