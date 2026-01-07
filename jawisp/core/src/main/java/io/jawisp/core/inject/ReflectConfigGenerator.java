package io.jawisp.core.inject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.core.annotation.Application;
import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Service;

public class ReflectConfigGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReflectConfigGenerator.class);

    static final Set<String> TARGET_ANNOTATIONS = Set.of(
            Application.class.getSimpleName(),
            Controller.class.getSimpleName(),
            Service.class.getSimpleName());

    public static void main() throws IOException {
        ResourceConfigGenerator.run();

        var sourceDir = Paths.get("src/main/java");
        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("Not a directory: " + sourceDir);
        }

        List<ClassConfig> classes = new ArrayList<>();
        Set<String> processedClasses = new HashSet<>();

        Files.walk(sourceDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        ClassConfig cfg = analyzeClass(file, content);
                        if (cfg != null && processedClasses.add(cfg.name)) {
                            classes.add(cfg);
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading " + file + ": " + e.getMessage());
                    }
                });

        generateReflectConfig(classes);

        if (logger.isDebugEnabled()) {
            logger.debug("Generated reflect-config.json with {} classes.", classes.size());
        }
    }

    static ClassConfig analyzeClass(Path file, String content) {
        var packageName = extractPackageName(content);
        var className = extractClassName(content, packageName);

        if (className == null)
            return null;

        var cfg = new ClassConfig(className);
        if (hasAnyAnnotation(content, TARGET_ANNOTATIONS)) {
            cfg.needsReflection = true;
        }

        var injectFields = findInjectFields(content);
        cfg.injectFields.addAll(injectFields);

        return cfg.isEmpty() ? null : cfg;
    }

    static String extractPackageName(String content) {
        var m = Pattern.compile("^\\s*package\\s+([\\w\\.]+);", Pattern.MULTILINE).matcher(content);
        return m.find() ? m.group(1) : "";
    }

    static String extractClassName(String content, String packageName) {
        var m = Pattern.compile("public\\s+class\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(content);
        if (m.find()) {
            var simpleName = m.group(1);
            return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
        }
        return null;
    }

    static boolean hasAnyAnnotation(String content, Set<String> annotations) {
        for (var annot : annotations) {
            if (content.contains("@" + annot)) {
                return true;
            }
        }
        return false;
    }

    static List<String> findInjectFields(String content) {
        List<String> fields = new ArrayList<>();

        // Only match @Inject fields (not method params/lambdas)
        var fieldPattern = Pattern.compile(
                "(@Inject\\s*\\n?\\s*)(private\\s+[^@\\n]+?\\s+)([a-z][a-zA-Z0-9]*?)\\s*(?:;|$)",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

        var m = fieldPattern.matcher(content);
        while (m.find()) {
            var fieldName = m.group(3);
            if (fieldName != null && fieldName.length() > 1 && isValidFieldName(fieldName)) {
                if (!fields.contains(fieldName)) {
                    fields.add(fieldName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found @Inject field: {}", fieldName);
                    }
                }
            }
        }
        return fields;
    }

    static boolean isValidFieldName(String name) {
        // Filter out short names (1 letter), keywords, and common lambda params
        return name.length() > 1 &&
                !name.matches("^(e|ex|it|arg|args|lambda|if|while|for|class)$") &&
                Character.isLowerCase(name.codePointAt(0));
    }

    static void generateReflectConfig(List<ClassConfig> classes) throws IOException {
        var outputPath = Path.of("build", "resources", "main", "META-INF", "native-image", "reflect-config.json");
        try (var writer = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(outputPath), StandardCharsets.UTF_8))) {

            writer.println("[");
            for (int i = 0; i < classes.size(); i++) {
                ClassConfig cfg = classes.get(i);
                writer.printf("  {\n");
                writer.printf("    \"name\": \"%s\",\n", cfg.name);
                writer.printf("    \"allDeclaredConstructors\": true,\n");
                writer.printf("    \"allPublicConstructors\": true,\n");
                writer.printf("    \"allDeclaredMethods\": true,\n");
                writer.printf("    \"allPublicMethods\": true");

                if (!cfg.injectFields.isEmpty()) {
                    writer.printf(",\n    \"fields\": [");
                    for (int j = 0; j < cfg.injectFields.size(); j++) {
                        writer.printf("\n      {\n        \"name\": \"%s\"%s\n      }%s",
                                cfg.injectFields.get(j),
                                j < cfg.injectFields.size() - 1 ? "," : "",
                                j < cfg.injectFields.size() - 1 ? "," : "");
                    }
                    writer.printf("\n    ]");
                }
                writer.printf("\n  }%s\n", i < classes.size() - 1 ? "," : "");
            }
            writer.println("]");
        }
    }

    static class ClassConfig {
        final String name;
        final List<String> injectFields = new ArrayList<>();
        boolean needsReflection = false;

        ClassConfig(String name) {
            this.name = name;
        }

        boolean isEmpty() {
            return injectFields.isEmpty() && !needsReflection;
        }
    }
}
