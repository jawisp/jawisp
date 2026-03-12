package io.jawisp.template.pebble;

import io.jawisp.plugin.PluginFactory;
import io.jawisp.template.TemplateEngine;

/**
 * The PebblePlugin class implements the TemplateEnginePlugin interface
 * and provides a method to create a PebbleTemplateEngine instance.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class PebblePlugin implements PluginFactory<TemplateEngine> {

    /**
     * Constructs a new PebblePlugin instance.
     */
    public PebblePlugin() {
    }

    /**
     * Returns the name of the plugin.
     *
     * @return the name of the plugin
     */
    @Override
    public String getName() {
        return "pebble";
    }

    /**
     * Creates a new PebbleTemplateEngine instance using the provided configuration.
     *
     * @return a new {@link TemplateEngine} instance
     */
    @Override
    public TemplateEngine create() {
        return new PebbleTemplateEngine();
    }
}