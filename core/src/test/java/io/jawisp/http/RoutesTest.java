package io.jawisp.http;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoutesTest {

    Handler mockHandler = new Handler() {
        @Override
        public void handle(Context context) {
            // Mock implementation - doesn't matter for route testing
        }
    };

    @Test
    public void testContextPathRoot() {
        Routes routes = new Routes("/api/v1");
        routes.get("/", mockHandler);

        Route rootRoute = routes.getRoutes().get(0);
        assertEquals(HttpMethod.GET, rootRoute.getMethod());
        assertEquals("/api/v1", rootRoute.getPath());
    }

    @Test
    public void testContextPathUsers() {
        Routes routes = new Routes("/api/v1");
        routes.post("/users", mockHandler);

        Route userRoute = routes.getRoutes().get(0);
        assertEquals(HttpMethod.POST, userRoute.getMethod());
        assertEquals("/api/v1/users", userRoute.getPath()); // No double slash!
    }

    @Test
    public void testContextPathUsersId() {
        Routes routes = new Routes("/api/v1");
        routes.get("/users/:id", mockHandler);

        assertEquals("/api/v1/users/:id", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testEmptyContextPath() {
        Routes routes = new Routes("");
        routes.get("/home", mockHandler);

        assertEquals("/home", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testTrailingSlashContext() {
        Routes routes = new Routes("/api/");
        routes.get("/test", mockHandler);

        assertEquals("/api/test", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testNoLeadingSlashRoute() {
        Routes routes = new Routes("/api/v1");
        routes.get("users", mockHandler);

        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testNestedRoutes() {
        Routes routes = new Routes("/api/v1");
        routes.path("/users", users -> {
            users.get("/", mockHandler);
            users.get("/:id", mockHandler);
            users.post("/", mockHandler);
        });

        assertEquals(3, routes.getRoutes().size());
        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(1).getPath());
        assertEquals("/api/v1/users", routes.getRoutes().get(2).getPath());
    }

    @Test
    public void testNestedRoutesWithEmptyContextPath() {
        Routes routes = new Routes("");
        routes.path("/users", users -> {
            users.get("/", mockHandler);
            users.get("/:id", mockHandler);
            users.post("/", mockHandler);
        });

        assertEquals(3, routes.getRoutes().size());
        assertEquals("/users", routes.getRoutes().get(0).getPath());
        assertEquals("/users/:id", routes.getRoutes().get(1).getPath());
        assertEquals("/users", routes.getRoutes().get(2).getPath());
    }

    @Test
    public void testNestedRoutesWithTrailingSlashContext() {
        Routes routes = new Routes("/api/");
        routes.path("/users", users -> {
            users.get("/", mockHandler);
            users.get("/:id", mockHandler);
            users.post("/", mockHandler);
        });

        assertEquals(3, routes.getRoutes().size());
        assertEquals("/api/users", routes.getRoutes().get(0).getPath());
        assertEquals("/api/users/:id", routes.getRoutes().get(1).getPath());
        assertEquals("/api/users", routes.getRoutes().get(2).getPath());
    }

    @Test
    public void testNestedRoutesWithNoLeadingSlashRoute() {
        Routes routes = new Routes("/api/v1");
        routes.path("users", users -> {
            users.get("/", mockHandler);
            users.get("/:id", mockHandler);
            users.post("/", mockHandler);
        });

        assertEquals(3, routes.getRoutes().size());
        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(1).getPath());
        assertEquals("/api/v1/users", routes.getRoutes().get(2).getPath());
    }

    @Test
    public void testNestedRoutesWithMultipleLevels() {
        Routes routes = new Routes("/api/v1");
        routes.path("/users", users -> {
            users.get("/", mockHandler);
            users.path("/:id", user -> {
                user.get("/", mockHandler);
                user.post("/", mockHandler);
            });
            users.post("/", mockHandler);
        });

        assertEquals(4, routes.getRoutes().size());
        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(1).getPath());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(2).getPath());
        assertEquals("/api/v1/users", routes.getRoutes().get(3).getPath());
    }

    @Test
    public void testNestedRoutesWithMultipleMultipleLevels() {
        Routes routes = new Routes("/api/v1");
        routes.path("users", users -> {
            users.get("/", mockHandler);
            users.get("/:id", mockHandler);
            users.post("/", mockHandler);
            // orders
            users.path("/orders", orders -> orders
                    .get("/", mockHandler)
                    .post("create", mockHandler)
                    .put("/:id", mockHandler)
                    // shops
                    .path("/shops", shop -> shop
                            .get("/", mockHandler)
                            .get("/:id", mockHandler)
                            .get("/create", mockHandler)
                            .get("delete/:id", mockHandler)));
        });

        assertEquals(10, routes.getRoutes().size());
        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(1).getPath());
        assertEquals("/api/v1/users", routes.getRoutes().get(2).getPath());
        assertEquals("/api/v1/users/orders", routes.getRoutes().get(3).getPath());
        assertEquals("/api/v1/users/orders/create", routes.getRoutes().get(4).getPath());
        assertEquals("/api/v1/users/orders/:id", routes.getRoutes().get(5).getPath());
        assertEquals("/api/v1/users/orders/shops", routes.getRoutes().get(6).getPath());
        assertEquals("/api/v1/users/orders/shops/:id", routes.getRoutes().get(7).getPath());
        assertEquals("/api/v1/users/orders/shops/create", routes.getRoutes().get(8).getPath());
        assertEquals("/api/v1/users/orders/shops/delete/:id", routes.getRoutes().get(9).getPath());
    }

    @Test
    public void testNestedRoutesWithDifferentMethods() {
        Routes routes = new Routes("/api/v1");
        routes.path("/users", users -> {
            users.get("/", mockHandler);
            users.post("/", mockHandler);
            users.put("/:id", mockHandler);
            users.delete("/:id", mockHandler);
        });

        assertEquals(4, routes.getRoutes().size());
        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
        assertEquals(HttpMethod.GET, routes.getRoutes().get(0).getMethod());
        assertEquals("/api/v1/users", routes.getRoutes().get(1).getPath());
        assertEquals(HttpMethod.POST, routes.getRoutes().get(1).getMethod());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(2).getPath());
        assertEquals(HttpMethod.PUT, routes.getRoutes().get(2).getMethod());
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(3).getPath());
        assertEquals(HttpMethod.DELETE, routes.getRoutes().get(3).getMethod());
    }

}
