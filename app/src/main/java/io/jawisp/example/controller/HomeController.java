package io.jawisp.example.controller;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Inject;
import io.jawisp.core.annotation.Secured;
import io.jawisp.core.annotation.Secured.SecurityRule;
import io.jawisp.http.MediaType;
import io.jawisp.example.model.User;
import io.jawisp.example.service.HomeService;
import io.jawisp.http.annotation.Produces;
import io.jawisp.http.annotation.Route;

@Controller(basePath = "/")
public class HomeController {
    
    @Inject
    private HomeService homeService;

    @Route(method = "GET", path = "/")
    @Secured(securityRule = SecurityRule.IS_AUTHENTICATED)
    public String getHome() {
        return homeService.getHome();
    }

    @Route(method = "GET", path = "/page")
    @Secured(securityRule = SecurityRule.IS_AUTHENTICATED)
    @Produces(MediaType.TEXT_HTML)
    public String getPage() {
        return homeService.getPage();
    }

    @Route(method = "GET", path = "/api")
    @Produces(MediaType.APPLICATION_JSON)
    public User getApi() {
        return homeService.getUser(); 
    }
    
}
