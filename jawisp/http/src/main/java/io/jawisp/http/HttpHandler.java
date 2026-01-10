package io.jawisp.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;

/**
 * Handles HTTP requests by routing them to appropriate controllers based on method and path.
 * It uses registered route handlers to match incoming requests and invoke the corresponding controller methods.
 */
public class HttpHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    /**
     * List of route handlers that define how to process different HTTP methods and paths.
     */
    private final List<RouteHandler> routeHandlers;

    /**
     * ObjectMapper instance used for serializing response objects to JSON.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs an HttpHandler with a list of route handlers.
     *
     * @param routeHandlers List of RouteHandler instances defining routes and their associated logic.
     */
    public HttpHandler(List<RouteHandler> routeHandlers) {
        this.routeHandlers = routeHandlers;
    }

    /**
     * Handles an incoming HTTP request by finding the matching route handler,
     * invoking the appropriate controller method, and setting the response accordingly.
     *
     * @param req The HTTP request to handle.
     * @param res The HTTP response to populate.
     */
    @Override
    public void handle(Request req, Response res) {
        RouteHandler handler = findMatchingRoute(req.method, req.path);
        if (handler != null) {
            Map<String, Object> pathParams = new HashMap<>();
            Matcher matcher = handler.getPattern().matcher((req.path));

            if (matcher.matches()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} request {}", req.method, req.path);
                }

                try {
                    var result = callControllerMethod(handler, pathParams);
                    if (result != null) {
                        var produces = handler.getProduces();
                        res.contentType = produces.getMediaType();
                        switch (produces) {
                            case APPLICATION_JSON:
                                var content = mapper.writeValueAsString(result);
                                res.body = String.valueOf(content).getBytes();
                                break;
                            default:
                                res.body = String.valueOf(result).getBytes();
                                break;
                        }
                    }
                } catch (Exception e) {
                    // Handle errors
                    logger.debug("Request error: {}, {}, method: {}", e.getLocalizedMessage(), req.path, req.method);
                }
            }
        }
    }

    /**
     * Finds the first matching RouteHandler for a given HTTP method and path.
     *
     * @param method The HTTP method (e.g., GET, POST).
     * @param path The requested path.
     * @return A RouteHandler if a match is found, otherwise null.
     */
    private RouteHandler findMatchingRoute(String method, String path) {
        for (RouteHandler handler : routeHandlers) {
            if (handler.getHttpMethod().name().equals(method) && matchesPath(handler.getPath(), path)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Checks whether the request path matches the route pattern.
     * This implementation converts path parameters (e.g., {id}) into regex groups.
     *
     * @param routePattern The route pattern (with placeholders like {id}).
     * @param requestPath The actual path requested.
     * @return true if the path matches the pattern, false otherwise.
     */
    private boolean matchesPath(String routePattern, String requestPath) {
        // Create a simple regex pattern for matching
        Pattern pattern = Pattern.compile("^" +
                routePattern.replaceAll("\\{([^}]+)\\}", "([^/]+)") + "$");
        return pattern.matcher(requestPath).matches();
    }

    /**
     * Invokes the controller method associated with the route handler.
     *
     * @param handler The RouteHandler containing the method to invoke.
     * @param pathParams Map of extracted path parameters.
     * @return The result returned by the controller method.
     * @throws Exception If the method invocation fails.
     */
    private Object callControllerMethod(RouteHandler handler, Map<String, Object> pathParams) throws Exception {
        var method = handler.getMethod();
        Object[] params = new Object[method.getParameterCount()];

        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Map.class) {
                params[i] = pathParams;
            } else if (paramTypes[i] == String.class) {
                params[i] = "";
            }
        }

        method.setAccessible(true);
        return method.invoke(handler.getController(), params);
    }
}
