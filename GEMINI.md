# AI Framework Spring

## Project Overview

`ai-framework-spring` is a Spring Boot-based AI framework designed to provide core functionalities and extensibility for AI-related applications. It leverages Spring Boot's auto-configuration to set up essential services and offers a Service Provider Interface (SPI) mechanism for developers to customize or extend its behavior.

**Key Features:**

*   **Auto-configuration:** Automatically configures default implementations for core framework components, including JSON serialization, bean provisioning, and type registration.
*   **Extensibility (SPIs):** Allows developers to provide custom implementations for various services (e.g., `JsonSerializer`, `BeanProvider`, `TypeRegistry`) by defining their own Spring beans.
*   **Interaction Recording & Replay:** Includes an Aspect-Oriented Programming (AOP) aspect for recording and replaying interactions, configurable via `ai-framework.aspect-enabled` property.

**Technologies Used:**

*   **Language:** Java 21
*   **Framework:** Spring Boot 4.0.2
*   **Build Tool:** Gradle
*   **Core Libraries:** Spring AOP, AspectJ, Jackson (for JSON processing), Lombok
*   **Testing:** JUnit Platform

**Architecture Highlights:**

The framework's architecture is centered around auto-configuration and SPIs. The `AiFrameworkAutoConfiguration` class automatically detects and registers default implementations for various `spi` interfaces. Developers can easily override these defaults to tailor the framework to their specific needs. Key components like `BusinessHelper` and `BusinessAssembly` are initialized with these configured providers.

## Building and Running

This project uses Gradle as its build automation tool.

### Building the Project

To build the project and generate the JAR file:

```bash
./gradlew build
```

### Running the Application

To run the Spring Boot application:

```bash
./gradlew bootRun
```

### Running Tests

To execute the unit and integration tests:

```bash
./gradlew test
```

## Development Conventions

*   **Spring Boot Auto-configuration:** Follows Spring Boot's conventions for auto-configuration and conditional bean creation, allowing for flexible and extensible module design.
*   **Service Provider Interfaces (SPI):** Employs an SPI-driven approach for core services, promoting modularity and customizability.
*   **Lombok:** Utilizes Lombok to reduce boilerplate code for common Java patterns (e.g., getters, setters, constructors).
*   **Aspect-Oriented Programming (AOP):** Integrates AOP for cross-cutting concerns, particularly for the interaction recording and replay mechanism.
*   **Package Structure:** The `com.tianrenservice.ai_framework_spring` package is organized into `autoconfigure`, `core`, and other feature-specific sub-packages.
