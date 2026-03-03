package io.jawisp.http.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJsonMapper implements JsonMapper {
    private final ObjectMapper mapper;
    
    public JacksonJsonMapper() {
        this.mapper = new ObjectMapper();
    }
    
    public JacksonJsonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public String toJsonString(Object obj, Type type) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
    
    @Override
    public InputStream toJsonStream(Object obj, Type type) {
        try {
            return new ByteArrayInputStream(mapper.writeValueAsBytes(obj));
        } catch (Exception e) {
            throw new RuntimeException("JSON stream failed", e);
        }
    }
    
    @Override
    public void writeToOutputStream(Object obj, OutputStream outputStream) {
        try {
            mapper.writeValue(outputStream, obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON write failed", e);
        }
    }
    
    @Override
    public <T> T fromJsonString(String json, Type targetType) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return mapper.readValue(json, mapper.constructType(targetType));
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
    
    @Override
    public <T> T fromJsonStream(InputStream json, Type targetType) {
        try {
            return mapper.readValue(json, mapper.constructType(targetType));
        } catch (Exception e) {
            throw new RuntimeException("JSON stream deserialization failed", e);
        }
    }
}
