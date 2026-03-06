package io.jawisp.plugin.template.pebble;

import io.jawisp.core.Config;
import io.jawisp.plugin.template.TemplateEngine;
import io.jawisp.plugin.template.TemplateEnginePlugin;

/**
 * The PebblePlugin class implements the TemplateEnginePlugin interface
 * and provides a method to create a PebbleTemplateEngine instance.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class PebblePlugin implements TemplateEnginePlugin {

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
     * @param config the configuration to use
     * @return a new {@link TemplateEngine} instance
     */
    @Override
    public TemplateEngine createEngine(Config config) {
        return new PebbleTemplateEngine();
    }
}