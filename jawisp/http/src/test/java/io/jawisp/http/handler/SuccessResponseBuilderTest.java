package io.jawisp.http.handler;

import io.jawisp.http.MediaType;
import io.jawisp.http.Server.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuccessResponseBuilderTest {

    @Mock private Response response;

    @Test
    void testExecute_NoResult_Returns204() {
        var builder = new SuccessResponseBuilder();
        builder.reset(response).execute();

        verify(response).setStatus(204);
        verifyNoMoreInteractions(response);
    }

    @Test
    void testExecute_WithResultJson_MediaType_SetsContentTypeAndBody() {
        var builder = new SuccessResponseBuilder();
        String result = "test data";
        builder.reset(response)
               .result(result)
               .mediaType(MediaType.APPLICATION_JSON)
               .execute();

        verify(response).setContentType("application/json");
        verify(response).setBody(any(byte[].class));
        verifyNoMoreInteractions(response);
    }

    @Test
    void testExecute_WithResultDefaultMediaType_UsesToString() {
        var builder = new SuccessResponseBuilder();
        String result = "test data";
        builder.reset(response)
               .result(result)
               .mediaType(MediaType.TEXT_PLAIN)  // or any non-JSON
               .execute();

        verify(response).setContentType("text/plain");
        verify(response).setBody(any(byte[].class));
    }

    @Test
    void testReset_FluencyAndClearsState() {
        var builder = new SuccessResponseBuilder();
        var result = builder.reset(response).result("test").mediaType(MediaType.APPLICATION_JSON);

        assert result == builder;
    }

    @Test
    void testResult_Fluency() {
        var builder = new SuccessResponseBuilder();
        var result = builder.reset(response).result("test");

        assert result == builder;
    }

    @Test
    void testMediaType_Fluency() {
        var builder = new SuccessResponseBuilder();
        var result = builder.reset(response).mediaType(MediaType.APPLICATION_JSON);

        assert result == builder;
    }

    @Test
    void testExecute_NullResultAfterReset_204NoContent() {
        var builder = new SuccessResponseBuilder();
        // Previous state should be cleared by reset
        builder.reset(response).execute();

        verify(response).setStatus(204);
    }
}
