package io.jawisp.plugin;

/**
 * The PluginFactory interface defines the contract for creating plugin instances.
 *
 * @param <T> the type of the plugin that this factory creates
 * @author Taras Chornyi
 * @since 1.0.14
 */
public interface PluginFactory<T extends Plugin<T>> {

    /**
     * Returns the name of the plugin that this factory creates.
     *
     * @return the name of the plugin
     */
    String getName();

    /**
     * Creates an instance of the plugin.
     *
     * @return an instance of the plugin
     */
    T create();
}