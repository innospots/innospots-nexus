# Extension Runtime 实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:executing-plans 按任务逐步实施本计划。

**目标：** 将 extension 契约迁入 `innospots-nexus-core`，删除旧 console extension 包，并在 `innospots-nexus-console` 中实现 page-URL role 拦截。

**架构：** Core 拥有 framework-neutral extension descriptor、page/menu declaration、provider discovery 与 lifecycle state。Console 拥有 active page-URL registry 及 Jakarta REST request filter 契约，用于校验 page-source header、URL template 与当前 user role。现有无关 permission-domain 工作保持不变。

**技术栈：** Java 25、Java record、Jakarta REST、Java SPI、JUnit 5、AssertJ、Maven。

---

### Task 1：添加 core extension declaration 契约

**文件：**
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/contract/ConsoleExtensionProvider.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/contract/ConsoleExtensionEntry.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/ExtensionDescriptor.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/ExtensionModuleDeclaration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/PageDslDeclaration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/MenuDeclaration.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/extension/ExtensionDeclarationTest.java`

- [x] 编写 immutable collection、qualified page identity、menu node validation、module/page key validation 的测试。
- [x] 实现前运行聚焦契约测试；因契约不存在而失败。
- [x] 实现 record 与 provider annotation，通过 defensive copy 与 `NexusException` 校验。
- [x] 再次运行聚焦测试并确认通过。

### Task 2：添加 core provider discovery 与 lifecycle registry

**文件：**
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/lifecycle/ExtensionState.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/lifecycle/ExtensionRegistration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/discovery/ExtensionProviderDiscovery.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/lifecycle/ExtensionRegistry.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/extension/ExtensionRegistryTest.java`

- [x] 编写 direct provider registration、Java SPI discovery、duplicate extension rejection、default enabled state、disable/activate transitions、unknown menu page 或 conflicting path 导致 activation 失败的测试。
- [x] 实现前运行聚焦 registry 测试；因类型缺失而失败。
- [x] 实现 discovery，不做 unrestricted classpath scanning：仅 direct instance、`ServiceLoader` 与显式提供的 annotated class。
- [x] 实现 atomic registration、activation、disable、state lookup 与 active descriptor snapshot。
- [x] 运行聚焦 registry 测试并确认通过。

### Task 3：添加 console page-URL permission 契约

**文件：**
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlPermissionKey.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlPermissionRegistry.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/CurrentUserRoleProvider.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlRolePermissionChecker.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlPermissionInterceptor.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/PageUrlPermissionRegistryTest.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/PageUrlPermissionInterceptorTest.java`

- [x] 编写 URL normalization、named path-variable matching、单 page 内 duplicate URL deduplication、不同 page 上同一 URL 的独立 permission、missing/invalid page header denial、role-based allow/deny 的测试。
- [x] 实现前运行聚焦测试；因类型缺失而失败。
- [x] 实现 immutable composite permission key `(moduleKey, pageKey, urlPattern)` 与 registry。
- [x] 实现 interceptor，使用 `X-Nexus-Page-Key`、注入 provider 的 current role 与注入 checker；query string 与 HTTP method 不影响 key。
- [x] 运行聚焦测试并确认通过。

### Task 4：删除旧 console extension 包并解除 legacy 引用

**文件：**
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleExtension.java`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleContribution.java`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleMenuDeclaration.java`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleRouteDeclaration.java`
- Delete: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension/ConsoleExtensionTest.java`
- Modify: 导入已删除包的 legacy `console/permission` 引用。

- [x] 仅替换直接依赖已删除包的 import 与测试；无关 user 变更保持不变。
- [x] 运行 console compile；无对已删除包的 Java 引用。
- [x] 运行 console 测试套件并修复包删除导致的 migration 错误。

### Task 5：验证模块边界与文档

**文件：**
- Reference: `innospots-nexus-core/docs/archive/extension-design.md` 作为历史基线。
- Modify: 仅当实现名称或边界与当前设计不同时修改 `innospots-nexus-core/docs/plugin-extension-design.md`。

- [x] 运行 `mvn -pl innospots-nexus-core,innospots-nexus-console -am test`。
- [x] 所有 Java 变更后运行 `mvn clean compile`。
- [x] 运行 `mvn validate`、`mvn test`、`git diff --check`；sandbox 只读 Maven resolver status path 阻止 effective-POM 生成。
- [x] 确认 core 无 Jakarta REST 依赖，console 拥有 permission interceptor。
- [x] 确认旧 `console/extension` Java 包及所有直接 Java 引用已删除。
