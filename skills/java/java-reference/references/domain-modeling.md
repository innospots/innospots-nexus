# 领域建模设计决策

回答「这块业务数据应该长什么样」。代码模板见 `java:develop` → `code-templates.md`。

作用域层级见 [scope-hierarchy.md](scope-hierarchy.md)。包结构见 [package-structure.md](package-structure.md)
（**领域优先** + **功能子模块** + 单包 ≤15 类；禁止 `dao/role` 与 service 堆积）。本文件聚焦建模取舍。

---

## 实体的持久化范围选择

| 范围 | 基类 | 判定 |
|------|------|------|
| workspace（租户 + 工作区） | `WorkspaceBaseEntity` | **默认选择** |
| tenant（仅租户，不含工作区） | `TenantBaseEntity` | 租户级但不属于某个工作区 |
| 平台级 / realm 全局 | `BaseEntity` | 平台范围或全局（用户、凭据、服务注册） |
| project（工作区内项目隔离） | `ProjectBaseEntity` | **仅设计评审批准后**；工作区内需项目级数据隔离 |

`projectId` 仅出现在继承 `ProjectBaseEntity` 的实体上；不得在无项目语义的表上随意加列。
项目定义实体（如 `ProjectEntity`）通常仍用 `WorkspaceBaseEntity`。

范围选择错误的典型代价：唯一索引漏掉租户/工作区维度导致跨租户串数据；
或把全局数据套上工作区隔离导致系统级查询无法进行。

### 字段评审清单

每个拟加入的字段都要能回答：业务含义 / 必填选填 / Java 类型 / 持久化长度 /
默认值归属 / 是否可变 / 是否进索引 / 是否属于本域。

### 禁止加入实体的内容

- 继承来的审计与租户/工作区/项目字段
- 仅响应用的瞬态字段
- 与导航无关实体的 HTTP 方法、权限动作
- 框架特定的传输对象
- 「因为遗留实体里有」而保留的字段

### 主键与稳定键

| 键 | 规则 |
|----|------|
| 技术主键 | `String` + `@TableId(ASSIGN_UUID)` + `@Id` + `@Column(length=32)`；operator 不得手工赋值 |
| 稳定业务键 | 归属范围内唯一、创建后不可变；排除出 update 请求 |

`idPrefix()` 短小、小写、稳定；不得在普通重构中更改。

### 索引设计

从访问模式反推：`uk_` 唯一查找、`idx_` 外键/过滤/排序。
工作区唯一性含 `workspace_id`；项目唯一性含 `project_id`。

---

## 请求记录（Request）

**操作修改权不同就必须拆**。必须是 record；禁止把实体当请求。

| 用途 | 命名 |
|------|------|
| 创建 | `XxxCreateRequest` |
| 更新可变属性 | `XxxUpdateRequest`（排除稳定键） |
| 状态变更 | `XxxStatusUpdateRequest` |
| 分页/树/排序/成员 | `XxxPageRequest` / `XxxTreeRequest` / `XxxOrderRequest` / `XxxAddRequest` / `XxxReplaceRequest` |

集合在紧凑构造器中 `List.copyOf` / `Set.copyOf` / `Map.copyOf`。

---

## 视图记录（VO）

| 场景 | 命名 |
|------|------|
| 主管理/详情视图 | `XxxVo` |
| 紧凑选择器 | `XxxOptionVo` |

必须是 record；禁止直接暴露持久化实体；禁止大写 `VO` / `Dto` / `Response`。

---

## 内部模型、枚举、事件、状态码

- **Model**：仅当既非实体也非传输 record 时需要；按业务概念命名，无 `Model` 后缀
- **Enums**：封闭业务概念；`State`/`Status`/`Mode`/`Type` 语义区分
- **Event**：不可变 record；发布域拥有；kernel 与 platform 不得互引对方事件类型
- **StatusCode**：先搜现有目录；九字符全码；见 [`standards/exception-status-code.md`](../standards/exception-status-code.md)

---

## 领域建模评审门禁

- [ ] 持久化范围与基类正确（默认 `WorkspaceBaseEntity`）
- [ ] `ProjectBaseEntity` 仅在设计批准后使用
- [ ] 主键与稳定键已区分
- [ ] 每个字段都能回答评审八问
- [ ] 索引与访问模式匹配
- [ ] 请求按修改权拆分；VO 未暴露实体
- [ ] 枚举/事件/状态码归属正确
- [ ] 未创建投机性 model / event 包
