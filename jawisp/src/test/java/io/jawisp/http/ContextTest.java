package io.jawisp.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContextTest {

    @Test
    void body_returnsContentAsString() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/test");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);  // FIX: HttpUtil needs this
        
        ByteBuf content = mock(ByteBuf.class);
        when(content.toString(StandardCharsets.UTF_8)).thenReturn("body-data");
        when(request.content()).thenReturn(content);
        
        Context ctx = new Context(request, new Route(HttpMethod.GET, "/test", null));
        assertEquals("body-data", ctx.body());  // Line 30 - PASSES
    }

    @Test
    void result_chainsCorrectly() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);  // FIX: HttpUtil safe
        
        Context ctx = new Context(request, new Route(HttpMethod.GET, "/", null));
        ctx.result("Hello").result(" World");  // Line 51 - PASSES
        assertEquals("Hello World", ctx.getResult().toString());
    }

    @Test
    void json_setsContentType() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);  // FIX: HttpUtil safe
        
        Context ctx = new Context(request, new Route(HttpMethod.GET, "/", null));
        ctx.json("{\"test\":true}");  // Line 61 - PASSES
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
}
