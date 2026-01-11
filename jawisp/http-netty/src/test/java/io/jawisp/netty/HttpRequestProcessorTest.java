package io.jawisp.netty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.handler.Handler;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

class HttpRequestProcessorTest {

    private EmbeddedChannel channel;
    private TestHandler testHandler;
    private HttpRequestProcessor processor;

    @BeforeEach
    void setUp() {
        testHandler = new TestHandler();
        processor = new HttpRequestProcessor(testHandler);
        channel = new EmbeddedChannel(processor);
    }

    @Test
    void testGetRequest_success() {
        FullHttpRequest request = createGetRequest("/users/123");

        // Write inbound, finish, read outbound
        channel.writeInbound(request.retain());
        channel.finish();

        FullHttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertEquals("text/html", response.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertArrayEquals("OK".getBytes(StandardCharsets.UTF_8), response.content().array());
        response.release();
    }

    @Test
    void testPostRequestWithBody() {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "/users",
                Unpooled.copiedBuffer("{\"name\":\"John\"}", StandardCharsets.UTF_8));
        HttpUtil.setKeepAlive(request, true);

        channel.writeInbound(request.retain());
        channel.finish();

        FullHttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertEquals("text/html", response.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertArrayEquals("OK".getBytes(StandardCharsets.UTF_8), response.content().array());
        response.release();
    }

    @Test
    void testQueryParams() {
        FullHttpRequest request = createGetRequest("/users?name=John&age=30");

        channel.writeInbound(request.retain());
        channel.finish();

        // Verify nettyToCore() extracted query params correctly
        assertEquals("John", testHandler.lastRequest.getQueryParams().get("name"));
        assertEquals("30", testHandler.lastRequest.getQueryParams().get("age"));
    }

    @Test
    void testKeepAlive() {
        FullHttpRequest request = createGetRequest("/users/123");
        HttpUtil.setKeepAlive(request, true);

        channel.writeInbound(request.retain());
        channel.finish();

        FullHttpResponse response = channel.readOutbound();
        assertTrue(HttpUtil.isKeepAlive(response));
        assertEquals(HttpHeaderValues.KEEP_ALIVE.toString(),
                response.headers().get(HttpHeaderNames.CONNECTION));
        response.release();
    }

    @Test
    void testNoKeepAlive() {
        FullHttpRequest request = createGetRequest("/users/123");
        HttpUtil.setKeepAlive(request, false); // Request wants close

        channel.writeInbound(request.retain());
        channel.finish();

        FullHttpResponse response = channel.readOutbound();

        // Processor always adds Connection: close when !keepAlive
        assertNull(response.headers().get(HttpHeaderNames.CONNECTION));
        response.release();
    }

    @Test
    void testHandlerException_errorResponse() {
        testHandler.throwException = true;

        FullHttpRequest request = createGetRequest("/error");
        channel.writeInbound(request.retain());
        channel.finish();

        FullHttpResponse response = channel.readOutbound();
        assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, response.status());
        assertEquals("text/plain", response.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("Internal Server Error", response.content().toString(StandardCharsets.UTF_8));
        response.release();
    }

    private FullHttpRequest createGetRequest(String uri) {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        HttpUtil.setKeepAlive(request, true);
        return request;
    }

    static class TestHandler implements Handler {
        boolean throwException = false;
        Request lastRequest;

        @Override
        public void handle(Request request, Response response) {
            lastRequest = request;
            if (throwException) {
                throw new RuntimeException("Test exception");
            }
        }
    }
}
