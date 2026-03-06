package io.jawisp.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The JacksonJsonMapper class is an implementation of the JsonMapper interface.
 * It uses the Jackson library to serialize and deserialize objects to and from JSON.
 *
 * @author reftch
 * @since 1.0.0
 */
public class JacksonJsonMapper implements JsonMapper {
    private final ObjectMapper mapper;

    /**
     * Constructs a new JacksonJsonMapper instance with a default ObjectMapper.
     */
    public JacksonJsonMapper() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Constructs a new JacksonJsonMapper instance with the provided ObjectMapper.
     *
     * @param mapper the ObjectMapper to use for JSON serialization and deserialization
     */
    public JacksonJsonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param obj the object to convert
     * @param type the type of the object
     * @return the JSON string representation of the object
     * @throws RuntimeException if JSON serialization fails
     */
    @Override
    public String toJsonString(Object obj, Type type) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Converts an object to a JSON stream.
     *
     * @param obj the object to convert
     * @param type the type of the object
     * @return an InputStream containing the JSON data
     * @throws RuntimeException if JSON stream creation fails
     */
    @Override
    public InputStream toJsonStream(Object obj, Type type) {
        try {
            return new ByteArrayInputStream(mapper.writeValueAsBytes(obj));
        } catch (Exception e) {
            throw new RuntimeException("JSON stream failed", e);
        }
    }

    /**
     * Writes an object to an OutputStream in JSON format.
     *
     * @param obj the object to write
     * @param outputStream the OutputStream to write to
     * @throws RuntimeException if JSON writing fails
     */
    @Override
    public void writeToOutputStream(Object obj, OutputStream outputStream) {
        try {
            mapper.writeValue(outputStream, obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON write failed", e);
        }
    }

    /**
     * Converts a JSON string to an object of the specified type.
     *
     * @param json the JSON string
     * @param targetType the type of the target object
     * @param <T> the type of the target object
     * @return the deserialized object
     * @throws RuntimeException if JSON deserialization fails
     */
    @Override
    public <T> T fromJsonString(String json, Type targetType) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return mapper.readValue(json, mapper.constructType(targetType));
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Converts a JSON stream to an object of the specified type.
     *
     * @param json the JSON stream
     * @param targetType the type of the target object
     * @param <T> the type of the target object
     * @return the deserialized object
     * @throws RuntimeException if JSON stream deserialization fails
     */
    @Override
    public <T> T fromJsonStream(InputStream json, Type targetType) {
        try {
            return mapper.readValue(json, mapper.constructType(targetType));
        } catch (Exception e) {
            throw new RuntimeException("JSON stream deserialization failed", e);
        }
    }
}