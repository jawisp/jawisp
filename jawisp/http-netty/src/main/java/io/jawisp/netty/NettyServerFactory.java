package io.jawisp.netty;

import io.jawisp.core.Jawisp.Config;
import io.jawisp.http.HttpServer;
import io.jawisp.http.ServerFactory;

public class NettyServerFactory implements ServerFactory  {
    
    @Override
    public String getName() {
        return "netty";
    }

    @Override
    public HttpServer create(Config config) {
        return new NettyServer(config);
    }

}
