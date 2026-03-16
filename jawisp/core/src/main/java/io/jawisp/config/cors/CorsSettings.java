package io.jawisp.config.cors;

import java.util.List;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Represents the configuration settings for Cross-Origin Resource Sharing (CORS).
 *
 * @author Taras Chornyi
 * @since 1.0.18
 */
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

    /**
     * Creates a default CORS settings instance where CORS is disabled.
     *
     * @return a {@link CorsSettings} instance with CORS disabled
     */
    public static CorsSettings disabled() {
        return new CorsSettings(false, false,
                List.of(), List.of(), List.of(), List.of(),
                false, false, true, 0);
    }

    /**
     * Creates a CORS settings instance suitable for development where all origins are allowed.
     *
     * @return a {@link CorsSettings} instance with all origins allowed
     */
    public static CorsSettings devAllOrigins() {
        return new CorsSettings(true, true,
                List.of(), List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS),
                List.of("Content-Type", "Authorization"),
                List.of(), true, true, true, 3600);
    }
}