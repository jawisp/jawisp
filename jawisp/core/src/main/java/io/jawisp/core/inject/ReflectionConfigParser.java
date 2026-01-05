package io.jawisp.core.inject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.core.annotation.Application;
// import io.jawisp.core.graal.ReflectConfigBuilder;

public class ReflectionConfigParser {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionConfigParser.class);

    public List<ReflectionEntry> getReflectionEntries() {
        var entries = new ArrayList<ReflectionEntry>();

        // try {
        //     String className = findMainClassName();
        //     System.out.println("XAX " + className);
        //     // List<String> appClasses = List.of("io.jawisp.example");
        //     Class<?> appClass = Class.forName(className);
        //     if (appClass.isAnnotationPresent(Application.class)) {
        //         System.out.println("XAXAXAXAX !!!!");
        //     }
        // } catch (Exception e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        Class<?> mainClass = ClassFinder.findClassByAnnotation(Application.class);
        // if (mainClass == null) {
        //     throw new RuntimeErrorException(new Error("Cannot find main class of the application"));
        // }

        // Path nativeImageDir = Path.of("build", "resources", "main", "META-INF", "native-image", "reflect-config.json");
        // new ReflectConfigBuilder().buildFrom(createMainClassInstance(mainClass), nativeImageDir);

        String json = readJsonFile(mainClass);
        if (json == null || !isValidJsonArray(json)) {
            logger.info("Invalid JSON format or empty file");
            throw new RuntimeErrorException(new Error("Invalid JSON format or empty file"));
        }

        String content = removeOuterBrackets(json);
        if (content.isEmpty()) {
            return entries;
        }

        return parseEntries(content);
    }

    public static String findMainClassName() {
        Exception e = new Exception();
        StackTraceElement[] stack = e.getStackTrace();

        // Skip current frame (this method) - start from index 1
        for (int i = 1; i < stack.length; i++) {
            StackTraceElement el = stack[i];

            // Find "main" method invocation
            if ("main".equals(el.getMethodName())) {
                String className = el.getClassName();
                System.out.println("Found main() caller: " + el.getClassName());

                // In native, often the direct main class shows as first "main" frame
                // with no line number or simple name pattern
                // if (i == 1 || className.endsWith("Application") || className.endsWith("Main")) {
                    return className; // ← Return String instead of Class
                // }
            }
        }
        return null;
    }

    public Object createMainClassInstance(Class<?> mainClass) {
        if (mainClass == null) {
            throw new RuntimeErrorException(new Error("Cannot find main class of the application"));
        }
        try {
            return mainClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeErrorException(new Error("Cannot create instance of main class: " + e.getMessage()));
        }
    }

    private String readJsonFile(Class<?> mainClass) {
        try (InputStream is = mainClass.getResourceAsStream("/META-INF/native-image/reflect-config.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            return jsonContent.toString().trim();
        } catch (IOException e) {
            logger.info("No classes found {}", e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error reading reflect-config.json: {}", e.getMessage());
            return null;
        }
    }

    boolean isValidJsonArray(String json) {
        return !json.isEmpty() && json.startsWith("[");
    }

    String removeOuterBrackets(String json) {
        return json.substring(1, json.length() - 1).trim();
    }

    List<ReflectionEntry> parseEntries(String content) {
        var entries = new ArrayList<ReflectionEntry>();
        int objStart = 0;

        while (objStart < content.length()) {
            int nextObjStart = content.indexOf('{', objStart);
            if (nextObjStart == -1)
                break;

            int braceCount = 1;
            int i = nextObjStart + 1;

            while (i < content.length() && braceCount > 0) {
                if (content.charAt(i) == '{') {
                    braceCount++;
                } else if (content.charAt(i) == '}') {
                    braceCount--;
                }
                i++;
            }

            if (braceCount == 0) {
                String obj = content.substring(nextObjStart, i);
                try {
                    ReflectionEntry entry = parseEntry(obj);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (Exception e) {
                    logger.info("Error parsing object: {}", e.getMessage());
                }

                objStart = i;
            } else {
                objStart = nextObjStart + 1;
            }
        }

        return entries;
    }

    ReflectionEntry parseEntry(String obj) {
        String name = parseValue(obj, "\"name\"");
        if (name == null)
            return null;

        boolean allDeclaredConstructors = parseBoolean(obj, "\"allDeclaredConstructors\"");
        boolean allPublicConstructors = parseBoolean(obj, "\"allPublicConstructors\"");
        boolean allDeclaredMethods = parseBoolean(obj, "\"allDeclaredMethods\"");
        boolean allPublicMethods = parseBoolean(obj, "\"allPublicMethods\"");
        boolean allPrivateMethods = parseBoolean(obj, "\"allPrivateMethods\"");

        List<ReflectionEntry.Field> fields = new ArrayList<>();
        int fieldsStart = obj.indexOf("\"fields\"");
        if (fieldsStart != -1) {
            try {
                int fieldsEnd = obj.indexOf("]", fieldsStart);
                if (fieldsEnd != -1) {
                    String fieldsJson = obj.substring(fieldsStart, fieldsEnd + 1);
                    fields = parseFields(fieldsJson);
                }
            } catch (Exception e) {
                logger.error("Error parsing fields: {}", e.getMessage());
            }
        }

        return new ReflectionEntry(name, allDeclaredConstructors, allPublicConstructors,
                allDeclaredMethods, allPublicMethods, allPrivateMethods, fields);
    }

    // Helper methods
    String parseValue(String json, String key) {
        int startIndex = json.indexOf(key);
        if (startIndex == -1)
            return null;

        int valueStart = json.indexOf('"', startIndex + key.length());
        if (valueStart == -1)
            return null;

        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd == -1)
            return null;

        return json.substring(valueStart + 1, valueEnd).trim();
    }

    boolean parseBoolean(String json, String key) {
        String value = parseValue(json, key);
        return "true".equalsIgnoreCase(value);
    }

    List<ReflectionEntry.Field> parseFields(String fieldsJson) {
        List<ReflectionEntry.Field> fields = new ArrayList<>();
        int startIndex = 0;
        while (startIndex < fieldsJson.length()) {
            int fieldStart = fieldsJson.indexOf('{', startIndex);
            if (fieldStart == -1)
                break;

            int fieldEnd = fieldsJson.indexOf('}', fieldStart);
            if (fieldEnd == -1)
                break;

            String fieldObj = fieldsJson.substring(fieldStart, fieldEnd + 1);
            String name = parseValue(fieldObj, "\"name\"");
            fields.add(new ReflectionEntry.Field(name));

            startIndex = fieldEnd + 1;
        }
        return fields;
    }

}
