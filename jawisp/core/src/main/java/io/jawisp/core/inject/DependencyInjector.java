package io.jawisp.core.inject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Route;
// import io.jawisp.core.annotation.Secured;
// import io.jawisp.core.annotation.Secured.SecurityRule;
import io.jawisp.http.HttpMethod;
import io.jawisp.http.RouteHandler;

public class DependencyInjector {
    private static final Logger logger = LoggerFactory.getLogger(DependencyInjector.class);
    private final List<ReflectionEntry> reflectionEntries;
    private final List<RouteHandler> routeHandlers = new ArrayList<>();

    public DependencyInjector() {
        ReflectionConfigParser parser = new ReflectionConfigParser();
        this.reflectionEntries = parser.getReflectionEntries();

        registerControllers();
    }

    private void registerControllers() {
        for (var entry : reflectionEntries) {
            try {
                Class<?> clazz = Class.forName(entry.name());
                if (clazz.isAnnotationPresent(Controller.class)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Registering controller: {}", clazz.getName());
                    }
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    // injectDependencies(controller, clazz);
                    registerRoutes(controller);
                }
            } catch (Exception e) {
                logger.error("Failed to instantiate controller: {}, {}", entry.name(), e);
            }
        }
    }

    // Method to register controller
    private void registerRoutes(Object controller) {
        Class<?> controllerClass = controller.getClass();
        String basePath = "";

        // Get base path from Controller annotation
        if (controllerClass.isAnnotationPresent(Controller.class)) {
            basePath = controllerClass.getAnnotation(Controller.class).basePath();
            if (basePath.isEmpty() || !basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
        }

        // Scan methods for Route annotations
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                Route route = method.getAnnotation(Route.class);
                HttpMethod httpMethod = HttpMethod.valueOf(route.method().toUpperCase());
                String path = route.path();

                // Combine base path and method path
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                String fullPath = basePath + path;
                logger.info("Registering route: {} {} -> {}.{}()",
                        httpMethod.name(), fullPath, controllerClass.getSimpleName(), method.getName());

                // Security rule to determine access control
                // boolean isAnonymous = true;
                // if (method.isAnnotationPresent(Secured.class)) {
                //     Secured secured = method.getAnnotation(Secured.class);
                //     if (secured.securityRule() == SecurityRule.IS_AUTHENTICATED) {
                //         isAnonymous = false;
                //     }
                // }

                // String view = "";
                // if (method.isAnnotationPresent(View.class)) {
                //     view = method.getAnnotation(View.class).value();
                // }

                routeHandlers.add(new RouteHandler(controller, method, httpMethod, fullPath));
            }
        }
    }

    public List<RouteHandler> getRoutes() {
        return routeHandlers;
    }

}
