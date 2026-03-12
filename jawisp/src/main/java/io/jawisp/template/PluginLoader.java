package io.jawisp.template;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The PluginLoader class is responsible for loading template engine plugins.
 * It provides methods to load all available plugins and to debug them.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class PluginLoader {

    /**
     * Constructs a new PluginLoader instance.
     */
    public PluginLoader() {
    }

    /**
     * Loads all available template engine plugins.
     *
     * @return A list of loaded {@link TemplateEnginePlugin} instances.
     */
    public static List<TemplateEnginePlugin> loadAll() {
        ServiceLoader<TemplateEnginePlugin> loader =
            ServiceLoader.load(TemplateEnginePlugin.class,
                Thread.currentThread().getContextClassLoader());

        return StreamSupport.stream(loader.spliterator(), false)
            .collect(Collectors.toList());
    }

}