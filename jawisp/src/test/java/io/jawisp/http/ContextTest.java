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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.jawisp.http.netty.NettyContext;
import io.jawisp.json.JsonMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

class ContextTest {

    private ChannelHandlerContext context = mock(ChannelHandlerContext.class);

    @Test
    void body_returnsContentAsString() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/test");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        ByteBuf content = mock(ByteBuf.class);
        when(content.toString(StandardCharsets.UTF_8)).thenReturn("body-data");
        when(request.content()).thenReturn(content);

        Route route = new Route(HttpMethod.GET, "/test", null);

        // Now constructor works with all 3 params
        NettyContext ctx = new NettyContext(context, request, route);
        assertEquals("body-data", ctx.body());
    }

    @Test
    void result_overwriteCorrectly() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1); // FIX: HttpUtil safe

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        ctx.text("Hello World");
        assertEquals("Hello World", ctx.result().toString());
        ctx.text("Hello");
        assertEquals("Hello", ctx.result().toString());
    }

    @Test
    void json_setsContentType() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1); // FIX: HttpUtil safe

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));
        ctx.json("{\"test\":true}"); // Line 61 - PASSES
        assertEquals("application/json; charset=UTF-8", ctx.contentType());
    }

    @Test
    void request_getProperPathParameter() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals("42", ctx.pathParam("id"));
    }

    @Test
    void request_getWrongPathParameter() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals(null, ctx.pathParam("id2"));
    }

    @Test
    void request_getEmptyPathParameter() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals(null, ctx.pathParam(""));
    }

    @Test
    void constructor_setsFieldsProperly() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.uri()).thenReturn("/users/42");
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/user/:id", null));
        assertEquals("/users/42", ctx.path());
        assertEquals(200, ctx.status());
        assertTrue(ctx.isKeepAlive());
    }

    @Test
    void testBodyNotNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        String body = "Hello, World!";
        ByteBuf content = Unpooled.wrappedBuffer(body.getBytes());
        when(request.content()).thenReturn(content);
        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        // Act
        String result = ctx.body();

        // Assert
        assertNotNull(result);
        assertEquals(body, result);
    }

    @Test
    void testBodyNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.content()).thenReturn(null);
        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        // Act
        String result = ctx.body();

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
        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        // Act
        String result = ctx.body();

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
        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        // Act
        byte[] result = ctx.bodyAsBytes();

        // Assert
        assertNotNull(result);
        assertArrayEquals(body.getBytes(), result);
    }

    @Test
    void testBodyAsBytesNullContent() {
        // Arrange
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.content()).thenReturn(null);
        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        // Act
        byte[] result = ctx.bodyAsBytes();

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
        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        // Act
        byte[] result = ctx.bodyAsBytes();

        // Assert
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    void bodyAsClass_noContentTypeHeader_throwsException() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn(null);

        Context ctx = new NettyContext(context, request, new Route(HttpMethod.GET, "/", null));

        assertThrows(UnsupportedOperationException.class,
                () -> ctx.bodyAsClass(String.class));
    }

    private Context mockContextWithJsonMapper(FullHttpRequest request, Route route, String expectedResult) {
        Context ctx = new NettyContext(context, request, route);

        try {
            // Use reflection to inject mock JsonMapper (bypasses ServiceLoader for tests)
            Field jsonMapperField = NettyContext.class.getDeclaredField("jsonMapper");
            jsonMapperField.setAccessible(true);

            JsonMapper mockMapper = mock(JsonMapper.class);
            String json = new String(((ByteBuf) request.content()).array(), StandardCharsets.UTF_8);

            when(mockMapper.fromJsonString(eq(json), any(Type.class))).thenReturn(expectedResult);

            jsonMapperField.set(ctx, mockMapper);
            return ctx;
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
        Context ctx = mockContextWithJsonMapper(request, new Route(HttpMethod.GET, "/", null), "parsed");

        // Act & Assert
        String result = ctx.bodyAsClass(String.class);
        assertEquals("parsed", result);
    }

    @Test
    void bodyAsClass_jsonWithNoCharset_succeeds() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");

        String json = "{\"id\":1}";
        when(request.content()).thenReturn(Unpooled.copiedBuffer(json, StandardCharsets.UTF_8));

        Context ctx = mockContextWithJsonMapper(request, new Route(HttpMethod.GET, "/", null), "parsed");

        String result = ctx.bodyAsClass(String.class);
        assertEquals("parsed", result);
    }

    @Test
    void bodyAsClass_emptyBody_returnsNull() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");
        when(request.content()).thenReturn(Unpooled.EMPTY_BUFFER);

        Context ctx = mockContextWithJsonMapper(request, new Route(HttpMethod.GET, "/", null), null);

        String result = ctx.bodyAsClass(String.class);
        assertNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sessionAttributeSet_getsSameValue() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        Attribute<Object> attr = mock(Attribute.class);
        AttributeKey<Object> key = AttributeKey.valueOf("session.userId");

        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(key)).thenReturn(attr);

        when(attr.get()).thenReturn("alice");

        Context nettyCtx = new NettyContext(ctx, request, new Route(HttpMethod.GET, "/", null));

        // When
        nettyCtx.sessionAttribute("userId", "alice");
        String result = nettyCtx.sessionAttribute("userId");

        // Then
        assertEquals("alice", result);
        verify(channel, atLeast(1)).attr(key);
        verify(attr).set("alice");
        verify(attr, atLeast(1)).get();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sessionAttributeGet_notSet_returnsNull() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        Attribute<Object> attr = mock(Attribute.class);
        AttributeKey<Object> key = AttributeKey.valueOf("session.missing");

        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(key)).thenReturn(attr);
        when(attr.get()).thenReturn(null);

        Context nettyCtx = new NettyContext(ctx, request, new Route(HttpMethod.GET, "/", null));

        // When
        String result = nettyCtx.sessionAttribute("missing");

        // Then
        assertNull(result);
        verify(channel).attr(key);
        verify(attr).get();
    }

    @Test
    void clientIp_returnsRemoteAddress() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);

        InetSocketAddress remoteAddr = new InetSocketAddress("192.168.1.100", 12345);
        when(ctx.channel()).thenReturn(channel);
        when(channel.remoteAddress()).thenReturn(remoteAddr);

        Context nettyCtx = new NettyContext(ctx, request, new Route(HttpMethod.GET, "/", null));

        String result = nettyCtx.ip();
        assertEquals("192.168.1.100", result);
    }

    @Test
    void clientHost_returnsHostname() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        when(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).thenReturn("application/json");

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        InetSocketAddress remoteAddr = mock(InetSocketAddress.class, RETURNS_DEEP_STUBS);

        when(ctx.channel()).thenReturn(channel);
        when(channel.remoteAddress()).thenReturn(remoteAddr);
        when(remoteAddr.getAddress()).thenReturn(mock(InetAddress.class));
        when(remoteAddr.getAddress().getHostName()).thenReturn("client.example.com");
        when(remoteAddr.getAddress().getHostAddress()).thenReturn("192.168.1.100");

        Context nettyCtx = new NettyContext(ctx, request, new Route(HttpMethod.GET, "/", null));

        String result = nettyCtx.host();
        assertEquals("client.example.com", result);
    }

    @Test
    void redirect_setsLocationHeader() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

        Context nettyCtx = new NettyContext(ctx, request, new Route(HttpMethod.GET, "/", null));

        nettyCtx.redirect("/login", 302);

        assertEquals(302, nettyCtx.status());
        assertEquals("/login", nettyCtx.response().headers().get(HttpHeaderNames.LOCATION));
        assertEquals("0", nettyCtx.response().headers().get(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    void html_setsContentTypeAndContent() {
        FullHttpRequest request = mock(FullHttpRequest.class, RETURNS_DEEP_STUBS);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

        Context nettyCtx = new NettyContext(ctx, request, new Route(HttpMethod.GET, "/", null));

        nettyCtx.html("<h1>Hello</h1>");

        assertEquals("text/html; charset=UTF-8", nettyCtx.contentType());
        assertEquals("<h1>Hello</h1>", nettyCtx.result());
    }

}
