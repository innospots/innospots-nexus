# Agent Operating Guide

This repository is a greenfield reconstruction. The previous Innospots project
is a reference, not a source template.

## Core Constraints

- Never copy legacy source code into this repository.
- Never move legacy files into this repository.
- Never reproduce legacy POM or package structure mechanically.
- Ask for or infer the current developer intent before creating new behavior.
- Keep the foundation lightweight and dependency-minimal.

## Dependency Rules

- `innospots-nexus-base` must remain middleware-free.
- Internal Java modules should inherit `innospots-nexus-parent`.
- Dependency versions belong in `innospots-nexus-bom`.
- Shared Java module dependencies belong in `innospots-nexus-parent`, not in the
  root aggregator or BOM.
- `innospots-nexus-core` may depend on `innospots-nexus-base`.
- `innospots-nexus-core` may define port/interface-level middleware boundaries
  later, but must not bind itself to Spring Boot auto-configuration.
- Concrete infrastructure belongs in future adapter, plugin, extension, or
  application modules.

## DDD Rules

- Name packages and modules by responsibility and boundary.
- Keep domain concepts independent from infrastructure implementations.
- Prefer ports and adapters for middleware integration.
- Add new modules only when the boundary is clear enough to test independently.

## Coding Style

- All `if`, `else`, `for`, `while` blocks must use braces `{}`, even for
  single-statement bodies. No bare statements on the same line as the
  condition.
- Use 4-space indentation.
- Keep line width reasonable (prefer 120 chars max).

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
