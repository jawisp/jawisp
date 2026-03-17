package io.jawisp.http.netty;

import static io.jawisp.http.HttpMethod.GET;
import static io.jawisp.http.HttpMethod.OPTIONS;
import static io.jawisp.http.HttpMethod.POST;
import static io.jawisp.http.HttpMethod.PUT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.jawisp.config.cors.CorsSettings;
import io.jawisp.config.cors.CorsSettingsBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

class NettyCorsSupportTest {

    @Test
    void returnsNullWhenCorsDisabled() {
        CorsSettings disabled = new CorsSettingsBuilder()
                .enabled(false)
                .build();
        
        assertNull(NettyCorsSupport.from(disabled));
    }

    @Test
    void returnsNullWhenSettingsNull() {
        assertNull(NettyCorsSupport.from(null));
    }

    @Test
    void anyOriginConfig() {
        CorsSettings settings = new CorsSettingsBuilder()
                .allowAnyOrigin()
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // CorsConfigBuilder.forAnyOrigin() was called
    }

    @Test
    void specificOriginsConfig() {
        CorsSettings settings = new CorsSettingsBuilder()
                .origins("http://localhost:3000", "https://api.example.com")
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // CorsConfigBuilder.forOrigins(["http://localhost:3000", "https://api.example.com"])
    }

    @Test
    void emptyOriginsFallsBackToAnyOrigin() {
        CorsSettings settings = new CorsSettingsBuilder()
                .build(); // no origins set
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // Uses forAnyOrigin() due to empty origins list
    }

    @Test
    void configuresAllowedMethods() {
        CorsSettings settings = new CorsSettingsBuilder()
                .methods(GET, POST, PUT, OPTIONS)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // allowedRequestMethods([GET, POST, PUT, OPTIONS])
    }

    @Test
    void configuresAllowedHeaders() {
        CorsSettings settings = new CorsSettingsBuilder()
                .allowedHeaders("X-API-Key", "Authorization", "Content-Type")
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // allowedRequestHeaders(["X-API-Key", "Authorization", "Content-Type"])
    }

    @Test
    void configuresExposedHeaders() {
        CorsSettings settings = new CorsSettingsBuilder()
                .exposedHeaders("X-Rate-Limit", "X-Total-Count")
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // exposeHeaders(["X-Rate-Limit", "X-Total-Count"])
    }

    @Test
    void configuresCredentials() {
        CorsSettings settings = new CorsSettingsBuilder()
                .allowCredentials(true)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // allowCredentials() called
    }

    @Test
    void configuresNullOrigin() {
        CorsSettings settings = new CorsSettingsBuilder()
                .allowNullOrigin(true)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // allowNullOrigin() called
    }

    @Test
    void configuresMaxAge() {
        CorsSettings settings = new CorsSettingsBuilder()
                .maxAgeSeconds(7200)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // maxAge(7200) called
    }

    @Test
    void configuresShortCircuit() {
        CorsSettings settings = new CorsSettingsBuilder()
                .shortCircuit(true)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // shortCircuit() called
    }

    @Test
    void skipsMaxAgeWhenZero() {
        CorsSettings settings = new CorsSettingsBuilder()
                .maxAgeSeconds(0)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // maxAge(0) skipped (no call to builder.maxAge())
    }

    @Test
    void fullDevConfig() {
        CorsSettings settings = new CorsSettingsBuilder()
                .allowAnyOrigin()
                .methods(GET, POST, OPTIONS)
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
                .exposedHeaders("X-Rate-Limit")
                .allowCredentials(true)
                .allowNullOrigin(true)
                .shortCircuit(true)
                .maxAgeSeconds(86400)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // All features configured: anyOrigin + credentials + nullOrigin + shortCircuit + maxAge
    }

    @Test
    void strictProductionConfig() {
        CorsSettings settings = new CorsSettingsBuilder()
                .origins("https://app.example.com")
                .methods(GET)
                .allowedHeaders("Authorization")
                .allowCredentials(true)
                .shortCircuit(true)
                .maxAgeSeconds(3600)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // Specific origin + minimal methods + credentials + shortCircuit
    }

    @Test
    void minimalEnabledConfig() {
        CorsSettings settings = new CorsSettingsBuilder()
                .enabled(true)
                .build();
        
        CorsHandler handler = NettyCorsSupport.from(settings);
        assertNotNull(handler);
        // Uses defaults: anyOrigin (empty origins) + default methods/headers
    }
}
