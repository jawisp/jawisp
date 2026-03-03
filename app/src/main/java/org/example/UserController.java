package org.example;

import io.jawisp.http.Context;

public class UserController {
    
    static void getUser(Context ctx) {
        var id = ctx.pathParam("id");
        ctx.status(200).json("{\"userId\":\"" + id + "\", \"name\":\"Taras\"}");
    }

    static void createUser(Context ctx) {
        var user = ctx.bodyAsClass(User.class);
        ctx.status(201).json(ctx.jsonMapper().toJsonString(user, User.class));
    }
}
