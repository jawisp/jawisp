package io.jawisp.http.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.jawisp.http.HttpMethod;
import io.jawisp.http.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public class ServerHandlerUtilsTest {
    
    @Mock
    private FullHttpRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindRouteWithMatchingRoute() {
        // Arrange
        when(request.uri()).thenReturn("/test");
        when(request.method()).thenReturn(io.netty.handler.codec.http.HttpMethod.GET);
        Route route = new Route(HttpMethod.GET, "/test", null);
        List<Route> routes = Arrays.asList(route);

        // Act
        Optional<Route> result = ServerHandlerUtils.findRoute(request, routes);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(route, result.get());
    }

    @Test
    void testFindRouteWithoutMatchingRoute() {
        // Arrange
        when(request.uri()).thenReturn("/test");
        when(request.method()).thenReturn(io.netty.handler.codec.http.HttpMethod.GET);
        Route route = new Route(HttpMethod.POST, "/test", null);
        List<Route> routes = Arrays.asList(route);

        // Act
        Optional<Route> result = ServerHandlerUtils.findRoute(request, routes);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindRouteWithBeforeFilter() {
        // Arrange
        when(request.uri()).thenReturn("/test");
        when(request.method()).thenReturn(io.netty.handler.codec.http.HttpMethod.GET);
        Route beforeFilterRoute = new Route(HttpMethod.BEFORE_FILTER, "/test", null);
        List<Route> routes = Arrays.asList(beforeFilterRoute);

        // Act
        Optional<Route> result = ServerHandlerUtils.findRoute(request, routes);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindRouteWithAfterFilter() {
        // Arrange
        when(request.uri()).thenReturn("/test");
        when(request.method()).thenReturn(io.netty.handler.codec.http.HttpMethod.GET);
        Route afterFilterRoute = new Route(HttpMethod.AFTER_FILTER, "/test", null);
        List<Route> routes = Arrays.asList(afterFilterRoute);

        // Act
        Optional<Route> result = ServerHandlerUtils.findRoute(request, routes);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindFilterWithMatchingFilter() {
        // Arrange
        Route filterRoute = new Route(HttpMethod.BEFORE_FILTER, "/test", null);
        List<Route> routes = Arrays.asList(filterRoute);

        // Act
        Optional<Route> result = ServerHandlerUtils.findFilter(HttpMethod.BEFORE_FILTER, routes);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(filterRoute, result.get());
    }

    @Test
    void testFindFilterWithoutMatchingFilter() {
        // Arrange
        Route filterRoute = new Route(HttpMethod.AFTER_FILTER, "/test", null);
        List<Route> routes = Arrays.asList(filterRoute);

        // Act
        Optional<Route> result = ServerHandlerUtils.findFilter(HttpMethod.BEFORE_FILTER, routes);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testMatchPathExactMatch() {
        // Arrange
        String pattern = "/test";
        String path = "/test";

        // Act
        boolean result = ServerHandlerUtils.matchPath(pattern, path);

        // Assert
        assertTrue(result);
    }

    @Test
    void testMatchPathWithParams() {
        // Arrange
        String pattern = "/user/:id";
        String path = "/user/123";

        // Act
        boolean result = ServerHandlerUtils.matchPath(pattern, path);

        // Assert
        assertTrue(result);
    }

    @Test
    void testMatchPathNoMatch() {
        // Arrange
        String pattern = "/test";
        String path = "/another";

        // Act
        boolean result = ServerHandlerUtils.matchPath(pattern, path);

        // Assert
        assertFalse(result);
    }

    @Test
    void testMatchPathWithDifferentParams() {
        // Arrange
        String pattern = "/user/:id";
        String path = "/user/456";

        // Act
        boolean result = ServerHandlerUtils.matchPath(pattern, path);

        // Assert
        assertTrue(result);
    }
}
