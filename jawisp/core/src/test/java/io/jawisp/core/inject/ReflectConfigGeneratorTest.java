package io.jawisp.core.inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReflectConfigGeneratorTest {

    @Test
    void extractPackageName_findsPackage() {
        String content = "package com.example;\npublic class Test {}";
        String pkg = ReflectConfigGenerator.extractPackageName(content);
        assertEquals("com.example", pkg);
    }

    @Test
    void extractPackageName_emptyWhenMissing() {
        String content = "public class Test {}";
        String pkg = ReflectConfigGenerator.extractPackageName(content);
        assertEquals("", pkg);
    }

    @Test
    void extractClassName_findsWithPackage() {
        String content = "package com.example;\npublic class TestClass {}";
        String className = ReflectConfigGenerator.extractClassName(content, "com.example");
        assertEquals("com.example.TestClass", className);
    }

    @Test
    void extractClassName_simpleWithoutPackage() {
        String content = "public class TestClass {}";
        String className = ReflectConfigGenerator.extractClassName(content, "");
        assertEquals("TestClass", className);
    }

    @Test
    void extractClassName_returnsNullWhenNoPublicClass() {
        String content = "class TestClass {}";
        String className = ReflectConfigGenerator.extractClassName(content, "");
        assertNull(className);
    }

    @Test
    void hasAnyAnnotation_findsController() {
        String content = "@Controller public class Test {}";
        boolean has = ReflectConfigGenerator.hasAnyAnnotation(content, ReflectConfigGenerator.TARGET_ANNOTATIONS);
        assertTrue(has);
    }

    @Test
    void hasAnyAnnotation_findsService() {
        String content = "@Service public class Test {}";
        boolean has = ReflectConfigGenerator.hasAnyAnnotation(content, ReflectConfigGenerator.TARGET_ANNOTATIONS);
        assertTrue(has); // Tests short name detection
    }

    @Test
    void hasAnyAnnotation_returnsFalseForUnknown() {
        String content = "@Unknown public class Test {}";
        boolean has = ReflectConfigGenerator.hasAnyAnnotation(content, ReflectConfigGenerator.TARGET_ANNOTATIONS);
        assertFalse(has);
    }

    @Test
    void findInjectFields_findsValidField() {
        String content = """
                @Inject
                private HomeService homeService;
                """;
        List<String> fields = ReflectConfigGenerator.findInjectFields(content);
        assertEquals(List.of("homeService"), fields);
    }

    @Test
    void findInjectFields_ignoresSingleLetter() {
        String content = """
                @Inject private int e;
                """;
        List<String> fields = ReflectConfigGenerator.findInjectFields(content);
        assertTrue(fields.isEmpty());
    }

    @Test
    void findInjectFields_ignoresKeywords() {
        String content = """
                @Inject private String it;
                @Inject private Object arg;
                """;
        List<String> fields = ReflectConfigGenerator.findInjectFields(content);
        assertTrue(fields.isEmpty());
    }

    @Test
    void findInjectFields_handlesMultipleValid() {
        String content = """
                @Inject private ServiceA serviceA;
                @Inject private ServiceB serviceB;
                """;
        List<String> fields = ReflectConfigGenerator.findInjectFields(content);
        assertLinesMatch(List.of("serviceA", "serviceB"), fields);
    }

    @Test
    void isValidFieldName_acceptsValidNames() {
        assertTrue(ReflectConfigGenerator.isValidFieldName("homeService"));
        assertTrue(ReflectConfigGenerator.isValidFieldName("userRepo"));
        assertTrue(ReflectConfigGenerator.isValidFieldName("dataSource"));
    }

    @Test
    void isValidFieldName_rejectsInvalid() {
        assertFalse(ReflectConfigGenerator.isValidFieldName("e"));
        assertFalse(ReflectConfigGenerator.isValidFieldName("it"));
        assertFalse(ReflectConfigGenerator.isValidFieldName("If"));
        assertFalse(ReflectConfigGenerator.isValidFieldName("E"));
    }

    @Test
    void analyzeClass_fullControllerWithInject(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("TestController.java");
        String content = """
                package com.example;

                import io.jawisp.core.annotation.Controller;
                import io.jawisp.core.inject.Inject;

                @Controller
                public class TestController {
                    @Inject
                    private TestService testService;
                }
                """;
        Files.writeString(file, content);

        ReflectConfigGenerator.ClassConfig cfg = ReflectConfigGenerator.analyzeClass(file, content);

        assertNotNull(cfg);
        assertEquals("com.example.TestController", cfg.name);
        assertTrue(cfg.needsReflection);
        assertEquals(List.of("testService"), cfg.injectFields);
    }

    @Test
    void analyzeClass_plainClassIsSkipped() {
        String content = "package com.example;\npublic class PlainClass {}";
        Path file = Path.of("PlainClass.java");

        ReflectConfigGenerator.ClassConfig cfg = ReflectConfigGenerator.analyzeClass(file, content);

        assertNull(cfg);
    }

    @Test
    void analyzeClass_serviceWithoutInject() {
        String content = """
                package com.example;
                import io.jawisp.core.annotation.Service;

                @Service
                public class TestService {}
                """;
        Path file = Path.of("TestService.java");

        ReflectConfigGenerator.ClassConfig cfg = ReflectConfigGenerator.analyzeClass(file, content);

        assertNotNull(cfg);
        assertEquals("com.example.TestService", cfg.name);
        assertTrue(cfg.needsReflection);
        assertTrue(cfg.injectFields.isEmpty());
    }

    @Test
    void classConfig_hasCorrectData() {
        ReflectConfigGenerator.ClassConfig cfg = new ReflectConfigGenerator.ClassConfig("com.example.TestController");
        cfg.needsReflection = true;
        cfg.injectFields.add("homeService");

        assertFalse(cfg.isEmpty());
        assertEquals("com.example.TestController", cfg.name);
        assertTrue(cfg.needsReflection);
        assertEquals(1, cfg.injectFields.size());
        assertEquals("homeService", cfg.injectFields.get(0));
    }

    @Test
    void emptyClassConfig_isSkipped() {
        ReflectConfigGenerator.ClassConfig cfg = new ReflectConfigGenerator.ClassConfig("com.example.PlainClass");
        assertTrue(cfg.isEmpty());
    }

}
