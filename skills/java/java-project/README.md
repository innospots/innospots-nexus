# java:project — 外部产品工程与 Maven 构建

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:project` 规定如何在 **innospots-nexus 仓库之外**、**模块已确认**的产品工程上调整 POM：
挂 `innospots-nexus-parent` / BOM。**新建**先问：仅管理 / 仅对外 / 两者，以及 Spring 或 Quarkus。
纯对外 → 无 console、ui；必有 bom、core 与所选运行时。**已有**工程不擅自加模块。

- 一级 Java 模块 **parent** → `innospots-nexus-parent`
- 产品 **BOM** → import `innospots-nexus-bom`

**不是**本仓库 `innospots-nexus-*` 平台库的说明书；改本仓库时以根 `AGENTS.md` 与
[module-layout.md](references/module-layout.md) 为准。

外部 reactor 与模块职责见 [external-project-layout.md](references/external-project-layout.md)。

## 怎么用

1. **`grill-me`（新建模块 / 结构变更前必经）** — 未安装则
   `npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"`（见 [grill-me.md](../java-reference/references/grill-me.md)）。
2. **区分仓库** — 外部产品按 [external-project-layout.md](references/external-project-layout.md)；本仓库内默认**不**新建 Maven 模块（见 project-deliverables）。
3. **新建模块** — 按 [project-deliverables.md](references/project-deliverables.md) 标准流程。
4. **选型依赖** — [dependency-conventions.md](references/dependency-conventions.md)。
5. **插件 / enforcer** — [build-config.md](references/build-config.md)。
6. **完成后** — 交 `java:design`（领域包与契约），再 `java:develop`；结构 PR 不夹带业务代码。

## 输入

| 类型 | 示例 |
|------|------|
| grill-me 结论 | 模块类型、依赖方向、是否 application/adapter |
| 工程意图 | 「加 adapter」「spring 统一进程 portal+platform」 |
| 模块边界 | 职责一句话、可独立测试、与 portal/platform 关系 |
| 目标运行时 | 纯库 / Spring Boot / Quarkus |
| 报错 | enforcer、传递依赖冲突、flatten |

## 输出

详见 [project-deliverables.md](references/project-deliverables.md)。

### 会修改的文件

| 变更 | 典型路径 |
|------|---------|
| 注册模块 | 根或 spring/quarkus `pom.xml` `<modules>` |
| 新模块 POM | `innospots-nexus-<name>/pom.xml` |
| BOM 登记 | `innospots-nexus-bom/pom.xml` |
| 目录骨架 | `src/main/java`、`src/test/java`、**包根 only** |
| 验证 | `validate`、`compile`、`effective-pom`、`dependency:tree` |

### Maven 模块类型（摘要）

| 类型 | 何时 |
|------|------|
| 领域包（无新 Maven 模块） | 新业务域、多数功能扩展 |
| adapter | 外部系统、客户专属集成 |
| application | 同进程 portal + platform |
| spring/quarkus 子模块 | 可运行服务 |

**不产出**：业务 Java 源码、设计文档、revision/BOM 大版本升级（交对应技能）。

## 适用场景

- 新建 Maven 模块或调整 reactor（经 grill-me）
- 配置 parent、BOM、flatten
- 最小依赖选型与排查
- 新建 Spring/Quarkus 可运行子模块
- 存量 POM 收敛（单独 PR）

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 领域包结构设计 | `java:design` + package-structure |
| 写 Entity/Endpoint | `java:develop` |
| 升 `${revision}` | `java:project-upgrade` |
| 升 JDK/第三方 | `java:dependency-upgrade` |
| Spring starter 细则 | `java:spring` |

**硬约束**：禁止 POM 内联 JAR `<version>`；禁止 portal↔platform 互依；禁止中立库绑 Spring/Quarkus。

## 与上下游技能

```text
grill-me（结构变更必经）
    ↓
java:project（Maven + 包根）
    ↓
java:design（领域包、契约）
    ↓
java:develop → java:check
```

若 design 判定只需加领域包，**撤销/不要**新建 Maven 模块（见 develop-deliverables「何时停 implement」）。

## 详细参考

- [project-deliverables.md](references/project-deliverables.md) — **交付物、模块类型、流程、清单**
- [dependency-conventions.md](references/dependency-conventions.md) — 依赖与可运行应用
- [external-project-layout.md](references/external-project-layout.md) — **外部产品**模块职责
- [module-layout.md](references/module-layout.md) — **本仓库**平台库模块职责
- [build-config.md](references/build-config.md) — POM 模板与排错
- [develop-deliverables.md](../java-develop/references/develop-deliverables.md) — 实现侧包结构
- [spring-dependencies.md](../java-spring/references/spring-dependencies.md) — Spring 模块
