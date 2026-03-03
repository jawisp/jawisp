package io.jawisp.http.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJsonMapperProvider implements JsonMapperProvider {

    @Override
    public JsonMapper createJsonMapper() {
        return new JacksonJsonMapper(new ObjectMapper());
    }

}
