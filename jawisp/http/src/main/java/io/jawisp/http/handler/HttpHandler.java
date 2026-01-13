package io.jawisp.http.handler;

import java.lang.reflect.Method;
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

import io.jawisp.http.Server.Request;
import io.jawisp.http.Server.Response;
import io.jawisp.http.annotation.Body;
import io.jawisp.http.annotation.Cookie;
import io.jawisp.http.annotation.Header;
import io.jawisp.http.annotation.PathVariable;
import io.jawisp.http.annotation.QueryValue;

public class HttpHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private final List<RouteHandler> routeHandlers;

    // ThreadLocal builder pool - zero allocation per-request after first use
    private static final ThreadLocal<BuilderPool> BUILDER_POOL = ThreadLocal.withInitial(BuilderPool::new);

    public HttpHandler(List<RouteHandler> routeHandlers) {
        this.routeHandlers = routeHandlers;
    }

    @Override
    public void handle(Request request, Response response) {
        BuilderPool pool = BUILDER_POOL.get();
        RouteMatch match = findRouteMatch(request);

        if (match.isNotFound()) {
            pool.notFound.reset(response, request).execute();
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} {}", request.getMethod(), request.getPath());
        }

        try {
            Object result = executeController(match.handler(), match.pathParams(), request);
            pool.success
                    .reset(response)
                    .result(result)
                    .mediaType(match.handler().getProduces())
                    .execute();
        } catch (Exception e) {
            pool.error
                    .reset(response, request)
                    .exception(e)
                    .execute();
        }
    }

    private static class BuilderPool {
        final SuccessResponseBuilder success = new SuccessResponseBuilder();
        final ErrorResponseBuilder error = new ErrorResponseBuilder();
        final NotFoundResponseBuilder notFound = new NotFoundResponseBuilder();
    }

    // Existing private methods remain unchanged until handleSuccess/handleError...

    private RouteMatch findRouteMatch(Request request) {
        RouteHandler handler = findMatchingRoute(request.getMethod(), request.getPath());
        if (handler == null) {
            return RouteMatch.notFound();
        }

        Matcher matcher = handler.getPattern().matcher(request.getPath());
        if (!matcher.matches()) {
            return RouteMatch.notFound();
        }

        return new RouteMatch(handler, extractPathParams(handler, matcher));
    }

    private RouteHandler findMatchingRoute(String method, String path) {
        return routeHandlers.stream()
                .filter(h -> h.getHttpMethod().name().equals(method))
                .filter(h -> matchesPath(h.getPath(), path))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesPath(String routePattern, String requestPath) {
        Pattern pattern = Pattern.compile("^" +
                routePattern.replaceAll("\\{([^}]+)\\}", "([^/]+)") + "$");
        return pattern.matcher(requestPath).matches();
    }

    private Map<String, Object> extractPathParams(RouteHandler handler, Matcher matcher) {
        Map<String, Object> pathParams = new HashMap<>();
        for (int i = 0; i < handler.getPathParams().size(); i++) {
            String paramName = handler.getPathParams().get(i);
            String paramValue = matcher.group(i + 1);
            pathParams.put(paramName, paramValue);
        }
        return pathParams;
    }

    private Object executeController(RouteHandler handler, Map<String, Object> pathParams, Request request)
            throws Exception {
        var method = handler.getMethod();
        Object[] params = resolveMethodParameters(method, pathParams, request);
        method.setAccessible(true);
        return method.invoke(handler.getController(), params);
    }

    private Object[] resolveMethodParameters(Method method, Map<String, Object> pathParams, Request request) {
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            params[i] = resolveParameter(parameters[i], pathParams, request, i);
        }
        return params;
    }

    private Object resolveParameter(Parameter param, Map<String, Object> pathParams, Request request, int index) {
        if (param.isAnnotationPresent(Body.class)) {
            return parseBody(request.getBody(), param.getType());
        }

        if (param.isAnnotationPresent(Header.class)) {
            return getHeaderValue(request.getHeaders(), param.getAnnotation(Header.class), param.getType());
        }

        if (param.isAnnotationPresent(Cookie.class)) {
            return getCookieValue(request.getHeaders(), param.getAnnotation(Cookie.class), param.getType());
        }

        String paramName = extractParamName(param, pathParams, index);
        Map<String, String> source = param.isAnnotationPresent(QueryValue.class) ? request.getQueryParams()
                : toStringMap(pathParams);

        return resolveSimpleType(param.getType(), source, paramName, param);
    }

    private String extractParamName(Parameter param, Map<String, Object> pathParams, int index) {
        var pathVar = param.getAnnotation(PathVariable.class);
        if (pathVar != null && !pathVar.value().isEmpty()) {
            return pathVar.value();
        }

        var query = param.getAnnotation(QueryValue.class);
        if (query != null && !query.value().isEmpty()) {
            return query.value();
        }

        return pathParams.keySet().stream().findFirst().orElse("param" + index);
    }

    private Object resolveSimpleType(Class<?> type, Map<String, String> source,
            String paramName, Parameter param) {
        String value = source.get(paramName);

        var queryAnno = param.getAnnotation(QueryValue.class);
        if (queryAnno != null) {
            if (value == null) {
                if (queryAnno.required()) {
                    throw new IllegalArgumentException("Missing required query param: " + paramName);
                }
                return parseDefaultValue(queryAnno.defaultValue(), type);
            }
        }

        return convertValue(value, type);
    }

    // Private helpers methods

    private Object parseBody(String body, Class<?> type) {
        if (body == null || body.trim().isEmpty())
            return null;
        return convertValue(body, type);
    }

    private Object getHeaderValue(Map<String, String> headers, Header anno, Class<?> type) {
        String value = headers.get(anno.value());
        if (value == null) {
            if (anno.required()) {
                throw new IllegalArgumentException("Missing required header: " + anno.value());
            }
            return parseDefaultValue(anno.defaultValue(), type);
        }
        return convertValue(value, type);
    }

    private Object getCookieValue(Map<String, String> headers, Cookie anno, Class<?> type) {
        String cookieHeader = headers.get("Cookie");
        if (cookieHeader == null) {
            if (anno.required()) {
                throw new IllegalArgumentException("Missing required cookie: " + anno.value());
            }
            return parseDefaultValue(anno.defaultValue(), type);
        }

        String value = parseCookieValue(cookieHeader, anno.value());
        if (value == null) {
            if (anno.required()) {
                throw new IllegalArgumentException("Missing required cookie: " + anno.value());
            }
            return parseDefaultValue(anno.defaultValue(), type);
        }
        return convertValue(value, type);
    }

    private String parseCookieValue(String cookieHeader, String cookieName) {
        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(part -> part.startsWith(cookieName + "="))
                .map(part -> part.substring(cookieName.length() + 1))
                .findFirst().orElse(null);
    }

    private Object parseDefaultValue(String defaultValue, Class<?> type) {
        return defaultValue.isEmpty() ? getTypeDefault(type) : convertValue(defaultValue, type);
    }

    private Object getTypeDefault(Class<?> type) {
        if (type == int.class || type == Integer.class)
            return 0;
        if (type == long.class || type == Long.class)
            return 0L;
        if (type == boolean.class || type == Boolean.class)
            return false;
        if (type == double.class || type == Double.class)
            return 0.0;
        return null;
    }

    private Object convertValue(String value, Class<?> type) {
        if (value == null || value.trim().isEmpty()) {
            return getTypeDefault(type);
        }

        return switch (type.getName()) {
            case "java.lang.String" -> value;
            case "java.lang.Integer", "int" -> Integer.valueOf(value.trim());
            case "java.lang.Long", "long" -> Long.valueOf(value.trim());
            case "java.lang.Boolean", "boolean" -> Boolean.valueOf(value.trim());
            case "java.lang.Double", "double" -> Double.valueOf(value.trim());
            default -> throw new IllegalArgumentException("Unsupported type: " + type.getSimpleName());
        };
    }

    private Map<String, String> toStringMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

}
