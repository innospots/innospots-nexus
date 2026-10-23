# REST 端点契约

管理 API 使用 `jakarta.ws.rs`、`application/json`，载荷包装在 `R<T>` 中。
需登录的控制台路由标注 `@NexusAuthenticatedApi`（Bearer）。

运行时 CDI/Spring/Quarkus 绑定在 **adapter/application** — 不在本模块。

JAX-RS 类路径使用 `com.innospots.nexus.console.config.ConsoleConstant` 前缀：

| 常量 | 值 |
|------|-----|
| `API_PREFIX` | **`/api/d/nexus`** |
| `PUBLIC_API_PREFIX` | **`/api/public`**（免 Bearer 鉴权） |

下文完整路径均基于上述前缀。

## 路径约定

| 前缀 | 交付模块 | 说明 |
|--------|----------|------|
| `/api/d/nexus/**` | **console** | 工作空间/租户会话下的管理 API |
| `/api/public/**` | **console** | 公共开放 API（免鉴权；另含 `/api/d/{domain}/public/**` 业务域形态） |
| `/openapi/specs`、`/openapi/ui` | **core** | OpenAPI 规范目录与 Scalar 文档钩子 |
| `/tenant/auth`、`/tenant/scope` | **portal** | 租户身份与作用域令牌链 |
| `/platform/auth` | **platform** | 运营管理平台登录 |

## `/api/d/nexus`
### `ConsoleEndpoint` — `/api/d/nexus`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/status` | `String`（非 `R`） |

### `ConsoleCatalogEndpoint` — `/api/d/nexus/catalog`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/permission-catalog` | `R<List<CatalogNodeVo>>` |
| GET | `/tree` | `R<List<CatalogNodeVo>>` |
| POST | `/sync` | `R<PermissionResourceSyncVo>` |

### `PluginManagementEndpoint` — `/api/d/nexus/plugins`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<PluginManagementVo>>` |
| GET | `/{pluginId}` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/install` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/enable` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/disable` | `R<PluginManagementVo>` |
| POST | `/{pluginId}/retry` | `R<PluginManagementVo>` |

### `NavigationMenuEndpoint` — `/api/d/nexus/navigation/menus`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<NavigationMenuVo>>` |

### `CurrentAuthorizationEndpoint` — `/api/d/nexus/me/permissions`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<List<PermissionResourceVo>>` |

### `GrantManagementEndpoint` — `/api/d/nexus`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/roles/{roleId}/permissions` | `R<PermissionGrantReplaceRequest>` |
| PUT | `/roles/{roleId}/permissions` | `R<Void>` |
| GET | `/organization-units/{unitId}/permissions` | `R<PermissionGrantReplaceRequest>` |
| PUT | `/organization-units/{unitId}/permissions` | `R<Void>` |

PUT 为 catalog 授权项的**全量替换**；当前 catalog v2 拒绝非空
`constraintDefinition`，不会持久化 datasource 路由或附加约束。

### `RoleEndpoint` — `/api/d/nexus/roles`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<RoleVo>>` |
| GET | `/{roleId}` | `R<RoleVo>` |
| POST | `/` | `R<RoleVo>` |
| PUT | `/{roleId}` | `R<RoleVo>` |
| PUT | `/{roleId}/status` | `R<Void>` |
| DELETE | `/{roleId}` | `R<Void>` |
| GET | `/options` | `R<List<RoleOptionVo>>` |

### `RoleBindingEndpoint` — `/api/d/nexus/roles/{roleId}/bindings`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<RoleBindingVo>>` |
| POST | `/` | `R<Void>` |
| DELETE | `/{bindingId}` | `R<Void>` |

### `DictionaryTypeEndpoint` — `/api/d/nexus/dictionary-types`

该接口声明方法级路由与 grant；类级路径由业务域实现绑定。实现类必须重复 JAX-RS /
OpenAPI 方法注解供构建期扫描，但不得重复 grant。字典项随类型详情与请求体维护。

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | `R<PageResult<DictionaryTypeVo>>` |
| GET | `/{dictionaryTypeId}` | `R<DictionaryTypeVo>` |
| POST | `/` | `R<DictionaryTypeVo>` |
| PUT | `/{dictionaryTypeId}` | `R<DictionaryTypeVo>` |
| PUT | `/{dictionaryTypeId}/status` | `R<Void>` |
| DELETE | `/{dictionaryTypeId}` | `R<Void>` |
| GET | `/options` | `R<List<DictionaryTypeOptionVo>>` |

## `/api/public`（免鉴权）

### `PageAccessEndpoint` — `/api/public/page-access`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| POST | `/verify` | `R<PageAccessVerifyResult>` |

校验 catalog 页面入口授权；是否要求登录由业务实现决定。

## `/`

### `MainRootEndpoint` — `/`

| 方法 | 路径 | 响应 |
|--------|------|----------|
| GET | `/` | 307 重定向至文档入口（`nexus.console.web.jersey.root-path`，默认 `/openapi/ui`） |

## 租户 / 平台（非 console 源码）

`/tenant/**`、`/platform/**` 的 JAX-RS 资源与请求 record 由 **portal** / **platform** 模块实现；
形状与旧版 console 契约兼容，详见对应模块 API 索引。

## 契约规则

1. 请求体：`domain.request` **records**；分页查询使用 `@BeanParam` page request records。
2. 错误：`NexusException` + `StatusCode`（统一经 `core.jaxrs` 异常映射为 `R.fail`）。
3. ID 路径参数使用稳定业务 ID（`roleId`、`dictionaryTypeId`、`pluginId` 等）。
