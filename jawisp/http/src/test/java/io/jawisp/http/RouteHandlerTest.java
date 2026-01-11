package io.jawisp.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RouteHandlerTest {

    private Object mockController;
    private Method mockMethod;
    private HttpMethod httpMethod = HttpMethod.GET;
    private String path = "/users/{id}/posts/{postId}";
    private MediaType mediaType = MediaType.APPLICATION_JSON;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        mockController = new Object();
        mockMethod = mockController.getClass().getMethod("toString"); // dummy method
    }

    @Test
    void testConstructorInitializesFields() {
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                path,
                mediaType
        );

        assertEquals(mockController, routeHandler.getController());
        assertEquals(mockMethod, routeHandler.getMethod());
        assertEquals(httpMethod, routeHandler.getHttpMethod());
        assertEquals(path, routeHandler.getPath());
        assertEquals(mediaType, routeHandler.getProduces());
    }

    @Test
    void testPathParamsAreExtractedCorrectly() {
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                path,
                mediaType
        );

        List<String> pathParams = routeHandler.getPathParams();
        assertNotNull(pathParams);
        assertEquals(2, pathParams.size());
        assertTrue(pathParams.contains("id"));
        assertTrue(pathParams.contains("postId"));
    }

    @Test
    void testPatternIsCompiledCorrectly() {
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                path,
                mediaType
        );

        Pattern pattern = routeHandler.getPattern();

        assertNotNull(pattern);
        assertTrue(pattern.matcher("/users/123/posts/456").matches());
        assertFalse(pattern.matcher("/users/123/posts/456/extra").matches());
    }

    @Test
    void testPatternHandlesSpecialCharactersInPath() {
        String pathWithSpecialChars = "/api/v1/{resource}/{id}";
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                pathWithSpecialChars,
                mediaType
        );

        Pattern pattern = routeHandler.getPattern();

        assertTrue(pattern.matcher("/api/v1/users/123").matches());
    }

    @Test
    void testEmptyPathParameter() {
        String pathWithEmptyParam = "/api/{id}/";
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                pathWithEmptyParam,
                mediaType
        );

        Pattern pattern = routeHandler.getPattern();
        assertTrue(pattern.matcher("/api/123/").matches());
    }

    @Test
    void testNoParametersInPath() {
        String pathWithoutParams = "/api/users";
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                pathWithoutParams,
                mediaType
        );

        List<String> pathParams = routeHandler.getPathParams();
        assertTrue(pathParams.isEmpty());

        Pattern pattern = routeHandler.getPattern();
        assertTrue(pattern.matcher("/api/users").matches());
        assertFalse(pattern.matcher("/api/users/extra").matches());
    }

    @Test
    void testPathParamsAreUnique() {
        String pathWithRepeatedParams = "/{id}/{id}";
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                pathWithRepeatedParams,
                mediaType
        );

        List<String> pathParams = routeHandler.getPathParams();
        assertEquals(1, pathParams.size()); // Only one unique parameter name
        assertTrue(pathParams.contains("id"));
    }

    @Test
    void testPathWithMultipleParameters() {
        String path = "/{user}/{post}/{comment}";
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                path,
                mediaType
        );

        List<String> pathParams = routeHandler.getPathParams();
        assertEquals(3, pathParams.size());
        assertTrue(pathParams.contains("user"));
        assertTrue(pathParams.contains("post"));
        assertTrue(pathParams.contains("comment"));

        Pattern pattern = routeHandler.getPattern();
        assertTrue(pattern.matcher("/john/123/456").matches());
    }

    @Test
    void testPathWithTrailingSlash() {
        String path = "/api/{id}/";
        RouteHandler routeHandler = new RouteHandler(
                mockController,
                mockMethod,
                httpMethod,
                path,
                mediaType
        );

        Pattern pattern = routeHandler.getPattern();
        assertTrue(pattern.matcher("/api/123/").matches());
    }
}
