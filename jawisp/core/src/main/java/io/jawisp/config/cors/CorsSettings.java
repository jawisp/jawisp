package io.jawisp.config.cors;

import java.util.List;
import io.netty.handler.codec.http.HttpMethod;

public record CorsSettings(
        boolean enabled,
        boolean allowAnyOrigin,
        List<String> allowedOrigins,
        List<HttpMethod> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        boolean allowNullOrigin,
        boolean shortCircuit,
        long maxAgeSeconds
) {
    public static CorsSettings disabled() {
        return new CorsSettings(false, false,
                List.of(), List.of(), List.of(), List.of(),
                false, false, true, 0);
    }

    public static CorsSettings devAllOrigins() {
        return new CorsSettings(true, true,
                List.of(), List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS),
                List.of("Content-Type", "Authorization"),
                List.of(), true, true, true, 3600);
    }
}
