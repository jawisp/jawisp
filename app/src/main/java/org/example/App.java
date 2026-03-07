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
                .port(8080)
                .templateEngine("pebble")
                .staticResources("/static")
                .routes(route -> route
                        .get("/", App::homePage)
                        // Nested API v1
                        .path("/api/v1", api -> api
                                .path("users", users -> users // /api/v1/users
                                        .get("/:id", UserController::getUser)
                                        .post("/", UserController::createUser)
                                        .delete("/:id", UserController::createUser)
                                        .path("/orders", orders -> orders // /api/v1/users/orders
                                                .get("/:orderId", UserController::getOrder)
                                                .post("/", ctx -> ctx.text("create order")))))
                        .error(404, ctx -> ctx.text("Generic 404 Error"))))
                .start();

    }

}
