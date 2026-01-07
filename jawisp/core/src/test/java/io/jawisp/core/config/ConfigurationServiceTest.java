package io.jawisp.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadConfig() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        // Verify loaded values
        assertEquals("8080", service.getValue("server.port"));
        assertEquals("localhost", service.getValue("server.host"));
        assertEquals("jdbc:h2:mem:testdb", service.getValue("database.url"));
        assertEquals("true", service.getValue("database.enabled"));

        // Test default fallback
        assertEquals("", service.getValue("non.existent.key"));
    }

    @Test
    void testGetInt() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertEquals(8080, service.getInt("server.port"));
        assertEquals(0, service.getInt("non.existent.key"));
    }

    @Test
    void testGetBoolean() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertTrue(service.getBoolean("database.enabled"));
        assertFalse(service.getBoolean("non.existent.key"));
    }

    @Test
    void testGetAll() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        Map<String, String> all = service.getAll();
        assertNotNull(all);
        assertTrue(all.containsKey("server.port"));
        assertTrue(all.containsKey("database.url"));
    }

    @Test
    void testGetWithQuotedValues() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertEquals("INFO", service.getValue("logging.level"));
    }

    @Test
    void testEmptyOrCommentLinesIgnored() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertEquals("8080", service.getValue("server.port"));
        assertEquals("jdbc:h2:mem:testdb", service.getValue("database.url"));
    }
    
    @Test
    void testParseSimpleConfig() {
        ConfigurationService service = ConfigurationService.getInstance();
        
        String yamlContent = """
            server:
              port: 8080
              host: localhost
            database:
              url: jdbc:h2:mem:testdb
              enabled: true
            """;
        
        Map<String, String> result = service.parse(yamlContent);
        
        assertEquals("8080", result.get("server.port"));
        assertEquals("localhost", result.get("server.host"));
        assertEquals("jdbc:h2:mem:testdb", result.get("database.url"));
        assertEquals("true", result.get("database.enabled"));
    }
    
    @Test
    void testParseNestedConfig() {
        ConfigurationService service = ConfigurationService.getInstance();
        
        String yamlContent = """
            server:
              port: 8080
              host: localhost
              ssl:
                enabled: true
                certificate: /path/to/cert
            database:
              url: jdbc:h2:mem:testdb
              pool:
                maxSize: 10
                minSize: 2
            """;
        
        Map<String, String> result = service.parse(yamlContent);
        
        assertEquals("8080", result.get("server.port"));
        assertEquals("localhost", result.get("server.host"));
        assertEquals("true", result.get("server.ssl.enabled"));
        assertEquals("/path/to/cert", result.get("server.ssl.certificate"));
        assertEquals("jdbc:h2:mem:testdb", result.get("database.url"));
        assertEquals("10", result.get("database.pool.maxSize"));
        assertEquals("2", result.get("database.pool.minSize"));
    }
    
    @Test
    void testParseWithQuotedValues() {
        ConfigurationService service = ConfigurationService.getInstance();
        
        String yamlContent = """
            logging:
              level: "INFO"
              file: 'app.log'
            server:
              port: 8080
            """;
        
        Map<String, String> result = service.parse(yamlContent);
        
        assertEquals("INFO", result.get("logging.level"));
        assertEquals("app.log", result.get("logging.file"));
        assertEquals("8080", result.get("server.port"));
    }
        
    @Test
    void testParseWithDefaultEnvironmentVariables() {
        ConfigurationService service = ConfigurationService.getInstance();
        
        String yamlContent = """
            server:
              port: "{SERVER_PORT:8080}"
              host: "{SERVER_HOST:localhost}"
            """;
        
        // Ensure environment variables are not set to test default values
        System.clearProperty("SERVER_PORT");
        System.clearProperty("SERVER_HOST");
        
        Map<String, String> result = service.parse(yamlContent);
        
        assertEquals("8080", result.get("server.port"));
        assertEquals("localhost", result.get("server.host"));
    }
    
    @Test
    void testParseEmptyAndCommentLinesIgnored() {
        ConfigurationService service = ConfigurationService.getInstance();
        
        String yamlContent = """
            # This is a comment
            server:
              port: 8080
              
            # Another comment
            
            database:
              url: jdbc:h2:mem:testdb
            """;
        
        Map<String, String> result = service.parse(yamlContent);
        
        assertEquals("8080", result.get("server.port"));
        assertEquals("jdbc:h2:mem:testdb", result.get("database.url"));
        // assertEquals(result.containsKey("server.host"), ""); // Should not have this key
    }
    
    @Test
    void testParseComplexNestedStructure() {
        ConfigurationService service = ConfigurationService.getInstance();
        
        String yamlContent = """
            app:
              name: "MyApp"
              version: "1.0.0"
              features:
                logging:
                  enabled: true
                  level: "DEBUG"
                security:
                  enabled: false
                  roles:
                    admin:
                      permissions:
                        read: true
                        write: true
            """;
        
        Map<String, String> result = service.parse(yamlContent);
        
        assertEquals("MyApp", result.get("app.name"));
        assertEquals("1.0.0", result.get("app.version"));
        assertEquals("true", result.get("app.features.logging.enabled"));
        assertEquals("DEBUG", result.get("app.features.logging.level"));
        assertEquals("false", result.get("app.features.security.enabled"));
        assertEquals("true", result.get("app.features.security.roles.admin.permissions.read"));
        assertEquals("true", result.get("app.features.security.roles.admin.permissions.write"));
    }


}
