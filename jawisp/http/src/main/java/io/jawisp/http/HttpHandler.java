package io.jawisp.http;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.annotation.Body;
import io.jawisp.http.annotation.Cookie;
import io.jawisp.http.annotation.Header;
import io.jawisp.http.annotation.PathVariable;
import io.jawisp.http.annotation.QueryValue;

/**
 * Handles HTTP requests by routing them to appropriate controllers based on
 * method and path.
 * It uses registered route handlers to match incoming requests and invoke the
 * corresponding controller methods.
 */
public class HttpHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    /**
     * List of route handlers that define how to process different HTTP methods and
     * paths.
     */
    private final List<RouteHandler> routeHandlers;

    /**
     * ObjectMapper instance used for serializing response objects to JSON.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs an HttpHandler with a list of route handlers.
     *
     * @param routeHandlers List of RouteHandler instances defining routes and their
     *                      associated logic.
     */
    public HttpHandler(List<RouteHandler> routeHandlers) {
        this.routeHandlers = routeHandlers;
    }

    /**
     * Handles an incoming HTTP request by finding the matching route handler,
     * invoking the appropriate controller method, and setting the response
     * accordingly.
     *
     * @param req The HTTP request to handle.
     * @param res The HTTP response to populate.
     */
    @Override
    public void handle(Request request, Response response) {
        RouteHandler handler = findMatchingRoute(request.getMethod(), request.getPath());
        if (handler != null) {
            Matcher matcher = handler.getPattern().matcher((request.getPath()));
            
            if (matcher.matches()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} request {}", request.getMethod(), request.getPath());
                }
                
                Map<String, Object> pathParams = getPathParams(handler, matcher);

                // System.out.println("Request " + req.body);
                try {
                    var result = callControllerMethod(handler, pathParams, request);
                    if (result != null) {
                        var produces = handler.getProduces();
                        response.setContentType(produces.getMediaType());
                        switch (produces) {
                            case APPLICATION_JSON:
                                var content = mapper.writeValueAsString(result);
                                response.setBody(String.valueOf(content).getBytes());
                                break;
                            default:
                                response.setBody(String.valueOf(result).getBytes());
                                break;
                        }
                    }
                } catch (Exception e) {
                    // Handle errors
                    logger.debug("Request error: {}, {}, method: {}", e.getMessage(), request.getPath(), request.getMethod());
                }
            }
        }
    }

    private Map<String, Object> getPathParams(RouteHandler handler, Matcher matcher) {
        // Extract parameter values
        Map<String, Object> pathParams = new HashMap<>();
        for (int i = 0; i < handler.getPathParams().size(); i++) {
            String paramName = handler.getPathParams().get(i);
            String paramValue = matcher.group(i + 1); // Groups start at 1
            pathParams.put(paramName, paramValue);
        }
        return pathParams;
    }

    /**
     * Finds the first matching RouteHandler for a given HTTP method and path.
     *
     * @param method The HTTP method (e.g., GET, POST).
     * @param path   The requested path.
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
     * @param requestPath  The actual path requested.
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
     * @param handler    The RouteHandler containing the method to invoke.
     * @param pathParams Map of extracted path parameters.
     * @return The result returned by the controller method.
     * @throws Exception If the method invocation fails.
     */
    private Object callControllerMethod(RouteHandler handler, Map<String, Object> pathParams, Request request) throws Exception {
    // private Object callControllerMethod(RouteHandler handler, Request request) throws Exception {
        var method = handler.getMethod();
        Object[] params = new Object[method.getParameterCount()];

        Class<?>[] paramTypes = method.getParameterTypes();
        Parameter[] methodParams = method.getParameters();

        for (int i = 0; i < paramTypes.length; i++) {
            var param = methodParams[i];

            // Priority order: Body > Header > Cookie > QueryValue > PathVariable
            var bodyAnno = param.getAnnotation(Body.class);
            if (bodyAnno != null) {
                params[i] = parseBody(request.getBody(), paramTypes[i]);
                continue;
            }

            var headerAnno = param.getAnnotation(Header.class);
            if (headerAnno != null) {
                params[i] = getHeaderValue(request.getHeaders(), headerAnno, paramTypes[i]);
                continue;
            }

            var cookieAnno = param.getAnnotation(Cookie.class);
            if (cookieAnno != null) {
                params[i] = getCookieValue(request.getHeaders(), cookieAnno, paramTypes[i]);
                continue;
            }

            // PathVariable / QueryValue fallback
            String paramName = extractParamNameSafe(methodParams, handler, i);
            Map<String, String> sourceMap = extractParamSourceSafe(methodParams, i, pathParams, request.getQueryParams());

            switch (paramTypes[i].getName()) {
                case "java.util.Map" -> params[i] = pathParams;
                case "java.lang.String" -> params[i] = getStringValue(sourceMap, paramName, param);
                case "java.lang.Integer", "int" -> params[i] = getIntegerValue(sourceMap, paramName, param);
                case "java.lang.Long", "long" -> params[i] = getLongValue(sourceMap, paramName, param);
                case "java.lang.Boolean", "boolean" -> params[i] = getBooleanValue(sourceMap, paramName, param);
                case "java.lang.Double", "double" -> params[i] = getDoubleValue(sourceMap, paramName, param);
                default -> throw new IllegalArgumentException(
                        "Unsupported parameter type: " + paramTypes[i].getSimpleName() +
                                " at index " + i + " in " + method.getName());
            }
        }

        // System.out.println("Params: " + Arrays.toString(params) + " " + method);
        method.setAccessible(true);
        return method.invoke(handler.getController(), params);
    }

    // Helper methods
    private String extractParamNameSafe(Parameter[] methodParams, RouteHandler handler, int index) {
        if (index >= methodParams.length) {
            var pathParamNames = handler.getPathParams();
            return index < pathParamNames.size() ? pathParamNames.get(index) : "param" + index;
        }

        var param = methodParams[index];
        var pathVariable = param.getAnnotation(PathVariable.class);
        if (pathVariable != null && !pathVariable.value().isEmpty()) {
            return pathVariable.value();
        }

        var queryValue = param.getAnnotation(QueryValue.class);
        if (queryValue != null) {
            return queryValue.value();
        }

        var pathParamNames = handler.getPathParams();
        return index < pathParamNames.size() ? pathParamNames.get(index) : "param" + index;
    }

    private Map<String, String> extractParamSourceSafe(Parameter[] methodParams, int index,
            Map<String, Object> pathParams, Map<String, String> queryParams) {
        if (index >= methodParams.length) {
            return convertToStringMap(pathParams);
        }

        var param = methodParams[index];
        return param.getAnnotation(QueryValue.class) != null ? queryParams : convertToStringMap(pathParams);
    }

    private Map<String, String> convertToStringMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    private Object parseBody(String requestBody, Class<?> paramType) throws Exception {
        if (requestBody == null || requestBody.trim().isEmpty())
            return null;
        return convertValue(requestBody, paramType);
    }

    private Object getHeaderValue(Map<String, String> headers, Header headerAnno, Class<?> paramType) {
        String value = headers.get(headerAnno.value());
        if (value == null) {
            if (headerAnno.required()) {
                throw new IllegalArgumentException("Missing required header: " + headerAnno.value());
            }
            value = headerAnno.defaultValue();
        }
        return convertValue(value, paramType);
    }

    private Object getCookieValue(Map<String, String> headers, Cookie cookieAnno, Class<?> paramType) {
        String cookieHeader = headers.get("Cookie");
        if (cookieHeader == null) {
            if (cookieAnno.required()) {
                throw new IllegalArgumentException("Missing required cookie: " + cookieAnno.value());
            }
            return convertValue(cookieAnno.defaultValue(), paramType);
        }

        String value = parseCookieValue(cookieHeader, cookieAnno.value());
        if (value == null) {
            if (cookieAnno.required()) {
                throw new IllegalArgumentException("Missing required cookie: " + cookieAnno.value());
            }
            value = cookieAnno.defaultValue();
        }
        return convertValue(value, paramType);
    }

    private String parseCookieValue(String cookieHeader, String cookieName) {
        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(part -> part.startsWith(cookieName + "="))
                .map(part -> part.substring(cookieName.length() + 1))
                .findFirst()
                .orElse(null);
    }

    private Object convertValue(String value, Class<?> paramType) {
        if (value == null || value.trim().isEmpty())
            return null;

        return switch (paramType.getName()) {
            case "java.lang.String" -> value;
            case "java.lang.Integer", "int" -> Integer.valueOf(value.trim());
            case "java.lang.Long", "long" -> Long.valueOf(value.trim());
            case "java.lang.Boolean", "boolean" -> Boolean.valueOf(value.trim());
            case "java.lang.Double", "double" -> Double.valueOf(value.trim());
            default -> throw new IllegalArgumentException("Unsupported type: " + paramType.getSimpleName());
        };
    }

    // Type-specific handlers with annotation support
    private String getStringValue(Map<String, String> sourceMap, String paramName, Parameter param) {
        var queryValue = param.getAnnotation(QueryValue.class);
        if (queryValue != null) {
            String value = sourceMap.get(paramName);
            if (value == null) {
                if (queryValue.required())
                    throw new IllegalArgumentException("Missing required query param: " + paramName);
                return queryValue.defaultValue();
            }
            return value;
        }
        return sourceMap.getOrDefault(paramName, "");
    }

    private Integer getIntegerValue(Map<String, String> sourceMap, String paramName, Parameter param) {
        var queryValue = param.getAnnotation(QueryValue.class);
        if (queryValue != null) {
            String value = sourceMap.get(paramName);
            if (value == null) {
                if (queryValue.required())
                    throw new IllegalArgumentException("Missing required query param: " + paramName);
                return queryValue.defaultValue().isEmpty() ? 0 : Integer.parseInt(queryValue.defaultValue());
            }
            return Integer.valueOf(value);
        }
        var value = sourceMap.get(paramName);
        return value != null ? Integer.valueOf(value) : 0;
    }

    private Long getLongValue(Map<String, String> sourceMap, String paramName, Parameter param) {
        var queryValue = param.getAnnotation(QueryValue.class);
        if (queryValue != null) {
            String value = sourceMap.get(paramName);
            if (value == null) {
                if (queryValue.required())
                    throw new IllegalArgumentException("Missing required query param: " + paramName);
                return queryValue.defaultValue().isEmpty() ? 0L : Long.parseLong(queryValue.defaultValue());
            }
            return Long.valueOf(value);
        }
        var value = sourceMap.get(paramName);
        return value != null ? Long.valueOf(value) : 0L;
    }

    private Boolean getBooleanValue(Map<String, String> sourceMap, String paramName, Parameter param) {
        var queryValue = param.getAnnotation(QueryValue.class);
        if (queryValue != null) {
            String value = sourceMap.get(paramName);
            if (value == null) {
                if (queryValue.required())
                    throw new IllegalArgumentException("Missing required query param: " + paramName);
                return Boolean.parseBoolean(queryValue.defaultValue());
            }
            return Boolean.valueOf(value);
        }
        var value = sourceMap.get(paramName);
        return value != null ? Boolean.valueOf(value) : false;
    }

    private Double getDoubleValue(Map<String, String> sourceMap, String paramName, Parameter param) {
        var queryValue = param.getAnnotation(QueryValue.class);
        if (queryValue != null) {
            String value = sourceMap.get(paramName);
            if (value == null) {
                if (queryValue.required())
                    throw new IllegalArgumentException("Missing required query param: " + paramName);
                return queryValue.defaultValue().isEmpty() ? 0.0 : Double.parseDouble(queryValue.defaultValue());
            }
            return Double.valueOf(value);
        }
        var value = sourceMap.get(paramName);
        return value != null ? Double.valueOf(value) : 0.0;
    }
}
