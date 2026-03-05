package org.example;

import io.jawisp.core.Jawisp;

public class App {

    public static void main(String[] args) {
        Jawisp.build(config -> config
                .routes(route -> route
                    .get("/", ctx -> ctx.text("Hello World!"))
                    .get("/api/v1/users/:id", UserController::getUser)
                    .post("/api/v1/users", UserController::createUser)
                )).start();
    }

}
