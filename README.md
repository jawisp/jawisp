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

## Context

The context object provides all data to process an HTTP request. It contains the request and response, as well as a set of getters and setters.

```
/* Methods for the request */

- attribute("name", value)          // set an attribute on the request
- attribute("name")                 // get an attribute on the request
- body()                            // request body as string
- bodyAsBytes()                     // request body as array of bytes
- pathParam("name")                 // path parameter by name as string
- pathParamMap()                    // map of all path parameters
- path()                            // request path
- isKeepAlive()                     // is keep alive request

/* Methods for the response */

- result("result")                  // set result stream to specified string
- json(obj)                         // calls result(jsonString), and also sets content type to json
- status()                          // get the response status
- status(code)                      // set the response status code

```

