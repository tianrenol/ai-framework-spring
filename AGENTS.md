# Repository Guidelines

## Project Structure and Module Organization
- `src/main/java` contains application code. The main entry point is `com.tianrenservice.ai_framework_spring.AiFrameworkSpringApplication`.
- `src/main/resources` holds runtime configuration such as `application.properties`.
- `src/test/java` contains JUnit tests (for example, `AiFrameworkSpringApplicationTests`).
- `build/` is generated output from Gradle builds.

## Build, Test, and Development Commands
Use the Gradle wrapper to keep tooling consistent.

```bash
./gradlew build      # Compile and run tests
./gradlew test       # Run all tests
./gradlew bootRun    # Run the Spring Boot app locally
./gradlew clean      # Remove build outputs
./gradlew test --tests "com.tianrenservice.ai_framework_spring.SomeTest"  # Single test class
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Coding Style and Naming Conventions
- Language: Java 21 with Spring Boot 4.0.2.
- Package naming uses underscores (for example, `ai_framework_spring`) because hyphens are invalid in Java packages.
- Follow existing formatting (current sources use tabs and braces on the same line).
- Class names use PascalCase; test classes typically end with `Tests`.
- No formatter or linter is configured; keep changes consistent with neighboring code.

## Testing Guidelines
- Test framework: JUnit 5 (Jupiter) via `spring-boot-starter-test`.
- Place tests under `src/test/java` mirroring the main package structure.
- Prefer descriptive test method names (for example, `contextLoads`).
- Run `./gradlew test` before submitting changes.

## Commit and Pull Request Guidelines
- Git history is minimal and uses short, plain-English subjects (for example, "Initial commit", "init"). Keep commit messages brief and descriptive.
- PRs should include: a clear summary, steps to test, and any config changes. Link related issues when applicable.

## Security and Configuration Tips
- Keep secrets out of `application.properties`. Use environment variables or local overrides for credentials.
- If you add configuration keys, document them in the PR description and update samples if needed.
