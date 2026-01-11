package io.jawisp.http.handler;

import java.util.Map;

public record RouteMatch(RouteHandler handler, Map<String, Object> pathParams) {
    
    static RouteMatch notFound() {
        return new RouteMatch(null, Map.of());
    }

    boolean isNotFound() {
        return handler == null;
    }
}
