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

public class NettyServer implements HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Config config;

    private Channel channel;

    public NettyServer(Config config) {
        this.config = config;
        bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    }

    @Override
    public void start() throws Exception {
        long start = System.nanoTime();

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

            long end = System.nanoTime();
            long elapsedMs = (end - start) / 1_000_000;
            logger.info("Server started on {}:{}/ in {} ms", "http://localhost",
                    String.valueOf(config.getPort()), elapsedMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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
        logger.info("Jawisp Netty stopped");
    }

}
