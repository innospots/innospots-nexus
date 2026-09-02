# Nexus Plugin Minimal V1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or
> superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现一个可编译、可安装、可管理的最小插件体系，只支持 Java SPI 和 YAML 显式 Java Class，
并彻底移除 Extension 双轨。

**Architecture:** Core 使用 `PluginInstallationManager` 管理持久化安装事实，使用纯 `PluginManager` 管理
当前 JVM 生命周期。Java 和 YAML 都编译为运行时定义；Console 页面与菜单通过 `console@1` Contribution
进入同一插件事务。远程 bind 和 capability exposures 不进入本轮实现。

**Tech Stack:** Java 25、ServiceLoader、Jackson YAML、MyBatis-Plus、Jakarta Persistence、Jakarta REST、
JUnit 5、AssertJ、Maven。

---

## 1. V1 范围

### 1.1 必须实现

- 一个 Plugin SPI；
- Java `Plugin.definition()`；
- `META-INF/nexus/plugin.yaml` 多资源发现；
- YAML `bind.kind=java` + 显式 `class`；
- CapabilityTypeRegistry；
- pluginId 反向域名身份；
- providerId 插件内全局唯一；
- Plugin/Provider 两级配置；
- Tags 路由；
- CapabilityRequirement；
- `console@1` Contribution；
- prepare/stage/commit/rollback 生命周期；
- Plugin availability 门控；
- Core 插件安装表和 PluginInstallationManager；
- autoInstall、REGISTERED、DISABLED、MISSING 派生管理状态；
- Kernel 权限资源 ownerPluginId；
- 删除 Extension 生产代码和测试入口。

### 1.2 明确不实现

- `bind.kind=http/process/mcp/contract`；
- `exposures`；
- 动态 JAR 下载、安装和卸载；
- 独立 ClassLoader、依赖隔离和热重载；
- 安全沙箱、市场、签名；
- 请求排空和调用引用计数；
- Plugin 到具体 pluginId 的声明依赖。

Parser 必须能识别完整 DSL 的 kind，但对上述未实现能力返回类型化“不支持”错误，不能静默忽略。

### 1.3 V1 必须明确但不新增框架的边界

- `PluginDefinition` 继续作为**已解析的运行时定义**，不再额外创建同义的
  `ResolvedPluginDefinition`；`PluginManifest` 仅代表 YAML 输入，`PluginDefinitionSnapshot` 仅代表持久化快照。
- V1 统一使用宿主 `Application ClassLoader`。不创建子 ClassLoader，不切换 TCCL，不承诺插件依赖隔离；
  YAML 中的实现类必须对该 ClassLoader 可见。
- 内部业务代码通过构造器注入 `CapabilityManager`，调用 `require(type, tags)`、`find(type, tags)`；
  插件内部通过 `PluginContext.capabilities()` 使用相同入口。不得增加静态全局 Locator。
- DSL Schema 是跨系统交换与 CI 校验契约；V1 Runtime 使用严格 Jackson 绑定和语义校验，
  不为运行时 JSON Schema 校验额外引入依赖。
- 所有声明的 Contribution 在 V1 都是 required；DSL 尚未定义 optional Contribution，不能在实现中自行扩展。
- 当前仓库没有最终 Application/Bootstrap 模块，也没有 Flyway/Liquibase。Core 提供装配契约和参考 DDL，
  最终宿主负责注入 DAO、系统配置及 Console Handler；不得把宿主装配逻辑塞入 Console。
- 发现失败的定义只进入 `PluginDiscoveryReport` 和启动诊断，不写 `nx_plugin_installation`，
  管理列表只展示成功编译过且可形成稳定身份的插件。

## 2. 目标代码结构

```text
innospots-nexus-core
└── com.innospots.nexus.core.plugin
    ├── capability
    │   ├── CapabilityTypeRegistry
    │   ├── ProviderRef
    │   └── existing capability types
    ├── contribution
    │   ├── PluginContribution
    │   ├── PluginContributionType
    │   ├── PluginContributionDecoder
    │   ├── PluginContributionDecoderRegistry
    │   ├── PluginContributionHandler
    │   ├── PluginContributionSnapshotter
    │   ├── PluginContributionSnapshotterRegistry
    │   └── PreparedPluginContribution
    ├── declaration
    │   ├── PluginDefinition
    │   ├── PluginManifest
    │   ├── PluginSource
    │   └── PluginManifestParser
    ├── discovery
    │   ├── ClasspathPluginDiscovery
    │   ├── PluginDefinitionCompiler
    │   ├── PluginDiscoveryReport
    │   └── RejectedPluginDefinition
    ├── installation
    │   ├── config
    │   ├── dao
    │   ├── domain
    │   ├── repository
    │   └── service
    ├── lifecycle
    │   └── PluginAvailability
    ├── runtime
    └── status

innospots-nexus-console
└── com.innospots.nexus.console.plugin
    ├── contribution
    ├── domain.vo
    └── endpoint
```

不得创建空包。每个阶段只创建当前测试所需类型。

## 3. 实施门禁

每批 Java 修改后立即执行：

```bash
mvn clean compile
```

聚焦测试使用：

```bash
mvn -pl innospots-nexus-core -am test
mvn -pl innospots-nexus-console,innospots-nexus-kernel -am test
```

完整验收：

```bash
mvn validate
mvn test
mvn -q help:effective-pom
git diff --check
```

### 3.1 每个 Task 的执行协议

每个 Task 都必须按以下顺序执行，不能一次性写完后补测试：

1. 先增加该 Task 列出的失败测试，运行最小测试集并确认失败原因正是待实现行为；
2. 实现最小生产代码；
3. 运行 `mvn clean compile`，预期 `BUILD SUCCESS`；
4. 运行该 Task 的聚焦测试，预期测试全部通过；
5. 运行受影响模块测试；
6. 仅在上述步骤通过后提交，提交信息使用 `feat(plugin): ...`、`refactor(plugin): ...` 或
   `test(plugin): ...`。

若测试在第 1 步意外通过，必须先证明现有实现已满足契约，或修正测试使其覆盖真实缺口；不得继续写重复实现。

## 4. 实施任务

### Task 1：统一 Plugin 和 Provider 身份

**Files:**

- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/PluginDefinition.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/CapabilityContribution.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/CapabilityRequirement.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/capability/CapabilityRegistration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/capability/ProviderRef.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/declaration/PluginDefinitionTest.java`

- [ ] 将 Plugin `id` 规范为 `pluginId`，校验反向域名格式。
- [ ] 为 CapabilityContribution 增加 providerId、Provider Tags 和 Provider ConfigDefinition。
- [ ] CapabilityRequirement 增加不可变 `requiredTags`，依赖检查与实际路由使用相同 tags 子集语义。
- [ ] 保证 providerId 在整个 PluginDefinition 内唯一。
- [ ] 为 CapabilityRegistration 增加 ProviderRef。
- [ ] 保留现有 Factory 创建语义，不引入实现 SPI。
- [ ] 添加重复 providerId、非法 pluginId 和不可变集合测试。
- [ ] 将现有测试 fixture 的 kebab-case pluginId 改为反向域名；若存在已发布身份，必须使用人工确认的
  oldId → newPluginId 映射，禁止通过字符串规则猜测。
- [ ] 运行 `mvn clean compile` 和 Core 聚焦测试。

目标核心签名：

```java
public record ProviderRef(String pluginId, String providerId) {
}
```

```java
public record CapabilityContribution<T extends CapabilityProvider>(
        CapabilityType<T> type,
        String providerId,
        Tags tags,
        ConfigDefinition config,
        CapabilityProviderFactory<? extends T> factory
) {
}
```

`PluginDefinition` 至少包含 `pluginId`、`version`、`apiVersion`、本地化 `displayName/description`、
Plugin tags、Plugin config、capabilities、requirements 和 contributions；集合和 Map 必须防御性复制。

### Task 2：补齐 Provider 配置与 Tags 路由

**Files:**

- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/config/ConfigurationManager.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contract/CapabilityProviderContext.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/capability/CapabilityRouter.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/capability/CapabilityRegistry.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/dependency/DependencyResolver.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/config/ConfigurationManagerTest.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/capability/CapabilityRouterTest.java`

- [ ] 实现 `plugins.<pluginId>.providers.<providerId>.<localKey>`。
- [ ] 校验插件共享 key 与 Provider 私有 key 的环境变量映射冲突。
- [ ] Context 暴露当前 ProviderRef、共享配置和私有配置只读视图。
- [ ] 合并 Plugin Tags 和 Provider Tags，同名不同值失败。
- [ ] 路由只使用 ACTIVE Registration 和 Tags 子集匹配。
- [ ] CapabilityRequirement 的声明存在性和运行可用性均按 capability key + requiredTags 判断。
- [ ] 证明 YAML/Factory 顺序和 providerId 不参与歧义消解。
- [ ] 增加“宿主通过构造器注入 CapabilityManager”和“插件通过 PluginContext.capabilities()”的调用测试；
  两种入口必须只看到 ACTIVE Provider。
- [ ] 运行编译和配置/路由聚焦测试。

### Task 3：建立 Manifest、来源和定义编译边界

**Files:**

- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/PluginManifest.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/PluginSource.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/PluginManifestParser.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/JacksonPluginManifestParser.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/capability/CapabilityTypeRegistry.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/PluginDefinitionCompiler.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/PluginDiscoveryReport.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/RejectedPluginDefinition.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/status/PluginStatusCode.java`
- Modify: `innospots-nexus-core/pom.xml`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/discovery/PluginDefinitionCompilerTest.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/declaration/JacksonPluginManifestParserTest.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/status/PluginStatusCodeTest.java`

- [ ] 为 DSL 公共字段建立严格 Jackson 绑定模型，Contribution 保持为通用 manifest map，未知字段失败。
- [ ] Core 若直接使用 Jackson YAML，显式声明直接 Maven 依赖，不依赖 Base 的传递依赖；版本仍由 BOM 管理。
- [ ] 解析器禁止重复键、重复 tag、YAML alias，输入上限 1 MiB、嵌套深度 64；超过限制返回类型化 DSL 错误。
- [ ] 不引入 Runtime JSON Schema 引擎；Schema 校验留给发布/CI，Runtime 负责结构绑定和语义校验。
- [ ] CapabilityTypeRegistry 显式注册 type@majorVersion 到 Java API。
- [ ] 编译器只实现 `java` bind；其它合法 kind 返回 `UNSUPPORTED_BIND_KIND`。
- [ ] Java class 校验 concrete、public、CapabilityProvider、API assignable 和 public 无参构造。
- [ ] 编译器生成无副作用 Factory，实例化推迟到 Plugin 启动。
- [ ] DiscoveryReport 区分 valid Catalog 与 rejected definitions。
- [ ] PluginStatusCode 补充并测试：provider 重复、DSL 语法错误、DSL 结构错误、未知 capability type、
  不支持 bind/exposure/contribution、资源冲突、安装并发冲突、未安装、MISSING、持久化失败；
  状态码值必须在现有 `0013` 后顺序分配并通过全局唯一性测试。
- [ ] 添加 DSL 最小示例、未知字段、非法 class、未知类型和不支持 kind 测试。
- [ ] 运行编译和编译器聚焦测试。

Minimal V1 可执行 YAML：

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.message
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 消息插件
  capabilities:
    - type: message.sender
      majorVersion: 1
      providerId: wecom
      bind:
        kind: java
        class: com.example.message.WeComMessageSender
```

### Task 4：统一 Java SPI 与 YAML 发现

**Files:**

- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/ClasspathPluginDiscovery.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/DiscoveredPlugin.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/PluginCatalog.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/discovery/ManifestPlugin.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/discovery/ClasspathPluginDiscoveryTest.java`

- [ ] 使用 `ClassLoader.getResources` 枚举全部 plugin.yaml。
- [ ] 明确使用 `PluginRuntimeConfig.pluginClassLoader()` 作为唯一声明和实现类加载器；不切换线程 TCCL。
- [ ] 为每个来源记录 sourceType、sourceLocation 和 discoveredAt。
- [ ] 同 pluginId 同时来自 Java/YAML 时全局失败。
- [ ] Java definition 每个 Plugin 只调用一次。
- [ ] YAML 通过 ManifestPlugin 适配 Plugin 生命周期，不创建第二套 Runtime。
- [ ] 重复 pluginId 和 Capability API 映射冲突全局失败。
- [ ] 单插件定义错误进入 rejected report，不阻止无关合法插件。
- [ ] 测试多个 JAR/目录中的同名资源都能被发现，并证明实现类与 Capability API 使用同一 ClassLoader。
- [ ] 运行编译和发现聚焦测试。

### Task 5：引入通用 Contribution 契约

**Files:**

- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContribution.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionType.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionDecoder.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionDecoderRegistry.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionHandler.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionSnapshotter.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionSnapshotterRegistry.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PreparedPluginContribution.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/contribution/PluginContributionContext.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/declaration/PluginDefinition.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/contribution/PluginContributionContractsTest.java`

- [ ] PluginDefinition 增加不可变 contributions。
- [ ] 同一插件每个 ContributionType 最多一份。
- [ ] Core Manifest 将 Contribution 保持为通用字段 Map，通过显式 Decoder Registry 解码。
- [ ] Core 不 import Console Contribution；未知或未注册类型返回明确诊断。
- [ ] Snapshotter 将 Contribution 转成不含模块类型、Class、Handler 和 Secret 的通用不可变摘要；
  Core 安装快照不得通过反射理解 Console 声明。
- [ ] Handler validate 无副作用，prepare 返回可回滚句柄。
- [ ] Prepared handle 明确 stage/commit/rollback/close 幂等规则。
- [ ] V1 中所有已声明 Contribution 都是 required；未注册 Decoder、Handler 或 Snapshotter 时定义编译/启动失败。
- [ ] Core 测试不得引用 Console 声明类型。
- [ ] 运行编译和 Contribution 契约测试。

### Task 6：实现统一可用性和插件事务

**Files:**

- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/lifecycle/PluginAvailability.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/lifecycle/ManagedPlugin.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/runtime/DefaultPluginManager.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/runtime/PluginRuntimeConfig.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/lifecycle/PluginRuntimeInfo.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/lifecycle/ManagedPluginTest.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/runtime/PluginRuntimeScenariosTest.java`

- [ ] PluginState 保持纯运行状态，不增加 DISABLED/MISSING/REGISTERED。
- [ ] PluginRuntimeInfo 不包含 desiredEnabled、installed 或 presence。
- [ ] 按 prepare → initialize → start → stage → commit → availability ACTIVE 执行。
- [ ] commit 后、availability 前的资源仍不可查询。
- [ ] 启动失败逆序回滚并保留首个异常。
- [ ] 停止先关闭 availability，再撤出索引和释放资源。
- [ ] 定义 `FAILED` 重试语义：仅当前次启动的 staged/registered/resource 已完整清理后，允许
  `PluginManager.start(pluginId)` 执行 `FAILED -> STARTING`；否则拒绝重试并保留原诊断。
- [ ] disable 时即使 stop 失败也保留 `desiredEnabled=false`，管理视图必须展示“期望禁用/运行态未停止”的偏差。
- [ ] 不在持锁时调用 Plugin、Provider 或 Handler。
- [ ] 运行编译和 Runtime 场景测试。

### Task 7：将插件安装持久化迁入 Core

**Files:**

- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/config/PluginInstallationConfig.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/dao/PluginInstallationDao.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/entity/PluginInstallationEntity.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/enums/PluginPresence.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/enums/PluginSourceType.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/model/PluginDefinitionSnapshot.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/model/PluginDefinitionSnapshotMapper.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/model/PluginInstallation.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/repository/PluginInstallationRepository.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/installation/PluginInstallationRepositoryTest.java`
- Create: `innospots-nexus-core/docs/plugin-installation-schema.sql`

- [ ] 实体映射 `nx_plugin_installation`，使用 BaseEntity 和 `plg` ID 前缀。
- [ ] plugin_id 建唯一索引。
- [ ] Snapshot 显式映射，不序列化 Factory/Class/Handler/Secret。
- [ ] Snapshot 保存 plugin identity、来源、capability/provider 摘要和 contribution 资源身份；JSON 反序列化后应相等。
- [ ] Repository 实现首次登记、PRESENT 更新、MISSING 和恢复。
- [ ] 拒绝 installed=false && desiredEnabled=true。
- [ ] 并发登记依靠唯一索引收敛。
- [ ] 参考 DDL 完整覆盖 `plugin-installation-design.md` 第 5 节的字段、唯一索引和查询索引，
  不把 JPA/MyBatis Entity 当作迁移机制。
- [ ] 实施前选择并记录一种数据路径：绿地库直接创建新表/列；已有数据则先提供经人工审核的
  extensionKey → pluginId 映射，复制数据、核对总数与唯一键，再删除旧表/列。映射缺失时禁止自动迁移。
- [ ] 运行 `mvn clean compile` 和 Repository 测试。

### Task 8：实现 PluginInstallationManager

**Files:**

- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/service/PluginRuntimeFactory.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/service/PluginInstallationManager.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/domain/model/PluginManagementView.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/installation/PluginInstallationManagerTest.java`

- [ ] reconcile 只对 valid Catalog 写安装记录。
- [ ] autoInstall 默认 false 且只影响首次发现。
- [ ] 系统配置键固定为 `nexus.plugin.auto-install`；缺失、空值均解析为 false，非法布尔值启动失败。
- [ ] 只启动 PRESENT && installed && desiredEnabled。
- [ ] installAndStart、enable、disable 按 pluginId 串行且幂等。
- [ ] 增加 `retryStart(pluginId)`，只用于 installed && desiredEnabled && PRESENT 且 Runtime 为 FAILED；
  修复配置后的重试不得改写安装意图。
- [ ] 采用“提交意图 → Runtime 命令 → 诊断写入”三段式事务。
- [ ] Manager 聚合 Repository 和 PluginManager 查询，不污染 PluginRuntimeInfo。
- [ ] Manager 拥有 PluginManager 的 close 责任。
- [ ] 查询聚合同时返回 presence、installed、desiredEnabled、runtimeState、lastError、definitionSnapshot，
  明确区分持久化意图与实际运行状态。
- [ ] 覆盖 autoInstall、失败保留意图、MISSING 恢复和并发命令测试。
- [ ] 运行编译和安装 Manager 测试。

### Task 9：提供管理端插件操作 API

**Files:**

- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/endpoint/PluginManagementEndpoint.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/domain/vo/PluginManagementVo.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/converter/PluginManagementConverter.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/plugin/endpoint/PluginManagementEndpointTest.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/plugin/converter/PluginManagementConverterTest.java`

- [ ] Endpoint 使用 `GET /console/plugins`、`GET /console/plugins/{pluginId}`、
  `POST /console/plugins/{pluginId}/install`、`/enable`、`/disable`、`/retry`；不暴露删除 JAR/卸载接口。
- [ ] Endpoint 仅依赖 Core `PluginInstallationManager`，不得直接注入 DAO、Repository 或 PluginManager。
- [ ] 命令接口按 `pluginId` 定位，重复命令返回当前聚合状态；未知、未安装、MISSING 和非法状态使用类型化状态码。
- [ ] `PluginManagementVo` 同时展示 presence、installed、desiredEnabled、runtimeState、source、version、
  lastError 和更新时间，前端无需从一个 state 字段反推多个维度。
- [ ] 使用 MapStruct Converter 完成 Core 管理视图到 Console VO 的结构转换；Endpoint 不手写字段复制。
- [ ] 测试 API 路径、HTTP 方法、响应包装、幂等调用和异常透传；Console 测试不得连接真实数据库。
- [ ] 运行 `mvn clean compile` 和 Console Endpoint 聚焦测试。

### Task 10：补齐宿主装配与启动顺序

**Files:**

- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/service/PluginRuntimeFactory.java`
- Modify: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/plugin/installation/config/PluginInstallationConfig.java`
- Create: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/plugin/installation/PluginHostAssemblyTest.java`
- Create: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/plugin/PluginConsoleAssemblyTest.java`

- [ ] 固定启动顺序：注册 CapabilityType → 注册 Contribution Decoder/Handler/Snapshotter → 发现与编译 →
  reconcile 安装事实 → 创建唯一 PluginManager → 启动 eligible plugins → 发布管理 API。
- [ ] `PluginInstallationConfig` 是与框架无关的不可变配置，`autoInstall` 缺省为 false；宿主负责把系统配置映射进来。
- [ ] `PluginRuntimeFactory` 每次根据 valid Catalog 和已注册处理器创建 Runtime，禁止 Runtime 自行访问数据库或 Console。
- [ ] Console 只向宿主贡献 `console@1` Decoder、Handler、Snapshotter 和 Endpoint，不创建第二个 PluginManager。
- [ ] 集成测试使用内存 Repository/DAO stub，证明 `autoInstall=false` 只登记、`autoInstall=true` 首次发现即安装上线，
  并证明 Capability 与 Console Contribution 共享同一次 availability 切换。
- [ ] 当前仓库没有最终启动模块，因此本 Task 的交付是可执行装配契约和集成测试；真正 Application 必须在其模块出现后
  调用该契约。不得为解决此缺口临时创建无明确职责的新模块。
- [ ] 运行 `mvn clean compile` 和 Core/Console 装配测试。

### Task 11：迁移 Console Contribution

**Files:**

- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ConsolePluginContribution.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ConsoleModuleDeclaration.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/MenuDeclaration.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/UiSpecPageDeclaration.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ConsolePluginContributionHandler.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ConsolePluginContributionDecoder.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ConsoleContributionCatalog.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ConsolePluginContributionSnapshotter.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/plugin/contribution/ReservedPluginResourceCatalog.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/plugin/contribution/ConsolePluginContributionHandlerTest.java`

- [ ] 将旧页面、菜单声明迁移到 Console 包并改为 ownerPluginId 归属。
- [ ] 注册 `console@1` Decoder，把通用 Manifest 字段转换为不可变 Console 声明。
- [ ] Snapshotter 只输出 module/page/menu/resource 的稳定身份，供 MISSING 插件保留资源归属。
- [ ] 实现 module/page/menu/path/UiSpec 全局校验。
- [ ] Handler 校验活动 Catalog 与 `ReservedPluginResourceCatalog`；MISSING 记录继续占用原资源身份，
  其它插件不得抢占，恢复相同 pluginId 时允许复用。
- [ ] prepare 不发布，commit 写入带 generation 的活动 Catalog。
- [ ] availability 激活前 Catalog 不返回资源。
- [ ] 停止后原子撤出当前插件资源。
- [ ] 不在 Console 创建安装 Entity、DAO 或 Repository。
- [ ] 运行编译和 Console Contribution 测试。

### Task 12：迁移权限资源归属

**Files:**

- Modify: `innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/permission/service/PermissionResourceSyncService.java`
- Modify: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/domain/entity/PermissionResourceEntity.java`
- Modify: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/domain/vo/PermissionResourceVo.java`
- Create: `innospots-nexus-console/docs/plugin-permission-owner-schema.sql`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/domain/entity/PermissionEntityContractsTest.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/authorization/RequestAuthorizerTest.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/service/PermissionVisibilityServiceTest.java`
- Test: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/permission/service/PermissionResourceSyncServiceTest.java`

- [ ] PermissionResourceSyncService 改读 ConsoleContributionCatalog。
- [ ] 权限资源 extensionKey 改为 ownerPluginId。
- [ ] JPA 索引列由 `extension_key` 改为 `owner_plugin_id`，VO 和所有实体映射测试同步更新。
- [ ] Console 参考 DDL 只负责 `nx_permission_resource.extension_key -> owner_plugin_id` 的列迁移、索引重建和校验；
  Core 的安装表 DDL 不修改 Console 业务表。
- [ ] 停用和 MISSING 不删除历史授权，只使资源不可用。
- [ ] 相同 pluginId 和资源身份恢复时复用关联。
- [ ] 不机械修改 Session/Conversation 中可能具有独立业务语义的 extensionKey；先单独确认其领域含义。
- [ ] 运行编译和 Console/Kernel 权限测试。

### Task 13：删除 Extension 双轨

**Files:**

- Delete: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/**`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/**`
- Delete: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension/discovery/SpiConsoleExtensionProvider.java`
- Delete: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension/domain/entity/ExtensionInstallationEntityTest.java`
- Delete: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension/repository/ExtensionInstallationRepositoryTest.java`
- Delete: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension/service/ExtensionRegistryTest.java`
- Delete: `innospots-nexus-console/src/test/resources/META-INF/services/com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider`
- Modify: `innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/KernelModuleBoundaryTest.java`

- [ ] 确认 PluginManager 已完整承接所有活动插件。
- [ ] 删除旧 SPI、Discovery、Registry、Entity、DAO、Repository 和状态。
- [ ] 删除 `nexus_extension_installation` 的代码映射；物理表删除按 Task 7 选择的数据路径执行。
- [ ] 全仓检索旧类型，区分插件身份与其它业务中的同名 extensionKey。
- [ ] 确认不存在 deprecated 双轨入口。
- [ ] 运行 `mvn clean compile` 和 Core/Console/Kernel 测试。

### Task 14：完整验证和文档一致性

**Files:**

- Verify: `innospots-nexus-core/docs/plugin-*.md`
- Verify: `innospots-nexus-core/docs/plugin-dsl-v1.schema.json`
- Modify: `innospots-nexus-core/docs/plugin-runtime-design.md`
- Modify: `innospots-nexus-core/docs/plugin-installation-design.md`

- [ ] 用 JSON parser 校验 Schema 自身合法。
- [ ] 从 DSL 文档提取 Minimal V1 示例并通过 parser 测试。
- [ ] 验证完整示例结构合法，但返回受控不支持诊断。
- [ ] 将实施中确定的 FAILED 重试规则、唯一 ClassLoader 规则和管理重试命令回写分册，避免设计文档与实现相反。
- [ ] 检索 DISABLED 是否错误进入 PluginState。
- [ ] 检索安装 DAO/Entity 是否残留在 Console。
- [ ] 检索 Extension SPI/Registry 是否残留在生产代码。
- [ ] 检索 `extensionKey` 残留并逐项分类：权限/插件归属必须清零；Session/Conversation 等独立领域字段保留时，
  在审计记录中说明其含义与插件系统无关。
- [ ] 检索静态 Capability Locator、第二个 PluginManager、Console 安装 DAO/Entity、独立 ClassLoader，结果必须为空。
- [ ] 运行模块边界测试，确认 Core 不依赖 Console，Console 不拥有插件安装持久化，Kernel 不依赖旧 Extension。
- [ ] 执行 `mvn validate`、`mvn test` 和 effective-pom 检查。
- [ ] 执行 `git diff --check` 并确认未修改模块 SKILL.md/references。

## 5. 阶段交付

| 阶段 | Tasks | 可验证交付 |
|------|-------|------------|
| A：身份和声明 | 1–4 | Java/YAML 统一 Catalog，java bind 可实例化 |
| B：事务扩展面 | 5–6 | Capability 与 Contribution 原子可见 |
| C：安装管理 | 7–10 | Core 落库、autoInstall、管理 API 和宿主装配闭环 |
| D：Console 迁移 | 11–12 | 页面菜单 Contribution、MISSING 资源保留和 ownerPluginId 权限 |
| E：单轨收口 | 13–14 | Extension 完全删除，全仓测试通过 |

每个阶段都必须保持仓库可编译。阶段 D 完成前可以保留旧 Extension 代码用于迁移编译，但不得在应用装配中
同时启动两套 Runtime；阶段 E 必须物理删除旧入口。

## 6. 验收标准

- Java 和 YAML `java` bind 进入同一 PluginCatalog；
- Capability 实现类没有新增 SPI 或扫描机制；
- 宿主与插件内部均通过同一 CapabilityManager 调用内部能力，且不可观察未 ACTIVE Provider；
- providerId 插件内全局唯一且配置路径无冲突；
- CapabilityRequirement 的 tags 与运行路由使用相同匹配语义；
- YAML 声明和实现类只使用宿主 Application ClassLoader，不存在 TCCL 切换或隐含隔离承诺；
- PluginState 不包含安装管理状态；
- PluginManager 不访问数据库；
- PluginInstallationManager 位于 Core 并拥有安装命令；
- autoInstall 默认 false 且只影响首次发现；
- FAILED 插件在完整清理后可受控重试，重试不覆盖持久化启用意图；
- `/console/plugins` 管理 API 只调用 Core Manager，不绕过领域边界访问 DAO；
- Console 没有安装持久化和第二套状态机；
- Console Contribution 与 Capability 共享 availability；
- MISSING 插件的 Console 资源身份由安全快照保留，不能被其它插件抢占；
- Kernel 权限资源使用 ownerPluginId；
- 新表和字段具有显式参考 DDL；历史数据迁移不依赖自动猜测身份；
- 生产代码不再依赖 Extension SPI、Registry 和旧安装表；
- 完整 DSL 保持公开稳定，Minimal V1 对未实现能力显式拒绝；
- 全部 Maven 验证通过。
