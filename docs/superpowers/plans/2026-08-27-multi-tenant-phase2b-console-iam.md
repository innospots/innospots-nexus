# 多租户治理 Phase 2b 实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:executing-plans 按任务逐步实施本 plan。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 将 console 拥有的 IAM（role/menu/permission）迁出 kernel，修正 domain 包布局，停用 Group，并落地 AuthFacade 与 platform SupportAccessGrant。

**架构：** Console 拥有 role engine、menu catalog、permission catalog/grants、credential SPI 与 token 签发。Kernel 保留 tenant-user 存储、membership、org、workspace，以及 extension 驱动的 permission *sync*（使用 console DAO）。Platform 保留 ops users、tenant lifecycle 与 support access。Kernel 与 platform 永不相互依赖。

**技术栈：** Java 25、Maven、Jakarta Persistence + MyBatis-Plus、Jakarta REST、JUnit 5 + AssertJ、Lombok。

**规格：** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §6.8、§7、§11.3–11.4、§12 Phase 2。

## 全局约束

- 先 domain 后职责分包：`endpoint`、`dao`、`operator`、`service`、`api`、`domain/{entity,request,vo,model,enums,event}`。
- Request/VO 位于 `domain.request` / `domain.vo`，不得直接放在 domain 根或并列 `request`/`vo` 包。
- Console 不持久化 users。Kernel/platform 不签发 token。
- 禁止 `ProjectBaseEntity` / `projectId`。Permission subject 仅 `ROLE | ORG_UNIT`。
- 不更新模块 `SKILL.md`。除非明确要求，否则不 commit。
- Java 变更后：`mvn clean compile`。本 slice 完成后：`mvn test`。

## 目标 console 布局（auth 示例）

```text
console.auth
  ├── api/                 UserDirectory, CredentialStore, MembershipDirectory
  ├── endpoint/            PlatformAuthEndpoint, TenantAuthEndpoint
  ├── service/             AuthFacade, TokenIssuer
  └── domain
      ├── enums/           SecurityRealm
      ├── model/           AuthUser, CredentialRecord
      ├── request/         AuthLoginRequest, ...
      └── vo/              AuthTokenVo
```

迁移后 kernel 遗留：

```text
kernel.permission.service.PermissionResourceSyncService  (uses console DAOs + kernel ExtensionRegistry)
```

---

### Task 1：修正 auth/credential 包布局

将 Phase 2a 类型移入 `api` / `endpoint` / `domain.*`。将 `VerificationType` 移至 `credential.domain.enums`。重命名 `UserProfileVO` → `UserProfileVo`。

### Task 2：Console 持久化 + 迁移 role/menu/permission

为 console 添加 JPA / MyBatis-Plus / transaction API。将 kernel `role`、`menu`、`permission`（sync service 除外）及对应测试迁至 `com.innospots.nexus.console.*`。

### Task 3：Role owner + role binding

添加 `RoleOwnerType`（`PLATFORM|TENANT|WORKSPACE`）及 `RoleEntity` 上的 `ownerType`/`ownerId`。用 `nx_role_binding` / `RoleBindingEntity`（`subjectType` USER|ORG_UNIT）替换 `nx_user_role` / `UserRoleEntity`。

### Task 4：删除 Group；grant subject 改为 ORG_UNIT

删除 kernel `group` domain。`PermissionSubjectType` = `ROLE | ORG_UNIT`。`AuthorizationSubject.groupIds` → `orgUnitIds`。

### Task 5：AuthFacade token 签发

`AuthFacade` login/select-tenant/refresh/logout 使用 directory port + AES-GCM compact token（`CryptoUtils.encryptAesGcm`）。console 中不持久化 user。

### Task 6：Platform SupportAccessGrant

platform 中 `nx_support_access_grant`（`support` domain，正确的 `domain.entity` / `dao` / `endpoint`）。

### Task 7：验证

`mvn clean compile` && `mvn test`。kernel 生产代码中无 `role`/`menu`/`group` 包。Console 无 user entity。
