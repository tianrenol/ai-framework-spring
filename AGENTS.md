# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java` holds application code. The main entry point is `com.tianrenservice.ai_framework_spring.AiFrameworkSpringApplication`.
- `src/main/resources` stores runtime configuration such as `application.properties`.
- `src/test/java` contains JUnit tests mirroring the main package structure (for example, `AiFrameworkSpringApplicationTests`).
- `build/` is Gradle output and should not be edited manually.

## Build, Test, and Development Commands
Use the Gradle wrapper to keep tooling consistent:
- `./gradlew build` (or `gradlew.bat build` on Windows): compile and run tests.
- `./gradlew test`: run all tests.
- `./gradlew test --tests "com.tianrenservice.ai_framework_spring.SomeTest"`: run a single test class.
- `./gradlew bootRun`: run the Spring Boot app locally.
- `./gradlew clean`: remove build outputs.

## Coding Style & Naming Conventions
- Language: Java 21 with Spring Boot 4.0.2.
- Follow existing formatting: tabs for indentation and braces on the same line.
- Packages use underscores (e.g., `ai_framework_spring`) since hyphens are invalid in Java packages.
- Class names use PascalCase; test classes typically end with `Tests`.
- No formatter or linter is configured, so keep changes consistent with nearby code.

## Testing Guidelines
- Test framework: JUnit 5 (Jupiter) via `spring-boot-starter-test`.
- Keep test method names descriptive (e.g., `contextLoads`).
- Place tests under `src/test/java` and mirror the main package layout.

## Commit & Pull Request Guidelines
- Commit messages are short, plain-English subjects (e.g., `init`, `Initial commit`).
- PRs should include a brief summary, steps to test, and any configuration changes.
- Link related issues when applicable.

## Security & Configuration Tips
- Do not commit secrets to `application.properties`.
- Use environment variables or local overrides for credentials and API keys.
- If you add configuration keys, document them in the PR description and update any samples.
