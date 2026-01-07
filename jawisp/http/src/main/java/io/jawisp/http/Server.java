package io.jawisp.http;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public interface Server {
    void start(int port) throws Exception;

    void stop() throws Exception;

    class Request {
        public final String method, path, body;
        public final Map<String, String> headers, params;
        
        public Request(String method, String path, String body, 
                      Map<String, String> headers, Map<String, String> params) {
            this.method = method;
            this.path = path;
            this.body = body;
            this.headers = headers;
            this.params = params;
        }
    }
    
    class Response {
        public final int status;
        public final String contentType;
        public byte[] body;
        public final Map<String, String> headers;
        
        public Response(int status, String contentType, byte[] body) {
            this(status, contentType, body, new HashMap<>());
        }
        
        public Response(int status, String contentType, byte[] body, Map<String, String> headers) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
            this.headers = headers;
        }
    }

    static Server create(String impl, Handler handler) {
        return ServiceLoader.load(ServerFactory.class)
                .stream()
                .filter(p -> impl.equals(p.get().getName()))
                .findFirst()
                .map(p -> p.get().create(handler))
                .orElseThrow(() -> new IllegalArgumentException("No " + impl));
    }

    static Server create(Handler handler) {
        return ServiceLoader.load(ServerFactory.class)
                .stream()
                .findFirst()
                .map(p -> p.get().create(handler))
                .orElseThrow(() -> new IllegalArgumentException("No servers, please add server as a dependency"));
    }
}
