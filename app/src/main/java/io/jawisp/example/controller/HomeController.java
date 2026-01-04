package io.jawisp.example.controller;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Route;

@Controller(basePath = "/")
public class HomeController {
    
    @Route(method = "GET", path = "/")
    public String getHome() {
        return "Hello, world!";
    }

    @Route(method = "GET", path = "/api")
    public String getApi() {
        return "This is API";
    }
    
}
