package org.example;

import io.jawisp.core.Jawisp;

public class App {

    public static void main(String[] args) {
        Jawisp.run(config -> { config 
            .routes(route -> {
                route.get("/", ctx -> ctx.result("Hello World!"));
                route.get("/users/:id", ctx -> {
                    String id = ctx.pathParam("id");
                    ctx.status(200).json("{\"userId\":\"" + id + "\", \"name\":\"Taras\"}");
                });
                route.post("/users", ctx -> {
                    String body = ctx.body();
                    ctx.status(201).result("User created: " + body);
                });
            });
        });
    }
}
