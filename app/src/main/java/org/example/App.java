package org.example;

import java.util.Map;

import io.jawisp.Jawisp;
import io.jawisp.http.Context;

public class App {

    static void homePage(Context ctx) {
        // ctx.status(404);
        // ctx.text("Hello, world!");
        ctx.render("home.html", Map.of("name", "John Smith"));
    }

    public static void main(String[] args) {
        Jawisp.build(config -> config
                .templateEngine("pebble")
                .staticResources("/static")
                .routes(route -> route
                        .get("/", App::homePage)
                        .path("/api/v1/users", api -> api
                                .get(":id", UserController::getUser)
                                .post("/", UserController::createUser))
                        .error(404, ctx -> ctx.text("Generic 404 Error"))))
                .start();

    }

}
