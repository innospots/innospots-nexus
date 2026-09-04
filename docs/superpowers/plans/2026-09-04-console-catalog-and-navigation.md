# Console Catalog and Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** console 提供 catalog/navigation API；sync 迁入 console；启动与启停后保持 DB 索引一致。

**Architecture:** 单层 `nx_permission_resource`；sync 全量对账；启动 Task + PluginManagementEndpoint 触发 sync。

**Spec:**

- [2026-09-04-console-catalog-and-navigation-design.md](../specs/2026-09-04-console-catalog-and-navigation-design.md)
- [2026-09-04-core-host-bootstrap-design.md](../specs/2026-09-04-core-host-bootstrap-design.md)

## Global Constraints

- core：`NexusStartupTask` + `NexusStartup`（内含编排）+ `PluginHostStartupTask` only。
- sync：全量对账；仅 ACTIVE 输入；**无** PluginEventBus 监听器（V1）。
- 内置插件：`requiredPluginIds` 唯一方式。
- 每次 Java 变更后：`mvn clean compile`；结束：`mvn test`。

---

### Task 0: core NexusStartup（可与 Task 2 并行）

**Files:**
- Create: `innospots-nexus-core/.../bootstrap/NexusStartupTask.java`
- Create: `innospots-nexus-core/.../bootstrap/NexusStartupContext.java`
- Create: `innospots-nexus-core/.../bootstrap/NexusStartup.java`（Builder + `run()`）
- Create: `innospots-nexus-core/.../bootstrap/PluginHostStartupTask.java`
- Test: `NexusStartupTest.java`

- [ ] Builder 注册 task；`run()` 按 order 排序 + fail-fast
- [ ] `PluginHostStartupTask` 包装现有 `PluginHostBootstrap`
- [ ] Spring 配置类组装 `NexusStartup` Bean；`ApplicationRunner` 仅 `nexusStartup.run()`
- [ ] 删除/替代 `PluginHostBootstrapRunner`

---

### Task 1: CatalogNodeVo + ConsoleCatalogService

**Files:**
- Create: `console/catalog/domain/vo/CatalogNodeVo.java`
- Create: `console/catalog/service/ConsoleCatalogService.java`
- Test: `ConsoleCatalogServiceTest.java`

- [ ] 扁平实体 → 树；仅 ENABLED

---

### Task 2: 迁移 PermissionResourceSyncService

**Files:**
- Move to `console/catalog/service/PermissionResourceSyncService.java`
- Move test; update `KernelModuleBoundaryTest`

---

### Task 3: ConsoleCatalogSyncStartupTask + ConsoleCatalogEndpoint

**Files:**
- Create: `console/catalog/bootstrap/ConsoleCatalogSyncStartupTask.java`（`NexusStartupTask` order=200）
- Create: `console/catalog/endpoint/ConsoleCatalogEndpoint.java`
- Delete: `permission/endpoint/PermissionCatalogEndpoint.java`
- Modify: `plugin/endpoint/PluginManagementEndpoint.java` — enable/disable 成功后调用 `sync()`

---

### Task 4: AuthorizationSubjectResolver + Navigation

**Files:**
- Create: `permission/authorization/AuthorizationSubjectResolver.java`
- Extend: `NavigationMenuVo`
- Create: `navigation/service/NavigationMenuAssembler.java`
- Create: `navigation/endpoint/NavigationMenuEndpoint.java`
- Delete: `menu/endpoint/NavigationMenuEndpoint.java`

---

### Task 5: Spring/Quarkus 装配 + 验证

- [ ] 各模块注册 Task Bean；`NexusStartupConfiguration` 组装 `NexusStartup`
- [ ] 适配层 `ApplicationRunner` / `StartupEvent` 仅调用 `nexusStartup.run()`
- [ ] 配置 `requiredPluginIds` 含 6 个内置 entry
- [ ] `mvn test`

---

## 明确不做（本计划）

- `CatalogSyncOnPluginLifecycleListener`
- `NexusStartupPhase` 枚举
- `includeDisabled` / `disableByOwnerPluginId`
- `BuiltinConsolePluginInstallInitializer`
