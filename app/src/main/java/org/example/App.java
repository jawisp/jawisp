package org.example;

import io.jawisp.core.Jawisp;

public class App {

    public static void main(String[] args) {
        Jawisp.run(config -> config
                .contextPath("/api/v1")
                .routes(route -> {
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
