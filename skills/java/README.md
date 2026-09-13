# Java 技能体系

面向 `innospots-nexus` 及同类 Java 工程的分工化技能集。

- 规范条文权威来源：`skills/java/java-reference/standards/` 与 `AGENTS.md`
- 规范消费与复用入口：**`java:reference`**（索引、专题参考、红线速查）
- 其他技能是**执行入口**，通过 `java:reference` 链接规范，不在 SKILL 正文重复条文

## 八个技能

| 技能 | 目录 | 定位 |
|------|------|------|
| `java:reference` | `java-reference/` | 规范总索引、专题参考库与技能路由（**其他技能复用规范的入口**） |
| `java:project` | `java-project/` | 工程创建、结构、构建与项目配置 |
| `java:design` | `java-design/` | 架构、模块、接口、类、技术方案与**测试范围**设计 |
| `java:develop` | `java-develop/` | 功能开发、修改、Bug 修复、重构与**单元/契约测试**（与实现同一交付） |
| `java:check` | `java-check/` | 编译、测试、规范、质量、依赖、安全、性能风险检查 |
| `java:spring` | `java-spring/` | Spring / Spring Boot 专项能力 |
| `java:project-upgrade` | `java-project-upgrade/` | **工程产物版本**（`${revision}`）统一升版，避免手改各模块 version |
| `java:dependency-upgrade` | `java-dependency-upgrade/` | **依赖、JDK、开发框架**升级及影响分析 |

每个技能目录结构：

```text
java-<name>/
├── README.md         给人读：是什么、怎么用、输入/输出、边界（本文件体系）
├── SKILL.md          AI 入口：定位、流程、红线、路由
└── references/       详细参考：按主题拆分的执行手册

java-reference/
├── README.md
├── SKILL.md          规范总索引（java:reference）
├── standards/        规范权威原文（7 份，随技能包安装）
└── references/       专题参考、模块 API 索引
```

- **`README.md`** — 开发者手册：模块定位、使用方式、输入输出、适用与不适用场景。
- **`SKILL.md`** — 代理执行入口，保持精简；细节放 `references/`，避免上下文膨胀。

### 各技能 README 入口

| 技能 | 开发者手册 |
|------|-----------|
| `java:reference` | [java-reference/README.md](java-reference/README.md) |
| `java:project` | [java-project/README.md](java-project/README.md) |
| `java:design` | [java-design/README.md](java-design/README.md) |
| `java:develop` | [java-develop/README.md](java-develop/README.md) |
| `java:check` | [java-check/README.md](java-check/README.md) |
| `java:spring` | [java-spring/README.md](java-spring/README.md) |
| `java:project-upgrade` | [java-project-upgrade/README.md](java-project-upgrade/README.md) |
| `java:dependency-upgrade` | [java-dependency-upgrade/README.md](java-dependency-upgrade/README.md) |

## 路由原则

### 按动作类型

| 场景 | 技能 |
|------|------|
| 方案/边界压力测试、多路径决策消歧（动手前） | `grill-me`（见 `java-reference` → `grill-me.md`） |
| 查规范、问约定、不确定走哪个 | `java:reference` |
| 动工程骨架（模块、POM、构建） | `java:project` |
| 动设计决策（边界、契约、选型） | `java:design` |
| 动实现代码与单元/契约测试（功能、修 Bug、重构） | `java:develop` |
| 动验证动作（编译、检查、评审） | `java:check` |
| 涉及 Spring 生态 | `java:spring` |

### 按升级类型

```text
升工程 revision / 发版（0.1 → 0.2）     → java:project-upgrade
升 JDK / 依赖 / 开发框架               → java:dependency-upgrade
```

| 示例 | 归属 |
|------|------|
| `revision` `0.1.0-SNAPSHOT` → `0.2.0-SNAPSHOT` | `java:project-upgrade` |
| SNAPSHOT 转正发布 `1.0.0`、`versions:set` | `java:project-upgrade` |
| JDK 17 → 25 | `java:dependency-upgrade` |
| Spring Boot 2 → 3 / 3 → 4 | `java:dependency-upgrade` |
| Java EE → Jakarta EE | `java:dependency-upgrade` |
| Maven 插件、JUnit、BOM 第三方库 | `java:dependency-upgrade` |
| Jackson、MapStruct、Lombok、commons-*、内部 SDK | `java:dependency-upgrade` |

两者可连续执行，但**分提交、分验证**；不要与功能开发混在同一变更里。

## 标准开发流程（功能交付）

功能从立项到合入的**完整链路**如下。`java:reference` 可在任意阶段查阅规范，但不改变主顺序。

```text
① grill-me          新建模块 / 新设计前必经
        ↓
② java:project      需要新建 Maven 模块时（按需）
        ↓
③ java:design       新增模块 / 新领域 / 契约变更
        ↓
④ java:develop      实现与测试
        ↓
⑤ java:check        验证出口
```

### grill-me 安装（未安装时）

`java:project` 与 `java:design` **开始前**必须先能调用 `grill-me`。未安装时 Agent 须先执行：

```bash
npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"
```

然后**按生成技能的完整说明执行**（输出过长则先重定向到临时文件再读全文）；若提供
**supporting-files** 目录，相对路径从该目录解析。会话结束且开发者确认后，方可进入
`java:project` 或 `java:design`。详见 [grill-me.md](java-reference/references/grill-me.md)。

### 各步判据

| 步骤 | 是否必经 | 何时执行 |
|------|---------|---------|
| **grill-me** | **新建 Maven 模块或启动新设计时必经** | 在 `java:project` / `java:design` 动手前；未安装则先安装 |
| **java:project** | 按需 | 需要**新建 Maven 模块**、调整 reactor、登记 BOM、建包根与 POM 时，在 design 之前 |
| **java:design** | **新增模块 / 新业务领域 / 重大契约变更时必经** | grill-me 之后；产出设计文档（或 L0 结论块）与测试范围 |
| **java:develop** | 必经 | 测试先行 → 实现；每批 `mvn clean compile`；交付前 `mvn test` |
| **java:check** | 必经 | L0–L5 全量验证，统一出口 |

**简记：**

- **新建模块** → `grill-me` → `java:project` → `java:design` → `java:develop` → `java:check`。
- **新领域（无新模块）** → `grill-me` → `java:design` → `java:develop` → `java:check`。
- **已有域小改动、契约清晰** → 可跳过 grill-me 与 project；仍 `develop` → `check`（契约不清时补 design L0）。

涉及 Spring 运行时组装时，在 project / design 阶段对照 `java:spring`；不插入 develop 与 check 之间。

### 新建业务领域（示例）

```text
grill-me          必经；未安装则 npx skills use … --skill grill-me
    ↓
java:project      需要新 Maven 模块时：注册模块、parent、BOM、包根
    ↓
java:reference    查规范红线（贯穿）
    ↓
java:design       四步法 + 设计文档/测试范围
    ↓
java:develop      契约测试红灯 → 实现 → 单测转绿；mvn clean compile；mvn test
    ↓
java:check        L0–L5 全量验证
```

### 修 Bug / 改功能（已有域）

```text
java:design       仅当失败语义/契约不清时（可用 L0 结论块）；否则可跳过
    ↓
java:develop      先写复现测试 → 修改 → mvn test
    ↓
java:check        聚焦测试 + 全量验证
```

### 工程版本号升级（revision）

```text
java:project-upgrade  根 POM 改 revision / versions:set → validate → install
    ↓
java:check            effective-pom 与各模块版本一致
```

### 依赖升级

```text
java:check            建立升级前基线（dependency:tree）
    ↓
（可选）grill-me      JDK / Spring 等大版本方案
    ↓
java:dependency-upgrade     BOM/parent 改版本 → 扫描影响 → 分阶段改代码 → 测试
    ↓
java:check            与升级前基线对比
```

## 共通硬约束

1. **每批 Java 源文件改动后立即 `mvn clean compile`**，不得延后、不得降低
   `maven.compiler.release` 迁就旧 JDK（应报告环境不匹配）
2. 测试先行：由 `java:develop` 与实现同步完成；先写测试确认红灯，再实现；功能交付前 `mvn test` 通过；**不得**为让实现通过而削弱断言
3. 不得复制遗留工程源码、POM 结构或机械复刻包结构
4. 未获开发者显式请求时，**不得**创建、更新或同步模块 API 索引 `README.md` 与
   `references/` 文档（见 `java-reference/standards/module-skills.md`）。
   `references/modules/` 下是 API 索引，**不是**可安装的 `java:*` 技能。

## 规范与专题参考

所有规范通过 `java:reference` 消费：

| 类型 | 位置 |
|------|------|
| 权威原文 | `skills/java/java-reference/standards/*.md`、`AGENTS.md` |
| 章节索引 | `java-reference/references/standards-index.md` |
| 硬性红线 | `java-reference/references/quick-constraints.md` |
| 模块归属 | `java-reference/references/module-ownership.md` |
| 包结构（领域优先） | `java-reference/references/package-structure.md` |
| 作用域层级 | `java-reference/references/scope-hierarchy.md` |
| 领域建模 | `java-reference/references/domain-modeling.md` |
| API 契约 | `java-reference/references/api-contract.md` |
| 六阶段 checklist | `java-reference/references/domain-initialization-checklist.md` |
| 单元测试规约 | `java-develop/references/test-conventions.md` |
| 契约测试写法 | `java-develop/references/contract-tests.md` |
| 测试范围（设计） | `java-design/references/test-scope.md` |
| 设计产出物（格式/目录） | `java-design/references/design-deliverables.md` |
| 实现交付物（格式/目录） | `java-develop/references/develop-deliverables.md` |
| 工程交付物（POM/模块） | `java-project/references/project-deliverables.md` |
| 设计四步法门禁 | `java-design/references/design-four-steps.md` |
| 设计场景与 L0–L3 | `java-design/references/design-scenarios.md` |
| 持久化与配置（reference） | `java-reference/references/persistence-config.md` |
| 持久化契约（设计） | `java-design/references/persistence-contract.md` |
| 领域事件（设计） | `java-design/references/event-contract.md` |
| MyBatis-Plus 实现 | `java-develop/references/persistence-mybatis.md` |
| 测试规范路由 | `java-reference/references/testing-index.md` |
| 方案压力测试 | `java-reference/references/grill-me.md` |
| 模块 API 索引 | `java-reference/references/modules/<artifact-id>/README.md` |

已收录模块：`innospots-nexus-base`、`innospots-nexus-core`、`innospots-nexus-console`。

疑义一律回源 `skills/java/java-reference/standards/` 原文，不以技能摘要为准。

## grill-me（跨技能）

`grill-me` 用于重大方案与决策的压力测试，**不产出代码**。在标准开发流程中位于**最前段**：
**`java:project`（新建模块）与 `java:design`（新设计）开始前必经**。未安装时先执行
`npx skills use "https://github.com/mattpocock/skills" --skill "grill-me"` 并按技能全文操作。
调用时机与各技能衔接见 `java-reference/references/grill-me.md`。

简记：

- **必经**：新建 Maven 模块、新业务域设计、新增模块对应的设计工作。
- **按需**：升级方案定稿前、design 交 develop 前复审。
- **不调用**：develop 执行中、check 验证中、已有域 trivial 改动且契约已定。
