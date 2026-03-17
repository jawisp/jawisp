package io.jawisp.plugin;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The Plugin interface defines the contract for plugin creation.
 *
 * @param <T> the type of the plugin
 * @author Taras Chornyi
 * @since 1.0.14
 */
public interface Plugin<T> {

    /**
     * Creates an instance of the plugin with the specified name.
     *
     * @param <T> the type of the plugin to create
     * @param pluginName the name of the plugin to create
     * @return an instance of the plugin
     * @throws IllegalArgumentException if no plugin is found with the specified name
     */
    @SuppressWarnings("unchecked")
    public static <T extends Plugin<T>> T create(String pluginName) {
        ServiceLoader<PluginFactory<?>> loader =
            (ServiceLoader<PluginFactory<?>>) (ServiceLoader<?>) ServiceLoader.load(PluginFactory.class);

        Optional<? extends PluginFactory<?>> factory = loader.stream()
                .map(ServiceLoader.Provider::get)  // Get actual service instance from Provider
                .filter(p -> p.getName().equals(pluginName))
                .findFirst();

        return factory.map(f -> ((PluginFactory<T>) f).create())
                .orElseThrow(() -> new IllegalArgumentException("No plugin found for name: " + pluginName));
    }

}