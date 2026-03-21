package io.jawisp.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.jawisp.config.cors.CorsSettings;
import io.jawisp.config.cors.CorsSettingsBuilder;
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

    /**
     * The server port.
     */
    private int port;

    /**
     * The context path of the application.
     */
    private String contextPath;

    /**
     * The name of the configuration property file.
     */
    private String propertyFile;

    /**
     * The name of the template engine to be used.
     */
    private String templateEngineName;

    /**
     * A list of static resource paths.
     */
    private final List<String> staticResources = new ArrayList<>();

    /**
     * CORS settings.
     */
    private CorsSettings cors = CorsSettings.disabled();

    /**
     * A list of routes associated with this object.
     */
    private final List<Route> routes = new ArrayList<>();

    /**
     * Supplier to create a PropertyReader instance based on the current configuration.
     */
    private final Supplier<PropertyReader> reader = () -> PropertyReader.getInstance(propertyFile());

    /**
     * Default constructor for the Config class.
     */
    public Config() {
        this.port = 8080;
        this.contextPath = "/";
        this.propertyFile = "application.properties";
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
     * Gets the server port. If not explicitly set, it will be retrieved from the configuration file.
     *
     * @return the server port
     */
    public int port() {
        return reader.get()
                .get(PropertyReader.CONFIG_SERVER_PORT)
                .asInt()
                .orElse(port);
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
        this.templateEngineName = pluginName;
        return this;
    }

    /**
     * Gets the configured template engine.
     *
     * @return the configured template engine or null
     */
    public TemplateEngine templateEngine() {
        var engineName = reader.get()
                .get(PropertyReader.CONFIG_TEMPLATE_ENGINE)
                .asString()
                .orElse(templateEngineName);

        Optional<TemplateEngine> templateEngine = Optional.ofNullable(engineName)
                .map(Plugin::create);

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
     * Retrieves the list of static resource paths. If not explicitly set, it will be retrieved from the configuration file.
     *
     * @return a list of static resource paths
     */
    public List<String> staticResources() {
        var paths = reader.get()
                .get(PropertyReader.CONFIG_STATIC_RESOURCES)
                .asString()
                .orElse(null);

        if (paths != null) {
            staticResources.clear();
            staticResources.addAll(Arrays.asList(paths.split(",")));
        }

        return staticResources;
    }

    /**
     * Configures CORS settings using the provided consumer.
     *
     * @param corsConsumer a consumer to configure the CORS settings
     * @return the current Config instance
     *
     * @since 1.0.18
     */
    public Config cors(Consumer<CorsSettingsBuilder> corsConsumer) {
        CorsSettingsBuilder b = new CorsSettingsBuilder();
        corsConsumer.accept(b);
        this.cors = b.build();
        return this;
    }

    /**
     * Gets the CORS settings.
     *
     * @return the CORS settings
     *
     * @since 1.0.18
     */
    public CorsSettings cors() {
        return cors;
    }

    /**
     * Sets the name of the configuration property file.
     *
     * @param filename the name of the property file to be used for configuration
     * @return the current instance of {@link Config} to allow method chaining
     */
    public Config propertyFile(String filename) {
        this.propertyFile = filename;
        return this;
    }

    /**
     * Retrieves the name of the configuration property file.
     *
     * @return the name of the property file currently set
     */
    public String propertyFile() {
        return this.propertyFile;
    }
}