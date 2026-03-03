package org.example;

import io.jawisp.core.Jawisp;

public class App {

    void main() {
        Jawisp.run(config -> config
                .routes(route -> {
                    route.get("/", ctx -> ctx.result("Hello World!"));
                    route.get("/api/v1/users/:id", UserController::getUser);
                    route.post("/api/v1/users", UserController::createUser);
                }));
    }

}
