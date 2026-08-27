# Multi-Tenant Governance Phase 2c Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish remaining Phase 2 console isolation: TLC realm identity keys, `security_realm` on console IAM tables, and move extension runtime plus logger out of kernel.

**Architecture:** Console owns reusable control-plane persistence and runtime (role/menu/permission/extension/logger). Core keeps extension *declaration* contracts. Kernel keeps tenant domain plus permission *sync* that calls console `ExtensionRegistry`. Platform stays ops-only.

**Tech Stack:** Java 25, Maven, Jakarta Persistence + MyBatis-Plus, JUnit 5 + AssertJ, Lombok.

**Spec:** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §8.3, §9.3, §10.5–10.7, §12 Phase 2.

## Global Constraints

- Domain then responsibility packages; requests/VOs under `domain.request` / `domain.vo`.
- Console does not persist users. Kernel/platform do not issue tokens.
- Reuse `console.auth.domain.enums.SecurityRealm`. Persist as String 32, not null.
- Do not update module `SKILL.md`. Do not commit unless asked.
- After Java changes: `mvn clean compile`. After the slice: `mvn test`.

---

### Task 1: TLC realm identity keys

Add `securityRealm`, `tenantMemberId`, `platformUserId` to `TLC` with typed accessors. Extend `TLCTest`. Keep existing `userId` as Long.

- [x] Completed

### Task 2: `security_realm` + CAPABILITY

Add `securityRealm` to `RoleEntity`, `MenuEntity`, `PermissionResourceEntity`, `PermissionGrantEntity`. Add `PermissionResourceType.CAPABILITY`. Expose realm on `RoleCreateRequest` / `RoleVo`.

- [x] Completed

### Task 3: Move extension runtime to console

Relocate `kernel.extension` (entity/dao/repository/service/discovery + tests) to `console.extension` with the same inner layout. Update `PermissionResourceSyncService` to use console `ExtensionRegistry`. Leave kernel extension empty.

- [x] Completed

### Task 4: Move logger to console

Relocate `kernel.logger` to `console.logger` (`dao`, `domain.entity`, `domain.context`, `operator`, `handler`). Keep `@AuditLog` / `LogExecutor` at the logger domain root.

- [x] Completed

### Task 5: Verify

`mvn clean compile` && `mvn test`. No kernel production `extension`/`logger` packages.

- [x] Completed
