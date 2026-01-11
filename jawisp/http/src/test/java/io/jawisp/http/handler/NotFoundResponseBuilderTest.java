package io.jawisp.http.handler;

import io.jawisp.http.MediaType;
import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotFoundResponseBuilderTest {

    @Mock private Response response;
    @Mock private Request request;

    @Test
    void testExecute_SetsCorrectStatusAndContentType() {
        // Given
        var builder = new NotFoundResponseBuilder();
        builder.reset(response, request);

        // When
        builder.execute();

        // Then - Verify observable behavior
        verify(response).setStatus(404);
        verify(response).setContentType(MediaType.APPLICATION_JSON.getMediaType());
        verify(response).setBody(any(byte[].class));
    }

    @Test
    void testReset_Fluency() {
        // Given
        var builder = new NotFoundResponseBuilder();

        // When
        var result = builder.reset(response, request);

        // Then
        assert result == builder;
    }

    @Test
    void testReset_StoresReferences() {
        // Given
        var builder = new NotFoundResponseBuilder();
        var testResponse = mock(Response.class);
        var testRequest = mock(Request.class);

        // When
        builder.reset(testResponse, testRequest);
        builder.execute();

        // Then - Verifies that reset() properly stored the references
        verify(testResponse).setStatus(404);
        verifyNoInteractions(response); // Original mocks not affected
    }
}
