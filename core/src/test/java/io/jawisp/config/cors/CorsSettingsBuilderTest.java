package io.jawisp.config.cors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static io.jawisp.http.HttpMethod.*;
import io.netty.handler.codec.http.HttpMethod;
import java.util.List;

class CorsSettingsBuilderTest {
    
    private CorsSettingsBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new CorsSettingsBuilder();
    }
    
    @Test
    void defaultsAreCorrect() {
        CorsSettings settings = builder.build();
        
        assertTrue(settings.enabled());
        assertFalse(settings.allowAnyOrigin());
        assertTrue(settings.allowedOrigins().isEmpty());
        assertEquals(List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS), settings.allowedMethods());
        assertEquals(List.of("Content-Type", "Authorization"), settings.allowedHeaders());
        assertFalse(settings.allowedHeaders().isEmpty());
        assertTrue(settings.allowCredentials());
        assertFalse(settings.allowNullOrigin());
        assertTrue(settings.shortCircuit());
        assertEquals(3600, settings.maxAgeSeconds());
    }
    
    @Test
    void disabledCors() {
        CorsSettings settings = builder.enabled(false).build();
        assertFalse(settings.enabled());
    }
    
    @Test
    void allowAnyOrigin() {
        CorsSettings settings = builder.allowAnyOrigin().build();
        assertTrue(settings.allowAnyOrigin());
    }
    
    @Test
    void specificOrigins() {
        CorsSettings settings = builder
            .origins("http://localhost:3000", "https://api.example.com")
            .build();
        
        assertEquals(List.of("http://localhost:3000", "https://api.example.com"), 
                    settings.allowedOrigins());
    }
    
    @Test
    void customMethods() {
        CorsSettings settings = builder
            .methods(GET, POST, PUT, DELETE, OPTIONS)
            .build();
        
        assertEquals(List.of(HttpMethod.GET, HttpMethod.POST, 
                           HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS),
                    settings.allowedMethods());
    }
    
    @Test
    void ignoresJawispInternalMethods() {
        CorsSettings settings = builder
            .methods(BEFORE_FILTER, GET, AFTER_FILTER, POST, ERROR)
            .build();
        
        // Only standard HTTP methods should survive Utils.methods() conversion
        assertEquals(List.of(HttpMethod.GET, HttpMethod.POST), 
                    settings.allowedMethods());
    }
    
    @Test
    void customHeaders() {
        CorsSettings settings = builder
            .allowedHeaders("X-API-Key", "X-Custom", "Content-Type")
            .exposedHeaders("X-Rate-Limit", "X-Total-Count")
            .build();
        
        assertEquals(List.of("X-API-Key", "X-Custom", "Content-Type"), 
                    settings.allowedHeaders());
        assertEquals(List.of("X-Rate-Limit", "X-Total-Count"), 
                    settings.exposedHeaders());
    }
    
    @Test
    void credentialsAndNullOrigin() {
        CorsSettings settings = builder
            .allowCredentials(false)
            .allowNullOrigin(true)
            .build();
        
        assertFalse(settings.allowCredentials());
        assertTrue(settings.allowNullOrigin());
    }
    
    @Test
    void shortCircuitAndMaxAge() {
        CorsSettings settings = builder
            .shortCircuit(false)
            .maxAgeSeconds(7200)
            .build();
        
        assertFalse(settings.shortCircuit());
        assertEquals(7200, settings.maxAgeSeconds());
    }
    
    @Test
    void fullDevConfig() {
        CorsSettings settings = builder
            .allowAnyOrigin()
            .methods(GET, POST, OPTIONS)
            .allowedHeaders("Content-Type", "Authorization", "X-Requested-With")
            .allowCredentials(true)
            .allowNullOrigin(true)
            .maxAgeSeconds(86400)
            .build();
        
        assertTrue(settings.allowAnyOrigin());
        assertTrue(settings.allowCredentials());
        assertTrue(settings.allowNullOrigin());
        assertEquals(86400, settings.maxAgeSeconds());
    }
    
    @Test
    void fullStrictConfig() {
        CorsSettings settings = builder
            .origins("https://app.example.com")
            .methods(GET, POST)
            .allowedHeaders("Authorization")
            .allowCredentials(true)
            .shortCircuit(true)
            .maxAgeSeconds(3600)
            .build();
        
        assertEquals(List.of("https://app.example.com"), settings.allowedOrigins());
        assertTrue(settings.shortCircuit());
    }
    
    @Test
    void chainableBuilder() {
        CorsSettings settings = new CorsSettingsBuilder()
            .enabled(true)
            .allowAnyOrigin()
            .origins("http://localhost:3000")
            .methods(OPTIONS)
            .allowedHeaders()
            .exposedHeaders("X-Custom")
            .allowCredentials(false)
            .allowNullOrigin(true)
            .shortCircuit(false)
            .maxAgeSeconds(0)
            .build();
        
        assertTrue(settings.enabled());
        assertTrue(settings.allowAnyOrigin());
        assertFalse(settings.allowCredentials());
        assertTrue(settings.allowNullOrigin());
        assertFalse(settings.shortCircuit());
        assertEquals(0, settings.maxAgeSeconds());
    }
    
    @Test
    void emptyAllowedHeaders() {
        CorsSettings settings = builder.allowedHeaders().build();
        assertTrue(settings.allowedHeaders().isEmpty());
    }
}
