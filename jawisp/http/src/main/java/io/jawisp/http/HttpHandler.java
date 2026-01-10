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

public class HttpHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
    
    private final List<RouteHandler> routeHandlers;
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpHandler(List<RouteHandler> routeHandlers) {
        this.routeHandlers = routeHandlers;
    }

    @Override
    public void handle(Request req, Response res) {
        RouteHandler handler = findMatchingRoute(req.method, req.path);
        if (handler != null) {
            Map<String, Object> pathParams = new HashMap<>();
            Matcher matcher = handler.getPattern().matcher((req.path));

            if (matcher.matches()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} reguest {}", req.method, req.path);
                }
                // validate secure access
                // validateForSecureAccess(exchange, handler.isAnonymous);

                try {
                    var result = callControllerMethod(handler, pathParams);
                    if (result != null) {
                        var produces = handler.getProduces();
                        res.contentType =  produces.getMediaType();                
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

    private RouteHandler findMatchingRoute(String method, String path) {
        for (RouteHandler handler : routeHandlers) {
            if (handler.getHttpMethod().name().equals(method) && matchesPath(handler.getPath(), path)) {
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

    // private void validateForSecureAccess(HttpExchange exchange, boolean isAnonymous) {
    //     if (!isAnonymous) {
    //         var userSession = SessionManager.readUserSession(exchange);
    //         if (!userSession.isPresent()) {
    //             HttpUtils.redirectTo(exchange, "/login");
    //         }
    //     }
    // }

}
