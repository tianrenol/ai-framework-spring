# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a Gradle-based Spring Boot project using Java 21.

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.tianrenservice.ai_framework_spring.SomeTest"

# Run the Spring Boot application
./gradlew bootRun

# Clean build artifacts
./gradlew clean
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Architecture

This is a Spring Boot 4.0.2 application. The main entry point is `AiFrameworkSpringApplication.java`.

**Package structure:** `com.tianrenservice.ai_framework_spring` (note: underscores used because Java packages cannot contain hyphens)

**Current state:** Starter project with basic Spring Boot setup. No controllers, services, or repositories have been added yet.

## Technology Stack

- Java 21 (configured via Gradle toolchain)
- Spring Boot 4.0.2
- Gradle 8.14
- JUnit 5 (Jupiter) for testing
