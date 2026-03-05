package io.jawisp.plugin.template;

import io.jawisp.core.Config;

/**
 * The TemplateEnginePlugin interface defines methods for template engine plugins.
 * Implementing classes should provide the logic to create a template engine
 * and check if they support a specific file extension.
 *
 * @author reftch
 * @version 1.0.4
 */
public interface TemplateEnginePlugin {

    /**
     * Returns the name of the plugin.
     *
     * @return the name of the plugin
     */
    String getName();

    /**
     * Creates a new template engine instance using the provided configuration.
     *
     * @param config the configuration to use
     * @return a new {@link TemplateEngine} instance
     */
    TemplateEngine createEngine(Config config);

    /**
     * Checks if the plugin supports the specified file extension.
     *
     * @param extension the file extension to check
     * @return true if the plugin supports the extension, false otherwise
     */
    default boolean supportsExtension(String extension) {
        return getName().equals(extension.replace(".", ""));
    }
}