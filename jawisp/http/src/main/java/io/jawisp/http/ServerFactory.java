package io.jawisp.http;

import io.jawisp.core.Jawisp.Config;

public interface ServerFactory {

    String getName();
    
    HttpServer create(Config config);

}
