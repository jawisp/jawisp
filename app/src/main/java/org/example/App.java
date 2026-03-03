package org.example;

import io.jawisp.core.Jawisp;
import io.jawisp.http.Context;

public class App {

    static void createUser(Context ctx) {
        record User(long id, String name) {
        }
        var user = ctx.bodyAsClass(User.class);
        
        System.out.println("User " + user);
        ctx.status(201).json(user.toString());
    }

    void main() {
        Jawisp.run(config -> config
                .routes(route -> {
                    route.before("/", ctx -> {
                    });
                    route.after("/", ctx -> {
                    });
                    route.get("/", ctx -> ctx.result("Hello World!"));
                    route.get("/api/v1/users/:id", ctx -> ctx
                            .status(200)
                            .json("{\"userId\":\"" + ctx.pathParam("id") + "\", \"name\":\"Taras\"}"));
                    route.post("/api/v1/users", App::createUser);
                }));
    }

}
