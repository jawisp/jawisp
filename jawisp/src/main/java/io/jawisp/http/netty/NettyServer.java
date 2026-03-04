package io.jawisp.http.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.core.Config;
import io.jawisp.http.HttpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * The NettyServer class implements the HttpServer interface and uses the Netty framework to handle HTTP requests.
 * It sets up the Netty server with appropriate configurations and event loop groups.
 *
 * @author reftch
 * @version 1.0.2
 */
public class NettyServer implements HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Config config;

    private Channel channel;

    /**
     * Constructs a new NettyServer instance with the given configuration.
     *
     * @param config the configuration object for the server
     */
    public NettyServer(Config config) {
        this.config = config;
        bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    }

    /**
     * Starts the Netty server.
     * Binds the server to the specified port and sets up the necessary handlers.
     *
     * @throws Exception if an error occurs during server startup
     */
    @Override
    public void start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 4096)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true) // Enable TCP_NODELAY to disable Nagle's algorithm
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(65536),
                                new ServerHandler(config.getRoutes()));
                    }
                });

        try {
            ChannelFuture f = b.bind(config.getPort()).sync();
            channel = f.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops the Netty server.
     * Closes the channel and shuts down the event loop groups.
     *
     * @throws Exception if an error occurs during server shutdown
     */
    @Override
    public void stop() throws Exception {
        if (channel != null) {
            channel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        logger.info("Jawisp server stopped");
    }

}