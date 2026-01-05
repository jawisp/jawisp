package io.jawisp.example.service;

import io.jawisp.core.annotation.Service;

@Service
public class HomeService {
    
    public String getHome() {
        return "Hello from Home Service!";
    }
}
