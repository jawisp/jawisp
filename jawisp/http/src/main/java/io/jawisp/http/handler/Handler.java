package io.jawisp.http.handler;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;

public interface Handler {

    void handle(Request req, Response res);
    
}
