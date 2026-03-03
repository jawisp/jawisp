package org.example;

import io.jawisp.core.Jawisp;
import io.jawisp.http.Context;

public class App {

    static void createUser(Context ctx) {
        record User(long id, String name) {
        }
        var user = ctx.bodyAsClass(User.class);
        ctx.status(201).json(ctx.jsonMapper().toJsonString(user, User.class));
    }

    void main() {
        Jawisp.run(config -> config
                .routes(route -> {
                    route.get("/", ctx -> ctx.result("Hello World!"));
                    route.get("/api/v1/users/:id/mail/:num", ctx -> ctx
                            .status(200)
                            .json("{\"userId\":\"" + ctx.pathParam("id") + "\", \"name\":\"Taras\"}"));
                    route.post("/api/v1/users", App::createUser);
                }));
    }

}
