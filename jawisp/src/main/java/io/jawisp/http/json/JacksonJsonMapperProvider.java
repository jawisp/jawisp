package io.jawisp.http.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The JacksonJsonMapperProvider class is an implementation of the JsonMapperProvider interface.
 * It uses the Jackson library to provide JsonMapper functionality.
 *
 * @author reftch
 * @version 1.0.5
 */
public class JacksonJsonMapperProvider implements JsonMapperProvider {

    /**
     * Default constructor for the JacksonJsonMapperProvider class.
     */
    public JacksonJsonMapperProvider() {
    }
    
    /**
     * Creates a new JsonMapper instance using the Jackson library.
     *
     * @return a new JsonMapper instance
     */
    @Override
    public JsonMapper createJsonMapper() {
        return new JacksonJsonMapper(new ObjectMapper());
    }

}