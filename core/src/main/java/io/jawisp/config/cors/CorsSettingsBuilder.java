package io.jawisp.config.cors;

import java.util.ArrayList;
import java.util.List;
import io.jawisp.http.HttpMethod;

/**
 * Fluent builder for {@link CorsSettings}. Supports method chaining for configuring
 * CORS settings before creating an immutable {@link CorsSettings} instance.
 * <p>
 * Example usage:
 * <pre>{@code
 * Jawisp.build(config -> config 
 *             .cors(cors -> cors.origins("http://localhost:8080"))
 *             .start());
 * }</pre>
 *
 * @author Taras Chornyi
 * @since 1.0.18
 * @see CorsSettings
 */
public final class CorsSettingsBuilder {
    
    // All fields private for encapsulation
    private boolean enabled = true;
    private boolean allowAnyOrigin = false;
    private final List<String> allowedOrigins = new ArrayList<>();
    private final List<HttpMethod> allowedMethods = new ArrayList<>(List.of(
            HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS));
    private final List<String> allowedHeaders = new ArrayList<>(List.of(
            "Content-Type", "Authorization"));
    private final List<String> exposedHeaders = new ArrayList<>();
    private boolean allowCredentials = true;
    private boolean allowNullOrigin = false;
    private boolean shortCircuit = true;
    private long maxAgeSeconds = 3600;

    /**
     * Creates a new builder with default CORS settings (enabled, common methods/headers).
     */
    public CorsSettingsBuilder() {
        // Default constructor - all fields initialized above
    }

    /**
     * Enables or disables CORS handling entirely.
     *
     * @param enabled true to enable CORS processing
     * @return this builder
     */
    public CorsSettingsBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Allows requests from any origin ({@code Access-Control-Allow-Origin: *}).
     * Overrides {@link #origins(String...)}.
     *
     * @return this builder
     */
    public CorsSettingsBuilder allowAnyOrigin() {
        this.allowAnyOrigin = true;
        return this;
    }

    /**
     * Adds specific origins to the allow list. Ignored if {@link #allowAnyOrigin()}
     * is called.
     *
     * @param origins allowed origin URLs (e.g. "https://app.example.com")
     * @return this builder
     */
    public CorsSettingsBuilder origins(String... origins) {
        this.allowedOrigins.addAll(List.of(origins));
        return this;
    }

    /**
     * Sets allowed HTTP methods. Non-standard methods (BEFORE_FILTER, etc.) are filtered
     * out by {@link #convertMethods(List)}.
     *
     * @param methods allowed {@link HttpMethod}s
     * @return this builder
     */
    public CorsSettingsBuilder methods(HttpMethod... methods) {
        this.allowedMethods.clear();
        this.allowedMethods.addAll(List.of(methods));
        return this;
    }

    /**
     * Sets headers clients may send (validated in preflight OPTIONS requests).
     *
     * @param headers header names (e.g. "X-API-Key", "Authorization")
     * @return this builder
     */
    public CorsSettingsBuilder allowedHeaders(String... headers) {
        this.allowedHeaders.clear();
        this.allowedHeaders.addAll(List.of(headers));
        return this;
    }

    /**
     * Sets response headers clients can read via JavaScript.
     *
     * @param headers header names (e.g. "X-Rate-Limit")
     * @return this builder
     */
    public CorsSettingsBuilder exposedHeaders(String... headers) {
        this.exposedHeaders.clear();
        this.exposedHeaders.addAll(List.of(headers));
        return this;
    }

    /**
     * Allows credentials (cookies, auth headers) in cross-origin requests.
     * Cannot be used with {@link #allowAnyOrigin()}.
     *
     * @param allow true to permit credentials
     * @return this builder
     */
    public CorsSettingsBuilder allowCredentials(boolean allow) {
        this.allowCredentials = allow;
        return this;
    }

    /**
     * Allows requests from null origins (file://, data:// URLs).
     *
     * @param allow true to permit null Origin header
     * @return this builder
     */
    public CorsSettingsBuilder allowNullOrigin(boolean allow) {
        this.allowNullOrigin = allow;
        return this;
    }

    /**
     * Enables fast-failure for invalid CORS requests (returns 403 immediately).
     *
     * @param val true for short-circuit behavior
     * @return this builder
     */
    public CorsSettingsBuilder shortCircuit(boolean val) {
        this.shortCircuit = val;
        return this;
    }

    /**
     * Sets preflight response caching duration ({@code Access-Control-Max-Age}).
     *
     * @param seconds cache duration in seconds (0 = no cache)
     * @return this builder
     */
    public CorsSettingsBuilder maxAgeSeconds(long seconds) {
        this.maxAgeSeconds = seconds;
        return this;
    }

    /**
     * Converts a list of {@link io.jawisp.http.HttpMethod} to a list of
     * {@link io.netty.handler.codec.http.HttpMethod}.
     *
     * @param methods the list of Jawisp HTTP methods to convert
     * @return a list of Netty HTTP methods corresponding to the provided Jawisp
     *         HTTP methods
     */
    public List<io.netty.handler.codec.http.HttpMethod> convertMethods(List<HttpMethod> methods) {
        List<io.netty.handler.codec.http.HttpMethod> nettyMethods = new ArrayList<>();

        for (io.jawisp.http.HttpMethod jawisp : methods) {
            switch (jawisp) {
                case GET -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.GET);
                case POST -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.POST);
                case PUT -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.PUT);
                case DELETE -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.DELETE);
                case PATCH -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.PATCH);
                case HEAD -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.HEAD);
                case OPTIONS -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.OPTIONS);
                case TRACE -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.TRACE);
                case CONNECT -> nettyMethods.add(io.netty.handler.codec.http.HttpMethod.CONNECT);
                // skip BEFORE_FILTER, AFTER_FILTER, ERROR - they're Jawisp internals
                default -> {
                }
            }
        }

        return nettyMethods;
    }

    /**
     * Creates an immutable {@link CorsSettings} instance from current configuration.
     * Converts Jawisp {@link HttpMethod}s to Netty equivalents via {@link #convertMethods(List)}.
     *
     * @return configured {@link CorsSettings}
     */
    public CorsSettings build() {
        return new CorsSettings(
            enabled,
            allowAnyOrigin,
            List.copyOf(allowedOrigins),
            List.copyOf(convertMethods(allowedMethods)),
            List.copyOf(allowedHeaders),
            List.copyOf(exposedHeaders),
            allowCredentials,
            allowNullOrigin,
            shortCircuit,
            maxAgeSeconds
        );
    }
}
