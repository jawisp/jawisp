package io.jawisp.http;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a route handler that maps HTTP requests to controller methods.
 * This class encapsulates the information needed to handle routing for a
 * specific HTTP method, path pattern, and controller method combination
 */
public class RouteHandler {
    /** The controller instance that contains the method to be invoked */
    private final Object controller;

    /**
     * The method to be invoked on the controller when a matching request is
     * received
     */
    private final Method method;

    /**
     * The HTTP method (GET, POST, PUT, DELETE, etc.) that this handler responds to
     */
    private final HttpMethod httpMethod;

    /** The path pattern that this handler matches against incoming requests */
    private final String path;

    /** List of parameter names extracted from the path pattern */
    private final List<String> pathParams;

    /** The media type that this handler produces (e.g., application/json) */
    private final MediaType produces;

    /**
     * Compiled regex pattern used to match incoming request paths against this
     * handler's path
     */
    private final Pattern pattern;

    /**
     * Constructs a new RouteHandler with the specified parameters.
     * 
     * @param controller The controller instance containing the method to be invoked
     * @param method     The method to be invoked on the controller when a matching
     *                   request is received
     * @param httpMethod The HTTP method that this handler responds to (GET, POST,
     *                   etc.)
     * @param path       The path pattern that this handler matches against incoming
     *                   requests
     * @param produces   The media type that this handler produces (e.g.,
     *                   application/json)
     */
    public RouteHandler(Object controller, Method method, HttpMethod httpMethod, String path, MediaType produces) {
        this.controller = controller;
        this.method = method;
        this.httpMethod = httpMethod;
        this.path = path;
        this.produces = produces;
        this.pathParams = new ArrayList<>();
        // Parse path for parameters and create proper regex pattern
        this.pattern = parsePathAndCreatePattern(path);
    }

    /**
     * Parses the path string to extract parameter names and creates a regex pattern
     * that can match against incoming request paths.
     * 
     * @param path The path pattern containing parameter placeholders like {id}
     * @return A compiled Pattern object that can be used to match request paths
     */
    private Pattern parsePathAndCreatePattern(String path) {
        // Find all parameter placeholders like {id}
        Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = paramPattern.matcher(path);

        // Use a Set to track unique parameter names
        java.util.Set<String> uniqueParams = new java.util.HashSet<>();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            uniqueParams.add(paramName);
        }

        // Convert back to list if needed (maintaining order of first occurrence)
        this.pathParams.clear();
        matcher = paramPattern.matcher(path);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (uniqueParams.remove(paramName)) { // Only add first occurrence
                this.pathParams.add(paramName);
            }
        }

        // Escape special regex characters except for curly braces {} so placeholders
        // remain intact
        var regex = path.replaceAll("([\\\\.$|()\\[\\]^+*?])", "\\\\$1");

        // Replace {param} with capturing groups
        for (var param : this.pathParams) {
            regex = regex.replace("{" + param + "}", "([^/]+)");
        }

        // Add anchors
        regex = "^" + regex + "$";

        return Pattern.compile(regex);
    }

    /**
     * Gets the controller instance that contains the method to be invoked.
     * 
     * @return The controller object
     */
    public Object getController() {
        return controller;
    }

    /**
     * Gets the method to be invoked on the controller when a matching request is
     * received.
     * 
     * @return The Method object representing the controller method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the HTTP method that this handler responds to.
     * 
     * @return The HttpMethod enum value (GET, POST, PUT, DELETE, etc.)
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Gets the path pattern that this handler matches against incoming requests.
     * 
     * @return The path pattern string
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the media type that this handler produces.
     * 
     * @return The MediaType enum value (e.g., application/json)
     */
    public MediaType getProduces() {
        return produces;
    }

    /**
     * Gets the compiled regex pattern used to match incoming request paths.
     * 
     * @return The compiled Pattern object
     */
    public Pattern getPattern() {
        return pattern;
    }

    public List<String> getPathParams() {
        return pathParams;
    }
}
