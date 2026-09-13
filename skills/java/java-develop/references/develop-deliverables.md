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
│   ├── service/          # 仅多步编排或事务时需要
│   ├── operator/
│   ├── dao/
│   ├── converter/      # 非平凡 MapStruct
│   └── domain/
│       ├── entity/
│       ├── request/      # record
│       ├── vo/           # record
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

### 三层划分（先想清楚再建目录）

```text
Maven 模块（kernel / console / platform / adapter …）   ← 部署与依赖边界
  └── 业务领域（role / permission / catalog …）         ← 有界上下文
        └── 功能子模块（grant / authorization / sync …） ← 领域内独立曲面（按需）
              └── 职责包（endpoint / service / dao / domain/…） ← 技术职责
```

| 刀法 | 问什么 | 典型产物 |
|------|--------|---------|
| **第一刀：Maven 模块** | 能否独立测试？依赖方向是否干净？是否业务中立？ | 留在 `kernel` **或** 新建 `adapter` / `application` / 业务 extension |
| **第二刀：领域包** | 词汇是否独立？与相邻域是否常改同一批文件？ | `kernel.role.*`、`console.catalog.*` |
| **第三刀：功能子模块** | 领域内是否有多种工作流（鉴权、同步、授予、入口）？ | `permission.grant.*`、`permission.authorization.*` |

**小领域**（整个领域预计 < 15 个类型）：领域根下 `endpoint` + `dao` + `domain` 即可，
`service` 仅 1～3 个编排类。**不要**预建 `grant/`、`sync/` 等空子模块。

### 标准领域骨架（按任务扩展）

初始**只建当前任务需要的包**（与上文「产出位置」一致）。扩展规则：

| 包 | 何时添加 | 避免 |
|----|---------|------|
| `operator` | 单表读写、无跨表事务 | 用 operator 做服务编排 |
| `service` | 多步工作流、跨 operator、**需事务** | 一个类只做 endpoint→dao 转发 |
| `converter` | MapStruct、非平凡映射重复 ≥2 次 | 每个字段手写 copy |
| `domain/event` + `handler` | design 已定义事件与订阅 | 投机性空 `event` 包 |
| `<subcapability>/` | 子能力已有 ≥5 类且职责可命名 | 全堆进扁平 `service/` |

### 功能子模块示例（中/大领域）

对齐 `console.permission` 模式（见 package-structure.md）：

```text
com.innospots.nexus.console.permission
  ├── authorization/          # 请求鉴权（与 grant 分离）
  ├── entry/                # 插件入口
  ├── grant/                # 授权授予（继续长大时自带 service/operator/domain）
  │   ├── service/
  │   ├── operator/
  │   └── domain/...
  ├── endpoint/             # 领域级 HTTP；端点过多可 grant/endpoint
  ├── dao/
  └── domain/
```

`kernel` / `platform` 新领域应对齐同一模式：`com.innospots.nexus.kernel.<domain>.<responsibility>`。

### 单包 ≤15 类（硬上限）

同一包目录下**直接**的 `*.java` 数量（不含子目录）：

| 数量 | 要求 |
|------|------|
| ≤ 12 | 正常 |
| 13～15 | 可合并，但新增前应规划子包 |
| **> 15** | **禁止合并**；先拆子包再继续实现 |

适用：`endpoint`、`service`、`operator`、`dao`、`domain.request`、`domain.vo`、功能子模块根包。

自检（实现过程中随时跑）：

```bash
find innospots-nexus-<module>/src/main/java/<pkg/path> -maxdepth 1 -name '*.java' | wc -l
```

拆分顺序：**功能子模块** → 职责子包（`grant.service`）→ **禁止** `common`/`misc`/`util` 逃避。

### 禁止的结构（实现时常见误用）

| 禁止 | 应改为 |
|------|--------|
| `kernel.service.RoleService` + `kernel.service.MenuService` … | `kernel.role.service`、`kernel.menu.service` |
| `endpoint/role`、`dao/role`（技术层优先） | `role/endpoint`、`role/dao` |
| 一个 `permission.service` 里 20+ 编排类 | `permission.grant.service`、`permission.visibility.service` … |
| 为凑分层建空 `service`/`event` | 有任务再建 |
| 把 A 领域类型放进 B 领域包「方便调用」 | 下沉 `base`/`console` 契约或 service 编排 |
| 用事件让 `kernel` 与 `platform` 互引 | `application` 组装模块或下沉中立契约 |

### 何时停 implement，回 design / project

develop **不得**在代码里硬扩边界；出现下列信号应**暂停提交**，先修订设计或工程：

| 信号 | 动作 |
|------|------|
| 新能力说不清归 `kernel` 还是 `platform` | `java:design` + 必要时 `grill-me` |
| 需要 `kernel` 依赖 `platform`（或反向） | 契约下沉 `console`/`core`，或 `java:project` 新建 `application`/`adapter` |
| 单一领域包树预计 **> 40～50** 个生产类型且无功能子模块规划 | `java:design` 拆功能子模块或拆领域 |
| 整个 `innospots-nexus-kernel` 持续新增**无关**业务域 | 评估是否应独立 Maven 模块（插件、adapter、extension） |
| 业务逻辑塞进 `core`/`console` 只为少写一个模块 | 回到 module-ownership；业务归 kernel/platform |
| 插件能力 vs 管理台契约混淆 | plugin 管运行时与 DSL；console 管 REST/catalog 索引 |
| 任意包目录将超 15 类 | 先拆包再写新类 |

新建 **Maven** 模块的条件（须 `java:project`，且上游 `grill-me`）：

- 边界、依赖方向、**可独立测试**三者同时清晰；
- 不是「目录好看」或「复制遗留多模块」；
- 常见正当场景：`adapter`（外部系统）、`*-app`（可运行组装）、classpath 插件、与 kernel/platform 平行的业务 extension。

模块类型与交付清单见 `java:project` →
[project-deliverables.md](../../java-project/references/project-deliverables.md)。

**若 design 判定只需加领域包** → 不要新建 Maven 模块，在本模块内按上文包结构实施。

**不得**为每个技术层（dao 模块、service 模块）再建 Maven 子模块——在**同一** Maven 模块内用领域优先包解决。

### 实现阶段包结构自检（并入交付）

- [ ] 第一级业务包段是**领域名**，不是 `endpoint`/`dao`/`service`
- [ ] 模块根下**无**多领域共用的 `service/` 桶
- [ ] 中/大领域已按**功能子模块**拆分（非扁平 service 堆积）
- [ ] 每个包目录 `.java` **≤ 15**
- [ ] 未创建 `impl`/`common`/`misc`/`util` 逃避归属
- [ ] 共享类型在正确 **Maven 模块**（非邻近领域包塞入）
- [ ] `kernel` ↔ `platform` 无 Maven 互依

疑义回源 [package-structure.md](../../java-reference/references/package-structure.md) 全文。

---

## 按任务类型的最小交付集

| 任务 | 生产代码 | 测试 | 验证 |
|------|---------|------|------|
| **新领域** | 完整域包树（entity→dao→record→status→service/operator→endpoint→converter） | 实体/DAO/端点/状态码契约测试 + 关键行为单测 | `mvn -pl <m> -am clean compile test` → `java:check` |
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
| 3 | 领域类型 | Request/VO/枚举/状态码契约测试 | `domain/request`、`vo`、`enums` | 同上 |
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
| 端点 | `jakarta.ws.rs`，返回 `R<T>`；**禁止** Spring MVC 注解 |
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
- [ ] 包结构符合领域优先；单包 ≤15 类；无模块级 service 垃圾桶（见上文「模块与包结构」）

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
