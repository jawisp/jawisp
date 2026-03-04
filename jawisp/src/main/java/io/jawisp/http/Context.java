package io.jawisp.http;

import java.lang.reflect.Type;
import java.util.Map;

import io.jawisp.http.json.JsonMapper;

/**
 * The Context interface provides a contract for managing the context of an HTTP request and response.
 * It includes methods for setting and getting various properties such as result, status, JSON, body,
 * content type, path parameters, and attributes.
 *
 * @author reftch
 * @version 1.0.3
 */
public interface Context {

    /**
     * Gets the text result of the HTTP response.
     *
     * @return the text of the HTTP response
     */
    String result();

    /**
     * Sets the result of the HTTP response.
     *
     * @param text the text to set
     * @return the current Context instance
     */
    Context text(String text);

    /**
     * Sets the status code of the HTTP response.
     *
     * @param status the status code to set
     * @return the current Context instance
     */
    Context status(int status);

    /**
     * Gets the status code of the HTTP response.
     *
     * @return the status code of the HTTP response
     */
    int status();

    /**
     * Sets the JSON body of the HTTP response.
     *
     * @param json the JSON string to set
     * @return the current Context instance
     */
    Context json(String json);

    /**
     * Gets the body of the HTTP request or response.
     *
     * @return the body as a string
     */
    String body();

    /**
     * Gets the body of the HTTP request or response as bytes.
     *
     * @return the body as bytes
     */
    byte[] bodyAsBytes();

    /**
     * Converts the body of the HTTP request or response to a class of the specified type.
     *
     * @param type the type of the class to convert to
     * @param <T>  the type of the class
     * @return the converted object
     */
    <T> T bodyAsClass(Type type);

    /**
     * Gets the JsonMapper instance for handling JSON.
     *
     * @return the JsonMapper instance
     */
    JsonMapper jsonMapper();

    /**
     * Checks if the connection should be kept alive.
     *
     * @return true if the connection should be kept alive, false otherwise
     */
    boolean isKeepAlive();

    /**
     * Gets the content type of the HTTP request or response.
     *
     * @return the content type
     */
    String contentType();

    /**
     * Sets the content type of the HTTP request or response.
     *
     * @param contentType the content type to set
     * @return the current Context instance
     */
    Context contentType(String contentType);

    /**
     * Gets the path of the HTTP request.
     *
     * @return the path of the HTTP request
     */
    String path();

    /**
     * Gets a path parameter by name.
     *
     * @param name the name of the path parameter
     * @return the value of the path parameter
     */
    String pathParam(String name);

    /**
     * Gets all path parameters as a map.
     *
     * @return a map of path parameters
     */
    Map<String, String> pathParamMap();

    /**
     * Sets an attribute by name.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return the current Context instance
     */
    Context attribute(String name, Object value);

    /**
     * Gets an attribute by name.
     *
     * @param name the name of the attribute
     * @param <T>  the type of the attribute
     * @return the value of the attribute
     */
    <T> T attribute(String name);

    /**
     * Retrieves the value of the header with the specified name.
     *
     * @param name the name of the header to retrieve
     * @return the value of the header, or null if the header is not present
     */
    String header(String name);

    /**
     * Retrieves a map containing all the headers in this context.
     *
     * @return a map where the keys are header names and the values are header values
     */
    Map<String, String> headerMap();

}