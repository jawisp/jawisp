package io.jawisp.http;

public interface ServerFactory {

    String getName();
    
    Server create(Handler handler);
    
}
