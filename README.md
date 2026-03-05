# Jawisp 

## Overview

**Jawisp** is a lightweight Java Web framework that leverages the powerful Netty framework, which is renowned for its asynchronous event-driven architecture. Designed to simplify web
development, **Jawisp** is accessible to developers without requiring extensive knowledge of web technologies. Its primary objective is to enable rapid and efficient development
processes.

The **Jawisp** framework is particularly suited for applications that benefit from the non-blocking I/O model provided by Netty, allowing for high performance and scalability. By
abstracting many of the complexities associated with traditional Java web frameworks, **Jawisp** provides a streamlined development experience. This is achieved through its intuitive
API and minimalistic design, which focus on essential features while reducing boilerplate code.

Key features of **Jawisp** include:

1. **Asynchronous Processing**: Leveraging Netty's capabilities, **Jawisp** supports asynchronous request handling, enabling high throughput and low latency.
2. **Lightweight**: The framework is designed to be lightweight, minimizing resource consumption and ensuring efficient performance even on resource-constrained environments.
3. **Ease of Use**: With its straightforward API and minimal configuration requirements, **Jawisp** is easy to learn and integrate into existing projects.
4. **Rapid Development**: The framework's simplicity facilitates rapid development cycles, allowing developers to focus more on application logic rather than infrastructure.
5. **Scalability**: Built on Netty's robust architecture, **Jawisp** is scalable, capable of handling a large number of concurrent connections without significant performance
degradation.

Overall, **Jawisp** is an excellent choice for developers looking to build high-performance, scalable web applications with minimal complexity and effort. Its integration of Netty's
capabilities with a user-friendly interface makes it an attractive option for both new and experienced Java developers.

## Getting Started

**Jawisp** requires Java 21 or higher. To begin using **Jawisp** in your project, you need to add the appropriate dependency to your build configuration. Below are the instructions
for both Gradle and Maven.

### Gradle Dependency

For Gradle, add the following line to your `build.gradle` file:

```gradle
dependencies {
    implementation 'io.jawisp:jawisp:2.0.0'
}
```

### Maven Dependency

For Maven, add the following snippet to your `pom.xml` file:

```xml
<dependencies>
    <dependency>
        <groupId>io.jawisp</groupId>
        <artifactId>jawisp</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

### Quick Start Example

Once you have added the dependency, you can start implementing your web application. Below is a simple example to get you started:

```java
import io.jawisp.core.Jawisp;

public class MyWebApplication {
    public static void main(String[] args) {
        Jawisp.build(config -> config
            .routes(route -> route
                .get("/", ctx -> ctx.result("Hello World!"))
            )
        ).start();
    }
}
```

### Default Server Configuration

By default, the **Jawisp** server will run on port `8080`. You can customize this and other configuration options as needed. For example, to specify a different port, you can modify
the configuration like this:

```java
Jawisp.build(config -> config
    .port(8081)
    .routes(route -> route
        .get("/", ctx -> ctx.result("Hello World!"))
    )
).start();
```

### Running Your Application

To run your application, simply execute the `main` method. Your web server will start, and you should be able to access your "Hello World!" message by navigating to
`http://localhost:8080` in your web browser.

With these steps, you are now ready to start building your web application using **Jawisp**. The framework's simplicity and efficiency will help you focus on developing your
application's core features without getting bogged down in complex configurations.

## Context

The context object in **Jawisp** provides comprehensive access to all data required to process an HTTP request. It encapsulates both the request and response, offering a range of
methods for interacting with these components. Below is a detailed table outlining the available methods for the context object, categorized into request and response methods.

### Request Methods

| Method                     | Description                                                                 |
|----------------------------|-----------------------------------------------------------------------------|
| `attribute("name", value)`  | Sets an attribute on the request.                                        |
| `attribute("name")`         | Retrieves an attribute from the request.                                   |
| `body()`                   | Retrieves the request body as a string.                                    |
| `bodyAsBytes()`              | Retrieves the request body as an array of bytes.                           |
| `pathParam("name")`        | Retrieves a path parameter by name as a string.                             |
| `pathParamMap()`           | Retrieves a map of all path parameters.                                    |
| `path()`                   | Retrieves the request path.                                               |
| `isKeepAlive()`            | Checks if the request is a keep-alive request.                              |
| `headerMap()`              | Retrieves all header key-value pairs as a map.                              |
| `header("key")`             | Retrieves a header value by its key.                                      |
| `cookieMap()`              | Retrieves a map of all request cookies.                                    |
| `cookie("name")`            | Retrieves a request cookie by name.                                       |
| `sessionAttribute("name", value)` | Sets a session attribute.                                              |
| `sessionAttribute("name")`  | Retrieves a session attribute by name.                                     |
| `ip()`                     | Retrieves the IP address of the request.                                    |
| `host()`                   | Retrieves the host of the request.                                          |
| `request()`                | Retrieves the underlying `HttpRequest` object.                              |

### Response Methods

| Method                       | Description                                                                 |
|------------------------------|-----------------------------------------------------------------------------|
| `text("string")`            | Sets the response body to the specified string.                            |
| `json(obj)`                  | Converts the object to a JSON string, sets the response body, and sets the content type to JSON. |
| `status()`                   | Retrieves the current response status.                                      |
| `status(code)`                | Sets the response status code.                                            |
| `cookie("name", "value", maxAge)` | Sets a response cookie with the specified name, value, and optional max-age. |
| `removeCookie("name", "/path")` | Removes a response cookie by name and optional path.                      |
| `header("name", "value")`    | Sets a response header with the specified name and value.                    |
| `removeHeader("name")`       | Removes a response header by name.                                          |
| `redirect("/path", code)`    | Redirects to the specified path with the given status code.                  |
| `response()`                 | Retrieves the underlying `HttpResponse` object.                              |
| `html("html")`               | Sets the response body to the specified HTML string and sets the content type to HTML. |
| `render("/template.tmpl", model)` | Renders a template with the given model and sets the response body to the rendered HTML. |

These methods provide a comprehensive toolkit for handling HTTP requests and responses within the **Jawisp** framework. They allow developers to easily manipulate and access request
and response data, enabling the construction of robust and efficient web applications.


## Logging with SLF4J and Logback

Jawisp uses SLF4J as the logging facade with Logback as the default implementation for all framework components and your application code.

### Dependencies

Jawisp core includes SLF4J and Logback transitively. Explicitly declare in your build file for version control:
```groovy
dependencies {
    implementation 'ch.qos.logback:logback-classic:1.5.12'
}
```
For Maven:
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.12</version>
</dependency>
```
### Configuration File

Create src/main/resources/logback.xml - Jawisp automatically loads this:
```xml
<configuration debug="true">
  <!-- Console appender for output -->
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- Set Netty to INFO -->
  <logger name="io.netty" level="DEBUG" />
  <logger name="io.jawisp" level="INFO" />

  <!-- Root logger at WARN to minimize noise -->
  <root level="WARN">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```

## Template Rendering Guide

Jawisp provides a plugin-based template rendering system supporting Pebble, Thymeleaf engines through a unified TemplateEngine API. Drop plugin JARs on the classpath and configure with config.usePlugin().

### Quick start

1. Add Dependencies
```
implementation 'io.pebbletemplates:pebble:4.1.1'
implementation 'org.thymeleaf:thymeleaf:3.1.3.RELEASE'
```
2. Configure Template Engine with engine name: 'pebble', 'thymeleaf'
```
Jawisp.build(config -> config
        .usePlugin("pebble")
        .routes(route -> route.get("/", App::homePage))
      ).start();
```
3. Render Templates
```
static void homePage(Context ctx) {
    ctx.render("home.html", Map.of("name", "John Smith"));
}
```
### Template Locations

Default: **templates/** (classpath)

