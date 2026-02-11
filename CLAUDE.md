# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

Gradle-based Spring Boot project, Java 21. On Windows use `gradlew.bat` instead of `./gradlew`.

```bash
./gradlew build          # Build
./gradlew test           # Run all tests
./gradlew test --tests "com.tianrenservice.ai_framework_spring.SomeTest"  # Single test
./gradlew bootRun        # Run app
./gradlew clean          # Clean
```

## Architecture

This is a reusable business process orchestration framework with recording/replay capabilities, built as a Spring Boot 4.0.2 auto-configuration library.

**Package:** `com.tianrenservice.ai_framework_spring` (underscores because Java packages can't have hyphens)

### Core Layers

**`core.pipeline`** — The pipeline orchestration engine:
- `BusinessFacade` — Template-method entry point. Subclass and implement `doProcess(T)` to define business logic. Drives the full lifecycle: build → ready → process → complete.
- `BusinessAssembly` — Pipeline orchestrator that manages `BusinessAssemblyUnit` nodes. Handles test case modes (RECORD, REPLAY, CHECK, REVIEW, REGENERATE). Configured statically with `TypeRegistry` and `JsonSerializer`.
- `BusinessAssemblyUnit` — Single execution node in a pipeline, holding context + entity + deal VO.
- `BusinessContext` — Carries the input VO and assembly reference through the pipeline.

**`core.record`** — Recording/replay system:
- `@RecordAndReplay` annotation — Marks methods/classes for interaction recording. Applied via AOP.
- `RecordAndReplayAspect` — AOP aspect that intercepts annotated methods. In RECORD mode, captures calls; in REPLAY/CHECK/REVIEW modes, replays from stored records.
- `BusinessEnv` — Holds interaction records and manages mode-specific behavior.
- `InteractionRecord` — Stores method name, args, and result for a single interaction.

**`core.entity`** — Domain model base classes:
- `BusinessEntity` — Core container for business logic, wraps a `BusinessHelper`.
- `BusinessHelper` — Manages context, env, and lifecycle hooks (saveDB, delRedis, finish). Uses `BeanProvider` SPI for dependency lookup.

**`core.spi`** — Extension points (all replaceable via Spring `@Bean`):
- `TypeRegistry` — Registers and resolves business types and assembly types at runtime (replaces hardcoded enums).
- `JsonSerializer` — JSON serialization contract.
- `BeanProvider` — Spring bean lookup abstraction.
- `BusinessTypeIdentifier` / `AssemblyTypeIdentifier` / `ScopeIdentifier` — Type identity contracts.
- `TestCasePersistenceService` — Persistence for test cases.

**`core.constant`** — `BusinessMode` enum: LIVE, RECORD, REPLAY, CHECK, REVIEW, REGENERATE.

**`autoconfigure`** — Spring Boot auto-configuration:
- `AiFrameworkAutoConfiguration` — Wires default SPI implementations (Jackson, Spring context, DefaultTypeRegistry) and initializes the framework. All beans are `@ConditionalOnMissingBean` so consumers can override.
- `AiFrameworkProperties` — Config prefix `ai-framework` with `record-enabled` and `aspect-enabled` flags.

### Key Patterns

- **SPI-driven extensibility**: Core depends on interfaces (`core.spi`), auto-configuration provides defaults. Consumers override by declaring their own `@Bean`.
- **Template method**: `BusinessFacade.process()` orchestrates the lifecycle; subclasses implement `doProcess()`.
- **Reflective invocation via `CacheInvoke`**: Used throughout for dynamic method dispatch with caching.
- **Six business modes** control framework behavior — LIVE for production, RECORD to capture interactions, REPLAY/CHECK/REVIEW/REGENERATE for test case execution.

## Technology Stack

- Java 21 (Gradle toolchain)
- Spring Boot 4.0.2
- Gradle 8.14
- Lombok (compile-only)
- Spring AOP + AspectJ for record/replay
- Jackson for JSON serialization
- JUnit 5 for testing
