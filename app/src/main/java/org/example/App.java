package org.example;

import io.jawisp.core.Jawisp;

public class App {

    void main() {
        Jawisp.run(config -> config
                .routes(route -> {
                    route.before("/", ctx -> {});
                    route.after("/", ctx -> {});
                    route.get("/", ctx -> ctx.result("Hello World!"));
                    route.get("/users/:id", ctx -> ctx
                            .status(200)
                            .json("{\"userId\":\"" + ctx.pathParam("id") + "\", \"name\":\"Taras\"}"));
                    route.post("/users", ctx -> ctx
                            .status(201)
                            .result("User created: " + ctx.body()));
                }));
    }
}
