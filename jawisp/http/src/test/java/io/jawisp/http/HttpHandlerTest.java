package io.jawisp.http;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
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
import io.jawisp.http.exception.ResourceNotFoundException;
import io.jawisp.http.exception.UnauthorizedException;

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

    // ==================== EXISTING HAPPY PATH TESTS ====================
    @Test
    void testPathVariableAnnotation() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/users/123");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("User ID: 123")));
    }

    @Test
    void testQueryValueDefault() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/search");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("Search: dev")));
    }

    @Test
    void testQueryValueWithParam() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/search");
        when(request.getQueryParams()).thenReturn(Map.of("q", "java"));

        handler.handle(request, response);
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("Search: java")));
    }

    @Test
    void testHeaderPresent() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/api");
        when(request.getHeaders()).thenReturn(Map.of("X-API-Key", "secret123"));
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("API Key: secret123")));
    }

    @Test
    void testCookiePresent() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/profile");
        when(request.getHeaders()).thenReturn(Map.of("Cookie", "session=abc123; theme=dark"));
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("Session: abc123")));
    }

    @Test
    void testBodyString() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getPath()).thenReturn("/echo");
        when(request.getBody()).thenReturn("Hello World");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("Echo: Hello World")));
    }

    // ==================== NEW ERROR HANDLING TESTS ====================
    @Test
    void testRouteNotFoundReturns404() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/missing-route");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);

        verify(response).setStatus(404);
        verify(response).setContentType("application/json");
        verify(response).setBody(argThat(bytes -> {
            String body = new String(bytes, StandardCharsets.UTF_8);
            return body.contains("\"statusCode\":404") &&
                    body.contains("\"error\":\"Not Found\"") &&
                    body.contains("\"message\":\"Route not found\"");
        }));
    }

    @Test
    void testWrongHttpMethodReturns404() {
        when(request.getMethod()).thenReturn("POST"); // Wrong method for GET routes
        when(request.getPath()).thenReturn("/users_nonexisted/123");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);

        verify(response).setStatus(404);
        verify(response).setContentType("application/json");
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("\"statusCode\":404")));
    }

    @Test
    void testMissingRequiredQueryParamReturns400() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/search-strict");
        when(request.getQueryParams()).thenReturn(Map.of());

        handler.handle(request, response);

        verify(response).setStatus(400);
        verify(response).setContentType("application/json");
        verify(response).setBody(argThat(bytes -> {
            String body = new String(bytes);
            return body.contains("\"statusCode\":400") &&
                    body.contains("Missing required query param");
        }));
    }

    @Test
    void testControllerThrowsResourceNotFoundReturns404() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/users/999");
        when(request.getQueryParams()).thenReturn(Map.of());

        TestController testController = new TestController();
        handler = new HttpHandler(createErrorTestRouteHandlers(testController));

        handler.handle(request, response);

        verify(response).setStatus(404);
        verify(response).setContentType("application/json");
        verify(response).setBody(argThat(bytes -> {
            String body = new String(bytes);
            return body.contains("\"statusCode\":404") &&
                    body.contains("Resource 'User' with id '999' not found");
        }));
    }

    @Test
    void testControllerThrowsUnauthorizedReturns401() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/admin");
        when(request.getQueryParams()).thenReturn(Map.of());

        TestController testController = new TestController();
        handler = new HttpHandler(createErrorTestRouteHandlers(testController));

        handler.handle(request, response);

        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("\"statusCode\":401")));
    }

    @Test
    void testInvalidNumberParsingReturns400() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/users/invalid");
        when(request.getQueryParams()).thenReturn(Map.of());

        TestController testController = new TestController();
        handler = new HttpHandler(createErrorTestRouteHandlers(testController));

        handler.handle(request, response);

        verify(response).setStatus(400);
        verify(response).setContentType("application/json");
        verify(response).setBody(argThat(bytes -> new String(bytes).contains("\"statusCode\":400")));
    }

    // ==================== UPDATED CONTROLLER WITH ERROR CASES ====================
    public static class TestController {
        public String getUser(@PathVariable String id) {
            try {
                int userId = Integer.parseInt(id);
                if (userId == 999) {
                    throw new ResourceNotFoundException("User", userId);
                }
                return "User ID: " + id;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid user ID: " + id);
            }
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

        public String echo(@Body String message) {
            return "Echo: " + message;
        }

        public String createUserString(@Body String jsonBody) {
            return "Received JSON: " + jsonBody;
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

        // Error test methods
        public String emptyResponse() {
            return null;
        }

        public void adminOnly() {
            throw new UnauthorizedException("admin API");
        }
    }

    // ==================== ROUTE HANDLER CREATORS ====================
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

    private List<RouteHandler> createErrorTestRouteHandlers(Object controller) throws Exception {
        List<RouteHandler> handlers = new ArrayList<>();

        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("getUser", String.class),
                HttpMethod.GET, "/users/{id}", MediaType.APPLICATION_JSON));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("emptyResponse"),
                HttpMethod.GET, "/empty", MediaType.APPLICATION_JSON));
        handlers.add(new RouteHandler(controller, controller.getClass().getDeclaredMethod("adminOnly"),
                HttpMethod.GET, "/admin", MediaType.APPLICATION_JSON));

        return handlers;
    }
}
