# Naming Conventions

## Java

| Element | Convention | Example |
|---------|-----------|---------|
| Class | UpperCamelCase | `NexusConfig`, `DataPage`, `HttpUtils` |
| Interface | UpperCamelCase | `IFactorStatement`, `ResourceStore`, `Executor` |
| Enum | UpperCamelCase (singular) | `Mode`, `Operator`, `BasicStatus` |
| Enum constants | UPPER_SNAKE_CASE | `ENABLED`, `IS_NULL`, `GREATER_EQUAL` |
| Record | UpperCamelCase | `DataPage`, `HttpResult`, `PageResult` |
| Annotation | UpperCamelCase with `@` prefix | `@MaskValue`, `@ValueConverter`, `@I18n` |
| Method | lowerCamelCase | `getInputString()`, `normalizeValue()`, `nextId()` |
| getter/setter | `getXxx()` / `isXxx()` / `setXxx()` | `getInput()`, `isSuccessful()`, `hasNext()` |
| Static factory | `of()`, `create()`, `from()`, `named()` | `DataPage.of()`, `DataRequest.create()`, `IdGenerator.from()` |
| Field/param | lowerCamelCase | `pageNo`, `pageSize`, `executionId` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_QUEUE_CAPACITY`, `PLACEHOLDER_PREFIX_1` |
| Package | lowercase, dot-separated, singular | `domain.condition`, `domain.field`, `util` |

## Package Naming

- Group by responsibility and DDD boundary, not by layer type.
- Use singular nouns: `domain.condition` not `domain.conditions`.
- No "impl", "util" sub-packages for implementation details.
- Infrastructure belongs in adapter/plugin modules, not in domain packages.

```java
com.innospots.nexus.base
  ├── config           // configuration store
  ├── domain           // domain primitives
  │   ├── condition    // filter/condition DSL
  │   ├── data         // data operation contracts
  │   ├── field        // field metadata
  │   └── ...
  ├── events           // event bus
  ├── exception        // base exception
  ├── execution        // executor SPI
  ├── http             // HTTP client
  ├── i18n             // internationalization
  ├── json             // JSON utilities
  ├── mapstruct        // mapping support
  ├── resources        // resource store SPI
  ├── status           // status code system
  ├── thread           // concurrency utilities
  ├── ui               // UI descriptors
  └── util             // general utilities
```

## File Naming

- One top-level public class per file.
- File name matches class name (case-sensitive).
- Test files: `{ClassName}Test.java` placed under `src/test/java/` in the
  same package structure.