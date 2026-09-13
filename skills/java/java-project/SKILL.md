---
name: java:project
display_name: Java 工程与构建
description: |
  Java 工程创建与调整的使用约定与标准（非当前仓库依赖关系说明）。当用户要新建
  Maven 模块、调整已有工程结构、配置 POM（parent/BOM/最小依赖引用）、选择应引用
  console/kernel/platform/spring/quarkus 等模块、设置 JDK 与编译基线、处理多模块
  reactor 构建或排查构建结构问题时使用。所有 JAR 版本统一由 innospots-nexus-bom
  管理，parent 默认 innospots-nexus-parent，禁止模块内单独写依赖版本。
  触发词：新建模块、工程结构、模块划分、POM 配置、Maven、BOM、parent、依赖引用、
  最小依赖、console、kernel、platform、spring、quarkus、构建配置、编译基线。
category: java
version: 1.2.0
---

# 工程创建、结构、构建与项目配置

## 定位

负责**新建工程或对已有工程进行调整**时的约定与标准：模块怎么划、POM 怎么写、
依赖怎么引、版本谁管、构建怎么跑。

交付物形态、模块类型决策与清单见 [project-deliverables.md](references/project-deliverables.md)。

**本技能不是**「当前 innospots-nexus 仓库依赖关系说明书」。仓库现状以各模块
`pom.xml` 与 `mvn dependency:tree` 为准；本技能给出**应遵循的规范**与选型表。

**规范与存量：** 新模块按规范写最小依赖；存量 POM 若有多余声明，收敛须单独 PR，
不得以存量为由在新代码中复制冗余模式（见 `dependency-conventions.md` →「规范与存量 POM」）。

不负责类与接口的设计（`java:design`），也不负责实现代码（`java:develop`）。
**默认不新建 Maven 模块**——新业务域优先在 kernel/platform 内加领域包（见 project-deliverables）。

权威依赖约定见 [dependency-conventions.md](references/dependency-conventions.md)。

## grill-me（结构变更前，必经）

以下操作**之前必须先**完成 `grill-me` 压力测试（见 `java:reference` →
[grill-me.md](../java-reference/references/grill-me.md)）：

- 新建 Maven 模块或调整 reactor 拓扑
- 改变模块间依赖方向（含 kernel/platform 协作方式）
- 拆分/合并模块、引入新的 application/adapter 层

**未安装 `grill-me` 时不得开始上述操作。** 先执行：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

按生成技能的**完整说明**执行（输出过长则重定向到临时文件再读）；相对路径从
**supporting-files** 目录解析。开发者确认 grill-me 结论后，方可改 POM / 注册模块。

纯 POM 版本对齐、插件版本升级、单模块内配置微调无需 grill。

## 构建基线（默认值，以 parent 为准）

| 项 | 约定值 |
|----|--------|
| groupId | `com.innospots` |
| 版本属性 | `${revision}` + `flatten-maven-plugin` |
| **parent（默认）** | `innospots-nexus-parent` |
| **版本清单（必须）** | `innospots-nexus-bom` — 所有 JAR 版本唯一来源 |
| Java release | `25` |
| Maven / JDK 下限 | Maven ≥ 3.9.0，JDK ≥ 25（enforcer） |

插件、注解处理器、公共 test 依赖等细节见 [build-config.md](references/build-config.md)。

## 依赖引用速查

| 需求 | 引用模块 | 勿重复声明 |
|------|---------|-----------|
| 管理台契约与扩展地基 | `innospots-nexus-console` | 其下的 `plugin`/`core`/`base` |
| 租户侧管理业务（业务类管理端） | `innospots-nexus-kernel` | 已传递的 `console` 等 |
| 运营侧平台（系统运营类） | `innospots-nexus-platform` | `kernel`（禁止互依） |
| Spring Boot 可运行服务 | `innospots-nexus-spring` 子模块 | 在中立库模块中引 starter |
| Quarkus 可运行服务 | `innospots-nexus-quarkus` 子模块 | 在中立库模块中引 Quarkus BOM |
| classpath 插件（不经管理台） | `innospots-nexus-plugin` | `console` / `kernel` |
| 同进程 kernel + platform | 新建 **application** 组装模块 | 让 `kernel` 与 `platform` 库模块互依 |

**可运行应用（规范）：** `*-app` + `kernel`（租户）或 `*-app` + `platform`（运营）；
详见 [dependency-conventions.md](references/dependency-conventions.md) →「可运行应用组装决策表」。

**硬性规则：**

1. **禁止**在模块 POM 中为任何 JAR 单独写 `<version>`。
2. **禁止**绕过 BOM 引入未登记的第三方坐标。
3. **最小依赖**：只写直接使用的最上层模块，其余靠传递依赖。
4. 新增第三方依赖：先改 BOM，再在模块中声明 `groupId:artifactId`。

完整说明、正反例与 Spring/Quarkus 组装见
[dependency-conventions.md](references/dependency-conventions.md)。

## 库模块分层（职责边界，非 POM 复制清单）

```text
innospots-nexus-bom / innospots-nexus-parent
        ↓
base → core → plugin → console → kernel
                              ↘ platform
        ↓（运行时绑定，独立聚合）
innospots-nexus-spring / innospots-nexus-quarkus
```

模块职责边界见 `AGENTS.md` 与 [module-layout.md](references/module-layout.md)。

## 新建模块流程

完整步骤与交付清单见 [project-deliverables.md](references/project-deliverables.md)。

1. **grill-me** — 结构变更类必经；结论写入 PR 或 ADR。
2. **确认边界与模块类型** — 三者都清楚才建新 Maven 模块；否则在现有模块内加领域包。
3. 在根 `pom.xml`（或所属聚合器）的 `<modules>` 中注册。
4. 配置 `<parent>`（见下表）；POM 模板见 [build-config.md](references/build-config.md)。
5. 按 [dependency-conventions.md](references/dependency-conventions.md) 只声明**最小**直接依赖，不写 `<version>`。
6. 若新模块要被其他模块依赖，在 **`innospots-nexus-bom`** 的 `dependencyManagement` 中登记。
7. 建 `src/main/java`、`src/test/java` 与**包根 only** `com.innospots.nexus.<module>`（不预建领域子包）。
8. `mvn validate` → `mvn -pl <module> -am clean compile` → `mvn -q help:effective-pom` → `dependency:tree`。
9. 交 **`java:design`**（领域包与契约），再 `java:develop`。

### parent 与 relativePath

| 新建模块位置 | `<parent>` | `<relativePath>` |
|-------------|-----------|------------------|
| 仓库根下库模块（如 `innospots-nexus-foo`） | `innospots-nexus-parent` | `../innospots-nexus-parent/pom.xml` |
| `innospots-nexus-spring` 子模块 | `innospots-nexus-spring` | `../pom.xml` |
| `innospots-nexus-quarkus` 子模块 | `innospots-nexus-quarkus` | `../pom.xml` |

> 根聚合器是 `innospots-nexus`（`packaging=pom`），**构建 parent** 是
> `innospots-nexus-parent`。库模块的 parent 指向 **parent**，不要指向根聚合器。

## 不得做的事

- 不得为了迁就本地旧 JDK 而下调 `maven.compiler.release` 或 enforcer 的 `requireJavaVersion`
- 不得单独引用 JAR 版本；不得在模块 POM 内联写 `<version>`（绕过 BOM）
- 不得为已传递引入的 innospots 模块重复声明依赖（违反最小引用原则）
- 不得在 `base`/`core`/`console`/`kernel`/`platform` 中直接引入 Spring/Quarkus 运行时
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

## 详细参考

- [project-deliverables.md](references/project-deliverables.md) — 交付物、模块类型、流程、清单
- [dependency-conventions.md](references/dependency-conventions.md) — 依赖引用、可运行应用组装
- [build-config.md](references/build-config.md) — parent/BOM/插件、POM 模板、排错
- [module-layout.md](references/module-layout.md) — 模块职责与包结构
