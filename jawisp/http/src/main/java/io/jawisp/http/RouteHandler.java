package io.jawisp.http;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteHandler {
    private final Object controller;
    private final Method method;
    private final HttpMethod httpMethod;
    private final String path;
    private final List<String> pathParams;
    private final MediaType produces;
    Pattern pattern;

    public RouteHandler(Object controller, Method method, HttpMethod httpMethod, String path, MediaType produces) {
        this.controller = controller;
        this.method = method;
        this.httpMethod = httpMethod;
        this.path = path;
        this.produces = produces;
        this.pathParams = new ArrayList<>();

        // Parse path for parameters and create proper regex pattern
        parsePathAndCreatePattern(path);
    }

    private void parsePathAndCreatePattern(String path) {
        // Find all parameter placeholders like {id}
        Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = paramPattern.matcher(path);

        while (matcher.find()) {
            this.pathParams.add(matcher.group(1));
        }


        // Escape special regex characters except for curly braces {} so placeholders remain intact
        String regex = path.replaceAll("([\\\\.$|()\\[\\]^+*?])", "\\\\$1");

        // Replace {param} with capturing groups
        for (String param : this.pathParams) {
            regex = regex.replace("{" + param + "}", "([^/]+)");
        }

        // Add anchors
        regex = "^" + regex + "$";

        this.pattern = Pattern.compile(regex);
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public MediaType getProduces() {
        return produces;
    }
}