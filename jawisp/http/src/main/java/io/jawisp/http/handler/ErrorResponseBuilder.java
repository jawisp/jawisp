package io.jawisp.http.handler;

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jawisp.http.MediaType;
import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.exception.ResourceNotFoundException;
import io.jawisp.http.exception.UnauthorizedException;

public class ErrorResponseBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseBuilder.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private Response response;
    private Request request;
    private Exception exception;

    public ErrorResponseBuilder reset(Response response, Request request) {
        this.response = response;
        this.request = request;
        this.exception = null;
        return this;
    }

    public ErrorResponseBuilder exception(Exception exception) {
        this.exception = exception;
        return this;
    }

    public void execute() {
        ErrorResponse error = classifyError(exception != null ? exception : new RuntimeException("Unknown error"),
                request);
        logger.error("Error [{} {}]: {}", request.getMethod(), request.getPath(), error.getMessage(), exception);

        response.setStatus(error.getStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON.getMediaType());
        response.setBody(safeJsonSerialize(error));
    }

    private ErrorResponse classifyError(Exception e, Request request) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;

        return switch (cause) {
            case IllegalArgumentException ex -> new ErrorResponse(400, "Bad Request", ex.getMessage(), request);
            case ResourceNotFoundException ex -> new ErrorResponse(404, "Not Found", ex.getMessage(), request);
            case UnauthorizedException ex -> new ErrorResponse(401, "Unauthorized", "Authentication required", request);
            case AccessDeniedException ex -> new ErrorResponse(403, "Forbidden", "Access denied", request);
            case JsonProcessingException ex -> new ErrorResponse(400, "Bad Request", "Invalid JSON", request);
            default -> new ErrorResponse(500, "Internal Server Error", "Internal server error", request);
        };
    }

    private byte[] safeJsonSerialize(Object result) {
        try {
            return mapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            logger.warn("JSON serialization failed: {}", e.getMessage());
            return "{}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
