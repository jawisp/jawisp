package org.example;

import io.jawisp.core.Jawisp;

public class App {

    void main() {
        Jawisp.create().routes(routes -> {
            routes.get("/", ctx -> ctx.result("Hello World!"));
            routes.get("/api/v1/users/:id", ctx -> {
                String id = ctx.pathParam("id");
                ctx.status(200).json("{\"userId\":\"" + id + "\", \"name\":\"Taras\"}");
            });
            routes.post("/users", ctx -> {
                String body = ctx.body();
                ctx.status(201).result("User created: " + body);
            });
        }).start();
    }
}
