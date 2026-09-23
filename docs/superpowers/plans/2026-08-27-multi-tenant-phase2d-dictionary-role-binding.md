# 多租户治理 Phase 2d 实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 落地 console dictionary 持久化与 JAX-RS 契约，并将遗留 role-member API 替换为 `nx_role_binding` 契约。

**架构：** Console 拥有按 `security_realm` + `workspace_id` 隔离的 dictionary catalog。Type 与 item 为独立记录。Role 分配使用 `RoleBindingEntity`（`USER | ORG_UNIT`），而非 user-member 列表。

**技术栈：** Java 25、Maven、Jakarta Persistence + MyBatis-Plus、Jakarta REST、JUnit 5 + AssertJ、Lombok。

**规格：** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §9.3、§9.8、§10.1、§10.5、§12 Phase 2。

## 全局约束

- 先 domain 后职责分包；request/VO 位于 `domain.request` / `domain.vo`。
- 新 domain 使用具体 endpoint 类（非 interface）。
- Dictionary entity 继承 `WorkspaceBaseEntity`。`securityRealm` 持久化为 String 32，not null。
- 复用 `console.auth.domain.enums.SecurityRealm`。复用 `RoleBindingSubjectType`。
- 不更新模块 `SKILL.md`。除非明确要求，否则不 commit。
- Java 变更后：`mvn clean compile`。本 slice 完成后：`mvn test`。

---

### Task 1：Dictionary entity

**文件：**
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/dictionary/domain/entity/DictionaryEntityContractsTest.java`
- Create: `DictionaryTypeEntity.java`, `DictionaryItemEntity.java`

`nx_dictionary_type`（`dct`）：`dictionaryTypeId`、`typeCode` 64、`typeName` 128、`securityRealm` 32、`status` 32、`sortOrder`、`builtIn`。唯一约束 `(workspace_id,security_realm,type_code)`。

`nx_dictionary_item`（`dci`）：`dictionaryItemId`、`typeCode` 64、`itemValue` 64、`itemName` 128、`securityRealm` 32、`status` 32、`sortOrder`、`builtIn`。唯一约束 `(workspace_id,security_realm,type_code,item_value)`。

- [x] 已完成

### Task 2：Dictionary DAO

`DictionaryTypeDao`、`DictionaryItemDao` 继承 `BaseMapper`。契约测试：interface + BaseMapper。

- [x] 已完成

### Task 3：Dictionary request、VO、endpoint

`DictionaryTypeEndpoint` `@Path("/console/dictionary-types")` page/get/create/update/status/delete/options。

`DictionaryItemEndpoint` `@Path("/console/dictionary-types/{typeCode}/items")` page/create/update/status/delete。

具体类在 operator 存在前抛出 `UnsupportedOperationException`（与 `MenuEndpoint` 相同）。

- [x] 已完成

### Task 4：Role binding endpoint 替换 role members

用 `RoleBindingEndpoint` `@Path("/console/roles/{roleId}/bindings")`（使用 `RoleBindingSubjectType`）替换 `RoleMemberEndpoint` 与 `RoleMember*` request/VO。更新 `RoleEndpointContractsTest`。

- [x] 已完成

### Task 5：验证

`mvn clean compile` && `mvn test`。

- [x] 已完成
