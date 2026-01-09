package io.jawisp.core.inject;

import java.util.List;

/**
 * A record that represents reflection metadata for a class.
 * This record contains information about the class's constructors,
 * methods, and fields that can be used for reflection-based operations.
 */
public record ReflectionEntry(
        String name,
        boolean allDeclaredConstructors,
        boolean allPublicMethods,
        List<Field> fields) {
    
    /**
     * A record that represents a field in a class.
     * This record contains the name of the field.
     */
    public record Field(String name) {
    }
}