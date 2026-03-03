package io.jawisp.http.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface JsonMapper {

    default JsonMapper jsonMapper() {
        return this;
    }

    default String toJsonString(Object obj, Type type) {
        throw new UnsupportedOperationException("JsonMapper#toJsonString not implemented");
    }

    default InputStream toJsonStream(Object obj, Type type) {
        throw new UnsupportedOperationException("JsonMapper#toJsonString not implemented");
    }

    default void writeToOutputStream(Object stream, OutputStream outputStream) {
        throw new UnsupportedOperationException("JsonMapper#writeToOutputStream not implemented");
    }

    default <T> T fromJsonString(String json, Type targetType) {
        throw new UnsupportedOperationException("JsonMapper#fromJsonString not implemented");
    }

    default <T> T fromJsonStream(InputStream json, Type targetType) {
        throw new UnsupportedOperationException("JsonMapper#fromJsonStream not implemented");
    }

}
