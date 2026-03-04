package io.jawisp.http.netty;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jawisp.http.HttpMethod;
import io.jawisp.http.Route;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * The ServerHandlerUtils class provides utility methods for handling HTTP routes and filters in the Netty server.
 * It includes methods for finding routes and filters, as well as matching request paths to route patterns.
 *
 * @author reftch
 * @version 1.0.0
 */
public class ServerHandlerUtils {

    private static final Pattern PARAM_PATTERN = Pattern.compile(":[^/]+");

    /**
     * Finds the appropriate route for the given HTTP request.
     *
     * @param request the FullHttpRequest object representing the HTTP request
     * @param routes the list of routes to search through
     * @return an Optional containing the matched Route, or an empty Optional if no match is found
     */
    public static Optional<Route> findRoute(FullHttpRequest request, List<Route> routes) {
        String uri = request.uri();
        String method = request.method().name();
        return routes.stream()
                .filter(r -> r.getMethod() != HttpMethod.BEFORE_FILTER && r.getMethod() != HttpMethod.AFTER_FILTER)
                .filter(r -> r.getMethod().name().equals(method) && matchPath(r.getPath(), uri))
                .findFirst();
    }

    /**
     * Finds the first filter of the specified type in the list of routes.
     *
     * @param filter the HttpMethod representing the type of filter to find
     * @param routes the list of routes to search through
     * @return an Optional containing the matched Route, or an empty Optional if no match is found
     */
    public static Optional<Route> findFilter(HttpMethod filter, List<Route> routes) {
        return routes.stream()
                .filter(r -> r.getMethod() == filter)
                .findFirst();
    }

    /**
     * Matches the given request path to the route pattern.
     *
     * @param pattern the route pattern to match against
     * @param path the request path to match
     * @return true if the request path matches the route pattern, false otherwise
     */
    public static boolean matchPath(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }
        Pattern compiledPattern = Pattern.compile(PARAM_PATTERN.matcher(pattern).replaceAll("([^/]+)"));
        Matcher matcher = compiledPattern.matcher(path);
        return matcher.matches();
    }
}