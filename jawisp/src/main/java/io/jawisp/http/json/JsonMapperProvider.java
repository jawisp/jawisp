package io.jawisp.http.json;

import java.util.ServiceLoader;

/**
 * The JsonMapperProvider interface provides a contract for creating JsonMapper instances.
 * It includes a method to create a JsonMapper and a static method to load the first available JsonMapperProvider
 * using the ServiceLoader mechanism.
 *
 * @author reftch
 * @version 1.0.4
 */
public interface JsonMapperProvider {

    /**
     * Creates a new JsonMapper instance.
     *
     * @return a new JsonMapper instance
     */
    JsonMapper createJsonMapper();

    /**
     * Loads the first available JsonMapperProvider using the ServiceLoader mechanism
     * and creates a JsonMapper instance from it.
     * Throws an IllegalStateException if no JsonMapperProvider is found.
     *
     * @return a JsonMapper instance
     * @throws IllegalStateException if no JsonMapperProvider is found
     */
    static JsonMapper load() {
        return ServiceLoader.load(JsonMapperProvider.class)
                .findFirst()
                .map(JsonMapperProvider::createJsonMapper)
                .orElseThrow(() -> new IllegalStateException(
                        "No JsonMapperProvider found. Add Jackson: com.fasterxml.jackson.core:jackson-databind"));
    }

}