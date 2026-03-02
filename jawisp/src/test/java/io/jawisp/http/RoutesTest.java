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
        assertEquals("/api/v1/users", userRoute.getPath());  // No double slash!
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
}
