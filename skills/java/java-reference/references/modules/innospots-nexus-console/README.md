# innospots-nexus-console — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：1.0.0

## 模块概览

基于 `innospots-nexus-core` 和 `innospots-nexus-plugin` 的管理控制台 **API 表面**：
Jakarta REST 契约、请求/响应 record、转换器、框架中立的认证/作用域端口、
目录索引持久化、权限运行时，以及内置控制台入口插件。

**不包含：** Spring Boot 自动配置、Servlet 绑定、具体用户持久化（kernel/platform 实现端口）、
插件规范 / Page DSL 定义，或菜单/角色/字典 CRUD 的完整业务工作流（契约已存在；kernel 负责实现）。

**能力一览：**

| 能力 | 说明 |
|------------|-------------|
| **REST 契约** | `/console`、`/tenant`、`/platform` 下的 14 个 Jakarta JAX-RS 表面 |
| **认证与作用域** | `SecurityRealm` 分离、令牌签发、作用域选择、会话绑定 |
| **目录索引** | 从插件贡献 + Page DSL 同步到 `nx_console_catalog_resource` |
| **权限运行时** | 授权存储、可见性、`RequestAuthorizer`（框架中立） |
| **导航** | 基于目录 + 授权的权限过滤侧边栏组装 |
| **插件管理** | 通过 `PluginInstallationManager` 安装/启用/禁用/重试 |
| **内置入口** | 六个控制台模块的 `Plugin` 入口描述符 |
| **管理 CRUD 契约** | 角色、菜单、字典端点形态（实现在 kernel 中） |
| **审计日志** | `nx_audit_log` 实体 + 调用日志管道 |

## 边界契约

### 控制台 vs 插件 vs kernel/platform

| 关注点 | 归属 | 控制台提供 |
|---------|-------|------------------|
| 插件发现、贡献解码、Page DSL | **plugin** | 仅消费 `ConsoleContributionCatalog`、`PageDslLoader` 用于同步 |
| 目录持久化索引（`nx_console_catalog_resource`） | **console** | `ConsoleCatalogSyncService`、`ConsoleCatalogService` |
| 权限授权（`nx_permission_grant`） | **console** | 实体、DAO、授权替换契约、`RequestAuthorizer` |
| 用户 / 成员持久化 | **kernel** / **platform** | `UserDirectory`、`MembershipDirectory`、`CredentialStore` 端口 |
| 租户 / 工作空间 / 项目快照 | **kernel** / **platform** | `TenantScopeDirectory`、`WorkspaceScopeDirectory`、`ProjectScopeDirectory` |
| 角色/菜单/字典业务工作流 | **kernel** | 实现控制台端点契约 + operator |
| HTTP 运行时绑定 | **adapter/application** | 装配端点、过滤器、`AuthorizationSubjectResolver` |

### 分层（强制）

```text
endpoint → service → operator → dao
```

- 端点：仅使用 `jakarta.ws.rs`；返回 `R<T>`（或 `R` 内的 `PageResult<T>`）。
- `domain.request` / `domain.vo`：**records**。
- 事务：仅在 service/operator 上使用 `jakarta.transaction.Transactional`。
- 控制台端点可以是**接口**（kernel 实现）或**类**（console 自带逻辑）。

### 安全域

| 域 | 路径前缀 | 注册 | 说明 |
|-------|-------------|----------|-------|
| `PLATFORM` | `/platform/auth` | 无公开注册 | 仅运维域登录 |
| `TENANT` | `/tenant/auth`、`/tenant/scope` | `/tenant/auth/register` | 身份 → 租户 → 工作空间 → 项目令牌链 |

会话快照通过 `SessionScopeBinder` 绑定到 `SessionContext` / `TLC`
（base 传输类型 — 见 `innospots-nexus-base` 作用域参考）。

### 持久化表（控制台归属）

| 表 | 实体 | 作用域基类 |
|-------|--------|------------|
| `nx_console_catalog_resource` | `ConsoleCatalogResourceEntity` | `BaseEntity`（行内含工作空间） |
| `nx_permission_grant` | `PermissionGrantEntity` | `WorkspaceBaseEntity` |
| `nx_role` | `RoleEntity` | `WorkspaceBaseEntity` |
| `nx_role_binding` | `RoleBindingEntity` | `WorkspaceBaseEntity` |
| `nx_menu` | `MenuEntity` | `WorkspaceBaseEntity` |
| `nx_dictionary_type` | `DictionaryTypeEntity` | `WorkspaceBaseEntity` |
| `nx_dictionary_item` | `DictionaryItemEntity` | `WorkspaceBaseEntity` |
| `nx_audit_log` | `AuditLogEntity` | `BaseEntity` |

## 扩展指南

| 需求 | 使用控制台中的 | 实现在 |
|------|------------------|--------------|
| 租户/平台登录 | `AuthFacade`、`AuthTokenPairIssuer`、认证端点 | kernel / platform + adapter |
| 作用域目录查询 | `*ScopeDirectory` 端口 | kernel（租户数据）/ platform |
| 用户查询 | `UserDirectory`、`CredentialStore` | kernel / platform |
| 登录时密码验证 | `PasswordVerificationOperator`、`PasswordDecryptor` | adapter 或 kernel |
| HTTP 请求授权 | `RequestAuthorizer` + `AuthorizationRequest` | adapter 过滤器调用 authorizer |
| 当前用户主体 | `AuthorizationSubjectResolver` | adapter（从令牌/会话） |
| 插件启用时目录同步 | `ConsoleCatalogSyncService`、启动任务 | 宿主注册 `NexusStartupTask` |
| 新管理 REST 区域 | 在 console 中添加端点接口 + request/vo records | kernel service/operator/dao |
| 新内置控制台模块页面 | `ConsoleModuleDescriptor` + `*EntryPlugin` | console entry 包 |

**不要**在 console 中放置插件规范、贡献约束或 Spring 绑定。

## REST 端点索引

完整方法表：[endpoint-contracts.md](references/endpoint-contracts.md)。

| 路径前缀 | 端点 | 风格 |
|-------------|----------|-------|
| `/console` | `ConsoleEndpoint` | interface |
| `/console/catalog` | `ConsoleCatalogEndpoint` | class |
| `/console/plugins` | `PluginManagementEndpoint` | class |
| `/console/navigation/menus` | `NavigationMenuEndpoint` | class |
| `/console/me/permissions` | `CurrentAuthorizationEndpoint` | interface |
| `/console/roles` | `RoleEndpoint` | interface |
| `/console/roles/{roleId}/bindings` | `RoleBindingEndpoint` | class (stub) |
| `/console/roles/{roleId}/permissions` | `GrantManagementEndpoint` | interface |
| `/console/organization-units/{unitId}/permissions` | `GrantManagementEndpoint` | interface |
| `/console/menus` | `MenuEndpoint` | class (stub) |
| `/console/dictionary-types` | `DictionaryTypeEndpoint` | class (stub) |
| `/console/dictionary-types/{typeCode}/items` | `DictionaryItemEndpoint` | class (stub) |
| `/tenant/auth` | `TenantAuthEndpoint` | interface |
| `/tenant/scope` | `TenantScopeEndpoint` | interface |
| `/platform/auth` | `PlatformAuthEndpoint` | interface |

## 类参考

### 包 `auth.api`（端口 — kernel/platform 实现）

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `UserDirectory` | `interface` | 按域 + 标识符查找登录身份 |
| `MembershipDirectory` | `interface` | 为身份令牌交换解析租户成员关系 |
| `CredentialStore` | `interface` | 为认证流程加载/更新凭证材料 |

### 包 `auth.service`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `AuthFacade` | `class` | 编排登录、注册、刷新、密码流程 |
| `AuthTokenPairIssuer` | `class` | 从 `TokenClaims` 签发访问/刷新令牌对 |
| `AuthSessionScope` | `record` | 令牌中携带的租户/工作空间/项目 ID |

### 包 `scope.api`（端口）

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `TenantScopeDirectory` | `interface` | 按租户 ID 获取 `TenantScope` |
| `WorkspaceScopeDirectory` | `interface` | 按租户 + 工作空间 ID 获取 `WorkspaceSnapshot` |
| `ProjectScopeDirectory` | `interface` | 按租户 + 工作空间 + 项目 ID 获取 `ProjectSnapshot` |

### 包 `scope.service`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ScopeFacade` | `class` | 工作空间/项目选择编排 |
| `SessionScopeBinder` | `class` | 将认证用户 + 作用域快照绑定到 `SessionContext` |

### 包 `catalog`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `ConsoleCatalogSyncService` | `class` | 将 ACTIVE 贡献同步到 `nx_console_catalog_resource` |
| `ConsoleCatalogService` | `class` | 为权限 UI 读取目录树 |
| `ConsoleCatalogResourceEntity` | `class` | 持久化目录节点 |
| `CatalogResourceType` | `enum` | `MODULE`、`MENU`、`PAGE`、`ACTION`、`DATASOURCE`、`CAPABILITY` |
| `ConsoleCatalogSyncStartupTask` | `class` | 启动时同步的 `NexusStartupTask` 钩子 |

### 包 `permission.authorization`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `RequestAuthorizer` | `class` | 基于目录 + 授权的 PAGE 然后 DATASOURCE 授权 |
| `AuthorizationSubjectResolver` | `interface` | 从运行时解析当前 `AuthorizationSubject` |
| `AuthorizationRequest` | `record` | 用于检查的规范化 HTTP 方法、路径、页面键 |
| `AuthorizationDecision` | `record` | 允许/拒绝 + 原因 |
| `AuthorizationScope` | `class` | 线程本地认证上下文（`AutoCloseable`） |

### 包 `permission.service`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `PermissionGrantService` | `class` | 为角色和组织单元替换/列出授权 |
| `PermissionVisibilityService` | `class` | 当前用户可见资源 |

### 包 `navigation.service`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `NavigationMenuAssembler` | `class` | 从目录 + 授权构建 `NavigationMenuVo` 树 |

### 包 `entry`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `BuiltinConsoleEntryPlugins` | `class` | 必需的内置入口插件 ID 常量 |
| `ConsoleModuleDescriptor` | `record` | 入口插件的模块/页面/菜单元数据 |
| `ConsoleModuleEntrySupport` | `class` | 共享入口贡献辅助工具 |

### 包 `credential.api`

| 类 | 类型 | 说明 |
|-------|------|-------------|
| `PasswordDecryptor` | `interface` | 解密客户端加密密码 |
| `PasswordVerificationOperator` | `interface` | 针对存储凭证验证密码 |

## 包参考

| 领域 | 文件 |
|------|------|
| REST 路径与方法契约 | [endpoint-contracts.md](references/endpoint-contracts.md) |
| 认证、凭证、作用域、会话 | [auth-scope.md](references/auth-scope.md) |
| 目录同步与权限运行时 | [catalog-permission.md](references/catalog-permission.md) |
| 菜单、角色、字典契约 | [management-domains.md](references/management-domains.md) |
| 插件管理与入口插件 | [plugin-entry.md](references/plugin-entry.md) |
| 审计与调用日志 | [logger.md](references/logger.md) |

## 技能用法（java:reference）

本目录**不是** `java:console` 或任何可安装技能。在以下场景通过 **`java:reference`** 消费：

- 扩展 kernel/platform 以实现控制台端口或端点接口
- 在 kernel 实现之前在 console 中添加 REST 契约或 VO
- 判断目录、权限或插件逻辑应归属 console 还是 plugin
- 审查 `/tenant` 与 `/platform` 路径及 `SecurityRealm` 分离

工作流：

```text
java:reference  → module-ownership.md + this README
java:design     → boundary + endpoint contracts before code
java:develop    → implement in kernel/platform/adapter; console stays contract-neutral
java:check      → mvn clean compile / test per module
```

相关技能：`java:design`（契约）、`java:develop`（实现）、
`java:spring` 或适配器模块（运行时绑定）。插件/Page DSL 详情见
`innospots-nexus-plugin` 文档 — 此处不重复。
