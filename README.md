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
    Jawisp.build(config -> config
            .routes(route -> route
                .get("/", ctx -> ctx.result("Hello World!"))
            )).start();
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
- headerMap()                       // get all header key/values as map
- header("key")                     // get a header
- cookieMap()                       // map of all request cookies
- cookie("name")                    // request cookie by name

/* Methods for the response */

- text("string")                    // set result stream to specified string
- json(obj)                         // calls result(jsonString), and also sets content type to json
- status()                          // get the response status
- status(code)                      // set the response status code
- cookie("name", "value", maxAge)   // set response cookie by name, with value and max-age (optional).
- removeCookie("name", "/path")     // removes cookie by name and path (optional)

```

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




