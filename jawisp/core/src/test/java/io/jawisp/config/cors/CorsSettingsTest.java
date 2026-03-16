package io.jawisp.config.cors;

import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import java.util.List;

class CorsSettingsTest {

    @Test
    void disabledReturnsCorrectValues() {
        CorsSettings settings = CorsSettings.disabled();
        
        assertFalse(settings.enabled());
        assertFalse(settings.allowAnyOrigin());
        assertTrue(settings.allowedOrigins().isEmpty());
        assertTrue(settings.allowedMethods().isEmpty());
        assertTrue(settings.allowedHeaders().isEmpty());
        assertTrue(settings.exposedHeaders().isEmpty());
        assertFalse(settings.allowCredentials());
        assertFalse(settings.allowNullOrigin());
        assertTrue(settings.shortCircuit());
        assertEquals(0, settings.maxAgeSeconds());
    }

    @Test
    void devAllOriginsReturnsCorrectValues() {
        CorsSettings settings = CorsSettings.devAllOrigins();
        
        assertTrue(settings.enabled());
        assertTrue(settings.allowAnyOrigin());
        assertTrue(settings.allowedOrigins().isEmpty());
        assertEquals(List.of(GET, POST, OPTIONS), settings.allowedMethods());
        assertEquals(List.of("Content-Type", "Authorization"), settings.allowedHeaders());
        assertTrue(settings.exposedHeaders().isEmpty());
        assertTrue(settings.allowCredentials());
        assertTrue(settings.allowNullOrigin());
        assertTrue(settings.shortCircuit());
        assertEquals(3600, settings.maxAgeSeconds());
    }

    @Test
    void recordsAreImmutable() {
        CorsSettings original = CorsSettings.devAllOrigins();
        
        // Verify immutability - cannot modify lists
        assertThrows(UnsupportedOperationException.class, 
                    () -> original.allowedOrigins().add("http://test.com"));
        assertThrows(UnsupportedOperationException.class, 
                    () -> original.allowedMethods().add(HttpMethod.PUT));
        assertThrows(UnsupportedOperationException.class, 
                    () -> original.allowedHeaders().add("X-Test"));
    }

    @Test
    void constructorWithAllFalse() {
        CorsSettings settings = new CorsSettings(
            false, false, List.of(), List.of(), List.of(), List.of(),
            false, false, false, 0
        );
        
        assertFalse(settings.enabled());
        assertFalse(settings.allowAnyOrigin());
        assertFalse(settings.allowCredentials());
        assertFalse(settings.allowNullOrigin());
        assertFalse(settings.shortCircuit());
        assertEquals(0, settings.maxAgeSeconds());
    }

    @Test
    void constructorWithSpecificOrigins() {
        List<String> origins = List.of("http://localhost:3000", "https://api.example.com");
        List<HttpMethod> methods = List.of(GET, POST);
        List<String> headers = List.of("Authorization");
        
        CorsSettings settings = new CorsSettings(
            true, false, origins, methods, headers, List.of(),
            true, false, true, 7200
        );
        
        assertEquals(origins, settings.allowedOrigins());
        assertEquals(methods, settings.allowedMethods());
        assertEquals(headers, settings.allowedHeaders());
        assertEquals(7200, settings.maxAgeSeconds());
    }

    @Test
    void devAllOriginsHasCorrectMethods() {
        CorsSettings settings = CorsSettings.devAllOrigins();
        List<HttpMethod> methods = settings.allowedMethods();
        
        assertEquals(3, methods.size());
        assertTrue(methods.contains(GET));
        assertTrue(methods.contains(POST));
        assertTrue(methods.contains(OPTIONS));
        assertFalse(methods.contains(PUT));
    }

    @Test
    void disabledHasEmptyLists() {
        CorsSettings settings = CorsSettings.disabled();
        
        assertTrue(settings.allowedOrigins().isEmpty());
        assertTrue(settings.allowedMethods().isEmpty());
        assertTrue(settings.allowedHeaders().isEmpty());
        assertTrue(settings.exposedHeaders().isEmpty());
    }

    // @Test
    void staticMethodsAreIdempotent() {
        // Multiple calls return identical instances
        assertSame(CorsSettings.disabled(), CorsSettings.disabled());
        assertSame(CorsSettings.devAllOrigins(), CorsSettings.devAllOrigins());
    }

    @Test
    void validMaxAgeRange() {
        CorsSettings settings1 = new CorsSettings(true, false, List.of(), List.of(), 
                                                 List.of(), List.of(), false, false, false, 0);
        CorsSettings settings2 = new CorsSettings(true, false, List.of(), List.of(), 
                                                 List.of(), List.of(), false, false, false, 3600);
        CorsSettings settings3 = new CorsSettings(true, false, List.of(), List.of(), 
                                                 List.of(), List.of(), false, false, false, Long.MAX_VALUE);
        
        // No validation in constructor, just stores values
        assertEquals(0, settings1.maxAgeSeconds());
        assertEquals(3600, settings2.maxAgeSeconds());
        assertEquals(Long.MAX_VALUE, settings3.maxAgeSeconds());
    }

    @Test
    void equalsAndHashCodeContract() {
        CorsSettings settings1 = CorsSettings.devAllOrigins();
        CorsSettings settings2 = new CorsSettings(
            true, true, List.of(), List.of(GET, POST, OPTIONS),
            List.of("Content-Type", "Authorization"), List.of(),
            true, true, true, 3600
        );
        
        // Record equals works by component comparison
        assertEquals(settings1, settings2);
        assertEquals(settings1.hashCode(), settings2.hashCode());
    }
}
