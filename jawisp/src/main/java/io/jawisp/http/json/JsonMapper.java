package io.jawisp.http.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * The JsonMapper interface provides a contract for converting objects to and from JSON.
 * It includes methods for converting objects to JSON strings or streams, and vice versa.
 * Implementations of this interface should provide the actual logic for JSON serialization and deserialization.
 *
 * @author reftch
 * @version 1.0.3
 */
public interface JsonMapper {

    /**
     * Gets the JsonMapper instance.
     * By default, returns the current instance.
     *
     * @return the JsonMapper instance
     */
    default JsonMapper jsonMapper() {
        return this;
    }

    /**
     * Converts an object to a JSON string.
     * Throws UnsupportedOperationException if not implemented by a subclass.
     *
     * @param obj the object to convert
     * @param type the type of the object
     * @return the JSON string representation of the object
     * @throws UnsupportedOperationException if the method is not implemented
     */
    default String toJsonString(Object obj, Type type) {
        throw new UnsupportedOperationException("JsonMapper#toJsonString not implemented");
    }

    /**
     * Converts an object to a JSON stream.
     * Throws UnsupportedOperationException if not implemented by a subclass.
     *
     * @param obj the object to convert
     * @param type the type of the object
     * @return an InputStream containing the JSON data
     * @throws UnsupportedOperationException if the method is not implemented
     */
    default InputStream toJsonStream(Object obj, Type type) {
        throw new UnsupportedOperationException("JsonMapper#toJsonString not implemented");
    }

    /**
     * Writes an object to an OutputStream in JSON format.
     * Throws UnsupportedOperationException if not implemented by a subclass.
     *
     * @param stream the object to write
     * @param outputStream the OutputStream to write to
     * @throws UnsupportedOperationException if the method is not implemented
     */
    default void writeToOutputStream(Object stream, OutputStream outputStream) {
        throw new UnsupportedOperationException("JsonMapper#writeToOutputStream not implemented");
    }

    /**
     * Converts a JSON string to an object of the specified type.
     * Throws UnsupportedOperationException if not implemented by a subclass.
     *
     * @param json the JSON string
     * @param targetType the type of the target object
     * @param <T> the type of the target object
     * @return the deserialized object
     * @throws UnsupportedOperationException if the method is not implemented
     */
    default <T> T fromJsonString(String json, Type targetType) {
        throw new UnsupportedOperationException("JsonMapper#fromJsonString not implemented");
    }

    /**
     * Converts a JSON stream to an object of the specified type.
     * Throws UnsupportedOperationException if not implemented by a subclass.
     *
     * @param json the JSON stream
     * @param targetType the type of the target object
     * @param <T> the type of the target object
     * @return the deserialized object
     * @throws UnsupportedOperationException if the method is not implemented
     */
    default <T> T fromJsonStream(InputStream json, Type targetType) {
        throw new UnsupportedOperationException("JsonMapper#fromJsonStream not implemented");
    }
}