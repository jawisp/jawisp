package io.jawisp.http.netty;

import io.jawisp.config.cors.CorsSettings;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

/**
 * A utility class to provide support for configuring CORS (Cross-Origin
 * Resource Sharing)
 * in Netty using {@link CorsSettings}.
 *
 * @author Taras Chornyi
 * @since 1.0.18
 */
public final class NettyCorsSupport {

    /**
     * Creates a {@link CorsHandler} from the provided {@link CorsSettings}.
     *
     * @param s the CORS settings to use for configuration
     * @return a {@link CorsHandler} based on the provided settings, or null if CORS
     *         is disabled
     */
    public static CorsHandler from(CorsSettings s) {
        if (s == null || !s.enabled()) {
            return null;
        }
        CorsConfigBuilder builder;

        if (s.allowAnyOrigin() || s.allowedOrigins().isEmpty()) {
            builder = CorsConfigBuilder.forAnyOrigin();
        } else {
            builder = CorsConfigBuilder.forOrigins(s.allowedOrigins().toArray(String[]::new));
        }

        if (!s.allowedMethods().isEmpty()) {
            builder.allowedRequestMethods(
                    s.allowedMethods().toArray(HttpMethod[]::new) 
            );
        }

        if (!s.allowedHeaders().isEmpty()) {
            builder.allowedRequestHeaders(s.allowedHeaders().toArray(String[]::new));
        }

        if (!s.exposedHeaders().isEmpty()) {
            builder.exposeHeaders(s.exposedHeaders().toArray(String[]::new));
        }

        if (s.allowCredentials()) {
            builder.allowCredentials();
        }

        if (s.allowNullOrigin()) {
            builder.allowNullOrigin();
        }

        if (s.maxAgeSeconds() > 0) {
            builder.maxAge(s.maxAgeSeconds());
        }

        if (s.shortCircuit()) {
            builder.shortCircuit();
        }

        CorsConfig config = builder.build();
        return new CorsHandler(config);
    }

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private NettyCorsSupport() {
    }
}