package io.jawisp.http;

import java.lang.reflect.Type;
import java.util.Map;

import io.jawisp.http.json.JsonMapper;

public interface Context {
    
    Context result(String result);

    String result();

    Context status(int status);

    int status();

    Context json(String json);

    String body();

    byte[] bodyAsBytes();

    <T> T bodyAsClass(Type type);

    JsonMapper jsonMapper();

    boolean isKeepAlive();

    String contentType(); 

    Context contentType(String contentType);

    String path();

    String pathParam(String name);

    Map<String, String> pathParamMap();

    Context attribute(String name, Object value);

    <T> T attribute(String name);

}
