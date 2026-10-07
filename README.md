# innospots-nexus

`innospots-nexus` 是新一代 AI 企业平台的轻量级基础工程。
项目从简单的平台能力起步，逐步演进为插件、扩展与应用体系。

## 坐标

- Group: `com.innospots`
- Artifact: `innospots-nexus`
- Version property: `revision`
- Current version: `1.0.0-SNAPSHOT`
- JDK: 25
- Build: Maven

## 版本管理

项目采用 Maven CI-friendly 版本管理。在根 `pom.xml` 中保持单一事实来源：

```xml
<revision>1.0.0-SNAPSHOT</revision>
```

模块 parent 版本与内部依赖版本应引用 `${revision}`。`flatten-maven-plugin`
绑定到构建流程以生成解析后的 consumer POM。生成的 `.flattened-pom.xml` 文件由 git 忽略。

## 设计原则

- 领域驱动设计优先：模块应清晰表达领域、应用、端口、扩展与基础设施边界。
- 最小依赖：基础框架只提供 foundational 平台能力。
- 基础层不硬依赖 Spring：当具体运行时需要时，由 adapter 或 application 模块引入 Spring。
- 开发者驱动代码创建：可阅读旧项目代码以理解上下文，但不得复制、迁移或机械改写到本仓库。
- 增量演进：仅在领域边界、依赖方向与测试策略明确后再新增模块。

## 模块

### `innospots-nexus-parent`

内部 Java 模块的构建 parent。它导入项目 BOM、集中管理插件配置、
强制 JDK/Maven 基线，并提供 SLF4J API、Lombok、测试库等共享模块依赖。

### `innospots-nexus-bom`

内部模块与扩展候选的依赖管理。BOM 集中管理版本，不要求下游模块继承
Spring Boot parent 或 runtime。它应仅做 dependency-management，
不得定义继承的模块依赖。

### `innospots-nexus-base`

纯 Java 基础模块。此模块仅用于代码级契约与小型工具。
不得依赖数据库、缓存、消息 broker、HTTP runtime、Spring 或其他中间件。

首次初始化只创建模块与源码目录。此阶段不生成 Java 实现。

### `innospots-nexus-core`

Core 平台模块，承载领域、应用、端口与扩展边界。可依赖 `innospots-nexus-base`，
后续可定义 interface 级中间件 port，但在 foundation 阶段不应提供
Spring Boot starter、数据库 auto-configuration 或具体基础设施实现。

## 参考项目策略

旧项目位于 `/Users/yxy/works/innospots_ent/innospots_premium`，仅作参考。
可用于理解产品历史、命名压力与大致模块职责。不得直接复制源文件、
POM 片段、包布局或实现细节。

## 构建

```bash
mvn validate
mvn test
mvn -q help:effective-pom
```

构建强制 JDK 25。若 Maven 使用较旧的本地 JDK，在切换到 JDK 25 之前验证失败是预期行为。
