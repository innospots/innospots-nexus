---
name: java:project
display_name: Java 工程与构建
description: |
  Java 工程创建、结构设计、构建与项目配置。当用户要新建 Maven 模块、调整模块
  结构、配置 POM（parent/bom/dependencyManagement/插件/版本）、设置 JDK 与编译
  基线、配置注解处理器（Lombok/MapStruct）、处理多模块 reactor 构建、配置
  enforcer 规则或排查构建结构问题时使用。
  触发词：新建模块、工程结构、模块划分、POM 配置、Maven、BOM、parent、
  dependencyManagement、构建配置、多模块、编译基线、JDK、annotationProcessor。
category: java
version: 1.0.0
---

# 工程创建、结构、构建与项目配置

## 定位

负责**工程骨架**：模块怎么划、POM 怎么写、版本谁管、构建怎么跑。
不负责类与接口的设计（`java:design`），也不负责实现代码（`java:develop`）。

## 当前工程基线

| 项 | 值 |
|----|----|
| groupId | `com.innospots` |
| 根 artifactId | `innospots-nexus`（`packaging=pom`） |
| 版本 | `${revision}`（`0.1.0-SNAPSHOT`），配合 `flatten-maven-plugin` 的 `resolveCiFriendliesOnly` |
| Java release | `25`（`maven.compiler.release`） |
| Maven 最低版本 | `3.9.0`（enforcer `requireMavenVersion`） |
| JDK 最低版本 | `25`（enforcer `requireJavaVersion`） |
| 编码 | UTF-8 |
| 构建插件 | clean 3.5.0 / resources 3.5.0 / compiler 3.15.0 / surefire 3.5.6 / failsafe 3.5.6 / enforcer 3.6.3 / jar 3.5.0 / source 3.4.0 / javadoc 3.12.0 / dependency 3.11.0 / versions 2.21.0 / flatten 1.7.3 |
| 注解处理器 | Lombok 1.18.46 + MapStruct 1.6.3（在 compiler 插件 `annotationProcessorPaths` 中声明） |
| 全局测试依赖 | `junit-jupiter`、`assertj-core`、`mockito-core`（parent 统一提供，test scope） |
| 全局编译依赖 | `slf4j-api`、`lombok`（provided） |

## 模块职责与依赖方向

```text
innospots-nexus-bom          （依赖版本清单）
innospots-nexus-parent       （构建 parent：属性、插件、公共依赖）
        ↓
innospots-nexus-base         （纯 Java 基础，零中间件）
        ↓
innospots-nexus-core         （业务中立的中间件/数据库/平台基础设施）
        ↓
innospots-nexus-console      （管理控制台契约与扩展声明）
        ↓                 ↓
innospots-nexus-kernel   innospots-nexus-platform   （平行，互不依赖）
```

| 模块 | 允许包含 | 禁止包含 |
|------|---------|---------|
| `base` | 异常、状态码、响应包装、领域事件契约、MapStruct 支持、JSON、ID 生成、加解密、HTTP 工具等依赖轻量的通用能力 | 任何业务领域逻辑；数据库、消息、调度、Servlet、Spring、Quarkus 等运行时基础设施 |
| `core` | 共享持久化实体与公共表、数据库支持、调度、服务生命周期、会话基础设施、watcher、扩展边界 | 具体业务域（用户/角色/权限/菜单不属于此）；绑定 Spring Boot 自动配置 |
| `console` | Jakarta REST 端点契约、扩展声明、菜单/路由贡献模型、共享管理台抽象 | 具体管理业务功能实现 |
| `kernel` | 认证、注册、用户、角色、权限、菜单、字典、审计等基础管理能力 | 与 platform 相互依赖 |
| `platform` | 租户（`nx_tenant`）、企业主体（`nx_enterprise`）、平台用户、支持访问、平台审计；暴露 `/platform/**` | 对外自助注册入口；依赖 kernel |

完整职责说明见 `AGENTS.md` 与 [module-layout.md](references/module-layout.md)。

## 依赖管理规则

1. **依赖版本只在 `innospots-nexus-bom` 中定义**，模块 POM 只声明 `groupId:artifactId`，不写 `<version>`。
2. **共享的 Java 模块依赖放 `innospots-nexus-parent`**，不放根聚合器、不放 BOM。
3. 内部模块依赖（`innospots-nexus-base` 等）也在 BOM 里统一管版本。
4. 引入 BOM 形式的第三方（如 `junit-bom`、`jackson-bom`）用 `<scope>import</scope>` + `<type>pom</type>`。
5. 新增第三方依赖：先在 BOM 加 `<xxx.version>` 属性与 `dependencyManagement` 条目，再在具体模块声明依赖。
6. 业务专属基础设施放在其所属业务模块或独立 adapter / plugin / extension / application 模块。

## 新建模块流程

1. **确认边界** — 模块边界、依赖方向、可独立测试性三者都清楚才建新模块；否则并入现有模块。
2. 在根 `pom.xml` 的 `<modules>` 中注册。
3. 模块 POM 以 `innospots-nexus-parent` 为 parent，`<relativePath>../innospots-nexus-parent/pom.xml</relativePath>`（若在项目根则 `../pom.xml` 指向聚合器时需注意 parent 应为 parent 模块）。
4. 只声明需要的依赖，不写 `<version>`。
5. 若新模块要被其他模块依赖，在 BOM 的 `dependencyManagement` 中登记。
6. 建 `src/main/java`、`src/test/java` 与包根 `com.innospots.nexus.<module>`。
7. 运行 `mvn clean compile` 与 `mvn -q help:effective-pom` 验证。

> 注意：本仓库根聚合器是 `innospots-nexus`，构建 parent 是 `innospots-nexus-parent`。
> 各业务模块的 `<parent>` 应指向 `innospots-nexus-parent`，而不是根聚合器。

## 不得做的事

- 不得为了迁就本地旧 JDK 而下调 `maven.compiler.release` 或 enforcer 的 `requireJavaVersion`
- 不得在模块 POM 内联写依赖版本（绕过 BOM）
- 不得让 `kernel` 依赖 `platform` 或反向依赖
- 不得在 `base` 引入任何中间件或运行时框架依赖
- 不得在 `core` 绑定 Spring Boot 自动配置
- 不得复制遗留工程的 POM 结构

## 常用验证命令

```bash
mvn clean compile                 # 编译门禁
mvn validate                      # enforcer 校验 Maven/JDK/插件版本
mvn -q help:effective-pom         # 检查 POM 合并结果与依赖版本来源
mvn -pl <module> -am clean install # 单模块及其依赖构建
mvn versions:display-dependency-updates   # 依赖升级候选（需人工评估）
```

详细配置模板与插件说明见 [build-config.md](references/build-config.md)。
