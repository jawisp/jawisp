package io.jawisp.config.cors;

import java.util.ArrayList;
import java.util.List;

import io.jawisp.http.HttpMethod;
import io.jawisp.http.netty.Utils;

/**
 * A builder class for creating instances of {@link CorsSettings}.
 * Provides a fluent API to configure CORS settings.
 *
 * @author Taras Chornyi
 * @since 1.0.18
 */
public final class CorsSettingsBuilder {
    private boolean enabled = true;
    private boolean allowAnyOrigin = false;
    private List<String> allowedOrigins = new ArrayList<>();
    private List<HttpMethod> allowedMethods = new ArrayList<>(List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS));
    private List<String> allowedHeaders = new ArrayList<>(List.of("Content-Type", "Authorization"));
    private List<String> exposedHeaders = new ArrayList<>();
    private boolean allowCredentials = true;
    private boolean allowNullOrigin = false;
    private boolean shortCircuit = true;
    private long maxAgeSeconds = 3600;

    /**
     * Sets whether CORS is enabled.
     *
     * @param enabled the enabled status to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Allows any origin for CORS requests.
     *
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder allowAnyOrigin() {
        this.allowAnyOrigin = true;
        return this;
    }

    /**
     * Adds allowed origins for CORS requests.
     *
     * @param origins the origins to add
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder origins(String... origins) {
        this.allowedOrigins.addAll(List.of(origins));
        return this;
    }

    /**
     * Sets the allowed HTTP methods for CORS requests.
     *
     * @param methods the HTTP methods to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder methods(HttpMethod... methods) {
        this.allowedMethods = new ArrayList<>(List.of(methods));
        return this;
    }

    /**
     * Sets the allowed headers for CORS requests.
     *
     * @param headers the headers to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder allowedHeaders(String... headers) {
        this.allowedHeaders = new ArrayList<>(List.of(headers));
        return this;
    }

    /**
     * Sets the exposed headers for CORS requests.
     *
     * @param headers the headers to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder exposedHeaders(String... headers) {
        this.exposedHeaders = new ArrayList<>(List.of(headers));
        return this;
    }

    /**
     * Sets whether credentials are allowed for CORS requests.
     *
     * @param allow the allow status to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder allowCredentials(boolean allow) {
        this.allowCredentials = allow;
        return this;
    }

    /**
     * Sets whether null origin is allowed for CORS requests.
     *
     * @param allow the allow status to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder allowNullOrigin(boolean allow) {
        this.allowNullOrigin = allow;
        return this;
    }

    /**
     * Sets whether CORS should short-circuit.
     *
     * @param val the short-circuit status to set
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder shortCircuit(boolean val) {
        this.shortCircuit = val;
        return this;
    }

    /**
     * Sets the maximum age in seconds for preflight requests.
     *
     * @param seconds the maximum age in seconds
     * @return the current CorsSettingsBuilder instance
     */
    public CorsSettingsBuilder maxAgeSeconds(long seconds) {
        this.maxAgeSeconds = seconds;
        return this;
    }

    /**
     * Builds and returns a {@link CorsSettings} instance based on the current configuration.
     *
     * @return the CorsSettings instance
     */
    public CorsSettings build() {
        return new CorsSettings(enabled, allowAnyOrigin, List.copyOf(allowedOrigins),
                List.copyOf(Utils.methods(allowedMethods)), List.copyOf(allowedHeaders),
                List.copyOf(exposedHeaders), allowCredentials, allowNullOrigin,
                shortCircuit, maxAgeSeconds);
    }
}