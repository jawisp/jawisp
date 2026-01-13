package io.jawisp.http;

/**
 * An enumeration of HTTP methods supported by the server.
 * This enum defines the standard HTTP methods that can be used
 * in handling HTTP requests within the application.
 */
public enum HttpMethod {
    /** GET method - used to retrieve information from the server */
    GET,
    /** POST method - used to submit data to the server */
    POST,
    /** PUT method - used to update or replace existing resources on the server */
    PUT,
    /** DELETE method - used to delete resources from the server */
    DELETE,
    /** PATCH method - used to apply partial modifications to a resource */
    PATCH,
    /**
     * HEAD method - similar to GET but returns only the headers without the body
     */
    HEAD,
    /**
     * OPTIONS method - used to describe the communication options for the target
     * resource
     */
    OPTIONS
}
