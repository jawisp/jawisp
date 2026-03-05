package io.jawisp.http;

/**
 * The Handler interface represents a functional interface that defines a method to handle an HTTP request.
 * The handle method takes a Context object as a parameter, which provides various methods to interact with
 * the request and response.
 *
 * @author reftch
 * @version 1.0.5
 */
@FunctionalInterface
public interface Handler {
    /**
     * Handles the HTTP request by interacting with the provided Context object.
     *
     * @param context the Context object representing the HTTP request and response
     */
    void handle(Context context);
}