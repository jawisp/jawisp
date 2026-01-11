package io.jawisp.http.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Authentication required");
    }
    
    public UnauthorizedException(String resource) {
        super("Access to resource '%s' requires authentication".formatted(resource));
    }
    
    public UnauthorizedException(String resource, Object identity) {
        super("User '%s' not authorized for resource '%s'".formatted(identity, resource));
    }
}
