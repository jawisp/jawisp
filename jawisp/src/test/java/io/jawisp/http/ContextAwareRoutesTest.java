package io.jawisp.http;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContextAwareRoutesTest {
    
    Handler mockHandler = new Handler() {
        @Override
        public void handle(Context context) {
            // Mock implementation - doesn't matter for route testing
        }
    };
    
    @Test
    public void testContextPathRoot() {
        ContextAwareRoutes routes = new ContextAwareRoutes("/api/v1");
        routes.get("/", mockHandler);
        
        Route rootRoute = routes.getRoutes().get(0);
        assertEquals(HttpMethod.GET, rootRoute.getMethod());
        assertEquals("/api/v1", rootRoute.getPath());  
    }

    @Test
    public void testContextPathUsers() {
        ContextAwareRoutes routes = new ContextAwareRoutes("/api/v1");
        routes.post("/users", mockHandler);
        
        Route userRoute = routes.getRoutes().get(0);
        assertEquals(HttpMethod.POST, userRoute.getMethod());
        assertEquals("/api/v1/users", userRoute.getPath());  // No double slash!
    }
    
    @Test
    public void testContextPathUsersId() {
        ContextAwareRoutes routes = new ContextAwareRoutes("/api/v1");
        routes.get("/users/:id", mockHandler);
        
        assertEquals("/api/v1/users/:id", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testEmptyContextPath() {
        ContextAwareRoutes routes = new ContextAwareRoutes("");
        routes.get("/home", mockHandler);
        
        assertEquals("/home", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testTrailingSlashContext() {
        ContextAwareRoutes routes = new ContextAwareRoutes("/api/");
        routes.get("/test", mockHandler);
        
        assertEquals("/api/test", routes.getRoutes().get(0).getPath());
    }

    @Test
    public void testNoLeadingSlashRoute() {
        ContextAwareRoutes routes = new ContextAwareRoutes("/api/v1");
        routes.get("users", mockHandler);
        
        assertEquals("/api/v1/users", routes.getRoutes().get(0).getPath());
    }
}
