package io.jawisp.template.thymeleaf;

import io.jawisp.config.Config;
import io.jawisp.template.TemplateEngine;
import io.jawisp.template.TemplateEnginePlugin;

/**
 * The ThymeleafPlugin class implements the TemplateEnginePlugin interface
 * and provides a method to create a ThymeleafTemplateEngine instance.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class ThymeleafPlugin implements TemplateEnginePlugin {

    /**
     * Constructs a new ThymeleafPlugin instance.
     */
    public ThymeleafPlugin() {
    }

    /**
     * Returns the name of the plugin.
     *
     * @return the name of the plugin
     */
    @Override
    public String getName() {
        return "thymeleaf";
    }

    /**
     * Creates a new ThymeleafTemplateEngine instance using the provided configuration.
     *
     * @param config the configuration to use
     * @return a new {@link TemplateEngine} instance
     */
    @Override
    public TemplateEngine createEngine(Config config) {
        return new ThymeleafTemplateEngine();
    }
}