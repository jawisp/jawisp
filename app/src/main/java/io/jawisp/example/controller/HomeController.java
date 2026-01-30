package io.jawisp.example.controller;

import io.jawisp.core.annotation.Controller;
import io.jawisp.core.annotation.Inject;
import io.jawisp.core.annotation.Secured;
import io.jawisp.core.annotation.Secured.SecurityRule;
import io.jawisp.example.model.User;
import io.jawisp.example.service.HomeService;
import io.jawisp.http.MediaType;
import io.jawisp.http.annotation.Cookie;
import io.jawisp.http.annotation.Header;
import io.jawisp.http.annotation.PathVariable;
import io.jawisp.http.annotation.Produces;
import io.jawisp.http.annotation.QueryValue;
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
    public User getDefaultUser() {
        return homeService.getUser("1", 33);
    }

    @Route(method = "GET", path = "/api/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(
            @PathVariable("id") String id, 
            @QueryValue(value = "age", defaultValue = "33") int age) {
        return homeService.getUser(id, age);
    }

    @Route(method = "GET", path = "/api/user/age/{age}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserByAge(
            @PathVariable("age") Integer age1,
            @QueryValue(value = "page", defaultValue = "0") int page,
            @Header("Accept") String header,
            @Cookie("sessionId") String session
        ) {
        return homeService.getUserByAge(age1);
    }

}
