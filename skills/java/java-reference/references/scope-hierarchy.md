# 作用域层级与 Session 模型

横切概念：**运行时作用域**（Session / Snapshot）与**持久化范围**（Entity 基类）
是两套相关但不同的边界。所有技能通过本文件理解二者，不得混用。

权威补充：`AGENTS.md`（模块职责）、[`standards/api-design.md`](../standards/api-design.md)（持久化实体）。

---

## 运行时作用域层级

```text
User → Tenant + Organization → Workspace → Project（可选）
```

| 层级 | 传输快照（base） | 说明 |
|------|-----------------|------|
| 用户 | `UserSnapshot` | 当前认证主体 |
| 租户 | `TenantSnapshot` | 租户标识与展示属性 |
| 组织档案 | `OrganizationSnapshot` | 租户业务档案（**不是** kernel `OrganizationUnit`） |
| 工作区 | `WorkspaceSnapshot` | 工作区共享资源边界 |
| 项目 | `ProjectSnapshot` | 可选；工作区内的业务隔离上下文 |

### SessionContext 绑定顺序

`SessionContext`（`base.thread`）按以下顺序绑定，后级可覆盖前级的 TLC 键：

```text
bindUser → bindTenant → bindWorkspace → bindProject（可选）
```

- `requireWorkspaceId()`：工作区级操作必须已有工作区
- `projectId()` / `project()`：项目为**可选**上下文，未选项目时为空
- Snapshot 类型归 **base**，业务实体与工作流归 **kernel / platform**

### Snapshot 与 Entity

| 维度 | Snapshot（base） | Entity（kernel/platform/core） |
|------|------------------|-------------------------------|
| 用途 | 传输、会话、JWT 声明 | 持久化、业务规则 |
| 形态 | record / 轻量 class | JPA/MyBatis-Plus 实体 |
| 示例 | `ProjectSnapshot` | `ProjectEntity`（继承 `WorkspaceBaseEntity`） |
| 归属模块 | `innospots-nexus-base` | 拥有该业务域的模块 |

不得把 Snapshot 当持久化实体，也不得把 Entity 直接暴露给端点响应。

---

## 持久化范围层级

```text
BaseEntity → TenantBaseEntity → WorkspaceBaseEntity → ProjectBaseEntity
```

| 基类 | 隔离键 | 默认？ | 判定 |
|------|--------|--------|------|
| `WorkspaceBaseEntity` | `tenantId` + `workspaceId` | **是** | 工作区共享的业务记录 |
| `TenantBaseEntity` | `tenantId` | | 租户级、不属于某个工作区 |
| `BaseEntity` | 无租户键 | | 平台级 / realm 全局 |
| `ProjectBaseEntity` | + `projectId` | **否，需设计批准** | 工作区内需要项目级数据隔离 |

### ProjectBaseEntity 过渡策略

- `ProjectBaseEntity` 由 **core** 提供，是基础设施能力，不是默认选择。
- **新业务实体默认继承 `WorkspaceBaseEntity`。**
- 仅当设计评审明确记录「需要项目级持久化隔离」时，才可继承 `ProjectBaseEntity`。
- 不得在无项目语义的表上单独加 `projectId` 列。
- 项目**定义**本身（如 `nx_project`）通常用 `WorkspaceBaseEntity`；项目**下属业务数据**
  才在批准后使用 `ProjectBaseEntity`。

索引须匹配范围列：工作区唯一性含 `workspace_id`；项目唯一性含 `project_id`。

---

## 常见误判

| 误判 | 正确做法 |
|------|---------|
| 有 `projectId` 会话就必须用 `ProjectBaseEntity` | 会话项目上下文 ≠ 持久化隔离；默认仍用 `WorkspaceBaseEntity` |
| `OrganizationSnapshot` 就是组织单元实体 | Snapshot 是传输档案；kernel 自有 `OrganizationUnit` 实体 |
| 在 base 放业务实体 | base 只放 Snapshot 与无中间件工具 |
| kernel 与 platform 互引对方的 Snapshot 扩展 | 各自域内建模；共享契约下沉 base/console |

---

## 评审门禁

- [ ] 运行时作用域（Session）与持久化范围（Entity 基类）已分别判定
- [ ] Snapshot 与 Entity 未混用
- [ ] 默认 `WorkspaceBaseEntity`；`ProjectBaseEntity` 有书面设计批准
- [ ] 索引维度与所选基类一致
- [ ] `SessionContext` 绑定顺序与端点授权上下文匹配
