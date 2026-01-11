package io.jawisp.http.handler;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jawisp.http.MediaType;
import io.jawisp.http.Server.Response;

public class SuccessResponseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SuccessResponseBuilder.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private Response response;
    private Object result;
    private MediaType mediaType;

    public SuccessResponseBuilder reset(Response response) {
        this.response = response;
        this.result = null;
        this.mediaType = null;
        return this;
    }

    public SuccessResponseBuilder result(Object result) {
        this.result = result;
        return this;
    }

    public SuccessResponseBuilder mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public void execute() {
        if (result == null) {
            response.setStatus(204);
            return;
        }

        response.setContentType(mediaType.getMediaType());
        response.setBody(serialize(result, mediaType));
    }

    private byte[] serialize(Object result, MediaType produces) {
        return switch (produces) {
            case APPLICATION_JSON -> safeJsonSerialize(result);
            default -> result.toString().getBytes(StandardCharsets.UTF_8);
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
