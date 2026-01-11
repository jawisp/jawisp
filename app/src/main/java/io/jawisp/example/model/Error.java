package io.jawisp.example.model;

import io.jawisp.core.annotation.Entity;
import io.jawisp.http.ErrorResponse;
import io.jawisp.http.Server.Request;

@Entity
public class Error extends ErrorResponse {

    public Error(int statusCode, String error, String message, Request request) {
        super(statusCode, error, message, request);
    }
    
}
