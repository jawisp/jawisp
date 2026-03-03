package io.jawisp.http.json;

import java.util.ServiceLoader;

public interface JsonMapperProvider {
    JsonMapper createJsonMapper();

    static JsonMapper load() {
        return ServiceLoader.load(JsonMapperProvider.class)
                .findFirst()
                .map(JsonMapperProvider::createJsonMapper)
                .orElseThrow(() -> new IllegalStateException(
                        "No JsonMapperProvider found. Add Jackson: com.fasterxml.jackson.core:jackson-databind"));
    }

}
