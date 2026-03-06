package org.example;

import java.util.Map;

import io.jawisp.Jawisp;
import io.jawisp.http.Context;

public class App {

    static void homePage(Context ctx) {
        // ctx.status(404);
        ctx.render("home.html", Map.of("name", "John Smith"));
    }

    public static void main(String[] args) {
        Jawisp.build(config -> config
                .templateEngine("pebble")
                .staticResources("/static")
                .routes(route -> route
                    .get("/", App::homePage)
                    .get("/api/v1/users/:id", UserController::getUser)
                    .post("/api/v1/users", UserController::createUser)
                    .error(404, ctx -> ctx.text("Generic 404 Error"))
                )).start();
    }

}
