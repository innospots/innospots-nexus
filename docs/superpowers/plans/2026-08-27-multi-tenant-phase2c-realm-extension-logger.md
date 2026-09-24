# 多租户治理 Phase 2c 实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 完成 Phase 2 剩余 console 隔离：TLC realm 身份键、console IAM 表的 `security_realm`，以及将 extension runtime 与 logger 迁出 portal。

**架构：** Console 拥有可复用 control-plane 持久化与 runtime（role/menu/permission/extension/logger）。Core 保留 extension *declaration* 契约。Portal 保留 tenant domain 及调用 console `ExtensionRegistry` 的 permission *sync*。Platform 保持 ops-only。

**技术栈：** Java 25、Maven、Jakarta Persistence + MyBatis-Plus、JUnit 5 + AssertJ、Lombok。

**规格：** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §8.3、§9.3、§10.5–10.7、§12 Phase 2。

## 全局约束

- 先 domain 后职责分包；request/VO 位于 `domain.request` / `domain.vo`。
- Console 不持久化 users。Portal/platform 不签发 token。
- 复用 `console.auth.domain.enums.SecurityRealm`。持久化为 String 32，not null。
- 不更新模块 `SKILL.md`。除非明确要求，否则不 commit。
- Java 变更后：`mvn clean compile`。本 slice 完成后：`mvn test`。

---

### Task 1：TLC realm 身份键

为 `TLC` 添加 `securityRealm`、`tenantMemberId`、`platformUserId` 及 typed accessor。扩展 `TLCTest`。保留现有 Long 型 `userId`。

- [x] 已完成

### Task 2：`security_realm` + CAPABILITY

为 `RoleEntity`、`MenuEntity`、`PermissionResourceEntity`、`PermissionGrantEntity` 添加 `securityRealm`。添加 `PermissionResourceType.CAPABILITY`。在 `RoleCreateRequest` / `RoleVo` 上暴露 realm。

- [x] 已完成

### Task 3：将 extension runtime 迁至 console

将 `portal.extension`（entity/dao/repository/service/discovery + tests）迁至 `console.extension`，保持相同内部布局。更新 `PermissionResourceSyncService` 使用 console `ExtensionRegistry`。portal extension 留空。

- [x] 已完成

### Task 4：将 logger 迁至 console

将 `portal.logger` 迁至 `console.logger`（`dao`、`domain.entity`、`domain.context`、`operator`、`handler`）。`@AuditLog` / `LogExecutor` 保留在 logger domain 根。

- [x] 已完成

### Task 5：验证

`mvn clean compile` && `mvn test`。portal 生产代码中无 `extension`/`logger` 包。

- [x] 已完成
