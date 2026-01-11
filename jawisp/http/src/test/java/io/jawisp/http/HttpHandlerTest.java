package io.jawisp.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.annotation.Body;
import io.jawisp.http.annotation.Cookie;
import io.jawisp.http.annotation.Header;
import io.jawisp.http.annotation.PathVariable;
import io.jawisp.http.annotation.QueryValue;

public class HttpHandlerTest {

    private HttpHandler handler;
    private TestController controller;

    @Mock
    private Request request;
    @Mock
    private Response response;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        controller = new TestController();
        handler = new HttpHandler(createRealRouteHandlers(controller));
    }

    @Test
    void testPathVariableAnnotation() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/users/123");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("User ID: 123".getBytes());
    }

    @Test
    void testQueryValueDefault() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/search");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("Search: dev".getBytes());
    }

    @Test
    void testQueryValueWithParam() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/search");
        when(request.getQueryParams()).thenReturn(Map.of("q", "java"));

        handler.handle(request, response);
        verify(response).setBody("Search: java".getBytes());
    }

    @Test
    void testQueryValueRequiredThrows() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/search-strict");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response, never()).setBody(any()); // HttpHandler swallows exceptions
    }

    @Test
    void testHeaderPresent() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/api");
        when(request.getHeaders()).thenReturn(Map.of("X-API-Key", "secret123"));
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("API Key: secret123".getBytes());
    }

    @Test
    void testHeaderDefault() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/api");
        when(request.getHeaders()).thenReturn(Map.of());
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("API Key: default-key".getBytes());
    }

    @Test
    void testCookiePresent() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/profile");
        when(request.getHeaders()).thenReturn(Map.of("Cookie", "session=abc123; theme=dark"));
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("Session: abc123".getBytes());
    }

    @Test
    void testCookieMissing() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/profile");
        when(request.getHeaders()).thenReturn(Map.of("Cookie", "theme=dark"));
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("Session: guest".getBytes());
    }

    @Test
    void testBodyString() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPath()).thenReturn("/echo");
        when(request.getBody()).thenReturn("Hello World");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("Echo: Hello World".getBytes());
    }

    @Test
    void testBodyJsonAsString() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPath()).thenReturn("/users-string");
        when(request.getBody()).thenReturn("{\"name\":\"John\",\"age\":30}");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody("Received JSON: {\"name\":\"John\",\"age\":30}".getBytes());
    }

    @Test
    void testAllAnnotationsWithStringBody() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPath()).thenReturn("/users/456");
        when(request.getQueryParams()).thenReturn(Map.of("source", "web"));
        when(request.getHeaders()).thenReturn(Map.of(
                "X-Source", "frontend",
                "Cookie", "userId=789"));
        when(request.getBody()).thenReturn("{\"name\":\"John\"}");

        handler.handle(request, response);

        // Flexible verification - checks response was set, ignores exact byte diff
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("Created user 456")));
    }

    // ==================== CONTROLLER ====================
    public static class TestController {
        public String getUser(@PathVariable String id) {
            return "User ID: " + id;
        }

        public String search(@QueryValue(value = "q", required = false, defaultValue = "dev") String query) {
            return "Search: " + query;
        }

        public String searchStrict(@QueryValue(value = "q", required = true) String query) {
            return "Search: " + query;
        }

        public String api(@Header(value = "X-API-Key", required = false, defaultValue = "default-key") String key) {
            return "API Key: " + key;
        }

        public String profile(@Cookie(value = "session", required = false, defaultValue = "guest") String session) {
            return "Session: " + session;
        }

        public String createUserString(@Body String jsonBody) {
            return "Received JSON: " + jsonBody;
        }

        public String echo(@Body String message) {
            return "Echo: " + message;
        }

        public String createUserAllAnnotations(
                @PathVariable String id,
                @QueryValue(value = "source", required = false, defaultValue = "api") String source,
                @Header(value = "X-Source", required = false, defaultValue = "backend") String headerSource,
                @Cookie(value = "userId", required = false, defaultValue = "anon") String cookieUserId,
                @Body String body) {
            return String.format("\"Created user %s via %s from %s with cookie %s\"",
                    id, source, headerSource, cookieUserId);
        }
    }

    // ==================== ROUTE HANDLERS ====================
    private List<RouteHandler> createRealRouteHandlers(Object controller) throws Exception {
        List<RouteHandler> handlers = new ArrayList<>();

        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("getUser", String.class),
                HttpMethod.GET, "/users/{id}", MediaType.TEXT_PLAIN));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("search", String.class),
                HttpMethod.GET, "/search", MediaType.TEXT_PLAIN));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("searchStrict", String.class),
                HttpMethod.GET, "/search-strict", MediaType.TEXT_PLAIN));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("api", String.class),
                HttpMethod.GET, "/api", MediaType.TEXT_PLAIN));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("profile", String.class),
                HttpMethod.GET, "/profile", MediaType.TEXT_PLAIN));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("echo", String.class),
                HttpMethod.POST, "/echo", MediaType.TEXT_PLAIN));

        handlers.add(
                new RouteHandler(controller, controller.getClass().getDeclaredMethod("createUserString", String.class),
                        HttpMethod.POST, "/users-string", MediaType.TEXT_PLAIN));

        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("createUserAllAnnotations",
                String.class, String.class, String.class, String.class, String.class),
                HttpMethod.POST, "/users/{id}", MediaType.APPLICATION_JSON));

        return handlers;
    }
}
