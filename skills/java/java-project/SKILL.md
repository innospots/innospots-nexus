---
name: java:project
display_name: Java 工程与构建
description: |
  **外部产品工程**的 Maven 创建与调整约定（非 innospots-nexus 本仓库说明书）。
  当用户在仓库外、**模块结构已确认**的产品工程上配置 POM、挂接 innospots-nexus-parent/BOM、
  引用 Nexus 平台库依赖、或排查外部 reactor 时使用。新建工程模块为 bom/core/console/service/ui
  与 spring 或 quarkus（二选一）；须询问管理/对外形态与运行框架。不得照搬 Nexus 模块名为产品模块。
  一级 Java 模块须继承 innospots-nexus-parent；产品 BOM 须 import innospots-nexus-bom。
  修改 innospots-nexus 仓库内的平台库模块时，以根 AGENTS.md 与 module-layout.md 为准，
  本技能仅作 parent/BOM/最小依赖引用补充。新建外部工程前必经 grill-me。
  触发词：外部工程、产品工程、nexmux、新建仓库、*-console、*-service、客户项目、
  Maven、BOM、parent、依赖引用、spring、quarkus、构建配置。
category: java
version: 1.4.0
---

# 工程创建、结构、构建与项目配置

## 定位

负责在 **innospots-nexus 仓库之外** 新建或调整**产品工程**时的约定：reactor 怎么划、
POM 怎么写、如何挂接 `innospots-nexus-parent` / `innospots-nexus-bom`、如何引用
Nexus 平台库（`console`、`portal`、`service` 等）。

| 场景 | 以谁为准 |
|------|---------|
| **外部产品工程**（`{product}-console` / `{product}-service` / …） | 本文 + [external-project-layout.md](references/external-project-layout.md) |
| **innospots-nexus 本仓库**（`innospots-nexus-*` 平台库） | 根 [`AGENTS.md`](../../../AGENTS.md) + [module-layout.md](references/module-layout.md) |

**本技能不是**「innospots-nexus 仓库依赖关系实时说明书」。本仓库现状以各模块 `pom.xml`
与 `mvn dependency:tree` 为准。

交付物见 [project-deliverables.md](references/project-deliverables.md)。
不负责类与接口设计（`java:design`）与业务实现（`java:develop`）。

**外部产品 — 新建：** 先问 **仅管理 / 仅对外 / 两者**；纯对外则 **不要** console、ui。
模块类型含 bom、core、按需 console/service/ui；**spring 或 quarkus 二选一**（须问用户）。
**外部产品 — 已有：** 以确认过的 `<modules>` 为准，不擅自加模块。
Nexus `portal` / `platform` 等仅为 **Maven 依赖**，不是产品 Maven 模块名。

**本仓库内：** 默认不新建 Maven 模块 — 新业务域优先在 `portal` / `platform` 内加领域包。

权威依赖约定见 [dependency-conventions.md](references/dependency-conventions.md)。

## AGENTS.md 前置（动工程前必读）

| 工作对象 | 必读 |
|----------|------|
| **外部产品仓库** | grill-me 结论 + [external-project-layout.md](references/external-project-layout.md)；在产品根 **新建/修订** `AGENTS.md`（见 [agents-template.md](../java-reference/references/agents-template.md)） |
| **innospots-nexus 本仓库** | 根 [`AGENTS.md`](../../../AGENTS.md) 模块职责与依赖规则；改 reactor 仍须 grill-me |

POM 变更须与对应 AGENTS 一致；交付后交 `java:check` 做合规检查。

## grill-me（结构变更前，必经）

以下操作**之前必须先**完成 `grill-me` 压力测试（见 `java:reference` →
[grill-me.md](../java-reference/references/grill-me.md)）：

- 新建 Maven 模块或调整 reactor 拓扑
- 改变模块间依赖方向（含 portal/platform 协作方式）
- 拆分/合并模块、引入新的 application/adapter 层

**未安装 `grill-me` 时不得开始上述操作。** 先执行：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

按生成技能的**完整说明**执行（输出过长则重定向到临时文件再读）；相对路径从
**supporting-files** 目录解析。审查范围须包含 **`AGENTS.md`**（模块职责、依赖规则）
与目标 `pom.xml` / `module-layout.md`。开发者确认 grill-me 结论后，再核对 AGENTS.md，
方可改 POM / 注册模块。

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
| 租户侧管理业务（业务类管理端） | `innospots-nexus-portal` | 已传递的 `console` 等 |
| 运营侧平台（系统运营类） | `innospots-nexus-platform` | `portal`（禁止互依） |
| Spring Boot 可运行服务 | `innospots-nexus-spring` 子模块 | 在中立库模块中引 starter |
| Quarkus 可运行服务 | `innospots-nexus-quarkus` 子模块 | 在中立库模块中引 Quarkus BOM |
| classpath 插件（不经管理台） | `innospots-nexus-plugin` | `console` / `portal` |
| 同进程 portal + platform | 新建 **application** 组装模块 | 让 `portal` 与 `platform` 库模块互依 |

**可运行应用（规范）：** `*-app` + `portal`（租户）或 `*-app` + `platform`（运营）；
详见 [dependency-conventions.md](references/dependency-conventions.md) →「可运行应用组装决策表」。

**硬性规则：**

1. **禁止**在模块 POM 中为任何 JAR 单独写 `<version>`。
2. **禁止**绕过 BOM 引入未登记的第三方坐标。
3. **最小依赖**：只写直接使用的最上层模块，其余靠传递依赖。
4. 新增第三方依赖：先改 BOM，再在模块中声明 `groupId:artifactId`。

完整说明、正反例与 Spring/Quarkus 组装见
[dependency-conventions.md](references/dependency-conventions.md)。

## 库模块分层（两套视图）

**外部产品工程：** 以产品**已有** `<modules>` 为准；仅在各模块 POM 上挂
`innospots-nexus-parent`、BOM 与 Nexus **库依赖**（不新增模块对齐 Nexus）。
见 [external-project-layout.md](references/external-project-layout.md)。

**Nexus 平台库**（本仓库 `innospots-nexus-*`）：

```text
innospots-nexus-bom / innospots-nexus-parent
        ↓
base → core → plugin → console → portal
                              ↘ platform
        ↓（运行时绑定，独立聚合）
innospots-nexus-spring / innospots-nexus-quarkus
```

见根 `AGENTS.md` 与 [module-layout.md](references/module-layout.md)。

## 新建模块流程

完整步骤与交付清单见 [project-deliverables.md](references/project-deliverables.md)。

1. **grill-me** — 结构变更类必经；结论写入 PR 或 ADR。
2. **核对 AGENTS.md** — 模块职责、依赖方向、核心约束与 grill-me 结论一致。
3. **确认边界与模块类型** — 三者都清楚才建新 Maven 模块；否则在现有模块内加领域包。
4. 在根 `pom.xml`（或所属聚合器）的 `<modules>` 中注册。
5. 配置 `<parent>`（见下表）；POM 模板见 [build-config.md](references/build-config.md)。
6. 按 [dependency-conventions.md](references/dependency-conventions.md) 只声明**最小**直接依赖，不写 `<version>`。
7. 若新模块要被其他模块依赖，在 **`innospots-nexus-bom`** 的 `dependencyManagement` 中登记。
8. 建 `src/main/java`、`src/test/java` 与**包根 only**（外部产品：`com.<vendor>.<product>.<module>`；本仓库：`com.innospots.nexus.<module>`，不预建领域子包）。
9. `mvn validate` → `mvn -pl <module> -am clean compile` → `mvn -q help:effective-pom` → `dependency:tree`。
10. 按 [agents-template.md](../java-reference/references/agents-template.md) **增补根 `AGENTS.md`**（模块职责 + 依赖规则）。
11. 交 **`java:design`**（领域包与契约），再 `java:develop` → **`java:check`**（含 AGENTS 合规）。

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
- 不得在 `base`/`core`/`console`/`portal`/`platform` 中直接引入 Spring/Quarkus 运行时
- 不得让 `portal` 依赖 `platform` 或反向依赖
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
- [external-project-layout.md](references/external-project-layout.md) — **外部产品**模块职责与 reactor
- [module-layout.md](references/module-layout.md) — **innospots-nexus 平台库**模块职责
- [agents-template.md](../java-reference/references/agents-template.md) — 新建模块时根 AGENTS.md 增补片段
