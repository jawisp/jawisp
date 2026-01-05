package io.jawisp.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;

public class HttpHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
    private final List<RouteHandler> routeHandlers;

    public HttpHandler(List<RouteHandler> routeHandlers) {
        this.routeHandlers = routeHandlers;
    }

    @Override
    public void handle(Request req, Response res) {
        RouteHandler handler = findMatchingRoute(req.method, req.path);
        if (handler != null) {
            Map<String, Object> pathParams = new HashMap<>();
            Matcher matcher = handler.pattern.matcher((req.path));

            if (matcher.matches()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} reguest {}", req.method, req.path);
                }
                // validate secure access
                // validateForSecureAccess(exchange, handler.isAnonymous);

                try {
                    var result = callControllerMethod(handler, pathParams);
                    res.body = String.valueOf(result).getBytes();
                } catch (Exception e) {
                    // Handle errors
                    logger.debug("Request error: {}, {}, method: {}", e.getLocalizedMessage(), req.path, req.method);
                }
            }
        }
    }

    private RouteHandler findMatchingRoute(String method, String path) {
        for (RouteHandler handler : routeHandlers) {
            if (handler.httpMethod.name().equals(method) && matchesPath(handler.path, path)) {
                return handler;
            }
        }
        return null;
    }

    // Check if path matches pattern (simplified version)
    private boolean matchesPath(String routePattern, String requestPath) {
        // Create a simple regex pattern for matching
        Pattern pattern = Pattern.compile("^" +
                routePattern.replaceAll("\\{([^}]+)\\}", "([^/]+)") + "$");
        return pattern.matcher(requestPath).matches();
    }

    private Object callControllerMethod(RouteHandler handler, Map<String, Object> pathParams) throws Exception {
        Object[] params = new Object[handler.method.getParameterCount()];

        Class<?>[] paramTypes = handler.method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Map.class) {
                params[i] = pathParams;
            } else if (paramTypes[i] == String.class) {
                params[i] = "";
            }
        }

        handler.method.setAccessible(true);
        return handler.method.invoke(handler.controller, params);
    }

    // private void validateForSecureAccess(HttpExchange exchange, boolean isAnonymous) {
    //     if (!isAnonymous) {
    //         var userSession = SessionManager.readUserSession(exchange);
    //         if (!userSession.isPresent()) {
    //             HttpUtils.redirectTo(exchange, "/login");
    //         }
    //     }
    // }

}
