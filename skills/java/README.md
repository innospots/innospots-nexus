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
├── SKILL.md          入口：定位、流程、红线、路由
└── references/       详细参考：按主题拆分的执行手册

java-reference/
├── SKILL.md          规范总索引（java:reference）
├── standards/        规范权威原文（7 份，随技能包安装）
└── references/       专题参考、模块 API 索引
```

`SKILL.md` 保持精简（入口与红线），细节放 `references/`，避免上下文膨胀。

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

## 典型工作流

### 新建业务领域

```text
（可选）grill-me  新域/跨模块时压力测试边界与假设
    ↓
java:reference  查规范红线
    ↓
java:design     定归属、建词汇、划边界、定契约
    ↓
（可选）grill-me  四步法结论交 develop 前评审
    ↓
java:develop    测试先行：契约测试红灯 → 实现 → 单测转绿；每批 mvn clean compile；交付前 mvn test
    ↓
java:check      L0–L5 全量验证
```

### 修 Bug / 改功能

```text
java:develop    先写复现测试 → 在拥有失败语义的边界修改 → mvn test
    ↓
java:check      聚焦测试 + 全量验证
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
| 方案压力测试 | `java-reference/references/grill-me.md` |
| 模块 API 索引 | `java-reference/references/modules/<artifact-id>/README.md` |

已收录模块：`innospots-nexus-base`、`innospots-nexus-core`、`innospots-nexus-console`。

疑义一律回源 `skills/java/java-reference/standards/` 原文，不以技能摘要为准。

## grill-me（跨技能）

`grill-me` 用于重大方案与决策的压力测试，**不产出代码**。调用时机与各技能衔接见
`java-reference/references/grill-me.md`。简记：**设计前、结构变更前、升级方案定稿前**
建议调用；**develop 执行中、check 验证中**默认不调用。
