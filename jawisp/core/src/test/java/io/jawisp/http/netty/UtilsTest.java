package io.jawisp.http.netty;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class UtilsTest {

    @Test
    public void testIsJsonWithJsonContentType() {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test",
                io.netty.buffer.Unpooled.buffer(0)
        );
        request.headers().set("Content-Type", "application/json");
        assertTrue(Utils.isJson(request));
    }

    @Test
    public void testIsJsonWithWildcardContentType() {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test",
                io.netty.buffer.Unpooled.buffer(0)
        );
        request.headers().set("Content-Type", "application/*");
        assertTrue(Utils.isJson(request));
    }

    @Test
    public void testIsJsonWithWildcardWildcardContentType() {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test",
                io.netty.buffer.Unpooled.buffer(0)
        );
        request.headers().set("Content-Type", "*/*");
        assertTrue(Utils.isJson(request));
    }

    @Test
    public void testIsJsonWithNonJsonContentType() {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test",
                io.netty.buffer.Unpooled.buffer(0)
        );
        request.headers().set("Content-Type", "text/plain");
        assertFalse(Utils.isJson(request));
    }

    @Test
    public void testIsJsonWithNoContentType() {
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test",
                io.netty.buffer.Unpooled.buffer(0)
        );
        assertFalse(Utils.isJson(request));
    }

    @Test
    public void testPathParamMapSimple() {
        Map<String, String> params = Utils.pathParamMap("/users/123", "/users/:id");
        assertEquals(Map.of("id", "123"), params);
    }

    @Test
    public void testPathParamMapMultiple() {
        Map<String, String> params = Utils.pathParamMap("/users/123/posts/456", "/users/:id/posts/:postId");
        assertEquals(Map.of("id", "123", "postId", "456"), params);
    }

    @Test
    public void testPathParamMapNoParams() {
        Map<String, String> params = Utils.pathParamMap("/users", "/users");
        assertTrue(params.isEmpty());
    }

    @Test
    public void testPathParamMapExtraParts() {
        Map<String, String> params = Utils.pathParamMap("/users/123/profile", "/users/:id");
        assertEquals(Map.of("id", "123"), params);
    }

    @Test
    public void testPathParamMapMissingParts() {
        Map<String, String> params = Utils.pathParamMap("/users", "/users/:id/posts/:postId");
        assertTrue(params.isEmpty());
    }

    @Test
    public void testPathParamMapNoRouteParams() {
        Map<String, String> params = Utils.pathParamMap("/users/123", "/users/123");
        assertTrue(params.isEmpty());
    }
}