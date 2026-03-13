package io.jawisp.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Route;
import io.jawisp.http.Routes;
import io.jawisp.plugin.Plugin;
import io.jawisp.template.TemplateEngine;

/**
 * The Config class is used to configure the Jawisp application.
 * It allows setting various parameters such as the server port, context path,
 * and routes.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private int port;
    private String contextPath;
    private Optional<TemplateEngine> templateEngine = Optional.empty();
    private final List<String> staticResources;

    private final List<Route> routes = new ArrayList<>();

    /**
     * Default constructor for the Config class.
     */
    public Config() {
        this.port = 8080;
        this.contextPath = "/";
        this.staticResources = new ArrayList<>();
    }

    /**
     * Configures the Jawisp instance using the provided consumer.
     *
     * @param config a consumer to configure the Jawisp instance
     * @return the current Config instance
     */
    public Config configure(Consumer<Config> config) {
        config.accept(this);
        return this;
    }

    /**
     * Sets the server port.
     *
     * @param port the port number to set
     * @return the current Config instance
     */
    public Config port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the context path.
     *
     * @param contextPath the context path to set
     * @return the current Config instance
     */
    public Config contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    /**
     * Configures the routes using the provided consumer.
     *
     * @param routesConfig a consumer to configure the routes
     * @return the current Config instance
     */
    public Config routes(Consumer<Routes> routesConfig) {
        Routes routing = new Routes(contextPath);
        routesConfig.accept(routing);
        this.routes.addAll(routing.getRoutes());
        return this;
    }

    /**
     * Returns the list of routes associated with this object.
     *
     * @return a {@link List} containing the {@link Route} objects
     */
    public List<Route> routes() {
        return this.routes;
    }

    /**
     * Gets the server port.
     *
     * @return the server port
     */
    public int port() {
        return port;
    }

    /**
     * Gets the configured routes.
     *
     * @return a list of configured routes
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * Gets the context path.
     *
     * @return the context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Loads and uses the specified template engine plugin.
     *
     * @param pluginName the name of the plugin to use
     * @return the current Config instance
     * @throws IllegalArgumentException if the specified plugin is not found
     */
    public Config templateEngine(String pluginName) {
        TemplateEngine plugin = Plugin.create(pluginName);
        log.info("Plugins: use '{}' template rendering engine", pluginName);
        this.templateEngine = Optional.of(plugin);
        return this;
    }

    /**
     * Gets the configured template engine.
     *
     * @return the configured template engine or null
     */
    public TemplateEngine templateEngine() {
        return templateEngine.orElse(null);
    }

    /**
     * Adds a collection of static resource paths.
     *
     * @param paths the collection of paths of static resources
     * @return the current Config instance
     */
    public Config staticResources(Collection<String> paths) {
        staticResources.addAll(paths);
        return this;
    }

    /**
     * Adds multiple static resource paths.
     *
     * @param paths the variable number of paths of static resources
     * @return the current Config instance
     */
    public Config staticResources(String... paths) {
        for (String path : paths) {
            staticResources.add(path);
        }
        return this;
    }

    /**
     * Retrieves the list of static resource paths.
     *
     * @return a list of static resource paths
     */
    public List<String> staticResources() {
        return staticResources;
    }

}