package io.jawisp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.http.Routes;
import io.jawisp.plugin.template.PluginLoader;
import io.jawisp.plugin.template.TemplateEngine;
import io.jawisp.plugin.template.TemplateEnginePlugin;
import io.jawisp.http.Route;

/**
 * The Config class is used to configure the Jawisp application.
 * It allows setting various parameters such as the server port, context path,
 * and routes.
 *
 * @author reftch
 * @version 1.0.5
 */
public class Config {

    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

    private int port;
    private String contextPath;
    private Optional<TemplateEngine> templateEngine = Optional.empty();

    private final List<Route> routes = new ArrayList<>();

    /**
     * Default constructor for the Config class.
     */
    public Config() {
        this.port = 8080;
        this.contextPath = "/";
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
     * Gets the server port.
     *
     * @return the server port
     */
    public int getPort() {
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
    public Config usePlugin(String pluginName) {
        List<TemplateEnginePlugin> plugins = PluginLoader.loadAll();
        log.info("Found {} plugins:", plugins.size());
        plugins.forEach(p -> log.info("  - {}", p.getName()));

        TemplateEnginePlugin plugin = plugins.stream()
                .filter(p -> p.getName().equals(pluginName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plugin '" + pluginName + "' not found"));

        this.templateEngine = Optional.of(plugin.createEngine(this));
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
}