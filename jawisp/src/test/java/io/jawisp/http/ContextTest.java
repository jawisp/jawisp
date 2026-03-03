package io.jawisp.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.jawisp.http.json.JsonMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpVersion;

class ContextTest {

    @Test
    void body_returnsContentAsString() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/test");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1); // FIX: HttpUtil needs this

        ByteBuf content = mock(ByteBuf.class);
        when(content.toString(StandardCharsets.UTF_8)).thenReturn("body-data");
        when(request.content()).thenReturn(content);

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/test", null));
        assertEquals("body-data", ctx.body()); // Line 30 - PASSES
    }

    @Test
    void result_chainsCorrectly() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1); // FIX: HttpUtil safe

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/", null));
        ctx.result("Hello").result(" World"); // Line 51 - PASSES
        assertEquals("Hello World", ctx.getResult().toString());
    }

    @Test
    void json_setsContentType() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1); // FIX: HttpUtil safe

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/", null));
        ctx.json("{\"test\":true}"); // Line 61 - PASSES
        assertEquals("application/json; charset=UTF-8", ctx.getContentType());
    }

    @Test
    void request_getProperPathParameter() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals("42", ctx.pathParam("id"));
    }

    @Test
    void request_getWrongPathParameter() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals(null, ctx.pathParam("id2"));
    }

    @Test
    void request_getEmptyPathParameter() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals(null, ctx.pathParam(""));
    }

    @Test
    void constructor_setsFieldsProperly() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new Context(request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals("/users/42", ctx.getPath());
        assertEquals(200, ctx.getStatus());
        assertTrue(ctx.isKeepAlive());
    }

    @Test
    void testBodyNotNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        String body = "Hello, World!";
        ByteBuf content = Unpooled.wrappedBuffer(body.getBytes());
        when(request.content()).thenReturn(content);
        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        // Act
        String result = context.body();

        // Assert
        assertNotNull(result);
        assertEquals(body, result);
    }

    @Test
    void testBodyNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.content()).thenReturn(null);
        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        // Act
        String result = context.body();

        // Assert
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void testBodyEmptyContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        ByteBuf content = Unpooled.buffer(0);
        when(request.content()).thenReturn(content);
        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        // Act
        String result = context.body();

        // Assert
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void testBodyAsBytesNotNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        String body = "Hello, World!";
        ByteBuf content = Unpooled.wrappedBuffer(body.getBytes());
        when(request.content()).thenReturn(content);
        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        // Act
        byte[] result = context.bodyAsBytes();

        // Assert
        assertNotNull(result);
        assertArrayEquals(body.getBytes(), result);
    }

    @Test
    void testBodyAsBytesNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.content()).thenReturn(null);
        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        // Act
        byte[] result = context.bodyAsBytes();

        // Assert
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    void testBodyAsBytesEmptyContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        ByteBuf content = Unpooled.buffer(0);
        when(request.content()).thenReturn(content);
        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        // Act
        byte[] result = context.bodyAsBytes();

        // Assert
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    void bodyAsClass_noContentTypeHeader_throwsException() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn(null);

        Context context = new Context(request, new Route(HttpMethod.GET, "/", null));

        assertThrows(UnsupportedOperationException.class,
                () -> context.bodyAsClass(String.class));
    }

    private Context mockContextWithJsonMapper(FullHttpRequest request, Route route, String expectedResult) {
        Context context = new Context(request, route);

        try {
            // Use reflection to inject mock JsonMapper (bypasses ServiceLoader for tests)
            Field jsonMapperField = Context.class.getDeclaredField("jsonMapper");
            jsonMapperField.setAccessible(true);

            JsonMapper mockMapper = mock(JsonMapper.class);
            String json = new String(((ByteBuf) request.content()).array(), StandardCharsets.UTF_8);

            when(mockMapper.fromJsonString(eq(json), any(Type.class))).thenReturn(expectedResult);

            jsonMapperField.set(context, mockMapper);
            return context;
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test JsonMapper", e);
        }
    }

    @Test
    void bodyAsClass_jsonContentType_callsJsonMapper() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE))
                .thenReturn("application/json; charset=UTF-8");

        String json = "{\"name\":\"test\"}";
        ByteBuf content = Unpooled.copiedBuffer(json, StandardCharsets.UTF_8);
        when(request.content()).thenReturn(content);

        // Use test utility
        Context context = mockContextWithJsonMapper(request, new Route(HttpMethod.GET, "/", null), "parsed");

        // Act & Assert
        String result = context.bodyAsClass(String.class);
        assertEquals("parsed", result);
    }

    @Test
    void bodyAsClass_jsonWithNoCharset_succeeds() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");

        String json = "{\"id\":1}";
        when(request.content()).thenReturn(Unpooled.copiedBuffer(json, StandardCharsets.UTF_8));

        Context context = mockContextWithJsonMapper(request, new Route(HttpMethod.GET, "/", null), "parsed");

        String result = context.bodyAsClass(String.class);
        assertEquals("parsed", result);
    }

    @Test
    void bodyAsClass_emptyBody_returnsNull() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");
        when(request.content()).thenReturn(Unpooled.EMPTY_BUFFER);

        Context context = mockContextWithJsonMapper(request, new Route(HttpMethod.GET, "/", null), null);

        String result = context.bodyAsClass(String.class);
        assertNull(result);
    }

}
