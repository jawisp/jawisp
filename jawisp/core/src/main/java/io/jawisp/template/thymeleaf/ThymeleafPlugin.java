package io.jawisp.template.thymeleaf;

import io.jawisp.plugin.PluginFactory;
import io.jawisp.template.TemplateEngine;

/**
 * The ThymeleafPlugin class implements the TemplateEnginePlugin interface
 * and provides a method to create a ThymeleafTemplateEngine instance.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class ThymeleafPlugin implements PluginFactory {

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
    public TemplateEngine create() {
        return new ThymeleafTemplateEngine();
    }
}