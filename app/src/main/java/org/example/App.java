package org.example;

import java.util.Map;

import io.jawisp.core.Jawisp;
import io.jawisp.http.Context;

public class App {

    static void homePage(Context ctx) {
        // ctx.render("thymeleaf.html", Map.of("name", "Taras!!!"));
        // ctx.render("home.html", Map.of("name", "Taras!!!"));
        ctx.text("Welcome to home page!");
    }

    public static void main(String[] args) {
        Jawisp.build(config -> config
                .usePlugin("pebble")
                .routes(route -> route
                    .get("/", App::homePage)
                    .get("/api/v1/users/:id", UserController::getUser)
                    .post("/api/v1/users", UserController::createUser)
                )).start();
    }

}
