package io.jawisp.http;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Routes class manages a collection of HTTP routes within the application.
 * It provides methods to add routes for various HTTP methods and to retrieve
 * the list of routes.
 * The class also ensures that paths are cleaned and properly formatted.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class Routes {

    private final String contextPath;
    private final List<Route> routes = new ArrayList<>();

    /**
     * Constructs a new Routes instance with the specified context path.
     * The context path is cleaned to remove leading and trailing slashes.
     *
     * @param contextPath the context path for the routes
     */
    public Routes(String contextPath) {
        // FORCE empty to be truly empty
        this.contextPath = (contextPath == null || contextPath.trim().isEmpty()) ? ""
                : contextPath.trim().replaceAll("^/+", "/").replaceAll("/+$", "");
    }

    /**
     * Gets the list of all configured routes.
     *
     * @return a list of Route objects
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * Adds a new route for the GET method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes get(String path, Handler handler) {
        routes.add(new Route(HttpMethod.GET, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the POST method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes post(String path, Handler handler) {
        routes.add(new Route(HttpMethod.POST, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the PUT method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes put(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PUT, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the PATCH method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes patch(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PATCH, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the DELETE method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes delete(String path, Handler handler) {
        routes.add(new Route(HttpMethod.DELETE, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the HEAD method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes head(String path, Handler handler) {
        routes.add(new Route(HttpMethod.HEAD, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the OPTIONS method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes options(String path, Handler handler) {
        routes.add(new Route(HttpMethod.OPTIONS, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the TRACE method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes trace(String path, Handler handler) {
        routes.add(new Route(HttpMethod.TRACE, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for the CONNECT method.
     *
     * @param path    the path pattern for the route
     * @param handler the handler to process requests to this route
     * @return the current Routes instance
     */
    public Routes connect(String path, Handler handler) {
        routes.add(new Route(HttpMethod.CONNECT, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for a before filter.
     *
     * @param path    the path pattern for the filter
     * @param handler the handler to process requests before the route
     * @return the current Routes instance
     */
    public Routes before(String path, Handler handler) {
        routes.add(new Route(HttpMethod.BEFORE_FILTER, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route for an after filter.
     *
     * @param path    the path pattern for the filter
     * @param handler the handler to process requests after the route
     * @return the current Routes instance
     */
    public Routes after(String path, Handler handler) {
        routes.add(new Route(HttpMethod.AFTER_FILTER, forceCleanPath(path), handler));
        return this;
    }

    /**
     * Adds a new route to handle error responses.
     *
     * @param code    the HTTP status code for which this error handler is
     *                registered
     * @param handler the handler to process requests when the specified error code
     *                is encountered
     * @return the current Routes instance
     */
    public Routes error(int code, Handler handler) {
        routes.add(new Route(HttpMethod.ERROR, "/", handler, code));
        return this;
    }

    /**
     * Adds nested routes under a specified path prefix.
     * All child routes will have the specified prefix automatically prepended to
     * their paths.
     *
     * @param pathPrefix   the path prefix to be added to all nested routes
     * @param nestedRoutes a consumer that configures the nested routes
     * @return the current {@link Routes} instance with the nested routes added
     */
    public Routes path(String pathPrefix, Consumer<Routes> nestedRoutes) {
        // Clean the prefix
        String cleanPrefix = forceCleanPath(pathPrefix);

        // Create nested routes instance with prefix
        Routes nested = new Routes(cleanPrefix);

        // Execute nested configuration
        nestedRoutes.accept(nested);

        // Merge all nested routes into current collection
        this.routes.addAll(nested.getRoutes());

        return this;
    }

    /**
     * Cleans and formats the given path.
     * Ensures that the path has no leading or trailing slashes and handles multiple
     * slashes.
     *
     * @param path the original path
     * @return the cleaned and formatted path
     */
    private String forceCleanPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "/";
        }

        // Remove ALL multiple slashes
        String cleanPath = path.replaceAll("/+", "/");

        // If no context path, return clean path directly
        if (this.contextPath.isEmpty()) {
            return cleanPath;
        }
        if (cleanPath.equals("/")) {
            return this.contextPath;
        }

        // Context path exists - strip leading slash from route path
        String route = cleanPath.startsWith("/") ? cleanPath.substring(1) : cleanPath;
        String prefix = this.contextPath;

        return prefix + "/" + route;
    }

}