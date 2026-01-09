package io.jawisp.http.utilities;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.lang.reflect.RecordComponent;

public class JsonSerializer {

    public static String writeValueAsString(Object obj) {
        if (obj == null) {
            return "null";
        }

        return switch (obj.getClass().getSimpleName()) {
            case "String" -> "\"" + escape(obj.toString()) + "\"";
            case "Integer", "Long", "Double", "Float", "BigDecimal" -> obj.toString();
            case "Boolean" -> obj.toString();
            default -> {
                if (obj instanceof Map)
                    yield mapToJson((Map<?, ?>) obj);
                if (obj instanceof Collection || obj.getClass().isArray())
                    yield collectionToJson(obj);
                yield pojoToJson(obj);
            }
        };
    }

    private static String pojoToJson(Object obj) {
        Class<?> clazz = obj.getClass();
        
        // Handle records explicitly - GraalVM needs reflection config for this path
        if (clazz.isRecord()) {
            return recordToJson(obj);
        }
        
        // BeanInfo for regular POJOs (GraalVM safe)
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            StringBuilder sb = new StringBuilder("{");
            
            for (int i = 0; i < properties.length; i++) {
                PropertyDescriptor pd = properties[i];
                if (pd.getName().equals("class")) continue;
                
                Method reader = pd.getReadMethod();
                if (reader != null) {
                    try {
                        Object value = reader.invoke(obj);
                        sb.append("\"").append(pd.getName()).append("\":")
                          .append(writeValueAsString(value));
                        if (i < properties.length - 1) sb.append(",");
                    } catch (Exception e) {
                        // Skip unreadable properties
                    }
                }
            }
            return sb.append("}").toString();
        } catch (Exception e) {
            // Fallback for unreadable objects
            return "\"" + escape(obj.toString()) + "\"";
        }
    }
    
    private static String recordToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Class<?> clazz = obj.getClass();
        RecordComponent[] components = clazz.getRecordComponents();
        
        for (int i = 0; i < components.length; i++) {
            RecordComponent rc = components[i];
            String name = rc.getName();
            try {
                Method accessor = rc.getAccessor();
                Object value = accessor.invoke(obj);
                sb.append("\"").append(name).append("\":")
                  .append(writeValueAsString(value));
                if (i < components.length - 1) sb.append(",");
            } catch (Exception e) {
                sb.append("\"").append(name).append("\":null");
            }
        }
        return sb.append("}").toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        map.forEach((k, v) -> {
            sb.append("\"").append(escape(k.toString())).append("\":")
              .append(writeValueAsString(v)).append(",");
        });
        if (!map.isEmpty())
            sb.setLength(sb.length() - 1);
        return sb.append("}").toString();
    }

    private static String collectionToJson(Object obj) {
        StringBuilder sb = new StringBuilder("[");
        Iterable<?> iterable = obj instanceof Iterable<?> ? (Iterable<?>) obj
                : obj instanceof Object[] arr ? Arrays.asList((Object[]) arr) : null;

        if (iterable != null) {
            for (Object item : iterable) {
                sb.append(writeValueAsString(item)).append(",");
            }
            if (!iterable.iterator().hasNext()) {
                sb.setLength(sb.length() - 1);
            }
        }
        return sb.append("]").toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
