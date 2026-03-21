package io.jawisp.config.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.jawisp.Jawisp;
import io.jawisp.http.HttpMethod;

public class CorsTest {

    @Test
    public void testUserEndpointPost() throws Exception {
        int testPort = 65001;

        Jawisp server = Jawisp.build(config -> config
                .port(testPort) // random port
                .cors(cors -> cors.origins("http://localhost:" + testPort))
                .routes(route -> route
                        .post("/api/v1/users", ctx -> {
                            ctx.status(201).json("{\"\":\"1\", \"name\":\"Test User\"}");
                        })));
        server.start();

        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = """
                {
                    "id": "1",
                    "name": "Test User",
                    "age": 33
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + testPort + "/api/v1/users"))
                .header("Content-Type", "application/json")
                .header("Origin", "http://localhost:" + testPort)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // CORS assertions
        assertEquals(201, response.statusCode());
        assertEquals("http://localhost:" + testPort,
                response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));

        String body = response.body().trim().replaceAll("\\s+", " ");
        assertTrue(body.contains("Test User"), "Should echo name back");

        server.stop();
    }

    @Test
    public void testDisabledOriginsUserEndpointPost() throws Exception {
        int testPort = 65002;

        Jawisp server = Jawisp.build(config -> config
                .port(testPort) // random port
                .cors(cors -> cors.origins("http://localhost:65000"))
                .routes(route -> route
                        .post("/api/v1/users", ctx -> {
                            ctx.status(201).json("{\"\":\"1\", \"name\":\"Test User\"}");
                        })));
        server.start();

        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = """
                {
                    "id": "1",
                    "name": "Test User",
                    "age": 33
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + testPort + "/api/v1/users"))
                .header("Content-Type", "application/json")
                .header("Origin", "http://localhost:" + testPort)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // CORS assertions
        assertEquals(403, response.statusCode());

        server.stop();
    }

    @Test
    public void testPutMethodBlockedPreflight() throws Exception {
        int testPort = 65002;

        Jawisp server = Jawisp.build(config -> config
                .port(testPort)
                .cors(cors -> cors
                        .origins("http://localhost:" + testPort)
                        .methods(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS) // No PUT!
                        .shortCircuit(true))
                .routes(route -> route
                        .options("/api/v1/users", ctx -> ctx.status(200)) // Handle OPTIONS
                        .put("/api/v1/users", ctx -> ctx.status(201)))); // Won't be reached

        server.start();

        HttpRequest preflight = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + testPort + "/api/v1/users"))
                .header("Origin", "http://localhost:" + testPort)
                .header("Access-Control-Request-Method", "PUT")
                .header("Access-Control-Request-Headers", "content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(preflight, HttpResponse.BodyHandlers.ofString());

            // Status 200 (expected), but NO PUT in allowed methods
            assertEquals(200, response.statusCode());

            // CRITICAL: Verify PUT is NOT in allowed methods response
            String allowedMethods = response.headers().firstValue("Access-Control-Allow-Methods").orElse("");
            assertFalse(allowedMethods.contains("PUT"), "PUT should NOT be in allowed methods");

            // Origin still allowed
            assertEquals("http://localhost:" + testPort,
                    response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        } finally {
            server.stop();
        }
    }

}