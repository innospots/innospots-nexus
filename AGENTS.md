# Agent Operating Guide

This repository is a greenfield reconstruction. The previous Innospots project
is a reference, not a source template.

## Core Constraints

- Never copy legacy source code into this repository.
- Never move legacy files into this repository.
- Never reproduce legacy POM or package structure mechanically.
- Ask for or infer the current developer intent before creating new behavior.
- Keep the foundation lightweight and dependency-minimal.
- **代码生成完成后必须编译验证** — 每次修改完 Java 源文件后，立即运行 `mvn clean compile` 确保没有不可编译的代码。禁止生成不能编译的通代码。
- Do not create, update, or synchronize module `SKILL.md` files or their
  `references/` documentation as part of ordinary code development.
- Skills documentation may be generated or refreshed only when a developer
  explicitly requests a module or project directory scan. That operation must
  update the selected documentation set together, rather than incrementally
  following individual code changes.

## Module Responsibilities

### `innospots-nexus-base`

- Pure Java foundation for shared dependencies, contracts, primitives, and
  utility packages.
- Provides reusable capabilities such as exceptions, status codes, response
  wrappers, domain-event contracts, MapStruct support, JSON helpers, ID
  generation, cryptography, HTTP utilities, and other dependency-light tools.
- Must not contain business-domain logic.
- Must remain middleware-free and must not depend on database, messaging,
  scheduling, Servlet, Spring, Quarkus, or other runtime infrastructure.

### `innospots-nexus-core`

- Extends `innospots-nexus-base` with business-neutral middleware, database,
  and platform infrastructure support.
- May provide shared persistence entities and public tables, database support,
  scheduling, server lifecycle, session infrastructure, watchers, extension
  boundaries, and other non-management common capabilities.
- Must remain business-domain neutral. User, role, permission, menu, and other
  concrete business domains do not belong in this module.
- May depend on middleware APIs and implementations needed for reusable
  platform support, but must not bind itself to Spring Boot
  auto-configuration.

### `innospots-nexus-console`

- Foundation and extension contract module for the management platform.
- Provides business-neutral management-console support such as Jakarta REST
  endpoint contracts, extension declarations, menu/route contribution models,
  and shared management-platform abstractions.
- Management-platform feature modules should depend on this module when they
  expose console capabilities.
- Must not implement concrete management business functions. User, role,
  permission, registration, and other management features belong in business
  modules such as `innospots-nexus-kernel`.

### `innospots-nexus-kernel`

- Core Nexus business-function module built on the console and core
  foundations.
- Owns foundational management capabilities such as authentication,
  registration, users, roles, permissions, menus, dictionaries, audit support,
  and other baseline platform functions.
- Organizes business code by domain first, then by responsibility packages
  such as `endpoint`, `dao`, `domain`, `converter`, `operator`, `service`,
  `handler`, `interceptor`, and `listener`.
- Must use the shared infrastructure and contracts from `base`, `core`, and
  `console` rather than reimplementing them.

## Dependency Rules

- `innospots-nexus-base` must remain middleware-free.
- Internal Java modules should inherit `innospots-nexus-parent`.
- Dependency versions belong in `innospots-nexus-bom`.
- Shared Java module dependencies belong in `innospots-nexus-parent`, not in the
  root aggregator or BOM.
- `innospots-nexus-core` may depend on `innospots-nexus-base`.
- `innospots-nexus-console` may depend on `innospots-nexus-core` and the
  transitive base foundation.
- `innospots-nexus-kernel` may depend on `innospots-nexus-console`,
  `innospots-nexus-core`, and their transitive base foundation.
- The primary dependency direction is
  `innospots-nexus-base -> innospots-nexus-core -> innospots-nexus-console
  -> innospots-nexus-kernel`; dependencies must not point back toward a higher
  layer.
- `innospots-nexus-core` may provide concrete business-neutral middleware and
  database support, but must not bind itself to Spring Boot
  auto-configuration.
- Business-specific infrastructure belongs with its owning business module or
  in a dedicated adapter, plugin, extension, or application module.

## DDD Rules

- Name packages and modules by responsibility and boundary.
- Keep domain concepts independent from infrastructure implementations.
- Prefer ports and adapters for middleware integration.
- Add new modules only when the boundary is clear enough to test independently.

## Coding Standards

Read the following files under `standards/` for complete rules. AI agents
must load these files before generating code or documentation.

| File | Content |
|------|---------|
| [`standards/code-style.md`](standards/code-style.md) | Braces, indentation, line width, import order |
| [`standards/code-comments.md`](standards/code-comments.md) | Javadoc tiers (class, method, inline) |
| [`standards/naming.md`](standards/naming.md) | Naming conventions for Java, packages, files |
| [`standards/api-design.md`](standards/api-design.md) | Method signatures, immutability, null handling, exceptions |
| [`standards/domain-module-initialization.md`](standards/domain-module-initialization.md) | Stage-gated workflow for initializing a business domain |
| [`standards/module-skills.md`](standards/module-skills.md) | SKILL.md and references/ directory format |

## Verification

Run these commands after structural changes when the local JDK supports the
configured release:

```bash
mvn validate
mvn test
mvn -q help:effective-pom
```

If the local JDK is older than 25, report the environment mismatch instead of
lowering the project baseline.
