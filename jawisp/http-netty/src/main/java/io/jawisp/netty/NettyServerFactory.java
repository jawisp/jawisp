package io.jawisp.netty;

import io.jawisp.http.Server;
import io.jawisp.http.ServerFactory;
import io.jawisp.http.handler.Handler;

public class NettyServerFactory implements ServerFactory {

    @Override
    public String getName() {
        return "netty";
    }

    @Override
    public Server create(Handler handler) {
        return new NettyServer(handler);
    }

}
