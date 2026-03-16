package io.jawisp.config.cors;

import java.util.ArrayList;
import java.util.List;

import io.jawisp.http.HttpMethod;
import io.jawisp.http.netty.Utils;

public final class CorsSettingsBuilder {
    boolean enabled = true;
    boolean allowAnyOrigin = false;
    List<String> allowedOrigins = new ArrayList<>();
    List<HttpMethod> allowedMethods = new ArrayList<>(List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS));
    List<String> allowedHeaders = new ArrayList<>(List.of("Content-Type", "Authorization"));
    List<String> exposedHeaders = new ArrayList<>();
    boolean allowCredentials = true;
    boolean allowNullOrigin = false;
    boolean shortCircuit = true;
    long maxAgeSeconds = 3600;

    public CorsSettingsBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public CorsSettingsBuilder allowAnyOrigin() {
        this.allowAnyOrigin = true;
        return this;
    }

    public CorsSettingsBuilder origins(String... origins) {
        this.allowedOrigins.addAll(List.of(origins));
        return this;
    }

    public CorsSettingsBuilder methods(HttpMethod... methods) {
        this.allowedMethods = new ArrayList<>(List.of(methods));
        return this;
    }

    public CorsSettingsBuilder allowedHeaders(String... headers) {
        this.allowedHeaders = new ArrayList<>(List.of(headers));
        return this;
    }

    public CorsSettingsBuilder exposedHeaders(String... headers) {
        this.exposedHeaders = new ArrayList<>(List.of(headers));
        return this;
    }

    public CorsSettingsBuilder allowCredentials(boolean allow) {
        this.allowCredentials = allow;
        return this;
    }

    public CorsSettingsBuilder allowNullOrigin(boolean allow) {
        this.allowNullOrigin = allow;
        return this;
    }

    public CorsSettingsBuilder shortCircuit(boolean val) {
        this.shortCircuit = val;
        return this;
    }

    public CorsSettingsBuilder maxAgeSeconds(long seconds) {
        this.maxAgeSeconds = seconds;
        return this;
    }

    public CorsSettings build() {
        return new CorsSettings(enabled, allowAnyOrigin, List.copyOf(allowedOrigins),
                List.copyOf(Utils.methods(allowedMethods)), List.copyOf(allowedHeaders),
                List.copyOf(exposedHeaders), allowCredentials, allowNullOrigin,
                shortCircuit, maxAgeSeconds);
    }
}
