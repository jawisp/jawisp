package io.jawisp.http;

/**
 * An enumeration of HTTP methods supported by the server.
 * This enum defines the standard HTTP methods that can be used
 * in handling HTTP requests within the application.
 * 
 * @author reftch
 * @since 1.0.0
 */
public enum HttpMethod {
    /**
     * A special method used to define a filter that runs before the main handler.
     * This method is not a standard HTTP method and is used internally.
     */
    BEFORE_FILTER,

    /**
     * A special method used to define a filter that runs after the main handler.
     * This method is not a standard HTTP method and is used internally.
     */
    AFTER_FILTER,

    /**
     * GET method - used to retrieve information from the server.
     * This method requests a representation of the specified resource.
     */
    GET,

    /**
     * POST method - used to submit data to the server.
     * This method submits data to be processed to a specified resource.
     */
    POST,

    /**
     * PUT method - used to update or replace existing resources on the server.
     * This method replaces all current representations of the target resource with
     * the uploaded content.
     */
    PUT,

    /**
     * DELETE method - used to delete resources from the server.
     * This method requests that the server deletes the specified resource.
     */
    DELETE,

    /**
     * PATCH method - used to apply partial modifications to a resource.
     * This method applies partial modifications to a resource.
     */
    PATCH,

    /**
     * HEAD method - similar to GET but returns only the headers without the body.
     * This method is used to retrieve meta-information about a resource without
     * transferring the body.
     */
    HEAD,

    /**
     * OPTIONS method - used to describe the communication options for the target
     * resource.
     * This method returns the HTTP methods that the server supports for the
     * specified URL.
     */
    OPTIONS,

    /**
     * TRACE method - used to perform a message loop-back test along the path to the
     * target resource.
     * This method echoes the received request so that a client can see what is
     * being received at each intermediate server.
     */
    TRACE,

    /**
     * CONNECT method - used to establish a tunnel to the server identified by a
     * given host and port.
     * This method is used to request a secure end-to-end connection.
     */
    CONNECT,

    /**
     * ERROR method - used to handle error responses.
     * This method is used to process requests when a specific error code is
     * encountered.
     */
    ERROR
}