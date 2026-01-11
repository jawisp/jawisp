package io.jawisp.http.exception;  

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Object id) {
        super("Resource '%s' with id '%s' not found".formatted(resource, id));
    }
    
    public ResourceNotFoundException(String resource, String field, Object value) {
        super("Resource '%s' with %s='%s' not found".formatted(resource, field, value));
    }
}
