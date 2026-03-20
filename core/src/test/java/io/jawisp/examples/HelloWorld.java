package io.jawisp.examples;

import java.util.Map;

import io.jawisp.Jawisp;
import io.jawisp.http.Context;

public class HelloWorld {

    static void homePage(Context ctx) {
        ctx.render("home.html", Map.of("name", "John Smith"));
    }

    static void getUser(Context ctx) {
        var id = ctx.pathParam("id");
        ctx.status(200).json("{\"userId\":\"" + id + "\", \"name\":\"Taras\"}");
    }

    static void createUser(Context ctx) {
        var user = ctx.bodyAsClass(User.class);
        ctx.status(201).json(ctx.jsonMapper().toJsonString(user, User.class));
    }

    record User(long id, String name, int age) {}

    public static void main(String[] args) {
        Jawisp.build(config -> config
                .propertyFile("test.properties")
                .templateEngine("pebble")
                .staticResources("/static")
                .routes(route -> route
                        .get("/", HelloWorld::homePage)
                        .path("/api/v1/users", api -> api
                                .get(":id", HelloWorld::getUser)
                                .post("/", HelloWorld::createUser))
                        .error(404, ctx -> ctx.text("Generic 404 Error"))))
                .start();
    }
}
