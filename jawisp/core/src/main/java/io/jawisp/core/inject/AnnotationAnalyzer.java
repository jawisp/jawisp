package io.jawisp.core.inject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationAnalyzer.class);

    private static final Set<String> TARGET_ANNOTATIONS = Set.of(
            "Application", "Controller", "Service");

    public static void main() throws IOException {
        Path sourceDir = Paths.get("src/main/java");
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
            logger.debug("✅ Generated reflect-config.json with {} classes.", classes.size());
        }
    }

    private static ClassConfig analyzeClass(Path file, String content) {
        String packageName = extractPackageName(content);
        String className = extractClassName(content, packageName);

        if (className == null)
            return null;

        ClassConfig cfg = new ClassConfig(className);

        if (hasAnyAnnotation(content, TARGET_ANNOTATIONS)) {
            cfg.needsReflection = true;
        }

        List<String> injectFields = findInjectFields(content);
        cfg.injectFields.addAll(injectFields);

        return cfg.isEmpty() ? null : cfg;
    }

    private static String extractPackageName(String content) {
        Matcher m = Pattern.compile("^\\s*package\\s+([\\w\\.]+);", Pattern.MULTILINE).matcher(content);
        return m.find() ? m.group(1) : "";
    }

    private static String extractClassName(String content, String packageName) {
        Matcher m = Pattern.compile("public\\s+class\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(content);
        if (m.find()) {
            String simpleName = m.group(1);
            return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
        }
        return null;
    }

    private static boolean hasAnyAnnotation(String content, Set<String> annotations) {
        for (String annot : annotations) {
            if (content.contains("@" + annot)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> findInjectFields(String content) {
        List<String> fields = new ArrayList<>();

        // 🎯 PRECISE PATTERN: Only match @Inject fields (not method params/lambdas)
        Pattern fieldPattern = Pattern.compile(
                "(@Inject\\s*\\n?\\s*)(private\\s+[^@\\n]+?\\s+)([a-z][a-zA-Z0-9]*?)\\s*(?:;|$)",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

        Matcher m = fieldPattern.matcher(content);
        while (m.find()) {
            String fieldName = m.group(3);
            if (fieldName != null && fieldName.length() > 1 && isValidFieldName(fieldName)) {
                if (!fields.contains(fieldName)) {
                    fields.add(fieldName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("✅ Found @Inject field: {}", fieldName);
                    }
                }
            }
        }
        return fields;
    }

    private static boolean isValidFieldName(String name) {
        // Filter out short names (1 letter), keywords, and common lambda params
        return name.length() > 1 &&
                !name.matches("^(e|ex|it|arg|args|lambda|if|while|for|class)$") &&
                Character.isLowerCase(name.codePointAt(0));
    }

    private static void generateReflectConfig(List<ClassConfig> classes) throws IOException {
        // Path outputPath = Path.of("src", "main", "resources", "META-INF", "native-image", "reflect-config.json");
        Path outputPath = Path.of("build", "resources", "main", "META-INF", "native-image", "reflect-config.json");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(outputPath), StandardCharsets.UTF_8))) {

            writer.println("[");
            for (int i = 0; i < classes.size(); i++) {
                ClassConfig cfg = classes.get(i);
                writer.printf("  {\n");
                writer.printf("    \"name\": \"%s\",\n", cfg.name);
                writer.printf("    \"allDeclaredConstructors\": true,\n");
                writer.printf("    \"allPublicConstructors\": true,\n");
                writer.printf("    \"allDeclaredMethods\": true,\n");
                writer.printf("    \"allPublicMethods\": true,\n");
                writer.printf("    \"allPrivateMethods\": true");

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
