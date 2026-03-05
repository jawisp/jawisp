package io.jawisp.http;

/**
 * The HttpServer interface defines the contract for an HTTP server.
 * It includes methods to start and stop the server, and may throw an Exception
 * if any error occurs during these operations.
 *
 * @author reftch
 * @version 1.0.5
 */
public interface HttpServer {

    /**
     * Starts the HTTP server.
     *
     * @throws Exception if an error occurs during server startup
     */
    void start() throws Exception;

    /**
     * Stops the HTTP server.
     *
     * @throws Exception if an error occurs during server shutdown
     */
    void stop() throws Exception;
}