package io.jawisp.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jawisp.plugin.PluginFactory;

/**
 * The JacksonJsonMapperProvider class is an implementation of the
 * JsonMapperProvider interface.
 * It uses the Jackson library to provide JsonMapper functionality.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class JacksonJsonMapperProvider implements PluginFactory<JacksonJsonMapper> {

    /**
     * Default constructor for the JacksonJsonMapperProvider class.
     */
    public JacksonJsonMapperProvider() {
    }

    /**
     * Returns the name of the plugin factory.
     * This name is used to identify the factory when creating a plugin instance.
     *
     * @return the name of the plugin factory, which is "jackson"
     */
    @Override
    public String getName() {
        return "jackson";
    }

    /**
     * Creates a new JsonMapper instance using the Jackson library.
     *
     * @return a new JsonMapper instance
     */
    @Override
    public JacksonJsonMapper create() {
        return new JacksonJsonMapper(new ObjectMapper());
    }

}