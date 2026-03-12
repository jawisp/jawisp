package io.jawisp.http;

/**
 * The Route class represents a single HTTP route within the application.
 * It contains the HTTP method, the path pattern, and the handler that processes
 * requests to this route.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class Route {

    private final HttpMethod method;
    private final String path;
    private final Handler handler;
    private int status;

    /**
     * Constructs a new Route instance with the specified HTTP method, path, and
     * handler.
     *
     * @param method  the HTTP method (GET, POST, etc.)
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     */
    public Route(HttpMethod method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.status = 200;
    }

    /**
     * Constructs a new Route instance with the specified HTTP method, path,
     * handler,
     * and status code.
     *
     * @param method  the HTTP method (GET, POST, etc.)
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @param status  the HTTP status code to be used for this route
     */
    public Route(HttpMethod method, String path, Handler handler, int status) {
        this(method, path, handler);
        this.status = status;
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

    /**
     * Gets the HTTP status code associated with this route.
     *
     * @return the HTTP status code
     */
    public int status() {
        return status;
    }
}