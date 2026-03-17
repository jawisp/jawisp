package io.jawisp.config.cors;

import java.util.List;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Immutable configuration for Cross-Origin Resource Sharing (CORS) in Netty servers.
 * Used by {@link io.jawisp.http.netty.NettyCorsSupport} to create {@link io.netty.handler.codec.http.cors.CorsHandler}.
 * <p>
 * All lists are unmodifiable. When {@link #allowAnyOrigin()} is {@code true},
 * {@link #allowedOrigins()} is ignored.
 *
 * @author Taras Chornyi
 * @since 1.0.18
 * @param enabled whether CORS handling is enabled
 * @param allowAnyOrigin whether all origins are permitted
 * @param allowedOrigins specific origins allowed 
 * @param allowedMethods HTTP methods permitted
 * @param allowedHeaders request headers clients may include
 * @param exposedHeaders response headers clients can access
 * @param allowCredentials whether credentials are allowed
 * @param allowNullOrigin whether null origins are permitted
 * @param shortCircuit whether to immediately reject invalid requests
 * @param maxAgeSeconds preflight response caching duration
 */
public record CorsSettings(
        /**
         * Whether CORS handling is enabled in the Netty pipeline.
         *
         * @param enabled {@code true} to enable CORS processing, {@code false} to skip
         *                {@link io.netty.handler.codec.http.cors.CorsHandler}
         */
        boolean enabled,

        /**
         * Whether all origins are permitted (equivalent to {@code Access-Control-Allow-Origin: *}).
         *
         * @param allowAnyOrigin {@code true} for wildcard origin matching, {@code false} to
         *                       use {@link #allowedOrigins()}
         */
        boolean allowAnyOrigin,

        /**
         * Specific origins allowed when {@link #allowAnyOrigin()} is {@code false}.
         * Examples: {@code "https://app.example.com"}, {@code "http://localhost:3000"}.
         *
         * @param allowedOrigins unmodifiable list of allowed origin URLs
         */
        List<String> allowedOrigins,

        /**
         * HTTP methods permitted in CORS requests and preflight responses.
         *
         * @param allowedMethods unmodifiable list of {@link HttpMethod} values
         */
        List<HttpMethod> allowedMethods,

        /**
         * Request headers clients may include (validated during preflight {@code OPTIONS}).
         * Common values: {@code "Content-Type"}, {@code "Authorization"}, custom headers.
         *
         * @param allowedHeaders unmodifiable list of header names
         */
        List<String> allowedHeaders,

        /**
         * Response headers clients can access via JavaScript.
         *
         * @param exposedHeaders unmodifiable list of header names to expose to client
         */
        List<String> exposedHeaders,

        /**
         * Whether credentials (cookies, HTTP authentication, client-side TLS certificates) are allowed.
         * Cannot be combined with {@link #allowAnyOrigin()}.
         *
         * @param allowCredentials {@code true} to permit credentials
         */
        boolean allowCredentials,

        /**
         * Whether requests from null origins are permitted (file://, data://, POSTMessage).
         *
         * @param allowNullOrigin {@code true} to allow requests with {@code Origin: null}
         */
        boolean allowNullOrigin,

        /**
         * Whether to immediately reject invalid CORS requests (403) instead of passing to next handler.
         *
         * @param shortCircuit {@code true} for fast-fail on invalid CORS requests
         */
        boolean shortCircuit,

        /**
         * Preflight response caching duration (seconds). Zero disables caching.
         * Maps to {@code Access-Control-Max-Age} header.
         *
         * @param maxAgeSeconds cache duration (0 = no caching)
         */
        long maxAgeSeconds) {

    /**
     * Returns a completely disabled CORS configuration.
     * No CORS headers are added to responses and preflight requests pass through unmodified.
     * <p>
     * Equivalent to not adding {@link io.netty.handler.codec.http.cors.CorsHandler} to pipeline.
     *
     * @return disabled CORS settings
     */
    public static CorsSettings disabled() {
        return new CorsSettings(false, false,
                List.of(), List.of(), List.of(), List.of(),
                false, false, true, 0);
    }

    /**
     * Returns permissive CORS settings suitable for <em>development only</em>.
     * Allows requests from any origin with credentials and common headers.
     * <b>Not recommended for production.</b>
     *
     * @return development-friendly CORS configuration
     */
    public static CorsSettings devAllOrigins() {
        return new CorsSettings(true, true,
                List.of(), List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS),
                List.of("Content-Type", "Authorization"),
                List.of(), true, true, true, 3600);
    }
}
