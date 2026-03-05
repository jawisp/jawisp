package io.jawisp.http;

import java.lang.reflect.Type;
import java.util.Map;

import io.jawisp.http.json.JsonMapper;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * The Context interface provides a contract for managing the context of an HTTP
 * request and response.
 * It includes methods for setting and getting various properties such as
 * result, status, JSON, body,
 * content type, path parameters, and attributes.
 *
 * @author reftch
 * @version 1.0.5
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
     * Converts the body of the HTTP request or response to a class of the specified
     * type.
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
     * @return a map where the keys are header names and the values are header
     *         values
     */
    Map<String, String> headerMap();

    /**
     * Sets a response header by name.
     *
     * @param name  the name of the header
     * @param value the value of the header
     */
    default void header(String name, String value) {
        response().headers().add(name, value);
    }

    /**
     * Removes a response header by name.
     *
     * @param name the name of the header to be removed
     */
    default void removeHeader(String name) {
        response().headers().remove(name);
    }

    /**
     * Retrieves a map containing all the cookies in this context.
     *
     * @return a map where the keys are cookie names and the values are cookie
     *         values
     */
    Map<String, String> cookieMap();

    /**
     * Retrieves the value of the cookie with the specified name.
     *
     * @param name the name of the cookie to retrieve
     * @return the value of the cookie, or null if the cookie is not present
     */
    String cookie(String name);

    /**
     * Sets a response cookie by name, with value and max-age (optional).
     *
     * @param name   the name of the cookie
     * @param value  the value of the cookie
     * @param maxAge the max-age of the cookie in seconds (optional)
     */
    void cookie(String name, String value, int maxAge);

    /**
     * Removes a cookie by name and path (optional).
     *
     * @param name the name of the cookie to remove
     * @param path the path of the cookie to remove (optional)
     */
    void removeCookie(String name, String path);

    /**
     * Retrieves the original HTTP request.
     *
     * @return the HTTP request
     */
    HttpRequest request();

    /**
     * Retrieves the original HTTP response.
     *
     * @return the HTTP response
     */
    HttpResponse response();

    /**
     * Sets a session attribute with the specified name and value.
     *
     * @param <T>   the type of the value
     * @param name  the name of the attribute
     * @param value the value of the attribute
     */
    <T> void sessionAttribute(String name, T value);

    /**
     * Retrieves a session attribute with the specified name.
     *
     * @param <T>  the type of the value
     * @param name the name of the attribute
     * @return the value of the attribute, or null if the attribute does not exist
     */
    <T> T sessionAttribute(String name);

    /**
     * Retrieves the IP address.
     *
     * @return the IP address as a String
     */
    String ip();

    /**
     * Retrieves the host name.
     *
     * @return the host name as a String
     */
    String host();

    /**
     * Redirects to the given path with the specified status code.
     *
     * @param path the path to redirect to
     * @param code the status code for the redirect
     */
    void redirect(String path, int code);

    /**
     * Sets the response content to the given HTML and sets the content type to
     * "text/html".
     *
     * @param html the HTML content to be set in the response
     */
    void html(String html);

    /**
     * Renders a template using the provided model and outputs the result as HTML.
     *
     * @param template The template string to be rendered.
     * @param model    A map containing key-value pairs that will be used to
     *                 populate the template.
     * @throws UnsupportedOperationException if no renderer is configured.
     */
    void render(String template, Map<String, Object> model) throws UnsupportedOperationException;

}