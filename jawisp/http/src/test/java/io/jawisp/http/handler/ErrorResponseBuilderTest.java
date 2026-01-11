package io.jawisp.http.handler;

import io.jawisp.http.MediaType;
import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.exception.ResourceNotFoundException;
import io.jawisp.http.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    @Mock private Response response;
    @Mock private Request request;

    @Test
    void testExecute_NoException_Default500() {
        var builder = new ErrorResponseBuilder();
        builder.reset(response, request).execute();

        verify(response).setStatus(500);
        verify(response).setContentType(MediaType.APPLICATION_JSON.getMediaType());
        verify(response).setBody(any(byte[].class));
    }

    @Test
    void testExecute_IllegalArgumentException_Returns400() {
        var exception = new IllegalArgumentException("Invalid input");
        var builder = new ErrorResponseBuilder();
        builder.reset(response, request).exception(exception).execute();

        verify(response).setStatus(400);
    }

    @Test
    void testExecute_ResourceNotFoundException_Returns404() {
        var exception = new ResourceNotFoundException("Resource missing");
        var builder = new ErrorResponseBuilder();
        builder.reset(response, request).exception(exception).execute();

        verify(response).setStatus(404);
    }

    @Test
    void testExecute_UnauthorizedException_Returns401() {
        var exception = new UnauthorizedException("Auth required");
        var builder = new ErrorResponseBuilder();
        builder.reset(response, request).exception(exception).execute();

        verify(response).setStatus(401);
    }

    @Test
    void testExecute_AccessDeniedException_Returns403() {
        var exception = new AccessDeniedException("Access denied");
        var builder = new ErrorResponseBuilder();
        builder.reset(response, request).exception(exception).execute();

        verify(response).setStatus(403);
    }

    @Test
    void testExecute_JsonProcessingException_Returns400() {
        var exception = new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {};
        var builder = new ErrorResponseBuilder();
        builder.reset(response, request).exception(exception).execute();

        verify(response).setStatus(400);
    }

    @Test
    void testReset_FluencyAndClearsException() {
        var builder = new ErrorResponseBuilder();
        var result = builder.reset(response, request).exception(new RuntimeException());

        assert result == builder;
    }

    @Test
    void testException_Fluency() {
        var builder = new ErrorResponseBuilder();
        var result = builder.reset(response, request).exception(new RuntimeException());

        assert result == builder;
    }
}
