# Java 技能体系

面向 `innospots-nexus` 及同类 Java 工程的分工化技能集。规范权威来源为仓库
`standards/` 目录与 `AGENTS.md`，本目录下的技能是它们的**执行入口**。

## 九个技能

| 技能 | 目录 | 定位 |
|------|------|------|
| `java:reference` | `java-reference/` | Java 通用规范与标准（总索引与路由） |
| `java:project` | `java-project/` | 工程创建、结构、构建与项目配置 |
| `java:design` | `java-design/` | 架构、模块、接口、类与技术方案设计 |
| `java:develop` | `java-develop/` | 功能开发、修改、Bug 修复、重构 |
| `java:test` | `java-test/` | 单元测试、集成测试、回归测试 |
| `java:check` | `java-check/` | 编译、测试、规范、质量、依赖、安全、性能风险检查 |
| `java:spring` | `java-spring/` | Spring / Spring Boot 专项能力 |
| `java:project-upgrade` | `java-project-upgrade/` | Java 工程整体版本与架构升级 |
| `java-tool-upgrade` | `java-tool-upgrade/` | 基础组件、基础工具、公共依赖升级 |

每个技能目录结构：

```text
java-<name>/
├── SKILL.md          入口：定位、流程、红线、路由
└── references/       详细参考：按主题拆分的执行手册
```

`SKILL.md` 保持精简（入口与红线），细节放 `references/`，避免上下文膨胀。

## 路由原则

### 按动作类型

| 场景 | 技能 |
|------|------|
| 查规范、问约定、不确定走哪个 | `java:reference` |
| 动工程骨架（模块、POM、构建） | `java:project` |
| 动设计决策（边界、契约、选型） | `java:design` |
| 动实现代码（功能、修 Bug、重构） | `java:develop` |
| 动测试代码 | `java:test` |
| 动验证动作（编译、检查、评审） | `java:check` |
| 涉及 Spring 生态 | `java:spring` |

### 按升级类型

```text
改变项目运行/构建基线  → java:project-upgrade
改变项目使用的基础组件 → java:tool-upgrade
```

| 示例 | 归属 |
|------|------|
| JDK 17 → 25 | `java:project-upgrade` |
| Spring Boot 2 → 3 / 3 → 4 | `java:project-upgrade` |
| Java EE → Jakarta EE | `java:project-upgrade` |
| Maven 3 → 4、Gradle 大版本 | `java:project-upgrade` |
| JUnit 4 → JUnit 5 | `java:project-upgrade` |
| 构建体系升级、模块结构调整、语言特性迁移 | `java:project-upgrade` |
| Jackson 2 → 3 | `java:tool-upgrade` |
| MapStruct、Lombok、SLF4J/Logback | `java:tool-upgrade` |
| commons-*、Guava、HTTP Client、JSON 工具 | `java:tool-upgrade` |
| 内部 common SDK、内部 starter、内部基础框架 | `java:tool-upgrade` |

一次升级同时涉及两者时，主流程由 `java:project-upgrade` 负责，
过程中调用 `java:tool-upgrade` 处理具体基础组件迁移。

## 典型工作流

### 新建业务领域

```text
java:reference  查规范红线
    ↓
java:design     定归属、建词汇、划边界、定契约
    ↓
java:test       写契约测试（先确认红灯）
    ↓
java:develop    六阶段领域初始化，每批 mvn clean compile
    ↓
java:check      L0–L5 全量验证
```

### 修 Bug / 改功能

```text
java:test       先写复现测试
    ↓
java:develop    在拥有失败语义的边界修改
    ↓
java:check      聚焦测试 + 全量验证
```

### 工程基线升级

```text
java:check      建立升级前基线
    ↓
java:project-upgrade  兼容性分析 → 方案 → 分阶段实施
    ↓  （涉及具体组件时）
java:tool-upgrade     该组件的识别 → 差异 → 扫描 → 替换 → 验证
    ↓
java:check      与升级前基线对比
```

## 共通硬约束

1. **每批 Java 源文件改动后立即 `mvn clean compile`**，不得延后、不得降低
   `maven.compiler.release` 迁就旧 JDK（应报告环境不匹配）
2. 测试先行：先写测试确认红灯，再实现；**不得**为让实现通过而削弱断言
3. 不得复制遗留工程源码、POM 结构或机械复刻包结构
4. 未获开发者显式请求时，**不得**创建、更新或同步模块 `SKILL.md` 与 `references/` 文档
   （见 `standards/module-skills.md`）

## 规范源文件

| 文件 | 管辖 |
|------|------|
| `standards/code-style.md` | 格式、Lombok、REST 端点、MapStruct、DAO、日志 |
| `standards/naming.md` | 命名决策顺序、类型后缀、方法动词、包与持久化命名 |
| `standards/api-design.md` | 签名、不可变性、空值、校验、异常、实体、契约、事务、事件、兼容性 |
| `standards/code-comments.md` | 包/类型/方法/行内注释与 TODO 规则 |
| `standards/exception-status-code.md` | 异常分类、`NexusException`、状态码九字符格式与扩展流程 |
| `standards/domain-module-initialization.md` | 六阶段领域初始化与门禁 |
| `standards/module-skills.md` | 模块技能文档的生成策略与格式 |
| `AGENTS.md` | 模块职责、依赖方向、遗留代码禁令 |

疑义一律回源这些文件，不以本目录的摘要为准。
