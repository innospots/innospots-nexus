# Nexus Plugin Runtime 设计

## 1. 文档定位

本文定义 Plugin Runtime 的规范模型，包括发现、定义编译、Capability、Contribution、依赖、生命周期、
并发、失败隔离和资源所有权。安装持久化见
[plugin-installation-design.md](plugin-installation-design.md)，YAML 语法见
[plugin-dsl-spec.md](plugin-dsl-spec.md)。

## 2. 设计原则

1. 一个 Plugin 是发现、配置、依赖、启停和失败隔离单元；
2. PluginManager 是当前 JVM 运行状态唯一事实源；
3. 定义阶段无副作用，实例和资源只在启动事务中创建；
4. Capability 和 Contribution 共享一个启动、停止和可用性边界；
5. Core 不解释具体 Contribution 字段；
6. 未安装或未启用插件不创建 Provider、不执行生命周期、不发布资源；
7. 运行时对象不进入数据库快照。

## 3. 运行时结构

```text
Declaration Sources
├── ServiceLoader<Plugin>
└── META-INF/nexus/plugin.yaml
              │
              ▼
      PluginDefinitionCompiler
              │
              ▼
      PluginDefinition
              │
              ▼
         PluginCatalog
              │ eligible pluginIds
              ▼
         PluginManager
       ┌──────┼───────────────┐
       ▼      ▼               ▼
 Capability  Contribution   ResourceScope
 Registry    Handlers       / EventBus
```

## 4. 唯一 SPI 与声明来源

Java 插件只通过以下 SPI 发现：

```text
META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
```

Capability 实现类不使用 SPI、注解扫描或包扫描。Java 插件通过 Factory 绑定实现；YAML Java 插件通过
`bind.kind=java` 和显式 `class` 绑定实现。

发现器必须枚举所有 Java SPI 和所有 `META-INF/nexus/plugin.yaml` 资源，并记录：

- sourceType：`JAVA` 或 `YAML`；
- sourceLocation：SPI 实现类或 YAML 资源 URL；
- sourceArtifact：可获得时记录 JAR 或 classes 根；
- discoveredAt。

同一个 pluginId 只能来自一个声明源。重复身份是 Catalog 全局错误，不按发现顺序消解。

## 5. 定义模型

### 5.1 PluginManifest

`PluginManifest` 是纯数据、可序列化的 YAML 源模型。它只能包含 DSL 字段，不包含 Java `Class`、Factory、
Constructor、Provider、Handler、ClassLoader 或实例配置值。

### 5.2 PluginDefinition

概念模型如下，具体 record 名称可在实现时保持现有兼容：

```java
public record PluginDefinition(
        String pluginId,
        String version,
        int apiVersion,
        I18nObject displayName,
        I18nObject description,
        Tags tags,
        ConfigDefinition config,
        List<CapabilityContribution<?>> capabilities,
        List<CapabilityRequirement> requirements,
        List<PluginContribution> contributions
) {
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

该模型可以保存无副作用 Factory，但不能保存 Provider 实例、配置值、ClassLoader 或运行状态。所有集合和 Map
必须防御性复制。YAML 的 `bind` 只在编译阶段解析为 Factory，V1 只支持 `kind=java`。

### 5.3 PluginDefinitionSnapshot

Snapshot 是持久化专用摘要，包含身份、版本、来源、CapabilityKey、providerId、标签、配置 schema 摘要和
Contribution 资源身份。Snapshot 不得通过直接序列化 PluginDefinition 生成。

## 6. 静态校验

### 6.1 全局致命错误

以下错误使 Catalog 构建失败：

- 重复 pluginId；
- 同一 CapabilityKey 映射到不同 Java API；
- 重复的全局资源身份或 exposure 身份；
- 同一声明源无法确定归属。

### 6.2 单插件定义错误

以下错误使对应插件进入拒绝报告，但不应阻止其他无关插件被发现：

- 非法字段、ID、版本或配置 schema；
- providerId 在插件内重复；
- 未知 CapabilityType；
- Java 类不存在、不可实例化或 API 类型不匹配；
- 宿主不支持合法的 bind、exposure 或 Contribution 类型。

发现结果应表达为：

```text
PluginDiscoveryReport
├── validCatalog
└── rejectedDefinitions[]
    ├── source
    ├── claimedPluginId
    └── diagnostics[]
```

重复 pluginId 无法安全确定归属，因此仍是全局致命错误。拒绝的定义不能写入安装表。

## 7. Capability

### 7.1 类型与 Provider 身份

Capability API 身份为：

```text
CapabilityKey(type, majorVersion)
```

Provider 身份为：

```text
ProviderRef(pluginId, providerId)
```

`providerId` 在一个插件内全局唯一。一个插件可以为同一 CapabilityKey 提供多个 Provider，也可以为不同
CapabilityKey 提供不同 Provider。

### 7.2 类型注册

宿主必须显式注册受支持的 API：

```java
CapabilityTypeRegistry.builder()
        .register(MessageSender.TYPE)
        .register(ToolInvoker.TYPE)
        .build();
```

注册表只建立 `type@majorVersion -> Java API`，不注册实现类。

### 7.3 Java Binding

Java Builder 直接接收 Factory：

```java
.provide(MessageSender.TYPE, "wecom", Tags.of("provider", "wecom"),
        WeComMessageSender::new)
```

YAML Java Binding 显式声明类：

```yaml
- type: message.sender
  majorVersion: 1
  providerId: wecom
  tags:
    provider: wecom
  bind:
    kind: java
    class: com.example.message.WeComMessageSender
```

YAML 编译顺序：

1. 从 CapabilityTypeRegistry 解析 API；
2. 使用声明源 ClassLoader 执行不初始化类的加载；
3. 校验 concrete、public、实现 CapabilityProvider 和 Capability API；
4. 校验 public 无参数构造函数；
5. 生成等价于 `constructor::newInstance` 的 Factory；
6. 启动时才创建实例并调用 initialize。

V1 使用 Application ClassLoader，因此只承诺“类对声明源可见”。如果宿主启用 JAR 来源校验，可以要求
YAML 资源与实现类 `CodeSource` 相同；没有独立 ClassLoader 时不能承诺真正的插件私有类隔离。

### 7.4 配置

插件共享配置：

```text
plugins.<pluginId>.<localKey>
```

Provider 私有配置：

```text
plugins.<pluginId>.providers.<providerId>.<localKey>
```

Provider Context 提供只读的插件共享配置和当前 Provider 私有配置。配置不选择实现类，不能修改 bind、
exposures 或静态身份。

### 7.5 Tags 路由

Provider Tags 由 Plugin 默认 Tags 与 Provider Tags 合并。同名不同值是定义错误。

路由顺序：

1. 取 CapabilityKey 的全部 ACTIVE Registration；
2. 以“请求 Tags 是 Provider Tags 的子集”过滤；
3. 调用方未提供 Tags 时应用宿主默认 Tags；
4. 仍无默认且只有一个 Provider 时返回该 Provider；
5. 零匹配返回未找到；
6. 多匹配返回 `CAPABILITY_AMBIGUOUS`。

SPI 顺序、YAML 顺序、providerId 和类名不能作为歧义决胜规则。管理和诊断可以按完整 ProviderRef 精确查询。

## 8. 远程 Binding 与 Exposure 扩展点

完整 DSL 定义 `http`、`process`、`mcp` 和 `contract`，但 Runtime 只有在宿主注册适配器后才能解析：

```java
public interface CapabilityBindingAdapter {

    CapabilityKey capabilityKey();

    String bindKind();

    CapabilityProviderFactory<?> compile(BindingDescriptor descriptor);
}
```

适配器必须拥有请求编码、响应解码、错误映射、超时和资源释放语义。任意 Java 接口不能仅凭 URL 或工具名
自动生成可靠代理。

最小 V1 不实现远程 Binding 和 exposures。完整宿主实现它们时，必须使用同一 Plugin availability 门控。

## 9. Capability 依赖

插件只声明 CapabilityRequirement，不声明对具体 pluginId 的直接依赖。依赖一个能力，而不是某个实现，
避免插件之间硬耦合。

required requirement 没有 ACTIVE Provider 时，插件进入 WAITING；optional requirement 不阻止启动。
停止单个插件前，如果会使某个 ACTIVE 插件的 required Capability 失去最后 Provider，则拒绝停止。应用整体
关闭时按启动逆序停止，不执行该保护。

宿主配置中的 requiredPluginIds 只是应用启动策略，不属于 PluginDefinition 的插件依赖。

## 10. Contribution

Core 提供最小类型化契约：

```java
public interface PluginContribution {

    PluginContributionType<? extends PluginContribution> type();
}
```

```java
public interface PluginContributionHandler<T extends PluginContribution> {

    PluginContributionType<T> type();

    void validate(PluginCatalog catalog, List<PluginContributionEntry<T>> entries);

    PreparedPluginContribution prepare(
            PluginContributionContext context,
            T contribution);
}
```

Handler 规则：

- `validate` 无副作用，负责该类型的全局规则；
- `prepare` 可以读取当前插件只读配置，但不能发布资源；
- Prepared handle 提供 `stage`、`commit`、`rollback` 和 `close`；
- `commit` 不执行高风险 I/O；
- Handler 不创建第二套插件状态机。

V1 只有 `console@1`，由 Console 模块解释。

YAML 编译器不能直接依赖 Console 类型。Core 提供 Contribution Decoder 注册边界：

```java
public interface PluginContributionDecoder<T extends PluginContribution> {

    PluginContributionType<T> type();

    T decode(Map<String, Object> declaration);
}
```

应用装配时由 Console 注册 `console@1` Decoder。Core 只保留通用 manifest 数据、类型身份和解码错误，
不得 import `ConsolePluginContribution`。Decoder 只负责从已经通过结构校验的声明构造不可变 Contribution，
业务全局校验仍由 Handler 完成。

## 11. PluginState

```text
DISCOVERED → DESCRIBED
DESCRIBED  → WAITING | STARTING | FAILED
WAITING    → STARTING | FAILED
STARTING   → ACTIVE | FAILED
ACTIVE     → STOPPING
STOPPING   → STOPPED | FAILED
STOPPED    → STARTING
```

PluginState 不包含 REGISTERED、DISABLED 或 MISSING。这些是安装管理概念。

## 12. 单插件启动事务

启动顺序必须固定：

```text
1. 管理器校验配置和 required Capability
2. 创建 ResourceScope、Context 和 availability token
3. ContributionHandler.prepare
4. 创建 CapabilityProvider
5. plugin.initialize
6. capabilityProvider.initialize
7. plugin.start
8. stage Contribution
9. 发布 Capability 注册并 commit Contribution，但仍由 availability 隐藏
10. availability CAS → ACTIVE
11. 发布 PluginStartedEvent
```

第 10 步是唯一外部可见性线性化点。`commit` 只把资源放入带 pluginId 和 generation 的活动索引；所有查询
和入口仍必须检查同一 availability snapshot，因此不会在 ACTIVE 前被观察到。

如果第 1–9 步任一步失败：

1. availability 保持不可用；
2. 逆序 rollback staged handles；
3. 销毁已创建 Provider；
4. 调用 plugin.stop；
5. close PreparedContribution 和 ResourceScope；
6. 清除强引用；
7. 记录 FAILED、phase 和首个根因。

清理失败作为 suppressed cause 或独立诊断保留，不能覆盖首个失败。

## 13. 停止事务

```text
1. ACTIVE → STOPPING
2. availability CAS → UNAVAILABLE
3. 拒绝新 Capability 查询和新入口请求
4. 从活动索引撤出 Capability
5. 逆序 rollback/close Contribution 句柄
6. 逆序销毁 Provider
7. plugin.stop
8. close ResourceScope
9. 清除强引用
10. STOPPED
```

最小 V1 不提供完整调用引用计数。已进入调用的请求可能与停止并发，宿主必须在管理界面明确需要维护窗口
或重启生效的场景。

## 14. 并发与资源所有权

- PluginManager 串行协调 discover/start/stop/close；
- 不在持有内部锁时调用 Plugin、Provider、Handler 或外部适配器；
- Capability、Contribution 和 availability 使用不可变快照与原子替换；
- 单次调用读取同一 generation 的 availability；
- 创建资源的组件负责登记到 ResourceScope；
- 资源按获取逆序关闭；
- close 幂等，关闭后不得再次启动。

## 15. 查询与诊断

`PluginRuntimeInfo` 只包含运行时事实：

- pluginId、version；
- PluginState 和 phase；
- Capability/Contribution 摘要；
- 依赖等待原因；
- discoveredAt、startedAt、stoppedAt；
- 当前 JVM 最后错误。

它不包含 desiredEnabled、installed 或 presence，也不得暴露 Plugin、Provider、Factory、Handler、
ClassLoader、配置值、Secret 或资源句柄。管理端组合视图由 PluginInstallationManager 聚合安装与运行信息。

## 16. 测试要求

至少覆盖：

- Java SPI 和多个 YAML 资源可确定发现；
- definition 每个 Plugin 只读取一次；
- 重复 pluginId 全局失败；
- 单插件非法定义不阻止无关合法插件进入 Catalog；
- providerId 插件内重复失败；
- Capability API 类型冲突失败；
- Java bind 类和构造函数校验；
- Tags 唯一选择、未找到和歧义；
- required Capability 的 WAITING、恢复和停止保护；
- Handler prepare/commit 失败不留下可见资源；
- availability 激活前不可查询；
- 停止先关闭门控再释放资源；
- 回滚逆序且保留首个异常；
- close 幂等并释放强引用。
