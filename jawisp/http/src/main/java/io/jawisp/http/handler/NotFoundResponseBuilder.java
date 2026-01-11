package io.jawisp.http.handler;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jawisp.http.MediaType;
import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;

public class NotFoundResponseBuilder {
    private static final Logger logger = LoggerFactory.getLogger(NotFoundResponseBuilder.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private Response response;
    private Request request;

    public NotFoundResponseBuilder reset(Response response, Request request) {
        this.response = response;
        this.request = request;
        return this;
    }

    public void execute() {
        ErrorResponse error = new ErrorResponse(404, "Not Found", "Route not found", request);
        response.setStatus(404);
        response.setContentType(MediaType.APPLICATION_JSON.getMediaType());
        response.setBody(safeJsonSerialize(error));
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
