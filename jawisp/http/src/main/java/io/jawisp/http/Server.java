package io.jawisp.http;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public interface Server {
    void start(int port) throws Exception;

    void stop() throws Exception;

    class Request {
        private final String method, path, body;
        private final Map<String, String> headers, queryParams;
        
        public Request(String method, String path, String body, 
                      Map<String, String> headers, Map<String, String> params) {
            this.method = method;
            this.path = path;
            this.body = body;
            this.headers = headers;
            this.queryParams = params;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public String getBody() {
            return body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Map<String, String> getQueryParams() {
            return queryParams;
        }
    }
    
    class Response {
        private final int status;
        private String contentType;
        private byte[] body;
        private final Map<String, String> headers;
        
        public Response(int status, String contentType, byte[] body) {
            this(status, contentType, body, new HashMap<>());
        }
        
        public Response(int status, String contentType, byte[] body, Map<String, String> headers) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
            this.headers = headers;
        }

        public int getStatus() {
            return status;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }

        public Map<String, String> getHeaders() {
            return headers;
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
