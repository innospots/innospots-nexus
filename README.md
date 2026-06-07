# innospots-nexus

`innospots-nexus` is the lightweight foundation for a new AI enterprise platform.
The project is built incrementally from simple platform capabilities toward
plugins, extensions, and applications.

## Coordinates

- Group: `com.innospots`
- Artifact: `innospots-nexus`
- Version property: `revision`
- Current version: `0.1.0-SNAPSHOT`
- JDK: 25
- Build: Maven

## Version Management

The project uses Maven CI-friendly versions. Keep the single source of truth in
the root `pom.xml`:

```xml
<revision>0.1.0-SNAPSHOT</revision>
```

Module parent versions and internal dependency versions should reference
`${revision}`. The `flatten-maven-plugin` is bound to the build to generate
resolved consumer POMs. Generated `.flattened-pom.xml` files are ignored by git.

## Design Principles

- Domain-driven design first: modules should express domain, application,
  port, extension, and infrastructure boundaries clearly.
- Minimal dependencies: the base framework should only provide foundational
  platform capability.
- No Spring hard dependency in the foundation: Spring can be introduced by
  adapters or application modules when a concrete runtime needs it.
- Developer-driven code creation: old project code may be read for context, but
  must not be copied, moved, or mechanically rewritten into this repository.
- Incremental growth: add modules only after their domain boundary, dependency
  direction, and test strategy are explicit.

## Modules

### `innospots-nexus-parent`

Build parent for internal Java modules. It imports the project BOM, centralizes
plugin configuration, enforces the JDK/Maven baseline, and provides shared
module dependencies such as SLF4J API, Lombok, and test libraries.

### `innospots-nexus-bom`

Dependency management for internal modules and extension candidates. The BOM
keeps versions centralized without forcing downstream modules to inherit a
Spring Boot parent or runtime. It should remain dependency-management only and
must not define inherited module dependencies.

### `innospots-nexus-base`

Pure Java base module. This module is reserved for code-level contracts and
small utilities only. It must not depend on databases, caches, message brokers,
HTTP runtimes, Spring, or other middleware.

The first initialization only creates the module and source directories. No Java
implementation is generated in this stage.

### `innospots-nexus-core`

Core platform module for domain, application, port, and extension boundaries. It
may depend on `innospots-nexus-base` and may define interface-level middleware
ports later, but it should not provide Spring Boot starters, database
auto-configuration, or concrete infrastructure implementations in the foundation
stage.

## Reference Project Policy

The previous project at `/Users/yxy/works/innospots_ent/innospots_premium` is a
reference only. Use it to understand product history, naming pressure, and broad
module responsibilities. Do not copy source files, POM fragments, package
layouts, or implementation details directly.

## Build

```bash
mvn validate
mvn test
mvn -q help:effective-pom
```

The build enforces JDK 25. If Maven runs with an older local JDK, validation is
expected to fail until the local toolchain is switched to JDK 25.
