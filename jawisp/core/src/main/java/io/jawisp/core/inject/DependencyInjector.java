package io.jawisp.core.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Inject;
import io.jawisp.core.annotation.Service;
import io.jawisp.http.HttpMethod;
import io.jawisp.http.MediaType;
import io.jawisp.http.annotation.Produces;
import io.jawisp.http.annotation.Route;
import io.jawisp.http.handler.RouteHandler;

public class DependencyInjector {
    private static final Logger logger = LoggerFactory.getLogger(DependencyInjector.class);

    private final Map<String, Object> services = new HashMap<>();
    private final List<ReflectionEntry> reflectionEntries;
    private final List<RouteHandler> routeHandlers = new ArrayList<>();

    public DependencyInjector() {
        ReflectionConfigParser parser = new ReflectionConfigParser();
        this.reflectionEntries = parser.getReflectionEntries();

        // Phase 1: Instantiate Services
        instantiateServices();
        // Phase 2: Inject dependencies into Services
        injectServiceDependencies();
        // Phase 3: Instantiate Controllers
        registerControllers();
    }

    private void instantiateServices() {
        for (var entry : reflectionEntries) {
            try {
                Class<?> clazz = Class.forName(entry.name());
                if (clazz.isAnnotationPresent(Service.class)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Registering service: {}", clazz.getName());
                    }
                    services.put(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
                }
            } catch (Exception e) {
                logger.error("Failed to instantiate service: {}, {}", entry.name(), e);
            }
        }
    }

    private void injectServiceDependencies() {
        for (Object service : services.values()) {
            try {
                injectDependencies(service, service.getClass());
            } catch (Exception e) {
                logger.error("Failed to inject dependencies for service: {0}, {0}", service.getClass().getName(), e);
            }
        }
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
                    injectDependencies(controller, clazz);
                    registerRoutes(controller);
                }
            } catch (Exception e) {
                logger.error("Failed to instantiate controller: {}, {}", entry.name(), e);
            }
        }
    }

    private void injectDependencies(Object instance, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                var fieldName = field.getType().getName();
                Object serviceInstance = services.get(fieldName);
                if (serviceInstance != null) {
                    field.set(instance, serviceInstance);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Injected service: {}", fieldName);
                    }
                }
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
                logger.info("{} {} -> {}.{}()",
                        httpMethod.name(), fullPath, controllerClass.getSimpleName(), method.getName());

                // Security rule to determine access control
                // var rule = SecurityRule.IS_ANONYMOUS;
                // if (method.isAnnotationPresent(Secured.class)) {
                //     rule = method.getAnnotation(Secured.class).securityRule();
                // }

                MediaType produces = MediaType.TEXT_PLAIN;
                if (method.isAnnotationPresent(Produces.class)) {
                    produces = method.getAnnotation(Produces.class).value();
                }

                routeHandlers.add(new RouteHandler(controller, method, httpMethod, fullPath, produces));
            }
        }
    }

    public List<RouteHandler> getRoutes() {
        return routeHandlers;
    }

}
