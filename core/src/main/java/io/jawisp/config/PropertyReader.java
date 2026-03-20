package io.jawisp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Singleton class to read properties from a configuration file like {@code application.properties}.
 * The properties can be accessed using a dot-separated path.
 * 
 * @author Taras Chornyi
 * @since 1.0.19
 */
public final class PropertyReader {

    // Singleton instance - loads application.properties by default
    private static volatile PropertyReader INSTANCE;

    private final Node root;
    private final String path;

    private PropertyReader(Node root, String path) {
        this.root = root;
        this.path = path;
    }

    /**
     * Get singleton instance for custom properties file.
     * Thread-safe lazy initialization.
     *
     * @param resourceName Name of the resource file to load properties from
     * @return Singleton instance of PropertyReader
     */
    public static PropertyReader getInstance(String resourceName) {
        PropertyReader instance = INSTANCE;
        if (instance == null) {
            synchronized (PropertyReader.class) {
                instance = INSTANCE;
                if (instance == null) {
                    INSTANCE = instance = create(resourceName);
                }
            }
        }
        return instance;
    }

    private static PropertyReader create(String resourceName) {
        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = PropertyReader.class.getClassLoader();
        }

        try (InputStream is = cl.getResourceAsStream(resourceName)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + resourceName, e);
        }

        Node root = buildTree(props);
        return new PropertyReader(root, "");
    }

    /**
     * Factory method to create a PropertyReader from an existing Node.
     *
     * @param root Root node of the property tree
     * @return A PropertyReader instance
     */
    public static PropertyReader of(Node root) {
        return new PropertyReader(root, "");
    }

    private PropertyReader(Node node, String path, boolean unused) {
        this.root = node;
        this.path = path;
    }

    /**
     * Navigate by dot-separated path OR single key part.
     * Supports both: {@code config.get("web.page-size")} and
     * {@code config.get("web").get("page-size")}
     *
     * @param keyPath Dot-separated path or a single key part
     * @return PropertyReader instance for the specified key path
     */
    public PropertyReader get(String keyPath) {
        Objects.requireNonNull(keyPath, "keyPath");

        String[] parts = keyPath.split("\\.");
        PropertyReader current = this;

        for (String part : parts) {
            Node child = current.root.children.get(part);
            if (child == null) {
                return new PropertyReader(Node.empty(), joinPath(current.path, part), true);
            }
            current = new PropertyReader(child, joinPath(current.path, part), true);
        }

        return current;
    }

    /**
     * Read the current node's value as String.
     *
     * @return A PropertyValue instance for the string value
     */
    public PropertyValue<String> asString() {
        return new PropertyValue<>(root.value, s -> s);
    }

    /**
     * Read the current node's value as int.
     *
     * @return A PropertyValue instance for the integer value
     */
    public PropertyValue<Integer> asInt() {
        return new PropertyValue<>(root.value, Integer::parseInt);
    }

    /**
     * Read the current node's value as boolean.
     * Accepts "true"/"false" (case-insensitive), "1"/"0", "yes"/"no".
     *
     * @return A PropertyValue instance for the boolean value
     */
    public PropertyValue<Boolean> asBoolean() {
        return new PropertyValue<>(root.value, PropertyReader::parseBoolean);
    }

    /**
     * Read the current node's value as long.
     *
     * @return A PropertyValue instance for the long value
     */
    public PropertyValue<Long> asLong() {
        return new PropertyValue<>(root.value, Long::parseLong);
    }

    /**
     * Returns true if this node has a value or at least one child.
     *
     * @return True if the node exists, false otherwise
     */
    public boolean exists() {
        return root.value != null || !root.children.isEmpty();
    }

    /**
     * Returns all direct children as map name -> PropertyReader.
     *
     * @return A map of child nodes with their names and corresponding PropertyReader instances
     */
    public Map<String, PropertyReader> children() {
        Map<String, PropertyReader> result = new LinkedHashMap<>();
        for (Map.Entry<String, Node> e : root.children.entrySet()) {
            result.put(e.getKey(), new PropertyReader(e.getValue(),
                    joinPath(path, e.getKey()), true));
        }
        return result;
    }

    @Override
    public String toString() {
        return "PropertyReader[" + (path.isEmpty() ? "<root>" : path) + "]";
    }

    // ----- Internal tree node -----

    /**
     * Immutable internal class representing a node in the property tree.
     */
    static final class Node {
        final Map<String, Node> children;
        final String value;

        Node(Map<String, Node> children, String value) {
            this.children = children;
            this.value = value;
        }

        /**
         * Factory method to create an empty node with no children and null value.
         *
         * @return An empty node
         */
        static Node empty() {
            return new Node(Collections.emptyMap(), null);
        }
    }

    // ----- Tree builder -----

    /**
     * Build the property tree from a Properties object.
     *
     * @param props Properties object containing configuration key-value pairs
     * @return Root node of the built property tree
     */
    private static Node buildTree(Properties props) {
        Node root = new Node(new LinkedHashMap<>(), null);

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            String[] parts = key.split("\\.");

            Node current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                Node child = current.children.get(part);
                if (child == null) {
                    child = new Node(new LinkedHashMap<>(), null);
                    current.children.put(part, child);
                }
                current = child;
            }

            // Last part gets the value
            String lastPart = parts[parts.length - 1];
            current.children.put(lastPart, new Node(new LinkedHashMap<>(), value));
        }
        return root;
    }

    /**
     * Helper method to join a base path with an additional part.
     *
     * @param base Base path
     * @param part Additional part to append
     * @return The joined path
     */
    private static String joinPath(String base, String part) {
        if (base == null || base.isEmpty())
            return part;
        return base + "." + part;
    }

    /**
     * Parse a string to a boolean value.
     *
     * @param s String to parse
     * @return Boolean representation of the string
     * @throws IllegalArgumentException if the string does not represent a valid boolean value
     */
    private static Boolean parseBoolean(String s) {
        String v = s.toLowerCase(Locale.ROOT);
        return switch (v) {
            case "true", "1", "yes", "y" -> true;
            case "false", "0", "no", "n" -> false;
            default -> throw new IllegalArgumentException("Not a boolean: " + s);
        };
    }

    // ----- Value wrapper with orElse -----

    /**
     * Class to wrap property values and provide methods like `orElse`, `orElseGet`, and `orElseThrow`.
     *
     * @param <T> Type of the wrapped value
     */
    public static final class PropertyValue<T> {
        private final String raw;
        private final java.util.function.Function<String, T> mapper;

        /**
         * Constructor to create a PropertyValue instance.
         *
         * @param raw Raw string representation of the property value
         * @param mapper Function to map the raw string to the desired type
         */
        PropertyValue(String raw,
                java.util.function.Function<String, T> mapper) {
            this.raw = raw;
            this.mapper = mapper;
        }

        /**
         * Convert the raw value to an Optional.
         *
         * @return An Optional containing the mapped value or empty if raw is null or mapping fails
         */
        public Optional<T> asOptional() {
            if (raw == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(mapper.apply(raw));
            } catch (RuntimeException ex) {
                return Optional.empty();
            }
        }

        /**
         * Get the value or a default value if not present.
         *
         * @param defaultValue Default value to return if raw is null or mapping fails
         * @return The mapped value or default value
         */
        public T orElse(T defaultValue) {
            return asOptional().orElse(defaultValue);
        }

        /**
         * Get the value or a value provided by a supplier if not present.
         *
         * @param supplier Supplier to provide the value if raw is null or mapping fails
         * @return The mapped value or the value from the supplier
         */
        public T orElseGet(java.util.function.Supplier<T> supplier) {
            return asOptional().orElseGet(supplier);
        }

        /**
         * Get the value or throw an exception if not present.
         *
         * @throws NoSuchElementException if raw is null or mapping fails
         * @return The mapped value
         */
        public T orElseThrow() {
            return asOptional()
                    .orElseThrow(() -> new NoSuchElementException("Missing config value"));
        }

        /**
         * Get the raw string representation of the property value.
         *
         * @return Raw string value
         */
        public String raw() {
            return raw;
        }
    }
}