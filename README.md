# HTTP Logging Spring Boot Starter

## Goal

Create a Spring Boot starter that adds HTTP request and response logging functionality to an application.

## Requirements

+ Implement logging using Aspect
+ Allow configuration of logging through application.properties or application.yml
+ Provide options for users to enable or disable logging, as well as choose the level of log detail

## Configuration Options

The following configuration options are available:

| Configuration                    | Description                                    |
|----------------------------------|------------------------------------------------|
| t1.http.request.logger.enabled   | Enable or disable HTTP logging (default: true) |
| t1.http.request.logger.log-level | Choose the level of log detail (default: INFO) |

+ TRACE: Log all request and response details
+ DEBUG: Log request and response headers and bodies
+ INFO: Log request and response headers
+ WARN: Log only warnings and errors
+ ERROR: Log only errors
+ NONE: Disable logging

## Usage

Add the starter to your Spring Boot project:

```
<dependency>
    <groupId>org.example.t1.http.logger</groupId>
    <artifactId>logger-starter</artifactId>
</dependency>
```

Configure logging in your application.properties

```
t1.http.request.logger.enabled=true
t1.http.request.logger.log-level=DEBUG
```

or application.yml file:

```
t1:
  http:
    request:
      logger:
        enabled: true
        log-level: INFO
```