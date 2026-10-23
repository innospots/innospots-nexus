# 插件二次开发流程

## 1. 先确定三件事

1. 插件身份：稳定的小写反向域名 `pluginId`。
2. 配置边界：插件需要哪些键、类型、默认值、必填与密文。
3. Contribution：当前实现是否只需要 `console@2` 目录声明。

当前插件不再声明 Capability、Provider、依赖或远程 bind。

## 2. 开发步骤

```text
① 定义 pluginId / version / displayName
② 用 ConfigDefinition 或 YAML spec.config 声明配置 schema
③ 用 ConsoleContributeNode 或 YAML contributions 声明 console@2
④ 选择 Java SPI 或 YAML 之一
⑤ 在宿主中注册 Contribution Decoder / Handler / Snapshotter
⑥ 提供 plugins.<pluginId>.<key> 运行值
⑦ 测试定义、配置、Entry 身份和启动事务
```

## 3. 声明方式

| 方式 | 适用 | 文档 |
|------|------|------|
| Java SPI | 需要编译期组装、常量复用或自定义 `Plugin` 生命周期 | [03-java-plugin.md](03-java-plugin.md) |
| YAML | 声明为主，无自定义 Java 生命周期 | [04-yaml-plugin.md](04-yaml-plugin.md) |

同一 `pluginId` 只能出现一种来源。Java 与 YAML 均编译为 `PluginDefinition`。

## 4. 最小 Java 定义

```java
public final class ReportsPlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        return ConsoleContributions.entryPlugin(
                "com.example.reports.console",
                "1.0.0",
                ConsoleContributions.discoveryTags(
                        "com.example.reports.console", "reports", List.of("reports")),
                I18nObject.of("zh-CN", "报表"),
                I18nObject.of("zh-CN", "报表管理"),
                "reports",
                List.of(ConsoleContributeNode.singlePageModule(
                        "reports",
                        10,
                        I18nObject.of("zh-CN", "报表"),
                        I18nObject.of("zh-CN", "报表管理"),
                        "reports-reports-main",
                        "chart")));
    }
}
```

需要配置时，使用 `PluginDefinition.builder(...).config(...).contribute(...)` 组装。

## 5. 最小 YAML 定义

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.reports.console
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 报表
  config: []
  contributions:
    - type: console
      majorVersion: 2
      domainKey: reports
      modules:
        - moduleKey: reports
          displayName:
            zh-CN: 报表
          entryPageKey: reports-reports-main
          icon: chart
          children: []
```

## 6. 配置接入

插件只声明 schema，运行值由宿主提供：

```yaml
plugins:
  com.example.reports.console:
    endpoint: https://reports.example.com
```

完整优先级与读取方式见 [08-configuration.md](08-configuration.md)。

## 7. 验收清单

- [ ] `pluginId` 为合法反向域名，且 classpath 内唯一。
- [ ] `definition()` 无副作用，不读取配置值或数据库。
- [ ] 配置项有稳定键、明确类型，`SECRET` 不声明默认值。
- [ ] `console@2` 至少一个 MODULE；`entryPageKey` 稳定且符合 kebab-case。
- [ ] 宿主已注册对应 Decoder、Handler 与 Snapshotter。
- [ ] 配置值通过 `PluginContext.config()` 读取，不在插件内直接读环境变量。
- [ ] 发现结果进入 `validCatalog`，安装后可启动为 `ACTIVE`。
