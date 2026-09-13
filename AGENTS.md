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
- Do not create, update, or synchronize module API reference docs under
  `skills/java/java-reference/references/modules/` as part of ordinary code
  development.
- Module API reference documentation may be generated or refreshed only when a
  developer explicitly requests a module or project directory scan. That
  operation must update the selected documentation set together, rather than
  incrementally following individual code changes.

## Module Responsibilities

### `innospots-nexus-base`

- Pure Java foundation for shared dependencies, contracts, primitives, and
  utility packages.
- Provides reusable capabilities such as exceptions, status codes, response
  wrappers, domain-event contracts, MapStruct support, JSON helpers, ID
  generation, cryptography, HTTP utilities, condition DSL, execution SPI,
  in-process events, and other dependency-light tools.
- Owns **transport/session snapshots** (`UserSnapshot`, `TenantSnapshot`,
  `OrganizationSnapshot`, `WorkspaceSnapshot`, `ProjectSnapshot`, etc.)
  as shared serializable shapes; kernel owns business entities and workflows.
- Scope hierarchy: **Tenant → Workspace (shared resources) → Project (business
  isolation)**. `OrganizationSnapshot` is the tenant business profile, not
  kernel `OrganizationUnit`.
- Must not contain business-domain logic or persistence bindings.
- Must remain middleware-free and must not depend on database, messaging,
  scheduling, Servlet, Spring, Quarkus, or other runtime infrastructure.
- Module API reference lives under
  `skills/java/java-reference/references/modules/<artifact-id>/README.md`
  (index only — not a `SKILL.md`; not under `src/main/resources/skills/`).
  Current indexes: `innospots-nexus-base`, `innospots-nexus-core`.
- **Reserved without current consumers:** `domain.condition` (filter DSL),
  `execution` (executor SPI). Do not remove without an explicit boundary
  decision; wire a real consumer or document as experimental before expanding.
- **New public APIs** in base require at least two upper-module consumers,
  unless explicitly marked experimental or reserved.
- **Forbidden in base** (belong in core, adapters, or business modules):
  - Cache (local or distributed)
  - Retry / circuit breaker
  - Jakarta Bean Validation
  - Scheduling runtime (Quartz/cron logic; scheduling enums may live in core)
  - Messaging middleware (Kafka and similar)
  - ORM / JDBC / connection pools
  - Spring / Servlet bindings
  - Business domain entities and service workflows

### `innospots-nexus-core`

- Extends `innospots-nexus-base` with business-neutral middleware, database,
  and platform infrastructure support.
- Owns shared persistence base entities (`BaseEntity` → `TenantBaseEntity` →
  `WorkspaceBaseEntity` → `ProjectBaseEntity`), audit fill, ID generation,
  Quartz scheduling, service-node registry, watcher runtime, startup SPI, and
  workspace-scoped file metadata (`nx_meta_resource` + `MetaResourceService`).
- Must remain business-domain neutral. User, role, permission, menu, catalog
  index, and other management-console concerns do not belong in this module.
- May depend on middleware APIs and implementations needed for reusable
  platform support, but must not bind itself to Spring Boot auto-configuration.
- **Forbidden in core** (belong in plugin, console, kernel, platform, or
  adapters):
  - Classpath plugin runtime, contribution decoders, Page DSL, plugin
    installation tables
  - Jakarta REST endpoints and console VOs
  - User/role/permission/menu/dictionary business entities and workflows
  - Auth/session conversation or chat product domains
  - Spring / Quarkus / Servlet bindings
- **New public APIs** in core require at least two upper-module consumers,
  unless explicitly marked experimental.
- Binary storage SPI stays in `base.resources.ResourceStore`; core binds stores
  to persisted metadata via `core.resource.storage.ResourceStorageRegistry`.

### `innospots-nexus-plugin`

- Extends `innospots-nexus-core` with classpath plugin runtime and contribution
  processing.
- Owns plugin discovery, declaration, lifecycle, installation, capability
  routing, contribution decode/validate/snapshot, and **Pactor Page DSL 1.0**
  under `core.plugin.contribution.console.ui.spec`.
- Owns `console@1` contribution contracts and runtime handlers; it does **not**
  own the persisted console catalog index (`nx_console_catalog_resource` — that
  belongs in `innospots-nexus-console`).
- Must remain business-domain neutral and middleware-binding-free (no Spring
  Boot auto-configuration).
- Package names remain under `com.innospots.nexus.core.plugin` for compatibility;
  the Maven artifact is `innospots-nexus-plugin`.

### `innospots-nexus-console`

- Management-console **API surface** module built on Core and Plugin.
- Provides Jakarta REST management endpoints, request/response VOs, converters,
  and the persisted **console catalog index** (`console.catalog.*`).
- Must **not** own plugin specification or contribution constraint definitions;
  those belong in `innospots-nexus-plugin`.
- Must not implement concrete management business functions. User, role,
  permission, registration, and other management features belong in business
  modules such as `innospots-nexus-kernel`.
- Module API reference lives under
  `skills/java/java-reference/references/modules/innospots-nexus-console/`
  (not under `src/main/resources/skills/`).

### `innospots-nexus-kernel`

- Core Nexus business-function module built on the console and core
  foundations.
- Owns foundational management capabilities such as authentication,
  registration, users, roles, permissions, menus, dictionaries, audit support,
  and other baseline platform functions.
- Organizes business code by domain first, then by responsibility packages
  such as `endpoint`, `dao`, `domain`, `converter`, `operator`, `service`,
  `handler`, `interceptor`, and `listener` (`kernel.role.endpoint`, not
  `kernel.endpoint.role`). Large domains use functional subpackages
  (`permission.authorization`, `grant.service`); no module-level `service`
  dumping ground; at most 15 `.java` files per package directory. See
  `skills/java/java-reference/references/package-structure.md`.
- Must use the shared infrastructure and contracts from `base`, `core`, and
  `console` rather than reimplementing them.

### `innospots-nexus-platform`

- Ops-domain platform built on the console foundation, parallel to kernel.
- Owns tenant lifecycle (`nx_tenant`), enterprise legal profile
  (`nx_enterprise`), and later platform users, support access, and platform
  audit.
- Exposes `/platform/**` contracts. Must not provide public self-registration.
- Must depend on `console` (and transitive `core` / `base`). Must not depend
  on `innospots-nexus-kernel`.

## Dependency Rules

- `innospots-nexus-base` must remain middleware-free.
- Internal Java modules should inherit `innospots-nexus-parent`.
- Dependency versions belong in `innospots-nexus-bom`.
- Shared Java module dependencies belong in `innospots-nexus-parent`, not in the
  root aggregator or BOM.
- `innospots-nexus-core` may depend on `innospots-nexus-base`.
- `innospots-nexus-plugin` may depend on `innospots-nexus-core` and the
  transitive base foundation.
- `innospots-nexus-console` may depend on `innospots-nexus-core`,
  `innospots-nexus-plugin`, and the transitive base foundation.
- `innospots-nexus-kernel` may depend on `innospots-nexus-console`,
  `innospots-nexus-core`, and their transitive base foundation.
- `innospots-nexus-platform` may depend on `innospots-nexus-console`,
  `innospots-nexus-core`, and their transitive base foundation.
- The primary dependency direction is
  `innospots-nexus-base -> innospots-nexus-core -> innospots-nexus-plugin ->
  innospots-nexus-console`, then `console -> innospots-nexus-kernel` and
  `console -> innospots-nexus-platform` in parallel. Kernel and platform must
  not depend on each other. Dependencies must not point back toward a higher
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

Read the following files under `skills/java/java-reference/standards/` for complete rules. AI
agents must load these files before generating code or documentation.

| File | Content |
|------|---------|
| [`skills/java/java-reference/standards/code-style.md`](skills/java/java-reference/standards/code-style.md) | Braces, indentation, line width, import order |
| [`skills/java/java-reference/standards/code-comments.md`](skills/java/java-reference/standards/code-comments.md) | Javadoc tiers (class, method, inline) |
| [`skills/java/java-reference/standards/naming.md`](skills/java/java-reference/standards/naming.md) | Naming conventions for Java, packages, files |
| [`skills/java/java-reference/standards/api-design.md`](skills/java/java-reference/standards/api-design.md) | Method signatures, immutability, null handling, exceptions |
| [`skills/java/java-reference/standards/domain-module-initialization.md`](skills/java/java-reference/standards/domain-module-initialization.md) | Stage-gated workflow for initializing a business domain |
| [`skills/java/java-reference/standards/module-skills.md`](skills/java/java-reference/standards/module-skills.md) | Module API index (`README.md`) and references/ directory format |

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
