package io.jawisp.core.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceConfigGeneratorTest {

    private Path tempProject;
    private Path resourcesDir;

    @BeforeEach
    void setUp() throws IOException {
        tempProject = Files.createTempDirectory("rgen-test");
        resourcesDir = tempProject.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);

        // Redirect user.dir so ResourceConfigGenerator writes inside this temp dir
        System.setProperty("user.dir", tempProject.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempProject)) {
            try (var walk = Files.walk(tempProject)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    @Test
    void toJson_shouldConvertMapAndListCorrectly() throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", List.of("a", "b"));

        String json = invokeToJson(map);

        assertTrue(json.contains("\"key1\":\"value1\""));
        assertTrue(json.contains("\"key2\":[\"a\",\"b\"]"));
    }

    @Test
    void escapeJson_shouldEscapeControlCharacters() throws Exception {
        String raw = "Hello \"world\"\npath\\file";
        String escaped = invokeEscapeJson(raw);
        assertEquals("Hello \\\"world\\\"\\npath\\\\file", escaped);
    }

    @Test
    void run_shouldGenerateResourceConfigFile() throws IOException {
        // Arrange: create structure under real working dir, not tempProject
        Path cwd = Path.of("").toAbsolutePath(); // real working directory
        Path resourcesDir = cwd.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);
        Files.createDirectories(resourcesDir.resolve("META-INF/native-image"));

        Path sample = resourcesDir.resolve("assets/sample.txt");
        Files.createDirectories(sample.getParent());
        Files.writeString(sample, "test-content");

        // Act
        ResourceConfigGenerator.run();

        // Assert: output under real cwd/build/...
        Path outputFile = cwd.resolve("build/resources/main/META-INF/native-image/resource-config.json");
        assertTrue(Files.exists(outputFile), "Output file missing at: " + outputFile);

        String json = Files.readString(outputFile);
        assertTrue(json.contains("META-INF/native-image/reflect-config.json"));
        assertTrue(json.contains("assets/sample.txt"));
    }

    // --- Private reflection helpers for private methods ---

    private static String invokeToJson(Object o) throws Exception {
        var m = ResourceConfigGenerator.class.getDeclaredMethod("toJson", Object.class);
        m.setAccessible(true);
        return (String) m.invoke(null, o);
    }

    private static String invokeEscapeJson(String s) throws Exception {
        var m = ResourceConfigGenerator.class.getDeclaredMethod("escapeJson", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, s);
    }
}
