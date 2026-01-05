package io.jawisp.example.controller;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Inject;
import io.jawisp.core.annotation.Route;
import io.jawisp.core.annotation.Secured;
import io.jawisp.core.annotation.Secured.SecurityRule;
import io.jawisp.example.service.HomeService;

@Controller(basePath = "/")
public class HomeController {
    
    @Inject
    private HomeService homeService;

    @Route(method = "GET", path = "/")
    @Secured(securityRule = SecurityRule.IS_AUTHENTICATED)
    public String getHome() {
        return homeService.getHome();
    }

    @Route(method = "GET", path = "/api")
    public String getApi() {
        return "This is API";
    }
    
}
