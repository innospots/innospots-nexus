# REST 端点契约

管理 API 使用 `jakarta.ws.rs`、`application/json`，载荷包装在 `R<T>` 中。
需登录的控制台路由标注 `@NexusAuthenticatedApi`（Bearer）。

运行时 CDI/Spring/Quarkus 绑定在 **adapter/application** — 不在本模块。

## 路径约定

| 前缀 | 交付模块 | 说明 |
|--------|----------|------|
| `/console/**` | **console** | 工作空间/租户会话下的管理 API |
| `/openapi/specs` | **console** | OpenAPI YAML 目录（构建期 bundled） |
| `/tenant/auth`、`/tenant/scope` | **portal** | 租户身份与作用域令牌链 |
| `/platform/auth` | **platform** | 运维平台登录 |

## `/console`

### `ConsoleEndpoint` — `/console`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/status` | `String`（非 `R`） |

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

### `NavigationMenuEndpoint` — `/console/navigation/menus`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<NavigationMenuVo>>` |

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

PUT 为授权 + 数据源条件的**全量替换**。

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

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<RoleBindingVo>>` |
| POST | `/` | `R<Void>` |
| DELETE | `/{bindingId}` | `R<Void>` |

### `DictionaryTypeEndpoint` — `/console/dictionary-types`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<DictionaryTypeVo>>` |
| GET | `/{dictionaryTypeId}` | `R<DictionaryTypeVo>` |
| POST | `/` | `R<DictionaryTypeVo>` |
| PUT | `/{dictionaryTypeId}` | `R<DictionaryTypeVo>` |
| PUT | `/{dictionaryTypeId}/status` | `R<Void>` |
| DELETE | `/{dictionaryTypeId}` | `R<Void>` |
| GET | `/options` | `R<List<DictionaryTypeOptionVo>>` |

### `DictionaryItemEndpoint` — `/console/dictionary-types/{typeCode}/items`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<DictionaryItemVo>>` |
| GET | `/{dictionaryItemId}` | `R<DictionaryItemVo>` |
| POST | `/` | `R<DictionaryItemVo>` |
| PUT | `/{dictionaryItemId}` | `R<DictionaryItemVo>` |
| PUT | `/{dictionaryItemId}/status` | `R<Void>` |
| DELETE | `/{dictionaryItemId}` | `R<Void>` |

## `/openapi`

### `OpenApiCatalogEndpoint` — `/openapi/specs`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<OpenApiSpecItemVo>>` |
| GET | `/{specId}` | `application/yaml` 正文 |

## 租户 / 平台（非 console 源码）

`/tenant/**`、`/platform/**` 的 JAX-RS 资源与请求 record 由 **portal** / **platform** 模块实现；
形状与旧版 console 契约兼容，详见对应模块 API 索引。

## 契约规则

1. 请求体：`domain.request` **records**；分页查询使用 `@BeanParam` page request records。
2. 错误：`NexusException` + `StatusCode`。
3. ID 路径参数使用稳定业务 ID（`roleId`、`dictionaryTypeId`、`pluginId` 等）。
