package io.jawisp.http;

/**
 * The Route class represents a single HTTP route within the application.
 * It contains the HTTP method, the path pattern, and the handler that processes requests to this route.
 *
 * @author reftch
 * @version 1.0.5
 */
public class Route {

    private final HttpMethod method;
    private final String path;
    private final Handler handler;

    /**
     * Constructs a new Route instance with the specified HTTP method, path, and handler.
     *
     * @param method  the HTTP method (GET, POST, etc.)
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     */
    public Route(HttpMethod method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }

    /**
     * Gets the HTTP method associated with this route.
     *
     * @return the HTTP method
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Gets the path pattern associated with this route.
     *
     * @return the path pattern
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the handler associated with this route.
     *
     * @return the handler
     */
    public Handler getHandler() {
        return handler;
    }
}