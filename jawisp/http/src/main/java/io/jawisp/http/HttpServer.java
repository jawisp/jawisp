package io.jawisp.http;

// import io.jawisp.core.Jawisp.Config;
import java.util.ServiceLoader;

public interface HttpServer {

    void start() throws Exception;

    void stop() throws Exception;

    static HttpServer create(Config config) {
        return ServiceLoader.load(ServerFactory.class)
                .stream()
                .findFirst()
                .map(p -> p.get().create(config))
                .orElseThrow(() -> new IllegalArgumentException("No servers, please add server as a dependency"));
    }
}
