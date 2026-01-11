package io.jawisp.http.handler;

import io.jawisp.http.Server.Request;

public class ErrorResponse extends AbstractErrorResponse {

    public ErrorResponse(int statusCode, String error, String message, Request request) {
        super(statusCode, error, message, request.getPath());
    }
    
}
