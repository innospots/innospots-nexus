# Nexus Plugin Runtime 设计（v2）

## 1. 文档定位

本文定义 **当前** Plugin Runtime：发现、YAML/Java 编译、**Contribution**（含 `console@2`）生命周期、
并发与失败隔离。安装持久化见 [plugin-installation-design.md](plugin-installation-design.md)；
Console 与权限 catalog 见 [plugin-extensions-v2-design.md](plugin-extensions-v2-design.md)。

**不包含**：Capability 路由、Provider 注册表、跨插件 Capability 依赖（已从代码库删除）。

## 2. 设计原则

1. 一个 **Plugin** 是发现、配置、启停和失败隔离单元；
2. `PluginManager` 是当前 JVM 运行状态唯一事实源；
3. 定义阶段无副作用；Contribution 与插件级资源在 **启动事务** 中创建；
4. Core 通过 **Contribution Handler** 解释具体贡献类型，不在 `ManagedPlugin` 内硬编码 console 字段；
5. 未安装或未启用插件不执行生命周期、不发布 Contribution 资源；
6. 运行时对象与 Secret **不**进入 `definition_snapshot`。

## 3. 结构

```text
Declaration Sources
├── ServiceLoader<Plugin>
└── META-INF/nexus/plugin.yaml
              │
              ▼
      PluginDefinitionCompiler
              │
              ▼
      PluginDefinition  (identity, tags, config, contributions[])
              │
              ▼
         PluginCatalog
              │ eligible pluginIds
              ▼
    PluginInstallationManager / PluginManager
              │
              ▼
         ManagedPlugin
       ┌──────┴───────────────┐
       ▼                      ▼
 Contribution Handlers    PluginContext
 (prepare/stage/commit)    config / events / resources
```

## 4. 发现

- Java：唯一 SPI `com.innospots.nexus.core.plugin.contract.Plugin`；
- YAML：枚举全部 `META-INF/nexus/plugin.yaml`；
- 同一 `pluginId` 只能有一个声明源；重复为 Catalog 全局错误；
- 发现元数据：`sourceType`（JAVA/YAML）、`sourceLocation`、`discoveredAt`。

YAML 编译拒绝 `spec.capabilities` 与 `spec.requirements`（须改用 `contributions`；目标形态见
[plugin-dsl-spec.md](plugin-dsl-spec.md)）。

## 5. 定义模型

### 5.1 PluginManifest

YAML 纯数据模型，不含 Class、Factory 或配置值。当前文档头为 `apiVersion: nexus.plugin/v1`（见 DSL 规范）。

### 5.2 PluginDefinition

```java
public record PluginDefinition(
        String pluginId,
        String version,
        int apiVersion,
        I18nObject displayName,
        I18nObject description,
        Tags tags,
        ConfigDefinition config,
        List<PluginContribution> contributions
) {
}
```

- `Tags`、`ProviderRef` 位于 `declaration` 包；**不再**用于 Capability 路由；
- 每种 `PluginContributionType` 在单插件内最多出现一次；
- Java `PluginDefinition.builder()` 与 YAML 编译结果形状一致。

### 5.3 PluginDefinitionSnapshot

持久化静态摘要：`capabilities` 字段保留为空列表以兼容历史 JSON；**contributions** 由各类型
`PluginContributionSnapshotter` 生成安全 Map。

## 6. Contribution 生命周期

`ManagedPlugin.start()` 顺序（简化）：

```text
config-resolve
  → contribution-prepare (handler.prepare)
  → plugin-initialize(PluginContext)
  → plugin-start()
  → contribution-stage
  → contribution-commit
  → ACTIVE
```

`PluginContext` 提供：`definition()`、`config()`、`events()`、`resources()`、`logger()`。
**不提供** Capability 查询。

`stop()` 逆序：`contribution rollback/close` → `plugin.stop()` → `resource.close()`。

失败时回滚已 prepare 的 Contribution，不发布部分资源。

## 7. PluginManager

- `start()` / `start(pluginId)` / `stop(pluginId)` / `plugins()` / `close()`；
- 无依赖多轮扫描；`requiredPluginIds` 未全部 `ACTIVE` 时整批启动失败并回滚当次已启动插件；
- `PluginRuntimeConfig`：`required` / `disabled` / `hostConfig` / `configSources` / `runtimeVariables` /
  `pluginClassLoader`（**无** defaultCapabilityRoutes）。

宿主通过注册 `PluginContributionDecoder`、`PluginContributionHandler`、
`PluginContributionSnapshotter` 支持 `console@2` 等类型。

## 8. 并发

- 管理操作（`PluginManager` 的 start/stop/list）通过短暂令牌串行化；
- 插件 `initialize` / `start` / Handler 钩子在 **ManagedPlugin 锁外**执行，避免死锁；
- `PluginAvailability` 在 Contribution commit 后与 Capability 时代相同，用于门控 catalog 可见性。

## 9. 规划（未实现）

- `extensions[]` 统一 manifest（`provide` / `subscribe` / `contribute`）；
- `FunctionRegistry` 与 `invoke(functionName)`；
- 将 console 解码从泛型 `contribution` 包内聚为 `extension.console` facade。

详见 [plugin-extensions-v2-design.md](plugin-extensions-v2-design.md) §5–§6。
