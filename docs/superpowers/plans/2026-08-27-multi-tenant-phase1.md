# 多租户治理 Phase 1 实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**状态：** Phase 1 Task 1–10 已实现并验证（`mvn clean compile`、`mvn test` BUILD SUCCESS，2026-08-27）。

**目标：** 落地治理规格中的 Phase 1 骨架：`innospots-nexus-platform` module、console package boundary，以及首批 tenant-domain / ops-domain 持久化契约（Tenant、Enterprise、Workspace、TenantMember、Organization Unit）。

**架构：** `platform` 与 `kernel` 均依赖 `console`；彼此不依赖。Platform 拥有 `nx_tenant` + `nx_enterprise`（global `BaseEntity`）。Kernel 拥有 `nx_workspace`、`nx_tenant_member`、`nx_organization_unit`、`nx_organization_member`（`TenantBaseEntity`）。隔离键为 `tenantId` / `workspaceId`；`ProjectBaseEntity` 已移除。

**技术栈：** Java 25、Maven multi-module、Jakarta Persistence + MyBatis-Plus、Jakarta REST 契约、JUnit 5 + AssertJ、Lombok。

**规格：** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §9–§10、§12 Phase 1。

## 全局约束

- `innospots-nexus-base` 保持 middleware-free；`core` 无 Spring Boot auto-configuration。
- 依赖方向：`base -> core -> console -> {kernel | platform}`；kernel 与 platform 永不相互依赖。
- 持久化 entity 继承 `BaseEntity`、`TenantBaseEntity` 或 `WorkspaceBaseEntity`。不得重新引入 `ProjectBaseEntity` 或 `projectId`。
- 具体 PK field 为 `String`，`@TableId(type = IdType.ASSIGN_UUID)`、`@Id`、`@Column(length = 32, nullable = false)`。
- String `@Column` 长度为 2 的幂。Index 名称显式且 table-prefixed。
- Domain `request` / `vo` 类型为 record。Entity 使用 Lombok `@Getter` `@Setter`。
- Import 顺序：`java.*`、third-party（含 Lombok）、`com.innospots.*`。每个 `if`/`else`/`for`/`while` 使用花括号。
- 不得复制 legacy Innospots 源码。本计划不将 role/menu/permission/user 迁出 kernel（Phase 2）。
- 不得创建空 architectural layer。除非用户要求 skill scan，否则不更新 module `SKILL.md`。
- 除非用户明确要求，否则不 git-commit。
- Java 变更后：`mvn clean compile`。结构 POM 变更后：`mvn validate` 与 `mvn test`。

## 已完成（勿重做）

- `innospots-nexus-core` 中的 `TenantBaseEntity` / `WorkspaceBaseEntity`
- `TLC.tenantId(String)` / `TLC.workspaceId(String)`
- `AuditMetaObjectHandler` 填充 `tenantId` + `workspaceId`
- Kernel/core workspace-scoped entity 不再继承 `ProjectBaseEntity`

## 不在范围（后续计划）

- Phase 2：拆分 `nx_user` 为 platform/tenant user；将 auth/menu/role/permission 迁至 console；删除 Group
- Phase 3：permission/menu 上的 `security_realm`
- Phase 4：删除 Group 表
- Platform user 表、support-access、audit log
- 消费 `TenantCreatedEvent` 的 Kernel provisioning listener（本计划仅 event type）

## 文件映射

| 文件 | 职责 |
|------|----------------|
| `innospots-nexus-platform/pom.xml` | 新 ops-domain 模块，依赖 console |
| `pom.xml`, `innospots-nexus-bom/pom.xml`, `AGENTS.md` | Aggregator、版本、模块契约 |
| `console/{auth,credential,role,menu,permission,extension,logger,dictionary}/package-info.java` | Phase 2 目标归属；尚无业务类型 |
| `platform/tenant/domain/entity/TenantEntity.java` | `nx_tenant` |
| `platform/enterprise/domain/entity/EnterpriseEntity.java` | `nx_enterprise` 与 tenant 1:1 |
| `platform/tenant/domain/event/TenantCreatedEvent.java` | 跨模块协作契约 |
| `platform/tenant/endpoint/TenantEndpoint.java` | `/platform/tenants` JAX-RS contract |
| `kernel/workspace/domain/entity/WorkspaceEntity.java` | `nx_workspace` |
| `kernel/member/domain/entity/TenantMemberEntity.java` | `nx_tenant_member` |
| `kernel/organization/domain/entity/OrganizationUnitEntity.java` | `nx_organization_unit` |
| `kernel/organization/domain/entity/OrganizationMemberEntity.java` | `nx_organization_member` |

---

### Task 1：Platform Maven module

**文件：**
- Create: `innospots-nexus-platform/pom.xml`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/PlatformModule.java`
- Create: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/PlatformModuleTest.java`
- Modify: `pom.xml` (add `<module>innospots-nexus-platform</module>` after kernel)
- Modify: `innospots-nexus-bom/pom.xml` (add `innospots-nexus-platform` dependencyManagement entry after kernel)
- Modify: `AGENTS.md` (add platform module section; extend dependency rules so kernel and platform both depend on console and not on each other)

**接口：**
- 消费： existing `innospots-nexus-parent`, `innospots-nexus-console`
- 产出： artifact `com.innospots:innospots-nexus-platform`

- [x] **Step 1：编写失败的 module marker 测试**
- [x] **Step 2：运行测试确认失败**
- [x] **Step 3：添加 aggregator、BOM、POM、marker class 与 AGENTS.md**
- [x] **Step 4：验证编译与 marker 测试**

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

- [ ] **Step 2：运行测试确认失败**

运行： `mvn -pl innospots-nexus-platform test -Dtest=PlatformModuleTest`
预期： FAIL，因 module / class 不存在。

- [ ] **Step 3：添加 aggregator、BOM、POM、marker class 与 AGENTS.md**

`innospots-nexus-platform/pom.xml` 必须继承 `innospots-nexus-parent`、依赖 `innospots-nexus-console`，并包含 kernel 使用的相同 persistence API (`jakarta.persistence-api`, `jakarta.transaction-api`, `mybatis-plus-core`, `mybatis-plus-extension`) plus `jakarta.ws.rs-api` (via console or explicit) for endpoints.

`PlatformModule` 为空 public marker，Javadoc：ops-domain platform；依赖 console；不得依赖 kernel。

AGENTS.md 新增：
- 新 `innospots-nexus-platform` 章节： owns `nx_tenant`, `nx_enterprise`, later platform users / support access / platform audit; `/platform/**`; no public self-register.
- 依赖： platform may depend on console + transitive core/base; must not depend on kernel.
- 方向： `console -> kernel` and `console -> platform` in parallel.

- [ ] **Step 4：验证编译与 marker 测试**

运行： `mvn -pl innospots-nexus-platform -am test -Dtest=PlatformModuleTest`
预期： PASS.

运行： `mvn validate`
预期： BUILD SUCCESS.

---

### Task 2：Console package skeleton

**文件：**
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/{auth,credential,role,menu,permission,extension,logger,dictionary}/package-info.java`
- Create: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/ConsolePackageSkeletonTest.java`

**接口：**
- 消费： existing `com.innospots.nexus.console.endpoint`
- 产出： documented package roots for Phase 2 moves; no entity/endpoint types yet

- [ ] **Step 1：编写失败的 package 测试**

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

源码中勿用 `package-info.class`（无效）。通过加载 package 断言：

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

- [ ] **Step 2：运行测试确认失败**

运行： `mvn -pl innospots-nexus-console test -Dtest=ConsolePackageSkeletonTest`
预期： `ClassNotFoundException` for `com.innospots.nexus.console.auth.package-info`.

- [ ] **Step 3：添加 package-info 文件**

每个文件说明 Phase 2 职责及 concrete type 在迁移前仍在 kernel。示例：

```java
/**
 * Login, registration orchestration, token, OAuth protocol, and tenant selection.
 * Concrete types remain in kernel until Phase 2.
 */
package com.innospots.nexus.console.auth;
```

- [ ] **Step 4：运行测试**

运行： `mvn -pl innospots-nexus-console test -Dtest=ConsolePackageSkeletonTest`
预期： PASS.

---

### Task 3：TenantEntity

**文件：**
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/domain/entity/TenantEntityContractsTest.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/enums/TenantStatus.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/entity/TenantEntity.java`

**接口：**
- 消费： `BaseEntity`, table `nx_tenant`
- 产出： `TenantEntity` with `idPrefix() == "tnt"`, fields below

字段（勿重复声明 audit 列）：

| 字段 | 类型 | 列 | 说明 |
|-------|------|--------|-------|
| tenantId | String | PK 32 | prefix `tnt` |
| tenantName | String | 128, not null | |
| tenantCode | String | 64, not null | unique index `uk_nx_tenant_code` |
| status | String | 32, not null | `TenantStatus` persisted as name |
| planCode | String | 64, nullable | |
| ownerTenantUserId | String | 32, nullable | |

`TenantStatus`: `ACTIVE`, `SUSPENDED`, `ARCHIVED`.

- [ ] **Step 1：编写失败的契约测试** (table name, superclass `BaseEntity`, PK, fields, unique index on `tenant_code`, `idPrefix()` via a new instance).
- [ ] **Step 2：运行** `mvn -pl innospots-nexus-platform test -Dtest=TenantEntityContractsTest` — 预期： compile failure / missing class.
- [ ] **Step 3：实现 `TenantStatus` + `TenantEntity`.**
- [ ] **Step 4：重跑测试 — 预期：PASS。** `mvn -pl innospots-nexus-platform -am clean compile`.

---

### Task 4：EnterpriseEntity

**文件：**
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/enterprise/domain/entity/EnterpriseEntityContractsTest.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/enterprise/domain/entity/EnterpriseEntity.java`

**接口：**
- 消费： `BaseEntity`, `TenantEntity.tenantId`
- 产出： `nx_enterprise`, `idPrefix() == "ent"`, unique `tenant_id`

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

- [ ] **Step 1：失败的契约测试。**
- [ ] **Step 2：运行测试 — 类型缺失。**
- [ ] **Step 3：实现 entity.**
- [ ] **Step 4：测试 PASS + `mvn -pl innospots-nexus-platform -am clean compile`.**

---

### Task 5：TenantCreatedEvent + Tenant DAO/operator create

**文件：**
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/event/TenantCreatedEvent.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/dao/TenantDao.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/enterprise/dao/EnterpriseDao.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/operator/TenantOperator.java`
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/operator/TenantOperatorTest.java`
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/dao/TenantDaoContractsTest.java`

**接口：**
- 消费： `TenantEntity`, `EnterpriseEntity`, `DomainEvent`
- 产出：

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

- [ ] **Step 1：编写 `TenantOperatorTest`，用 Mockito DAO 证明 create 插入两行并将 tenantId 复制到 enterprise。**
- [ ] **Step 2：运行 — operator 缺失。**
- [ ] **Step 3：实现 DAOs + operator (minimal validation).**
- [ ] **Step 4：测试 PASS。**

---

### Task 6：Platform TenantEndpoint contract

**文件：**
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/request/TenantCreateRequest.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/domain/vo/TenantVo.java`
- Create: `innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/tenant/endpoint/TenantEndpoint.java`
- Test: `innospots-nexus-platform/src/test/java/com/innospots/nexus/platform/tenant/endpoint/TenantEndpointContractsTest.java`

**接口：**
- Produces JAX-RS interface `@Path("/platform/tenants")`:

```java
@POST R<TenantVo> createTenant(TenantCreateRequest request);
@GET @Path("/{tenantId}") R<TenantVo> getTenant(@PathParam("tenantId") String tenantId);
```

`TenantCreateRequest` record: `tenantName`, `tenantCode`, `planCode`, `ownerTenantUserId`, `legalName`, `creditCode`, `industry`, `contactName`, `contactPhone`, `contactEmail`, `address`.

`TenantVo` record: `tenantId`, `tenantName`, `tenantCode`, `status`, `planCode`, `ownerTenantUserId`, `enterpriseId`, `legalName`.

No runtime implementation class in this task (contract only, same as `RoleEndpoint`).

- [ ] **Step 1：失败的 endpoint 契约测试** (`Path` = `/platform/tenants`, POST create, GET by id, request/vo are records).
- [ ] **Step 2：运行 — 类型缺失。**
- [ ] **Step 3：添加 request、vo、endpoint interface。**
- [ ] **Step 4：测试 PASS + `mvn -pl innospots-nexus-platform -am clean compile`.**

---

### Task 7：WorkspaceEntity

**文件：**
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/workspace/domain/entity/WorkspaceEntityContractsTest.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/workspace/domain/entity/WorkspaceEntity.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/workspace/dao/WorkspaceDao.java`
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/workspace/dao/WorkspaceDaoContractsTest.java`

**接口：**
- Superclass: `TenantBaseEntity` (inherits `tenantId`; do not redeclare)
- Table: `nx_workspace`
- Prefix: `wks`
- Fields: `workspaceId` PK, `workspaceName` 128 not null, `workspaceCode` 64 not null, `description` 512 nullable, `status` 32 not null
- Unique: `uk_nx_workspace_tenant_code` on `tenant_id,workspace_code`

- [ ] **Step 1：失败的契约测试。**
- [ ] **Step 2： Run — missing class.**
- [ ] **Step 3： Entity + DAO.**
- [ ] **Step 4： Tests PASS + `mvn -pl innospots-nexus-kernel -am clean compile`.**

---

### Task 8：TenantMemberEntity

**文件：**
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/member/domain/entity/TenantMemberEntityContractsTest.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/member/domain/enums/TenantMemberStatus.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/member/domain/entity/TenantMemberEntity.java`
- Create: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/member/dao/TenantMemberDao.java`
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/member/dao/TenantMemberDaoContractsTest.java`

**接口：**
- Superclass: `TenantBaseEntity`
- Table: `nx_tenant_member`
- Prefix: `tmb`
- Fields: `tenantMemberId` PK, `tenantUserId` 32 not null, `status` 32 not null, `joinedAt` `LocalDateTime` not null
- Unique: `uk_nx_tenant_member_user` on `tenant_id,tenant_user_id`
- `TenantMemberStatus`: `ACTIVE`, `DISABLED`, `PENDING`

- [ ] **Step 1–4：** TDD contract + entity + DAO.

---

### Task 9：Organization Unit and Member

**文件：**
- Tests under `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/organization/`
- Create: `.../organization/domain/enums/OrganizationUnitType.java` (`COMPANY`, `BRANCH`, `DEPARTMENT`, `TEAM`)
- Create: `.../organization/domain/entity/OrganizationUnitEntity.java`
- Create: `.../organization/domain/entity/OrganizationMemberEntity.java`
- Create: matching DAOs

**接口：**

`nx_organization_unit` extends `TenantBaseEntity`, prefix `org`:
- `unitId` PK, `parentId` 32 nullable, `unitCode` 64 not null, `unitName` 128 not null, `unitType` 32 not null, `sortOrder` Integer not null, `status` 32 not null
- Unique `uk_nx_organization_unit_code` on `tenant_id,unit_code`
- Index `idx_nx_organization_unit_parent` on `tenant_id,parent_id,sort_order`

`nx_organization_member` extends `TenantBaseEntity`, prefix `ogm`:
- `organizationMemberId` PK, `unitId` 32 not null, `tenantMemberId` 32 not null
- Unique `uk_nx_organization_member` on `tenant_id,unit_id,tenant_member_id`

- [ ] **Step 1–4：** TDD both entities and DAOs.

---

### Task 10：Phase 1 verification

- [ ] **Step 1：** `mvn clean compile`
- [ ] **Step 2：** `mvn test`
- [ ] **Step 3：** Confirm no `ProjectBaseEntity` / `TLC.projectId` in production Java.

## Self-Review

- Spec §9.5 platform module → Task 1
- Spec §9.3 console packages → Task 2
- Spec §10.3 `nx_tenant` / `nx_enterprise` → Tasks 3–6
- Spec §10.6 `nx_workspace` → Task 7
- Spec §10.4 member / org → Tasks 8–9
- Spec §11.2 isolation bases → already done; not reintroduced
- User split, console move, Group delete → Phase 2 plan, not this file
