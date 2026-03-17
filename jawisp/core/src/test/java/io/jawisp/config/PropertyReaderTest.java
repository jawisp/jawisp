package io.jawisp.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PropertyReaderTest {

    @TempDir
    Path tempDir;

    private Path testPropsPath;
    private String testResourceName;

    @BeforeEach
    void setUp() throws IOException {
        testPropsPath = tempDir.resolve("test.properties");
        Files.createFile(testPropsPath);

        testResourceName = "test.properties";

        Thread.currentThread().setContextClassLoader(new TestClassLoader(tempDir));
    }

    @AfterEach
    void tearDown() {
        resetSingleton();
        Thread.currentThread().setContextClassLoader(null);
    }

    @Test
    void testSingletonThreadSafety() throws InterruptedException {
        Runnable loader = () -> PropertyReader.getInstance(testResourceName);

        Thread t1 = new Thread(loader);
        Thread t2 = new Thread(loader);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertSame(PropertyReader.getInstance(testResourceName),
                PropertyReader.getInstance(testResourceName));
    }

    @Test
    void testLoadFromPropertiesFile() throws IOException {
        writeTestProperties("""
                web.page-size=50
                web.debug=true
                db.url=jdbc:postgresql://localhost/test
                server.port=8080
                http.cors.allowed-origins=http://localhost:8080
                """);

        PropertyReader config = PropertyReader.getInstance(testResourceName);

        assertTrue(config.exists());

        int pageSize = config.get("web.page-size").asInt().orElse(20);
        assertEquals(50, pageSize);

        int pageSize2 = config.get("web").get("page-size").asInt().orElse(20);
        assertEquals(50, pageSize2);

        String dbUrl = config.get("db.url").asString().orElse("none");
        assertEquals("jdbc:postgresql://localhost/test", dbUrl);

        boolean debug = config.get("web.debug").asBoolean().orElse(false);
        assertTrue(debug);

        String cors = config.get("http.cors.allowed-origins").asString().orElse("*");
        assertEquals("http://localhost:8080", cors);
        
        String cors2 = config.get("http").get("cors").get("allowed-origins").asString().orElse("*");
        assertEquals("http://localhost:8080", cors2);
    }

    @Test
    void testMissingPropertiesDefaultValues() throws IOException {
        writeTestProperties("web.page-size=30");

        PropertyReader config = PropertyReader.getInstance(testResourceName);

        int missingInt = config.get("web.missing").asInt().orElse(100);
        assertEquals(100, missingInt);

        boolean missingBool = config.get("debug.enabled").asBoolean().orElse(false);
        assertFalse(missingBool);

        String missingStr = config.get("app.name").asString().orElse("default-app");
        assertEquals("default-app", missingStr);
    }

    @Test
    void testBooleanParsing() throws IOException {
        writeTestProperties("""
                features.enabled=true
                features.beta=1
                features.alpha=yes
                features.gamma=Y
                features.off=false
                features.zero=0
                features.no=no
                features.n=N
                """);

        PropertyReader config = PropertyReader.getInstance(testResourceName);

        assertAll(
                () -> assertTrue(config.get("features.enabled").asBoolean().orElse(false)),
                () -> assertTrue(config.get("features.beta").asBoolean().orElse(false)),
                () -> assertTrue(config.get("features.alpha").asBoolean().orElse(false)),
                () -> assertTrue(config.get("features.gamma").asBoolean().orElse(false)),
                () -> assertFalse(config.get("features.off").asBoolean().orElse(true)),
                () -> assertFalse(config.get("features.zero").asBoolean().orElse(true)),
                () -> assertFalse(config.get("features.no").asBoolean().orElse(true)),
                () -> assertFalse(config.get("features.n").asBoolean().orElse(true)));
    }

    @Test
    void testEmptyPropertiesFile() {
        PropertyReader config = PropertyReader.getInstance(testResourceName);
        assertFalse(config.exists());
        assertEquals(0, config.children().size());
        assertEquals(42, config.get("missing").asInt().orElse(42));
    }

    @Test
    void testChildrenNavigation() throws IOException {
        writeTestProperties("""
                web.page-size=50
                web.debug=true
                web.cache.enabled=false
                db.url=jdbc:h2:mem:test
                """);

        PropertyReader config = PropertyReader.getInstance(testResourceName);
        Map<String, PropertyReader> children = config.children();

        assertEquals(2, children.size());
        assertTrue(children.containsKey("web"));
        assertTrue(children.containsKey("db"));

        PropertyReader web = children.get("web");
        assertTrue(web.exists());

        Map<String, PropertyReader> webChildren = web.children();
        assertEquals(3, webChildren.size());
        assertTrue(webChildren.containsKey("page-size"));
        assertTrue(webChildren.containsKey("debug"));
        assertTrue(webChildren.containsKey("cache"));
    }

    @Test
    void testInvalidNumberParsing() throws IOException {
        writeTestProperties("web.port=invalid");

        PropertyReader config = PropertyReader.getInstance(testResourceName);
        int port = config.get("web.port").asInt().orElse(8080);
        assertEquals(8080, port);
    }

    @Test
    void testNullPointerException() {
        PropertyReader config = PropertyReader.getInstance(testResourceName);
        assertThrows(NullPointerException.class, () -> config.get(null));
    }

    @Test
    void testOfFactoryMethod() {
        // Create Node using reflection or direct access since it's package-private
        PropertyReader.Node node = createTestNode("test-value");
        PropertyReader config = PropertyReader.of(node);

        assertEquals("test-value", config.asString().raw());
        assertTrue(config.exists());
    }

    @Test
    void testPropertyValueMethods() {
        PropertyReader.Node node = createTestNode("42");
        PropertyReader config = PropertyReader.of(node);
        PropertyReader.PropertyValue<Integer> value = config.asInt();

        assertEquals("42", value.raw());
        assertEquals(42, value.orElse(0));
        assertEquals(42, value.orElseGet(() -> 99));
        assertEquals(42, value.asOptional().orElse(0));
    }

    @Test
    void testPropertyValueMissing() {
        PropertyReader config = PropertyReader.of(createTestNode(null));
        PropertyReader.PropertyValue<Integer> value = config.asInt();

        assertNull(value.raw());
        assertEquals(100, value.orElse(100));
    }

    @Test
    void testPropertyValueOrElseThrow() {
        PropertyReader config = PropertyReader.of(createTestNode(null));
        PropertyReader.PropertyValue<Integer> value = config.asInt();

        assertThrows(NoSuchElementException.class, value::orElseThrow);
    }

    // ----- Helper methods -----

    private PropertyReader.Node createTestNode(String value) {
        return new PropertyReader.Node(new LinkedHashMap<>(), value);
    }

    private static void resetSingleton() {
        try {
            java.lang.reflect.Field instanceField = PropertyReader.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception ignored) {
        }
    }

    private static class TestClassLoader extends ClassLoader {
        private final Path tempDir;

        TestClassLoader(Path tempDir) {
            super(PropertyReaderTest.class.getClassLoader());
            this.tempDir = tempDir;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if ("test.properties".equals(name)) {
                try {
                    Path propFile = tempDir.resolve(name);
                    if (Files.exists(propFile)) {
                        return Files.newInputStream(propFile);
                    }
                } catch (IOException ignored) {
                }
            }
            return super.getResourceAsStream(name);
        }
    }

    private void writeTestProperties(String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(testPropsPath.toFile())) {
            writer.print(content.trim());
        }
    }

    @Test
    void debugTreeStructure() throws IOException {
        writeTestProperties("""
                web.page-size=50
                web.debug=true
                """);

        PropertyReader config = PropertyReader.getInstance(testResourceName);

        // Debug: print tree structure
        System.out.println("Root children: " + config.children().keySet());
        PropertyReader web = config.get("web");
        System.out.println("web node value: '" + web.asString().raw() + "'");
        System.out.println("web children: " + web.children().keySet());

        PropertyReader pageSize = web.get("page-size");
        System.out.println("page-size node value: '" + pageSize.asString().raw() + "'");

        // These should now pass
        assertEquals(50, config.get("web").get("page-size").asInt().orElse(20));
        assertTrue(config.get("web.debug").asBoolean().orElse(false));
    }

}
