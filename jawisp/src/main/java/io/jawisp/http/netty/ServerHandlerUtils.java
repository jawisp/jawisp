package io.jawisp.http.netty;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jawisp.http.HttpMethod;
import io.jawisp.http.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public class ServerHandlerUtils {

    private static final Pattern PARAM_PATTERN = Pattern.compile(":[^/]+");

    public static Optional<Route> findRoute(FullHttpRequest request, List<Route> routes) {
        String uri = request.uri();
        String method = request.method().name();
        return routes.stream()
                .filter(r -> r.getMethod() != HttpMethod.BEFORE_FILTER && r.getMethod() != HttpMethod.AFTER_FILTER)
                .filter(r -> r.getMethod().name().equals(method) && matchPath(r.getPath(), uri))
                .findFirst();
    }

    public static Optional<Route> findFilter(HttpMethod filter, List<Route> routes) {
        return routes.stream()
                .filter(r -> r.getMethod() == filter)
                .findFirst();
    }

    public static boolean matchPath(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }
        Pattern compiledPattern = Pattern.compile(PARAM_PATTERN.matcher(pattern).replaceAll("([^/]+)"));
        Matcher matcher = compiledPattern.matcher(path);
        return matcher.matches();
    }
}
