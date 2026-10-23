# 实现交付物规范

`java:develop` 的产出是**可编译源码与测试**，不是设计文档。设计输入来自 `java:design`
（见 [design-deliverables.md](../../java-design/references/design-deliverables.md)）。
全量评审报告由 `java:check` 产出。

---

## 上游前置（阶段零）

在写任何 `src/main/java` 文件之前：

| 检查 | 要求 |
|------|------|
| `java:design` 四步法 | 归属、词汇、边界、契约已完成 |
| 测试范围 | [test-scope.md](../../java-design/references/test-scope.md) 已定义测什么/不测什么 |
| 设计产物可定位 | L0：PR/Issue「设计结论」块；L1/L2：`<module>/docs/*-design.md` 或 `docs/design/` |
| 新 Maven 模块 | `java:project` 已完成（且上游 `grill-me` 已确认，见 [grill-me.md](../../java-reference/references/grill-me.md)） |
| 新域/新模块设计 | 上游 `grill-me` 已在 `java:design` 前完成 |

**找不到设计产物或测试范围** → 停止实现，回 `java:design`；不得凭口头约定开工。

### 从设计文档读取什么

| 设计章节 | develop 用法 |
|---------|---------------|
| 归属与词汇 | `com.innospots.nexus.<module>.<domain>.*` 包根 |
| API 与分层 | 端点表 → `endpoint`；record 骨架 → `domain.request` / `domain.vo` |
| 失败与状态码 | `domain.enums.*StatusCode` + 契约测试清单 |
| 测试范围 | 契约测试类名与行为单测边界 |
| 持久化意图 | `domain.entity` + `dao` |

---

## 产出位置（一律在 Maven 模块内）

```text
innospots-nexus-<module>/
├── src/main/java/com/innospots/nexus/<module>/<domain>/
│   ├── endpoint/
│   ├── service/
│   ├── operator/
│   ├── dao/
│   ├── converter/      # 非平凡 MapStruct
│   ├── filter/         # JAX-RS/请求链过滤（按需）
│   ├── loader/         # 配置、资源或快照加载（按需）
│   └── domain/
│       ├── entity/
│       ├── vo/           # record
│       ├── model/        # 不可变时优先 record
│       ├── request/      # record
│       ├── enums/
│       └── event/        # 领域事件 record（按需）
└── src/test/java/        # 镜像生产包路径
    ├── …/*ContractsTest.java
    ├── …/*Test.java
    └── …/*IT.java        # 集成测试（按需，见 integration-tests.md）
```

**禁止产出**：`skills/.../modules/` API 索引、设计文档（除非开发者显式要求同步）、
`standards/` 条文修改、模块 POM 新建（属 `java:project`）。

---

## 模块与包结构（避免膨胀）

实现阶段**不得**用「一个大 `service` 包」或「一个 Maven 模块装全部业务」消化边界。
权威条文：[package-structure.md](../../java-reference/references/package-structure.md)、
[module-ownership.md](../../java-reference/references/module-ownership.md)、
[module-layout.md](../../java-project/references/module-layout.md)。

### 四层划分（先想清楚再建目录）

```text
Maven 模块（portal / console / platform / adapter …）   ← 部署与依赖边界
  └── 业务领域（role / permission / catalog …）         ← 有界上下文
        └── 技术模块（endpoint / dao / service / operator / domain …） ← 技术职责
              └── 功能子包（grant / authorization / sync …，按需）
```

| 刀法 | 问什么 | 典型产物 |
|------|--------|---------|
| **第一刀：Maven 模块** | 能否独立测试？依赖方向是否干净？是否业务中立？ | 留在 `portal` **或** 新建 `adapter` / `application` / 业务 extension |
| **第二刀：领域包** | 词汇是否独立？与相邻域是否常改同一批文件？ | `portal.role.*`、`console.catalog.*` |
| **第三刀：技术模块** | 代码承担哪类职责？ | `permission.endpoint`、`permission.service`、`permission.domain.entity` |
| **第四刀：功能子包** | 某个技术模块是否类型较多且功能区分明显？ | `permission.service.grant`、`permission.endpoint.authorization` |

**小领域或功能区分度不大**：保持领域根下 `endpoint`、`dao`、`service`、`operator`、
`domain` 扁平划分。只有出现真实类型时才创建目录，**不要**预建空包或功能子包。
技术模块类型较多或存在清晰、可独立命名的功能曲面时，才在对应技术模块下按功能拆分。

### 标准领域骨架（按任务扩展）

包归属固定，目录在出现首个真实类型时创建（与上文「产出位置」一致）。扩展规则：

| 包 | 何时添加 | 避免 |
|----|---------|------|
| `operator` | 单表读写、无跨表事务 | 用 operator 做服务编排 |
| `service` | 多步工作流、跨 operator、**需事务** | 一个类只做 endpoint→dao 转发 |
| `converter` | MapStruct、非平凡映射重复 ≥2 次 | 每个字段手写 copy |
| `domain/event` + `handler` | design 已定义事件与订阅 | 投机性空 `event` 包 |
| `<technology>/<subcapability>/` | 技术模块内类型较多且功能边界清晰 | 为少量类强行分层，或继续堆进扁平包 |

### 功能子包示例（中/大领域）

对齐 `console.permission` 模式（见 package-structure.md）：

```text
com.innospots.nexus.console.permission
  ├── endpoint/
  │   ├── grant/             # 端点较多且功能区分清晰时
  │   └── visibility/
  ├── service/
  │   ├── grant/
  │   └── visibility/
  ├── operator/
  ├── dao/
  └── domain/
```

`portal` / `platform` 新领域应对齐同一模式。功能区分度不大时不要拆子包，保持技术模块扁平。

### 单包 ≤15 类（硬上限）

同一包目录下**直接**的 `*.java` 数量（不含子目录）：

| 数量 | 要求 |
|------|------|
| ≤ 12 | 正常 |
| 13～15 | 可合并，但新增前应规划子包 |
| **> 15** | **禁止合并**；先拆子包再继续实现 |

适用：`endpoint`、`service`、`operator`、`dao`、`domain.request`、`domain.vo`、技术模块内的功能子包。

自检（实现过程中随时跑）：

```bash
find innospots-nexus-<module>/src/main/java/<pkg/path> -maxdepth 1 -name '*.java' | wc -l
```

拆分顺序：**领域** → **技术模块**（`service`）→ **功能子包**（`service/grant`）→
**禁止** `common`/`misc`/`util` 逃避。

### 禁止的结构（实现时常见误用）

| 禁止 | 应改为 |
|------|--------|
| `portal.service.RoleService` + `portal.service.MenuService` … | `portal.role.service`、`portal.menu.service` |
| `endpoint/role`、`dao/role`（技术层优先） | `role/endpoint`、`role/dao` |
| 一个 `permission.service` 里 20+ 编排类 | `permission.service.grant`、`permission.service.visibility` … |
| 为凑分层建空 `service`/`event` | 有任务再建 |
| 把 A 领域类型放进 B 领域包「方便调用」 | 下沉 `base`/`console` 契约或 service 编排 |
| 用事件让 `portal` 与 `platform` 互引 | `application` 组装模块或下沉中立契约 |

### 何时停 implement，回 design / project

develop **不得**在代码里硬扩边界；出现下列信号应**暂停提交**，先修订设计或工程：

| 信号 | 动作 |
|------|------|
| 新能力说不清归 `portal` 还是 `platform` | `java:design` + 必要时 `grill-me` |
| 需要 `portal` 依赖 `platform`（或反向） | 契约下沉 `console`/`core`，或 `java:project` 新建 `application`/`adapter` |
| 单一领域包树预计 **> 40～50** 个生产类型且无功能子包规划 | `java:design` 拆技术模块内功能子包或拆领域 |
| 整个 `innospots-nexus-portal` 持续新增**无关**业务域 | 评估是否应独立 Maven 模块（插件、adapter、extension） |
| 业务逻辑塞进 `core`/`console` 只为少写一个模块 | 回到 module-ownership；业务归 portal/platform |
| 插件能力 vs 管理台契约混淆 | plugin 管运行时与 DSL；console 管 REST/catalog 索引 |
| 任意包目录将超 15 类 | 先拆包再写新类 |

新建 **Maven** 模块的条件（须 `java:project`，且上游 `grill-me`）：

- 边界、依赖方向、**可独立测试**三者同时清晰；
- 不是「目录好看」或「复制遗留多模块」；
- 常见正当场景：`adapter`（外部系统）、`*-app`（可运行组装）、classpath 插件、与 portal/platform 平行的业务 extension。

模块类型与交付清单见 `java:project` →
[project-deliverables.md](../../java-project/references/project-deliverables.md)。

**若 design 判定只需加领域包** → 不要新建 Maven 模块，在本模块内按上文包结构实施。

**不得**为每个技术层（dao 模块、service 模块）再建 Maven 子模块——在**同一** Maven 模块内用领域优先包解决。

### 实现阶段包结构自检（并入交付）

- [ ] 第一级业务包段是**领域名**，不是 `endpoint`/`dao`/`service`
- [ ] 模块根下**无**多领域共用的 `service/` 桶
- [ ] 技术模块较大或功能边界清晰时已按**功能子包**拆分；小模块未过度分层
- [ ] 每个包目录 `.java` **≤ 15**
- [ ] 未创建 `impl`/`common`/`misc`/`util` 逃避归属
- [ ] 共享类型在正确 **Maven 模块**（非邻近领域包塞入）
- [ ] `portal` ↔ `platform` 无 Maven 互依

疑义回源 [package-structure.md](../../java-reference/references/package-structure.md) 全文。

---

## 按任务类型的最小交付集

| 任务 | 生产代码 | 测试 | 验证 |
|------|---------|------|------|
| **新领域** | 完整域包树（entity→dao→record/model→status→service/operator→endpoint→converter） | 实体/DAO/端点/状态码契约测试 + 关键行为单测 | `mvn -pl <m> -am clean compile test` → `java:check` |
| **加功能** | 受影响分层的最小增量 | 新行为单测 + 必要时契约测试增补 | 聚焦 `mvn -pl <m> test` → `java:check` |
| **修 Bug** | 归属边界上的最小修复 | **先**复现测试（红灯）→ 修代码转绿 | 同上 |
| **重构** | 结构变更、无新公共契约 | 现有测试全绿；缺覆盖先补 | 全量 `mvn test` → `java:check` |
| **纯注释** | Javadoc/行内注释 | 可不新增 | `mvn clean compile` |

---

## 新领域：测试先行统一顺序

权威阶段定义：[domain-module-initialization.md](../../java-reference/standards/domain-module-initialization.md)。
执行勾选：[domain-initialization-checklist.md](../../java-reference/references/domain-initialization-checklist.md)。
逐步对照：[domain-initialization.md](domain-initialization.md)。

| 步 | 动作 | 测试（先写） | 生产（后写） | 命令 |
|----|------|-------------|-------------|------|
| 1 | 实体契约 | `{Domain}EntityContractsTest` | `domain/entity/*Entity.java` | 写测 → 红灯 → 实现 → compile |
| 2 | DAO 绑定 | `{Domain}DaoContractsTest` | `dao/*Dao.java` | 同上 |
| 3 | 领域类型 | Request/VO/Model/枚举/状态码契约测试 | `domain/request`、`vo`、`model`、`enums` | 同上 |
| 4 | 转换器（非平凡时） | `{Domain}ConverterTest` | `converter/*Converter.java` | 见 [contract-tests.md](contract-tests.md) |
| 5 | 编排与入口 | 行为单测 + 端点契约测试 | `operator`/`service`、`endpoint` | 同上 |
| 6 | 事件（按需） | 事件形状 + handler 行为单测 | `domain/event`、`handler` | 见 [domain-events.md](domain-events.md) |
| 7 | 集成（按需） | `*IT.java` | — | 见 [integration-tests.md](integration-tests.md) |
| 8 | 交付 | — | — | `mvn -pl <m> -am test` → `java:check` |

每完成一批生产/测试文件：**立即** `mvn -pl <module> -am clean compile`。

---

## 持久化与配置（硬约束）

| 必须 | 禁止 |
|------|------|
| MyBatis-Plus：`BaseMapper` + `default` + `Wrappers.lambdaQuery()` / `lambdaUpdate()` | `mapper.xml`、MyBatis XML、`beans.xml` |
| 业务/应用配置：`src/main/resources/**/*.yaml` | 新建 `*.properties` |
| 运行时绑定：`@Configuration` + `@ConfigurationProperties` | XML 装配依赖定义 |
| 一表一 `*Dao`；过大 Dao 拆表或上提 operator | 巨型单 Dao、join、Dao 内事务 |

细则 → [persistence-mybatis.md](persistence-mybatis.md)；设计契约 →
`java:design` → [persistence-contract.md](../../java-design/references/persistence-contract.md)。

---

## public 类型交付标准

实现阶段须满足 [code-comments.md](../../java-reference/standards/code-comments.md)：

| 项 | 要求 |
|----|------|
| public 类/接口/record/枚举 | 类级 Javadoc + `@author` + `@date`（`yyyy/MM/dd`）+ 有则 `@see` |
| public/protected 方法 | 方法 Javadoc（`@param` / `@return` / `@throws`） |
| 端点 | `jakarta.ws.rs`，返回 `R<T>`；**禁止** Spring MVC 注解；须 `@Operation` / `@Schema` 等见 `java:reference` → [openapi-contract.md](../../java-reference/references/openapi-contract.md) |
| 事务 | `jakarta.transaction.Transactional`；**禁止** Spring 事务注解 |
| 业务失败 | `NexusException` + `StatusCode`（见 [exception-handling.md](exception-handling.md)） |

---

## 常用构建命令

```bash
mvn -pl <module> -am clean compile          # 每批改动后
mvn -pl <module> -am test                   # 模块交付前
mvn -pl <module> -am test -Dtest='Role*Test'
mvn -pl <module> -am verify                 # 含 failsafe（有 *IT 时）
```

本地 SNAPSHOT 过期或「找不到上游类」时，优先 `-am` 重建，不得削弱测试。

---

## 交 `java:check` 前交付清单

- [ ] 设计产物与实现一致（契约未漂移）
- [ ] 每批改动后已 `mvn clean compile`
- [ ] 新功能/缺陷：测试在实现前曾红灯
- [ ] `mvn -pl <module> -am test`（或全量 `mvn test`）通过
- [ ] 无削弱断言、无注释掉的失败测试
- [ ] 新增 `throw` 均为 `NexusException`
- [ ] public 类型 Javadoc 完整（`@author` / `@date` / `@see`）
- [ ] 未误改模块 API 索引与设计文档（除非本任务包含）
- [ ] 未使用 `@RestController` / Spring Data / Spring Security
- [ ] 无 `mapper.xml` / `beans.xml`；无新建 `*.properties`；Dao 符合 persistence-mybatis
- [ ] 包结构符合领域优先；`domain` 固定包含 `entity`/`vo`/`model`/`request`；
      不可变类型优先 record；单包 ≤15 类；无模块级 service 垃圾桶

全量 L0–L5 由 `java:check` 执行；本清单是 develop **自检最小集**。

---

## 专题索引

| 主题 | 文件 |
|------|------|
| 领域优先包结构（权威） | [package-structure.md](../../java-reference/references/package-structure.md) |
| Maven 模块归属 | [module-ownership.md](../../java-reference/references/module-ownership.md) |
| 变更四类流程 | [change-workflow.md](change-workflow.md) |
| 契约测试写法 | [contract-tests.md](contract-tests.md) |
| 领域事件 | [domain-events.md](domain-events.md) |
| 集成测试 IT | [integration-tests.md](integration-tests.md) |
| MyBatis-Plus / yaml 配置 | [persistence-mybatis.md](persistence-mybatis.md) |
| 代码骨架 | [code-templates.md](code-templates.md) |
| 测试规约 | [test-conventions.md](test-conventions.md) |
