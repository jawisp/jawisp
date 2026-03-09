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
methods for interacting with these components. Below is a detailed list of the available methods for the context object, categorized into request and response methods.

### Request Methods

#### Retrieving Attributes
- **`attribute("name")`**: Retrieves an attribute from the request.
- **`attribute("name", value)`**: Sets an attribute on the request.

#### Retrieving Request Body
- **`body()`**: Retrieves the request body as a string.
- **`bodyAsBytes()`**: Retrieves the request body as an array of bytes.

#### Path Parameters
- **`pathParam("name")`**: Retrieves a path parameter by name as a string.
- **`pathParamMap()`**: Retrieves a map of all path parameters.

#### Request Information
- **`path()`**: Retrieves the request path.
- **`isKeepAlive()`**: Checks if the request is a keep-alive request.
- **`headerMap()`**: Retrieves all header key-value pairs as a map.
- **`header("key")`**: Retrieves a header value by its key.
- **`cookieMap()`**: Retrieves a map of all request cookies.
- **`cookie("name")`**: Retrieves a request cookie by name.
- **`sessionAttribute("name", value)`**: Sets a session attribute.
- **`sessionAttribute("name")`**: Retrieves a session attribute by name.
- **`ip()`**: Retrieves the IP address of the request.
- **`host()`**: Retrieves the host of the request.
- **`request()`**: Retrieves the underlying `HttpRequest` object.

### Response Methods

#### Setting Response Body

- **`text("string")`**: Sets the response body to the specified string.
- **`json(obj)`**: Converts the object to a JSON string, sets the response body, and sets the content type to JSON.
- **`html("html")`**: Sets the response body to the specified HTML string and sets the content type to HTML.
- **`render("/template.tmpl", model)`**: Renders a template with the given model and sets the response body to the rendered HTML.

#### Retrieving Response Body
- **`result()`**: Retrieves the response body as an array of bytes.

#### Response Status
- **`status()`**: Retrieves the current response status.
- **`status(code)`**: Sets the response status code.

#### Response Headers and Cookies
- **`cookie("name", "value", maxAge)`**: Sets a response cookie with the specified name, value, and optional max-age.
- **`removeCookie("name", "/path")`**: Removes a response cookie by name and optional path.
- **`header("name", "value")`**: Sets a response header with the specified name and value.
- **`removeHeader("name")`**: Removes a response header by name.
- **`headerMap()`**: Retrieves all response header key-value pairs as a map.

#### Redirects
- **`redirect("/path", code)`**: Redirects to the specified path with the given status code.

#### Retrieving Response Object
- **`response()`**: Retrieves the underlying `HttpResponse` object.

These methods provide a comprehensive toolkit for handling HTTP requests and responses within the **Jawisp** framework. They allow developers to easily manipulate and access request
and response data, enabling the construction of robust and efficient web applications.

## Route handlers

The Jawisp framework uses a builder pattern to configure the server and define routes. The `.routes()` method is used to define the routing rules, and nested `.path()` methods are used
to create hierarchical routes.

### Basic Routing

```java
Jawisp.build(config -> config
    .port(8080)
    .templateEngine("pebble")
    .staticResources("/static")
    .routes(route -> route
        .get("/", App::homePage)
        // Other routes
    )
    .start();
```

- **`.port(8080)`**: Sets the server to listen on port 8080.
- **`.templateEngine("pebble")`**: Configures the template engine to use Pebble.
- **`.staticResources("/static")`**: Serves static resources from the `/static` directory.
- **`.routes(route -> route)`**: Defines the routing rules.
  - **`.get("/", App::homePage)`**: Maps GET requests to the root path (`/`) to the `homePage` method in the `App` class.

### Nested Routing

Nested routing allows you to group related routes under a common base path. This makes the code cleaner and more organized, especially for RESTful APIs.

```java
.path("/api/v1", api -> api
    .path("users", users -> users // /api/v1/users
        .get("/:id", UserController::getUser)
        .post("/", UserController::createUser)
        .delete("/:id", UserController::deleteUser)
        .path("/orders", orders -> orders // /api/v1/users/orders
            .get("/:orderId", UserController::getOrder)
            .post("/", ctx -> ctx.text("create order"))))
```

- **`.path("/api/v1", api -> api)`**: Defines a base path `/api/v1` and groups related routes under this path.
  - **`.path("users", users -> users)`**: Defines a nested path `/api/v1/users`.
    - **`.get("/:id", UserController::getUser)`**: Maps GET requests to `/api/v1/users/:id` to the `getUser` method in the `UserController` class.
    - **`.post("/", UserController::createUser)`**: Maps POST requests to `/api/v1/users` to the `createUser` method in the `UserController` class.
    - **`.delete("/:id", UserController::deleteUser)`**: Maps DELETE requests to `/api/v1/users/:id` to the `deleteUser` method in the `UserController` class.
    - **`.path("/orders", orders -> orders)`**: Defines a nested path `/api/v1/users/orders`.
      - **`.get("/:orderId", UserController::getOrder)`**: Maps GET requests to `/api/v1/users/orders/:orderId` to the `getOrder` method in the `UserController` class.
      - **`.post("/", ctx -> ctx.text("create order"))`**: Maps POST requests to `/api/v1/users/orders` to a lambda function that returns the text "create order".

### Jawisp Configuration with Multiple Levels of Nested Routes

```java
Jawisp.build(config -> config
    .port(8080)  // Set the server port to 8080
    .templateEngine("pebble")  // Use Pebble as the template engine
    .staticResources("/static")  // Serve static resources from the /static directory
    .routes(route -> route
        .get("/", App::homePage)  // Define a GET route for the root path
        // Nested API v1
        .path("/api/v1", api -> api
            .path("users", users -> users  // Define /api/v1/users
                .get("/:id", UserController::getUser)  // GET /api/v1/users/:id
                .post("/", UserController::createUser)  // POST /api/v1/users
                .delete("/:id", UserController::createUser)  // DELETE /api/v1/users/:id
                .path("/orders", orders -> orders  // Define /api/v1/users/orders
                    .get("/:orderId", UserController::getOrder)  // GET /api/v1/users/orders/:orderId
                    .post("/", ctx -> ctx.text("create order"))  // POST /api/v1/users/orders
                )
            )
        )
        .error(404, ctx -> ctx.text("Generic 404 Error"))  // Handle 404 errors
    )
    .start();  // Start the Jawisp server
```

1. **Top-Level Path: `/api/v1`**
   - **Description**: This is the top-level path for version 1 of the API.
   - **Purpose**: It groups all API endpoints under a common base path, making it easier to manage and version the API.

2. **First-Level Nested Path: `/api/v1/users`**
   - **Description**: This path is nested under `/api/v1`.
   - **Purpose**: It handles routes related to users.
   - **Endpoints**:
     - **GET `/api/v1/users/:id`**: Retrieves a user by their ID.
     - **POST `/api/v1/users`**: Creates a new user.
     - **DELETE `/api/v1/users/:id`**: Deletes a user by their ID.

3. **Second-Level Nested Path: `/api/v1/users/orders`**
   - **Description**: This path is nested under `/api/v1/users`.
   - **Purpose**: It handles routes related to orders for a specific user.
   - **Endpoints**:
     - **GET `/api/v1/users/orders/:orderId`**: Retrieves an order by its ID for a specific user.
     - **POST `/api/v1/users/orders`**: Creates a new order for a specific user.

     
### Error Handling

```java
.error(404, ctx -> ctx.text("Generic 404 Error"))
```

- **`.error(404, ctx -> ctx.text("Generic 404 Error"))`**: Configures a custom handler for 404 errors, which returns the text "Generic 404 Error" when a request is made to a
non-existent route.  

## Logging with SLF4J and Logback

**Jawisp** uses SLF4J as the logging facade, with Logback as the default implementation for all framework components and your application code. This setup provides a flexible and
powerful logging infrastructure that can be easily configured and extended.

### Dependencies

Jawisp core includes SLF4J and Logback transitively, meaning they are automatically included when you add the **Jawisp** dependency to your project. However, for version control and
explicit management, it is recommended to explicitly declare these dependencies in your build file.

#### Gradle

For Gradle, add the following line to your `build.gradle` file:

```gradle
dependencies {
    implementation 'ch.qos.logback:logback-classic:1.5.12'
}
```

#### Maven

For Maven, add the following snippet to your `pom.xml` file:

```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.12</version>
</dependency>
```

### Configuration File

**Jawisp** automatically loads the `logback.xml` configuration file located in `src/main/resources`. This allows you to easily customize logging behavior without additional setup.

#### Example `logback.xml`

```xml
<configuration debug="true">
  <!-- Console appender for output -->
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- Set logging levels for specific packages -->
  <logger name="io.netty" level="DEBUG" />
  <logger name="io.jawisp" level="INFO" />

  <!-- Root logger at WARN to minimize noise -->
  <root level="WARN">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```

### Explanation of Configuration

- **Console Appender**: The `STDOUT` appender directs log messages to the console. The pattern specified (`%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`) includes the
timestamp, thread name, log level, logger name, and message.

- **Logger Levels**:
  - `io.netty` is set to `DEBUG` to capture detailed logs from the Netty framework.
  - `io.jawisp` is set to `INFO` to capture general logs from the **Jawisp** framework.

- **Root Logger**: The root logger is set to `WARN` to minimize noise from less important log messages, capturing only warnings and errors.

### Customization

You can customize the logging behavior by modifying the `logback.xml` file. Some additional configuration options include:

- **File Appender**: To log messages to a file, you can add a `FileAppender`:

  ```xml
  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>jawisp.log</file>
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  ```

- **Rolling File Appender**: For managing log files over time, use a `RollingFileAppender`:

  ```xml
  <appender name="ROLLING_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>jawisp.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>jawisp.%d{yyyy-MM-dd}.log</fileNamePattern>
      <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  ```

- **Asynchronous Logging**: To improve logging performance, especially under high load, use an `AsyncAppender`:

  ```xml
  <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="STDOUT" />
  </appender>
  ```

## Template Rendering Guide

**Jawisp** provides a flexible and extensible template rendering system that supports multiple template engines through a unified `TemplateEngine` API. This allows developers to use
popular template engines like Pebble and Thymeleaf seamlessly within their applications. The system is designed to be easy to set up and extend, with support for additional template
engines through plugins.

### Quick Start

Follow these steps to get started with template rendering in **Jawisp**:

#### 1. Add Dependencies

First, add the necessary dependencies for the template engine you want to use. For example, to use Pebble and Thymeleaf, include the following in your build file:

**Gradle:**

```groovy
dependencies {
    implementation 'io.pebbletemplates:pebble:4.1.1'
    implementation 'org.thymeleaf:thymeleaf:3.1.3.RELEASE'
}
```

**Maven:**

```xml
<dependencies>
    <dependency>
        <groupId>io.pebbletemplates</groupId>
        <artifactId>pebble</artifactId>
        <version>4.1.1</version>
    </dependency>
    <dependency>
        <groupId>org.thymeleaf</groupId>
        <artifactId>thymeleaf</artifactId>
        <version>3.1.3.RELEASE</version>
    </dependency>
</dependencies>
```

#### 2. Configure Template Engine

Next, configure the **Jawisp** framework to use the desired template engine. This is done by specifying the engine name using `config.usePlugin()`.

```java
import io.jawisp.core.Jawisp;
import io.jawisp.core.Context;

public class MyWebApplication {
    public static void main(String[] args) {
        Jawisp.build(config -> config
            .staticResources("/static")
            .templateEngine("pebble") // or "thymeleaf"
            .routes(route -> route.get("/", App::homePage))
        ).start();
    }
}
```

#### 3. Render Templates

With the template engine configured, you can now render templates in your route handlers. The `ctx.render()` method is used to render a template with the specified model.

```java
import io.jawisp.core.Context;

public class App {
    static void homePage(Context ctx) {
        ctx.render("home.html", Map.of("name", "John Smith"));
    }
}
```

### Template Locations

By default, **Jawisp** looks for templates in the `templates/` directory on the classpath. This means you can place your template files in `src/main/resources/templates/` and they
will be automatically detected and loaded.

### Supported Template Engines

**Jawisp** currently supports the following template engines:

- **Pebble**: A simple, flexible, and powerful templating engine for Java.
- **Thymeleaf**: A modern server-side Java template engine.

### Customizing Template Locations

If you want to specify a different location for your templates, you can configure the `templateDirectory` option in your `logback.xml` file.

```java
Jawisp.build(config -> config
    .usePlugin("pebble")
    .templateDirectory("myTemplates/")
    .routes(route -> route.get("/", App::homePage))
).start();
```

### Static resources

```java
Config config = new Config()
    .staticResources("/static", "/public", "/assets")  
    .staticResources(List.of("/css", "/js"))           
    .build();
```
