package io.jawisp.example.controller;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Inject;
import io.jawisp.core.annotation.Route;
import io.jawisp.example.service.HomeService;

@Controller(basePath = "/")
public class HomeController {
    
    @Inject
    private HomeService homeService;

    @Route(method = "GET", path = "/")
    public String getHome() {
        return "Hello, world!";
    }

    @Route(method = "GET", path = "/api")
    public String getApi() {
        return "This is API";
    }
    
}
