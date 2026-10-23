# Exposure 与 Contribution

## 1. Exposure

当前实现**不提供** exposure 自动映射。插件需要对外 HTTP API 时，由宿主应用编写
Jakarta REST 端点，并通过自身服务边界调用插件提供的业务能力。

不要在插件 YAML 中声明 `exposures`；未支持字段会被编译器拒绝。

## 2. Contribution — `console@2`

Contribution 描述静态控制台目录：`domainKey`、MODULE、MENU 与稳定 `entryPageKey`。
它不声明 REST 实现、页面资源或数据库查询。

### 2.1 边界

| plugin | console |
|--------|---------|
| `PluginContribution` 接口 | `ConsoleCatalogSyncService` |
| `ConsolePluginContribution` | `ConsoleCatalogResourceEntity` |
| Decoder / Handler / Snapshotter | catalog 索引与权限/导航投影 |

Core 不解析 console 字段语义；console 注册 `ConsolePluginContributionDecoder`、
Handler 与 Snapshotter。

### 2.2 YAML 声明

```yaml
spec:
  contributions:
    - type: console
      majorVersion: 2
      domainKey: reports
      displayName:
        zh-CN: 报表
      orderIndex: 0
      modules:
        - moduleKey: reports
          displayName:
            zh-CN: 报表
          orderIndex: 10
          viewGroups:
            - group: default
              label:
                zh-CN: 默认
          operationGroups:
            - group: default
              label:
                zh-CN: 默认
          children:
            - displayName:
                zh-CN: 订单报表
              orderIndex: 10
              entryPageKey: reports-orders
              children: []
```

### 2.3 Java 声明

```java
ConsoleContributeNode module = ConsoleContributeNode.singlePageModule(
        "reports",
        10,
        I18nObject.of("zh-CN", "报表"),
        I18nObject.of("zh-CN", "报表管理"),
        "reports-reports-main",
        "chart");

PluginDefinition definition = PluginDefinition.builder("com.example.reports.console")
        .name("Reports Console")
        .version("1.0.0")
        .contribute(new ConsolePluginContribution(
                "reports",
                I18nObject.of("zh-CN", "报表"),
                I18nObject.of("zh-CN", "报表管理"),
                0,
                List.of(module)))
        .build();
```

### 2.4 节点规则

- 扩展根 `modules[]` 至少一项，且只能放 MODULE。
- MODULE 必须有 `moduleKey`，并且有 `entryPageKey` 或非空 `children`，二者不能同时存在。
- MENU 没有 `moduleKey`，必须有 `entryPageKey`，且 `children` 为空。
- `viewGroups` / `operationGroups` 挂在 MODULE，必须包含 `default`。
- `entryPageKey` 在所属 MODULE 内唯一；`moduleKey` 在插件内唯一。
- `orderIndex` 只影响同级展示顺序，不参与资源身份。

### 2.5 生命周期

```text
Handler.validate(catalog, allEntries)   # 启动前全局校验
  → prepare(context, contribution)      # 启动事务内
  → stage()
  → commit()
```

失败则 rollback，不留下可见目录资源。查询侧检查 `PluginAvailability.isActive()`。

### 2.6 与权限

catalog 中 MODULE/MENU 的稳定键、`viewGroups` / `operationGroups` 与
`@EndpointModule` / `@EndpointGrant` 共同构成权限目录。访问项 `grantName` 使用叶子
MODULE/MENU 的 `entryPageKey`；操作项使用 `@EndpointGrant.name`。

完整规则见
[`../design/plugin-extensions-v2-design.md`](../design/plugin-extensions-v2-design.md) §6。
