# 包 `endpoint.grant`

REST 模块归属、方法级授权/审计策略与目录聚合（`com.innospots.nexus.core.endpoint.grant`）。
接口/方法解析与 JAX-RS 路径内省位于
[`endpoint.grant.support`](endpoint-grant-support.md)。Console 过滤器链与请求属性读写见
**innospots-nexus-console** → `jaxrs.filter` / `jaxrs.support`。

## `endpoint.grant.EndpointGrant`

**类型：** annotation，`@Target(METHOD)`、`@Retention(RUNTIME)`

单条 REST 方法的授权、审计与跳过策略（平台 REST 契约单一事实源）。宿主过滤器通过
`EndpointGrantAnnotations` 解析；platform/portal 实现 console 契约时不得重复声明。

### 注解成员

| 名称 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `action` | `EndpointGrantAction` | 必填 | 最具体的动作分类 |
| `name` | `String` | 必填 | 稳定业务名；进入 catalog 操作授权键 |
| `label` | `I18nLiteral` | 空字面量 | 审计和权限目录展示文本 |
| `recordArgs` | `boolean` | `true` | 审计是否记录请求参数 |
| `auditLog` | `boolean` | `false` | 是否写审计日志 |
| `ignorePermission` | `boolean` | `false` | 是否跳过页面权限校验 |
| `ignoreAuth` | `boolean` | `false` | 是否跳过登录鉴权 |
| `group` | `String` | `""` | 声明目标分组；当前解析结果与目录尚未消费 |

### 约束

- 只能标注 HTTP 方法，不能标注资源类。
- `name` 发布后应保持稳定；推荐 `domain.resource.operation`。
- 敏感参数必须 `recordArgs = false`。
- 开启 `auditLog` 时应提供 `label`。

## `endpoint.grant.I18nLiteral`

**类型：** annotation，`@Target({})`

注解内嵌 locale → 文案对；运行时由 `I18nLiteralSupport`（或 console `AuditLogMessages`）
转为 `I18nObject`。供 `@EndpointGrant#label()` 使用。

## `endpoint.grant.I18nLiteralSupport`

**类型：** class（final，静态工具）

| 方法 | 说明 |
|------|------|
| `toI18nObject(I18nLiteral) → I18nObject` | 校验偶数 `pairs` 并构造多语言对象 |
| `resolveOperationMessage(I18nLiteral) → String` | 按当前线程 locale 解析操作说明 |

## `endpoint.grant.EndpointGrantAction`

**类型：** enum

Endpoint 授权与审计的动作分类。与 HTTP 动词不严格一一对应，应选择最具体常量。

| 常量 | 说明 |
|------|------|
| `READ` | 查询、分页、详情、选项等只读访问 |
| `CREATE` | 创建资源 |
| `UPDATE` | 更新资源主体字段 |
| `DELETE` | 删除资源 |
| `STATUS` | 启停、审批等生命周期或状态变更 |
| `REPLACE` | 全量替换授权集合或配置 |
| `EXECUTE` | 安装、同步、重试等命令 |
| `EXPORT` | 导出、下载、报表 |
| `IMPORT` | 导入、上传、批量灌入 |
| `ASSIGN` | 建立成员、角色、权限绑定 |
| `REVOKE` | 解绑、回收、撤销授予 |
| `WRITE` | 泛化写操作兜底；新契约应优先使用更具体动作 |

`READ` 与 `EXPORT` 在模块目录中归入 `readOnly`，其余归入 `operable`。

## `endpoint.grant.EndpointModule`

**类型：** annotation，`@Target(TYPE)`、`@Retention(RUNTIME)`

声明 REST 契约归属的控制台模块。模块键应与 console@2 MODULE 的 `moduleKey` 一致。
通常标注在 console endpoint 接口；platform/portal 自有资源直接标注实现类。

### 注解成员

| 名称 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `String` | 必填 | 模块键 |
| `ignorePermission` | `boolean` | `false` | 模块内全部方法默认跳过权限 |
| `ignoreAuth` | `boolean` | `false` | 模块内全部方法默认跳过登录 |

模块级跳过标志与方法级 `@EndpointGrant` 的对应标志按 OR 合并。

## `endpoint.grant.EndpointGrantDescriptor`

**类型：** record

单条 REST 方法的 grant 元数据（`EndpointGrantManager` 内存快照；**非** endpoint 响应
`Vo`）。供运行时匹配、过滤器与 catalog 组装（映射为 `CatalogViewItemVo` 等）共用。

### 组件

| 名称 | 类型 | 说明 |
|------|------|------|
| `moduleKey` | `String` | entry 模块键 |
| `httpMethod` | `String` | 大写 HTTP 动词 |
| `path` | `String` | 类级与方法级 `@Path` 合并后的模板 |
| `name` | `String` | 稳定业务名 |
| `label` | `I18nObject` | 多语言操作说明 |
| `action` | `EndpointGrantAction` | 动作分类 |
| `recordArgs` | `boolean` | 审计是否记录参数 |
| `auditLog` | `boolean` | 是否写审计 |
| `ignorePermission` | `boolean` | 是否跳过权限 |
| `ignoreAuth` | `boolean` | 是否跳过登录 |
| `resourceClass` | `String` | 注册时的资源类名 |
| `resourceMethod` | `String` | 资源方法名 |

## `endpoint.grant.EndpointModuleGrantCatalog`

**类型：** record

按模块聚合的 grant 目录（运行时模型，非 REST 响应体）。

### 组件

| 名称 | 类型 | 说明 |
|------|------|------|
| `moduleKey` | `String` | 模块键 |
| `readOnly` | `List<EndpointGrantDescriptor>` | `READ` / `EXPORT` |
| `operable` | `List<EndpointGrantDescriptor>` | 其余动作 |
| `ignoreAuth` | `List<EndpointGrantDescriptor>` | 跳过登录的接口 |
| `ignorePermission` | `List<EndpointGrantDescriptor>` | 跳过权限的接口 |

## `endpoint.grant.EndpointGrantManager`

**类型：** class（final）

汇总已注册 JAX-RS 资源的 grant 元数据，提供模块目录、路径匹配与当前请求解析。

### 方法

#### `registerResource(Class<?> resourceClass) → void`

扫描资源类全部公开方法并登记有 grant 且有 HTTP 路由的方法。

- 无模块键、无 HTTP 动词或无 grant 的方法跳过。
- 同一方法有多个 HTTP 动词时分别登记。
- 方法为 `synchronized`，避免启动期并发登记破坏目录。

#### `moduleCatalog(String moduleKey) → Optional<EndpointModuleGrantCatalog>`

返回指定模块的只读聚合目录；未知或空白模块键返回空。

#### `allModuleCatalogs() → Map<String, EndpointModuleGrantCatalog>`

返回全部模块目录的不可变快照。

#### `match(String httpMethod, String requestPath) → Optional<EndpointGrantDescriptor>`

按大写 HTTP 动词与请求路径匹配第一个已登记路由。路径模板中的 `{param}` 作为单段通配。

#### `resolve(ResourceInfo resourceInfo) → Optional<EndpointGrantDescriptor>`

从 JAX-RS 当前资源方法与资源类解析有效 grant。无 grant 时返回空；路由信息按类级和
方法级 `@Path` 解析。

## 运行时链路（console 宿主）

```text
ConsoleResolvedEndpointGrantFilter  priority AUTHENTICATION - 100
ConsoleAuthenticationFilter         priority AUTHENTICATION
ConsolePagePermissionFilter         priority AUTHENTICATION + 100
ConsoleAuditLogFilter               priority USER + 100
```

`ConsoleResolvedEndpointGrantFilter` 将结果写入请求属性
`ConsoleRequestProperties.RESOLVED_ENDPOINT_BINDING`；后续过滤器通过
`ConsoleRequestGrants.resolvedGrant(...)` 读取。

## 测试锚点

- **core：** `EndpointGrantContractsTest`；grant 解析与目录测试位于 **console** /
  **sample** 模块（依赖 console 业务端点 fixture）。
- **console：** `EndpointGrantAnnotationsTest`、`EndpointGrantManagerTest`、
  `EndpointGrantHttpIntrospectionTest`、`ConsoleResolvedEndpointGrantFilterTest`、
  `ConsoleAuthenticationFilterTest`、`ConsolePagePermissionFilterTest`、
  `ConsoleAuditLogFilterTest`
- **platform：** `PlatformStandaloneEndpointGrantContractsTest`
