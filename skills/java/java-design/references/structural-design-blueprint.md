# 结构化设计蓝图（实体 · 枚举 · 状态码 · 异常 · 接口 · DDD · 模块 · 包）

`java:design` 在输出技术方案时，除四步法总流程外，须覆盖本文件所列 **八个设计面**。
与 [design-four-steps.md](design-four-steps.md)、[design-deliverables.md](design-deliverables.md) 配合使用；
规范条文仍回源 `java:reference` / `standards/`。

```text
① 工程模块划分（Maven）     →  module-ownership、module-layout、java:project
② 工程包结构 + DDD 边界     →  package-structure、scope-hierarchy、sample-extension-layout（若适用）
③ 实体（Entity）            →  domain-modeling、persistence-contract
④ 枚举（Enums）             →  domain-modeling、naming
⑤ 状态码（StatusCode）      →  exception-contract、exception-status-code.md
⑥ 异常（NexusException）    →  exception-contract
⑦ 接口（HTTP / Java API）   →  api-contract、console interface 例外
⑧ 分层与调用方向            →  api-contract、package-structure
```

L1/L2 设计文档须用 [design-deliverables.md](design-deliverables.md) 模板落盘；L0 在 PR 块中至少覆盖下表 **「最小信息面」**。

---

## 1. 工程模块划分（Maven）

**问题：** 能力放在哪个 `innospots-nexus-*`（或外部产品模块）？要不要新建 Maven 模块？

| 设计产出 | 内容 |
|----------|------|
| 归属表 | 每个有界上下文 → **一个 owning Maven 模块** |
| 依赖箭头 | 只画单向依赖；**portal ↔ platform 禁止** |
| 新建模块判定 | 默认 **只加 Java 包**；新建模块须 grill-me + `java:project` |
| sample / 外部产品 | sample 全量 reactor 仅作参考，**按需建子集**（见 sample-extension-layout §1.1、§2.2） |

**读：** [module-ownership.md](../../java-reference/references/module-ownership.md)、
[module-layout.md](../../java-project/references/module-layout.md)、
[project-deliverables.md](../../java-project/references/project-deliverables.md)。

### 设计门禁

- [ ] 每个领域只有一个 **持久化与编排真源** 模块（不得在 console 与 portal 各建一套 entity）
- [ ] console 仅 **契约/VO/catalog**；完整业务工作流在 portal/platform（或扩展库 core）
- [ ] 跨 portal+platform 同进程 → **application 组装模块**，非库模块互依

---

## 2. 工程包结构 + DDD 边界

**问题：** Java 包怎么长？有界上下文之间如何协作？

### 2.1 平台库（portal / platform / console 业务域）

**第一刀：领域名** → **第二刀：功能子模块（按需）** → **第三刀：职责包**。

```text
com.innospots.nexus.<module>.<domain>
  ├── <subcapability>/          # authorization、grant、entry …
  ├── endpoint | dao | domain/… | operator | service
```

禁止：`endpoint.<domain>`、`dao.<domain>`、模块根 `service` 多领域桶。

### 2.2 扩展库（sample-platform 等：交付面 × 领域）

**第一刀：交付面 `core` | `console` | `inbound`** → **第二刀：领域** → **第三刀：职责**。

- `core`：entity、dao、operator、service、loader（无 JAX-RS）
- `console` / `inbound`：endpoint interface + 薄 service
- **禁止** inbound 依赖 console 包

### 2.3 DDD 边界表（设计文档必填）

| 有界上下文 | Owning 模块 | 包根 | 对外暴露 | 禁止 |
|------------|-------------|------|----------|------|
| 示例 `announcement` | `sample-platform` | `…core.announcement` | inbound 只读 API；console 管理 API | 在 platform 内置域重复表/端点 |

协作方式只选：**同模块 service 编排** / **领域事件（契约在低层）** / **application 组装**；禁止跨域合并包树。

**读：** [package-structure.md](../../java-reference/references/package-structure.md)、
[sample-extension-layout.md](../../java-reference/references/sample-extension-layout.md) §3。

### 设计门禁

- [ ] 领域优先；单包 ≤15 个 `.java`
- [ ] `endpoint → service → operator → dao`；无 endpoint→dao、operator→service
- [ ] 跨表读：分批单表 + 内存组装（无 join 设计）

---

## 3. 实体（Entity）设计

**问题：** 表与 `*Entity` 长什么样？作用域哪一级？

### 3.1 设计产出

| 产出 | 说明 |
|------|------|
| 实体清单表 | 实体名、表名、`TABLE_NAME` 常量、主键字段、基类 |
| 字段表 | 列名、类型、长度、可空、可变、索引、业务含义 |
| 关系 | 仅逻辑关联（外键列）；**禁止** join 查询设计 |
| 生命周期 | 创建/更新/删除规则；谁调用 operator |

### 3.2 基类选择（与 Session 分离判定）

| 基类 | 何时用 |
|------|--------|
| `WorkspaceBaseEntity` | **默认** 租户 + 工作区作用域 |
| `TenantBaseEntity` | 租户级、无工作区 |
| `TenantProjectBaseEntity` / `ProjectBaseEntity` | 设计评审批准后的项目隔离 |
| `BaseEntity` | 平台级、realm 全局 |

Session/Snapshot 规则见 [scope-hierarchy.md](../../java-reference/references/scope-hierarchy.md)。

### 3.3 骨架示例（设计文档代码块，非实现）

```java
// 设计阶段：字段 + 表名 + 基类意图即可
@TableName(XxxEntity.TABLE_NAME)
public class XxxEntity extends WorkspaceBaseEntity {
    public static final String TABLE_NAME = "nx_xxx";
    // 技术主键 String xxxId; ASSIGN_UUID
    // 稳定业务键 xxxCode — 创建后不可变
}
```

**读：** [domain-modeling.md](../../java-reference/references/domain-modeling.md)、
[persistence-contract.md](persistence-contract.md)。

### 设计门禁

- [ ] 一表一 `*Entity`、一 `*Dao`
- [ ] 技术主键 vs 稳定业务键已区分
- [ ] 无「仅因遗留而有」的字段
- [ ] 表名/列名与词汇表一致

---

## 4. 枚举（Enums）设计

**问题：** 哪些封闭集合用 Java `enum`？与状态机、状态码如何区分？

| 类型 | 包 | 命名 | 用途 |
|------|-----|------|------|
| 领域状态 | `domain.enums` | `XxxStatus`、`XxxState` | 实体字段、业务生命周期 |
| 领域分类 | `domain.enums` | `XxxType`、`XxxMode` | 互斥分类；语义在词汇表定义 |
| 应用失败 | `domain.enums.*StatusCode` 或 `NexusStatusCode` | 九字符 `StatusCode` | **不是** 普通业务 enum 混用 |

**设计产出：**

- 枚举常量列表 + 含义 + 允许迁移（如 `DRAFT → PUBLISHED`）
- 与 DB 存储（字符串列长度，通常 ≤16）
- **禁止** 用枚举代替状态码表达「失败原因」

### 设计门禁

- [ ] `state` / `status` / `mode` / `type` 在设计词汇表中单义
- [ ] 枚举归属 owning 领域的 `domain.enums`

---

## 5. 状态码（StatusCode）设计

**问题：** 每个可观察失败对应哪个稳定九字符码？复用还是新增？

| 步骤 | 动作 |
|------|------|
| 1 | 先搜 `NexusStatusCode` 与领域 `*StatusCode` 目录 |
| 2 | 平台通用语义 → `NexusStatusCode` |
| 3 | 领域专属语义 → `domain.enums.XxxStatusCode`（`MODULE(3)+CATEGORY(2)+LOCAL(4)`） |
| 4 | 在设计文档建 **失败矩阵表**（场景 → 码 → 抛出层 → HTTP 意图） |

**读：** [exception-contract.md](exception-contract.md)、
[exception-status-code.md](../../java-reference/standards/exception-status-code.md)。

### 设计门禁

- [ ] 每个应用可见失败有且仅有一个主状态码
- [ ] module 三字与归属域一致
- [ ] 无「临时 RuntimeException，以后再补码」

---

## 6. 异常（NexusException）设计

**问题：** 在哪一层抛出？消息与 display 谁负责？

| 规则 | 设计写法 |
|------|----------|
| 唯一业务异常类型 | `NexusException` + `StatusCode` |
| 抛出边界 | operator（单记录校验）、service（工作流）、适配器（翻译基础设施） |
| endpoint | 不 catch 换类型；由运行时映射 `R.fail` |
| 推迟实现 | 设计注明将用的 `StatusCode`，禁止 `UnsupportedOperationException` 作契约 |

**读：** [exception-contract.md](exception-contract.md)。

### 设计门禁

- [ ] 方法/端点契约侧写明失败语义（Javadoc 或设计表）
- [ ] 禁止 JDK 通用异常作为对外失败

---

## 7. 接口设计（HTTP 与 Java）

### 7.1 Jakarta REST（端点）

| 产出 | 说明 |
|------|------|
| 端点表 | HTTP 方法、完整 `@Path`、request record、响应 `R<T>`、委托 service |
| 类形态 | 默认 **具体类**；**console 传输契约** 可为 `interface` + portal/platform 实现 |
| 数量 | 单 interface 约 **>7 方法** 须复审拆分 |

路径、分页、兼容性见 [api-contract.md](../../java-reference/references/api-contract.md)。

### 7.2 Java 接口（非 HTTP）

| 允许 | 禁止 |
|------|------|
| 真实模块边界、插件 SPI、测试替身 | 仅为 mock 的「先抽 interface」 |
| 端口在 owning 模块 `api` 包 | 每个 service 都配 `XxxService` + `XxxServiceImpl` |

### 7.3 Request / VO（契约骨架）

- 一律 **record**；按修改权拆分 `Create` / `Update` / `Page` …
- service/operator **不** 返回 `R<T>`

**读：** [domain-modeling.md](../../java-reference/references/domain-modeling.md)（request/vo 表）。

### 设计门禁

- [ ] 端点表与分层委托一致
- [ ] 新增 interface 在「不建什么」中论证，或列入必建清单

---

## 8. 四步法映射（速查）

| 四步法 | 本蓝图章节 |
|--------|------------|
| ① 定归属 | §1 工程模块 |
| ② 建词汇 | §4 枚举语义 + §5 状态码 module 前缀 + 实体/表命名 |
| ③ 划边界 | §2 包结构 + DDD 表 + §3 实体基类 |
| ④ 定契约 | §3 字段表 + §5–§7 失败矩阵 + 端点表 + Dao 表（persistence-contract） |

---

## 9. L0 最小信息面（PR 也必须覆盖）

即使不写 L2 文档，设计结论块须包含：

- **模块 + 包路径**（§1、§2）
- **实体/表** 有无新增；基类（§3）
- **枚举** 有无新增（§4）
- **StatusCode** 新增或复用列表（§5）
- **异常边界** 一句话（§6）
- **端点/接口** diff（§7）
- **测试范围**（[test-scope.md](test-scope.md)）

---

## 10. 相关文档

- [design-four-steps.md](design-four-steps.md) — 分步门禁
- [design-deliverables.md](design-deliverables.md) — L2 章节模板（已对齐本蓝图）
- [test-scope.md](test-scope.md) — 契约测试清单
