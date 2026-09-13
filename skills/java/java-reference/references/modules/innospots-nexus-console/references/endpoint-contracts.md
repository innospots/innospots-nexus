# REST 端点契约

所有管理 API 使用 `jakarta.ws.rs`、`application/json`，并将载荷包装在
`innospots-nexus-base` 的 `R<T>` 中。分页列表使用 `R<PageResult<T>>`。

运行时绑定（CDI、Spring、Quarkus）**不在**本模块中 — 适配器层注册实现。

## 路径约定

| 前缀 | 受众 | 认证域 |
|--------|----------|------------|
| `/console/**` | 管理 UI（工作空间作用域） | TENANT 业务会话 |
| `/tenant/auth`、`/tenant/scope` | 租户身份与作用域 | TENANT |
| `/platform/auth` | 平台运维控制台 | PLATFORM |

## `/console`

### `ConsoleEndpoint` — `/console`

| 方法 | 路径 | 响应 | 说明 |
|--------|------|----------|-------|
| GET | `/console/status` | `String` | 健康探测（非 `R` 包装） |

### `ConsoleCatalogEndpoint` — `/console/catalog`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/tree` | `R<List<CatalogNodeVo>>` |
| POST | `/sync` | `R<PermissionResourceSyncVo>` |

### `PluginManagementEndpoint` — `/console/plugins`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<PluginManagementVo>>` |
| GET | `/{pluginId}` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/install` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/enable` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/disable` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/retry` | `R<PluginManagementVo>` |

无卸载/删除 JAR API。

### `NavigationMenuEndpoint` — `/console/navigation/menus`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<NavigationMenuVo>>` |

需要 `SessionContext` 中的工作空间；未认证 → 空列表。

### `CurrentAuthorizationEndpoint` — `/console/me/permissions`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<PermissionResourceVo>>` |

### `GrantManagementEndpoint` — `/console`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/roles/{roleId}/permissions` | `R<PermissionGrantReplaceRequest>` |
| PUT | `/roles/{roleId}/permissions` | `R<Void>` |
| GET | `/organization-units/{unitId}/permissions` | `R<PermissionGrantReplaceRequest>` |
| PUT | `/organization-units/{unitId}/permissions` | `R<Void>` |

PUT 为授权 + 数据源条件的**完整替换**。

### `RoleEndpoint` — `/console/roles`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<RoleVo>>` |
| GET | `/{roleId}` | `R<RoleVo>` |
| POST | `/` | `R<RoleVo>` |
| PUT | `/{roleId}` | `R<RoleVo>` |
| PUT | `/{roleId}/status` | `R<Void>` |
| DELETE | `/{roleId}` | `R<Void>` |
| GET | `/options` | `R<List<RoleOptionVo>>` |

### `RoleBindingEndpoint` — `/console/roles/{roleId}/bindings`

| 方法 | 路径 | 响应 | 状态 |
|--------|------|----------|--------|
| GET | `/` | `R<PageResult<RoleBindingVo>>` | stub |
| POST | `/` | `R<Void>` | stub |
| DELETE | `/{bindingId}` | `R<Void>` | stub |

### `MenuEndpoint` — `/console/menus`

| 方法 | 路径 | 响应 | 状态 |
|--------|------|----------|--------|
| GET | `/` | `R<List<MenuVo>>` | stub |
| GET | `/{menuId}` | `R<MenuVo>` | stub |
| POST | `/` | `R<MenuVo>` | stub |
| PUT | `/{menuId}` | `R<MenuVo>` | stub |
| PUT | `/{menuId}/status` | `R<Void>` | stub |
| DELETE | `/{menuId}` | `R<Void>` | stub |
| PUT | `/order` | `R<Void>` | stub |
| GET | `/options` | `R<List<MenuOptionVo>>` | stub |

### `DictionaryTypeEndpoint` — `/console/dictionary-types`

| 方法 | 路径 | 响应 | 状态 |
|--------|------|----------|--------|
| GET | `/` | `R<PageResult<DictionaryTypeVo>>` | stub |
| GET | `/{typeId}` | `R<DictionaryTypeVo>` | stub |
| POST | `/` | `R<DictionaryTypeVo>` | stub |
| PUT | `/{typeId}` | `R<DictionaryTypeVo>` | stub |
| PUT | `/{typeId}/status` | `R<Void>` | stub |
| DELETE | `/{typeId}` | `R<Void>` | stub |
| GET | `/options` | `R<List<DictionaryTypeOptionVo>>` | stub |

### `DictionaryItemEndpoint` — `/console/dictionary-types/{typeCode}/items`

| 方法 | 路径 | 响应 | 状态 |
|--------|------|----------|--------|
| GET | `/` | `R<PageResult<DictionaryItemVo>>` | stub |
| GET | `/{itemId}` | `R<DictionaryItemVo>` | stub |
| POST | `/` | `R<DictionaryItemVo>` | stub |
| PUT | `/{itemId}` | `R<DictionaryItemVo>` | stub |
| PUT | `/{itemId}/status` | `R<Void>` | stub |
| DELETE | `/{itemId}` | `R<Void>` | stub |

## `/tenant`

### `TenantAuthEndpoint` — `/tenant/auth`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| POST | `/register` | `R<AuthTokenVo>` |
| POST | `/login` | `R<AuthTokenVo>` |
| POST | `/select-tenant` | `R<AuthTokenVo>` |
| POST | `/refresh` | `R<AuthTokenVo>` |
| POST | `/logout` | `R<Void>` |
| POST | `/password/change` | `R<Void>` |
| POST | `/password/reset` | `R<Void>` |

登录请求密码为**客户端加密**（`encryptedPassword` 字段）。

### `TenantScopeEndpoint` — `/tenant/scope`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| POST | `/select-workspace` | `R<AuthTokenVo>` |
| POST | `/select-project` | `R<AuthTokenVo>` |

## `/platform`

### `PlatformAuthEndpoint` — `/platform/auth`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| POST | `/login` | `R<AuthTokenVo>` |
| POST | `/refresh` | `R<AuthTokenVo>` |
| POST | `/logout` | `R<Void>` |
| POST | `/password/change` | `R<Void>` |
| POST | `/password/reset` | `R<Void>` |

平台路径上**无** `/register`。

## 契约规则

1. 请求体：`domain.request` **records**；查询/分页参数：使用处为 `@BeanParam` records。
2. 错误：`NexusException` + 类型化 `StatusCode` — 绝不向客户端暴露原始运行时异常。
3. 接口端点在 **kernel**（租户）或 **platform**（运维）中实现。
4. 带逻辑的类端点在 **console** 中交付；stub 在 kernel 装配服务前抛出异常。
5. ID 路径参数使用稳定的业务 ID（`roleId`、`typeCode`、`pluginId`），而非代理名称。
