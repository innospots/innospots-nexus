# Agent 操作指南

本仓库为 greenfield 重建工程。旧 Innospots 项目仅作参考，不是源码模板。

## 核心约束

- 不得将 legacy 源码复制到本仓库。
- 不得将 legacy 文件移动到本仓库。
- 不得机械复刻 legacy POM 或包结构。
- 创建新行为前，应询问或推断当前开发者意图。
- 保持 foundation 轻量、依赖最小。

## 依赖规则

- `innospots-nexus-base` 必须保持 middleware-free。
- 内部 Java 模块应继承 `innospots-nexus-parent`。
- 依赖版本归属 `innospots-nexus-bom`。
- 共享 Java 模块依赖归属 `innospots-nexus-parent`，不在根 aggregator 或 BOM 中定义。
- `innospots-nexus-core` 可依赖 `innospots-nexus-base`。
- `innospots-nexus-core` 后续可定义 port/interface 级中间件边界，
  但不得绑定 Spring Boot auto-configuration。
- 具体基础设施归属未来的 adapter、plugin、extension 或 application 模块。

## DDD 规则

- 按职责与边界命名包与模块。
- 保持领域概念独立于基础设施实现。
- 中间件集成优先采用 ports and adapters。
- 仅在边界足够清晰、可独立测试时再新增模块。

## 编码风格

- 所有 `if`、`else`、`for`、`while` 块必须使用花括号 `{}`，
  即使只有一条语句。条件与语句不得写在同一行。
- 使用 4 空格缩进。
- 保持合理行宽（建议最多 120 字符）。

## 验证

当本地 JDK 支持配置的 release 时，结构变更后运行：

```bash
mvn validate
mvn test
mvn -q help:effective-pom
```

若本地 JDK 低于 25，应报告环境不匹配，而不是降低项目基线。
