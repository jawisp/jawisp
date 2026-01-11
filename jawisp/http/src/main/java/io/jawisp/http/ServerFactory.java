package io.jawisp.http;

import io.jawisp.http.handler.Handler;

public interface ServerFactory {

    String getName();
    
    Server create(Handler handler);
    
}
