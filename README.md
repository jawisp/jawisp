# Jawisp - lightweight Java framework

## Getting started

Jawisp requires Java 21+. Add dependency for Gradle:
```
implementation 'io.jawisp:jawisp:2.0.0'
```
For Maven:
```
<dependency>
    <groupId>io.jawisp</groupId>
    <artifactId>jawisp</artifactId>
    <version>2.0.0</version>
</dependency>
```
Start implementing your perfect web application:
```
import io.jawisp.core.Jawisp;

void main() {
    Jawisp.run(config -> config
            .routes(route -> {
                route.get("/", ctx -> ctx.result("Hello World!"));
            }));
}
```
By default, server will run on port 8080
