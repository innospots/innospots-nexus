# Multi-Tenant Governance Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Status:** Phase 1 Tasks 1–10 implemented and verified (`mvn clean compile`, `mvn test` BUILD SUCCESS, 2026-08-27).

**Goal:** Land the Phase 1 skeleton from the governance spec: `innospots-nexus-platform` module, console package boundaries, and the first tenant-domain / ops-domain persistence contracts (Tenant, Enterprise, Workspace, TenantMember, Organization Unit).

**Architecture:** `platform` and `kernel` both depend on `console`; they do not depend on each other. Platform owns `nx_tenant` + `nx_enterprise` (global `BaseEntity`). Kernel owns `nx_workspace`, `nx_tenant_member`, `nx_organization_unit`, `nx_organization_member` (`TenantBaseEntity`). Isolation keys are `tenantId` / `workspaceId`; `ProjectBaseEntity` is gone.

**Tech Stack:** Java 25, Maven multi-module, Jakarta Persistence + MyBatis-Plus, Jakarta REST contracts, JUnit 5 + AssertJ, Lombok.

**Spec:** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §9–§10, §12 Phase 1.

## Global Constraints

- `innospots-nexus-base` stays middleware-free; `core` has no Spring Boot auto-configuration.
- Dependency direction: `base -> core -> console -> {kernel | platform}`; kernel and platform never depend on each other.
- Persistence entities inherit `BaseEntity`, `TenantBaseEntity`, or `WorkspaceBaseEntity`. Never reintroduce `ProjectBaseEntity` or `projectId`.
- Concrete PK fields are `String`, `@TableId(type = IdType.ASSIGN_UUID)`, `@Id`, `@Column(length = 32, nullable = false)`.
- String `@Column` lengths are powers of two. Index names are explicit and table-prefixed.
- Domain `request` / `vo` types are records. Entities use Lombok `@Getter` `@Setter`.
- Import order: `java.*`, third-party (incl. Lombok), `com.innospots.*`. Braces on every `if`/`else`/`for`/`while`.
- Do not copy legacy Innospots source. Do not move role/menu/permission/user off kernel in this plan (Phase 2).
- Do not create empty architectural layers. Do not update module `SKILL.md` unless the user asks for a skill scan.
- Do not git-commit unless the user explicitly asks.
- After Java changes: `mvn clean compile`. After structural POM changes: `mvn validate` and `mvn test`.

## Already Done (do not redo)

- `TenantBaseEntity` / `WorkspaceBaseEntity` in `innospots-nexus-core`
- `TLC.tenantId(String)` / `TLC.workspaceId(String)`
- `AuditMetaObjectHandler` fills `tenantId` + `workspaceId`
- Kernel/core workspace-scoped entities no longer extend `ProjectBaseEntity`

## Out of Scope (later plans)

- Phase 2: split `nx_user` into platform/tenant users; move auth/menu/role/permission to console; delete Group
- Phase 3: `security_realm` on permission/menu
- Phase 4: drop Group tables
- Platform user tables, support-access, audit logs
- Kernel provisioning listener that consumes `TenantCreatedEvent` (event type only in this plan)

## File Map

| File | Responsibility |
|------|----------------|
| `innospots-nexus-platform/pom.xml` | New ops-domain module, depends on console |
| `pom.xml`, `innospots-nexus-bom/pom.xml`, `AGENTS.md` | Aggregator, version, module contract |
| `console/{auth,credential,role,menu,permission,extension,logger,dictionary}/package-info.java` | Intended Phase 2 homes; no business types yet |
| `platform/tenant/domain/entity/TenantEntity.java` | `nx_tenant` |
| `platform/enterprise/domain/entity/EnterpriseEntity.java` | `nx_enterprise` 1:1 with tenant |
| `platform/tenant/domain/event/TenantCreatedEvent.java` | Cross-module collaboration contract |
| `platform/tenant/endpoint/TenantEndpoint.java` | `/platform/tenants` JAX-RS contract |
| `kernel/workspace/domain/entity/WorkspaceEntity.java` | `nx_workspace` |
| `kernel/member/domain/entity/TenantMemberEntity.java` | `nx_tenant_member` |
| `kernel/organization/domain/entity/OrganizationUnitEntity.java` | `nx_organization_unit` |
| `kernel/organization/domain/entity/OrganizationMemberEntity.java` | `nx_organization_member` |

---

### Task 1: Platform Maven module

**Files:**
- Create: `innospots-nexus-platform/pom.xml`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/PlatformModule.java`
- Create: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/PlatformModuleTest.java`
- Modify: `pom.xml` (add `<module>innospots-nexus-platform</module>` after kernel)
- Modify: `innospots-nexus-bom/pom.xml` (add `innospots-nexus-platform` dependencyManagement entry after kernel)
- Modify: `AGENTS.md` (add platform module section; extend dependency rules so kernel and platform both depend on console and not on each other)

**Interfaces:**
- Consumes: existing `innospots-nexus-parent`, `innospots-nexus-console`
- Produces: artifact `com.innospots:innospots-nexus-platform`

- [x] **Step 1: Write the failing module marker test**
- [x] **Step 2: Run test to verify it fails**
- [x] **Step 3: Add aggregator, BOM, POM, marker class, and AGENTS.md**
- [x] **Step 4: Verify compile and the marker test**

```java
package com.innospots.nexus.platform;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformModuleTest {

    @Test
    void platformModuleIsALoadableMarker() {
        assertThat(PlatformModule.class.getPackageName())
                .isEqualTo("com.innospots.nexus.platform");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl innospots-nexus-platform test -Dtest=PlatformModuleTest`
Expected: FAIL because the module / class does not exist.

- [ ] **Step 3: Add aggregator, BOM, POM, marker class, and AGENTS.md**

`innospots-nexus-platform/pom.xml` must inherit `innospots-nexus-parent`, depend on `innospots-nexus-console`, and include the same persistence APIs kernel uses (`jakarta.persistence-api`, `jakarta.transaction-api`, `mybatis-plus-core`, `mybatis-plus-extension`) plus `jakarta.ws.rs-api` (via console or explicit) for endpoints.

`PlatformModule` is an empty public marker with Javadoc: ops-domain platform; depends on console; must not depend on kernel.

AGENTS.md additions:
- New `innospots-nexus-platform` section: owns `nx_tenant`, `nx_enterprise`, later platform users / support access / platform audit; `/platform/**`; no public self-register.
- Dependency: platform may depend on console + transitive core/base; must not depend on kernel.
- Direction: `console -> kernel` and `console -> platform` in parallel.

- [ ] **Step 4: Verify compile and the marker test**

Run: `mvn -pl innospots-nexus-platform -am test -Dtest=PlatformModuleTest`
Expected: PASS.

Run: `mvn validate`
Expected: BUILD SUCCESS.

---

### Task 2: Console package skeleton

**Files:**
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/{auth,credential,role,menu,permission,extension,logger,dictionary}/package-info.java`
- Create: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/ConsolePackageSkeletonTest.java`

**Interfaces:**
- Consumes: existing `com.innospots.nexus.console.endpoint`
- Produces: documented package roots for Phase 2 moves; no entity/endpoint types yet

- [ ] **Step 1: Write the failing package test**

```java
package com.innospots.nexus.console;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsolePackageSkeletonTest {

    @Test
    void consoleDeclaresPhaseTwoPackageRoots() {
        assertThat(com.innospots.nexus.console.auth.package-info.class).isNotNull();
    }
}
```

Do not use `package-info.class` in source (invalid). Assert by loading packages:

```java
@Test
void consoleDeclaresPhaseTwoPackageRoots() {
    String[] packages = {
            "com.innospots.nexus.console.auth",
            "com.innospots.nexus.console.credential",
            "com.innospots.nexus.console.role",
            "com.innospots.nexus.console.menu",
            "com.innospots.nexus.console.permission",
            "com.innospots.nexus.console.extension",
            "com.innospots.nexus.console.logger",
            "com.innospots.nexus.console.dictionary"
    };
    for (String name : packages) {
        assertThat(Package.getPackage(name) == null ? Class.forName(name + ".package-info") : name)
                .isNotNull();
    }
}
```

Use `Class.forName(name + ".package-info")` only — `package-info` compiles to `package-info`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl innospots-nexus-console test -Dtest=ConsolePackageSkeletonTest`
Expected: `ClassNotFoundException` for `com.innospots.nexus.console.auth.package-info`.

- [ ] **Step 3: Add package-info files**

Each file states the Phase 2 responsibility and that concrete types still live in kernel until the move. Example:

```java
/**
 * Login, registration orchestration, token, OAuth protocol, and tenant selection.
 * Concrete types remain in kernel until Phase 2.
 */
package com.innospots.nexus.console.auth;
```

- [ ] **Step 4: Run the test**

Run: `mvn -pl innospots-nexus-console test -Dtest=ConsolePackageSkeletonTest`
Expected: PASS.

---

### Task 3: TenantEntity

**Files:**
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/domain/entity/TenantEntityContractsTest.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/enums/TenantStatus.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/entity/TenantEntity.java`

**Interfaces:**
- Consumes: `BaseEntity`, table `nx_tenant`
- Produces: `TenantEntity` with `idPrefix() == "tnt"`, fields below

Fields (do not redeclare audit columns):

| field | type | column | notes |
|-------|------|--------|-------|
| tenantId | String | PK 32 | prefix `tnt` |
| tenantName | String | 128, not null | |
| tenantCode | String | 64, not null | unique index `uk_nx_tenant_code` |
| status | String | 32, not null | `TenantStatus` persisted as name |
| planCode | String | 64, nullable | |
| ownerTenantUserId | String | 32, nullable | |

`TenantStatus`: `ACTIVE`, `SUSPENDED`, `ARCHIVED`.

- [ ] **Step 1: Write the failing contract test** (table name, superclass `BaseEntity`, PK, fields, unique index on `tenant_code`, `idPrefix()` via a new instance).
- [ ] **Step 2: Run** `mvn -pl innospots-nexus-platform test -Dtest=TenantEntityContractsTest` — Expected: compile failure / missing class.
- [ ] **Step 3: Implement `TenantStatus` + `TenantEntity`.**
- [ ] **Step 4: Re-run the test — Expected: PASS.** `mvn -pl innospots-nexus-platform -am clean compile`.

---

### Task 4: EnterpriseEntity

**Files:**
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/enterprise/domain/entity/EnterpriseEntityContractsTest.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/enterprise/domain/entity/EnterpriseEntity.java`

**Interfaces:**
- Consumes: `BaseEntity`, `TenantEntity.tenantId`
- Produces: `nx_enterprise`, `idPrefix() == "ent"`, unique `tenant_id`

| field | type | length | nullable |
|-------|------|--------|----------|
| enterpriseId | String PK | 32 | no |
| tenantId | String | 32 | no |
| legalName | String | 256 | no |
| creditCode | String | 64 | yes |
| industry | String | 64 | yes |
| contactName | String | 128 | yes |
| contactPhone | String | 32 | yes |
| contactEmail | String | 128 | yes |
| address | String | 512 | yes |
| extra | String | 1024 | yes (`columnDefinition` not required; length 1024) |

Indexes: `uk_nx_enterprise_tenant` unique `tenant_id`.

- [ ] **Step 1: Failing contract test.**
- [ ] **Step 2: Run test — missing class.**
- [ ] **Step 3: Implement entity.**
- [ ] **Step 4: Test PASS + `mvn -pl innospots-nexus-platform -am clean compile`.**

---

### Task 5: TenantCreatedEvent + Tenant DAO/operator create

**Files:**
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/event/TenantCreatedEvent.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/dao/TenantDao.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/enterprise/dao/EnterpriseDao.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/operator/TenantOperator.java`
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/operator/TenantOperatorTest.java`
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/dao/TenantDaoContractsTest.java`

**Interfaces:**
- Consumes: `TenantEntity`, `EnterpriseEntity`, `DomainEvent`
- Produces:

```java
public record TenantCreatedEvent(String tenantId, String tenantCode, String ownerTenantUserId)
        implements DomainEvent {
    @Override
    public String eventType() {
        return "platform.tenant.created";
    }
}

public interface TenantDao extends BaseMapper<TenantEntity> {}
public interface EnterpriseDao extends BaseMapper<EnterpriseEntity> {}

public TenantEntity create(TenantEntity tenant, EnterpriseEntity enterprise);
```

`TenantOperator.create`:
- reject null tenant / enterprise
- reject blank `tenantCode` / `tenantName` / `legalName`
- default tenant status to `ACTIVE` when blank
- set `enterprise.tenantId` from the persisted tenant id after insert
- insert tenant then enterprise
- do not call kernel

- [ ] **Step 1: Write `TenantOperatorTest` with Mockito DAOs proving create inserts both rows and copies tenantId onto enterprise.**
- [ ] **Step 2: Run — missing operator.**
- [ ] **Step 3: Implement DAOs + operator (minimal validation).**
- [ ] **Step 4: Tests PASS.**

---

### Task 6: Platform TenantEndpoint contract

**Files:**
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/request/TenantCreateRequest.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/vo/TenantVo.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/endpoint/TenantEndpoint.java`
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/endpoint/TenantEndpointContractsTest.java`

**Interfaces:**
- Produces JAX-RS interface `@Path("/platform/tenants")`:

```java
@POST R<TenantVo> createTenant(TenantCreateRequest request);
@GET @Path("/{tenantId}") R<TenantVo> getTenant(@PathParam("tenantId") String tenantId);
```

`TenantCreateRequest` record: `tenantName`, `tenantCode`, `planCode`, `ownerTenantUserId`, `legalName`, `creditCode`, `industry`, `contactName`, `contactPhone`, `contactEmail`, `address`.

`TenantVo` record: `tenantId`, `tenantName`, `tenantCode`, `status`, `planCode`, `ownerTenantUserId`, `enterpriseId`, `legalName`.

No runtime implementation class in this task (contract only, same as `RoleEndpoint`).

- [ ] **Step 1: Failing endpoint contract test** (`Path` = `/platform/tenants`, POST create, GET by id, request/vo are records).
- [ ] **Step 2: Run — missing types.**
- [ ] **Step 3: Add request, vo, endpoint interface.**
- [ ] **Step 4: Test PASS + `mvn -pl innospots-nexus-platform -am clean compile`.**

---

### Task 7: WorkspaceEntity

**Files:**
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/workspace/domain/entity/WorkspaceEntityContractsTest.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/workspace/domain/entity/WorkspaceEntity.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/workspace/dao/WorkspaceDao.java`
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/workspace/dao/WorkspaceDaoContractsTest.java`

**Interfaces:**
- Superclass: `TenantBaseEntity` (inherits `tenantId`; do not redeclare)
- Table: `nx_workspace`
- Prefix: `wks`
- Fields: `workspaceId` PK, `workspaceName` 128 not null, `workspaceCode` 64 not null, `description` 512 nullable, `status` 32 not null
- Unique: `uk_nx_workspace_tenant_code` on `tenant_id,workspace_code`

- [ ] **Step 1: Failing contract test.**
- [ ] **Step 2: Run — missing class.**
- [ ] **Step 3: Entity + DAO.**
- [ ] **Step 4: Tests PASS + `mvn -pl innospots-nexus-kernel -am clean compile`.**

---

### Task 8: TenantMemberEntity

**Files:**
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/member/domain/entity/TenantMemberEntityContractsTest.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/member/domain/enums/TenantMemberStatus.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/member/domain/entity/TenantMemberEntity.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/member/dao/TenantMemberDao.java`
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/member/dao/TenantMemberDaoContractsTest.java`

**Interfaces:**
- Superclass: `TenantBaseEntity`
- Table: `nx_tenant_member`
- Prefix: `tmb`
- Fields: `tenantMemberId` PK, `tenantUserId` 32 not null, `status` 32 not null, `joinedAt` `LocalDateTime` not null
- Unique: `uk_nx_tenant_member_user` on `tenant_id,tenant_user_id`
- `TenantMemberStatus`: `ACTIVE`, `DISABLED`, `PENDING`

- [ ] **Step 1–4:** TDD contract + entity + DAO.

---

### Task 9: Organization Unit and Member

**Files:**
- Tests under `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/organization/`
- Create: `.../organization/domain/enums/OrganizationUnitType.java` (`COMPANY`, `BRANCH`, `DEPARTMENT`, `TEAM`)
- Create: `.../organization/domain/entity/OrganizationUnitEntity.java`
- Create: `.../organization/domain/entity/OrganizationMemberEntity.java`
- Create: matching DAOs

**Interfaces:**

`nx_organization_unit` extends `TenantBaseEntity`, prefix `org`:
- `unitId` PK, `parentId` 32 nullable, `unitCode` 64 not null, `unitName` 128 not null, `unitType` 32 not null, `sortOrder` Integer not null, `status` 32 not null
- Unique `uk_nx_organization_unit_code` on `tenant_id,unit_code`
- Index `idx_nx_organization_unit_parent` on `tenant_id,parent_id,sort_order`

`nx_organization_member` extends `TenantBaseEntity`, prefix `ogm`:
- `organizationMemberId` PK, `unitId` 32 not null, `tenantMemberId` 32 not null
- Unique `uk_nx_organization_member` on `tenant_id,unit_id,tenant_member_id`

- [ ] **Step 1–4:** TDD both entities and DAOs.

---

### Task 10: Phase 1 verification

- [ ] **Step 1:** `mvn clean compile`
- [ ] **Step 2:** `mvn test`
- [ ] **Step 3:** Confirm no `ProjectBaseEntity` / `TLC.projectId` in production Java.

## Self-Review

- Spec §9.5 platform module → Task 1
- Spec §9.3 console packages → Task 2
- Spec §10.3 `nx_tenant` / `nx_enterprise` → Tasks 3–6
- Spec §10.6 `nx_workspace` → Task 7
- Spec §10.4 member / org → Tasks 8–9
- Spec §11.2 isolation bases → already done; not reintroduced
- User split, console move, Group delete → Phase 2 plan, not this file
