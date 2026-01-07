package io.jawisp.core.inject;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceConfigGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ResourceConfigGenerator.class);

    public static void run() {
        try {
            Path resourcesDir = Paths.get("src", "main", "resources");
            List<Map<String, String>> includes = new ArrayList<>();

            // Always include META-INF/native-image/reflect-config.json
            Map<String, String> mandatoryInclude = new LinkedHashMap<>();
            mandatoryInclude.put("pattern", "META-INF/native-image/reflect-config.json");
            includes.add(mandatoryInclude);

            Files.walkFileTree(resourcesDir, new IncludeVisitor(includes, resourcesDir));

            Map<String, Object> resources = new LinkedHashMap<>();
            resources.put("includes", includes);
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("resources", resources);

            var json = toJson(config);
            var outputPath = Path.of("build", "resources", "main", "META-INF", "native-image", "resource-config.json");
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, json.getBytes());

            if (logger.isDebugEnabled()) {
                logger.debug("Generated resource-config.json");
            }
        } catch (IOException e) {
            logger.error("GEnerates resource config file failed: {}", e.getMessage());
        }
    }

    private static String toJson(Object obj) {
        return switch (obj) {
            case Map<?, ?> map -> mapToJson(map);
            case List<?> list -> listToJson(list);
            default -> "\"" + escapeJson(obj.toString()) + "\"";
        };
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        String prefix = "";
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(prefix).append("\"").append(escapeJson(entry.getKey().toString())).append("\":");
            sb.append(toJson(entry.getValue()));
            prefix = ",";
        }
        return sb.append("}").toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        String prefix = "";
        for (Object item : list) {
            sb.append(prefix).append(toJson(item));
            prefix = ",";
        }
        return sb.append("]").toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static class IncludeVisitor extends SimpleFileVisitor<Path> {
        private final List<Map<String, String>> includes;
        private final Path baseDir;

        IncludeVisitor(List<Map<String, String>> includes, Path baseDir) {
            this.includes = includes;
            this.baseDir = baseDir;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            var relative = baseDir.relativize(file);
            var pattern = relative.toString().replace('\\', '/');

            // Skip the mandatory file to avoid duplicates
            if (pattern.equals("META-INF/native-image/resource-config.json")) {
                return FileVisitResult.CONTINUE;
            }

            Map<String, String> include = new LinkedHashMap<>();
            include.put("pattern", pattern);
            includes.add(include);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
