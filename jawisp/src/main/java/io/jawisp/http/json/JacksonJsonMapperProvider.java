/**
 * The JacksonJsonMapperProvider class is an implementation of the JsonMapperProvider interface.
 * It uses the Jackson library to provide JsonMapper functionality.
 *
 * @author reftch
 * @version 1.0.0
 */
package io.jawisp.http.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJsonMapperProvider implements JsonMapperProvider {

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