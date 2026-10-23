# 包 `endpoint.grant.support`

从 console 契约接口/实现方法解析 `@EndpointModule` / `@EndpointGrant`，并解析 JAX-RS
HTTP 动词与完整路径模板（`com.innospots.nexus.core.endpoint.grant.support`）。包级主类型见
[`endpoint.grant`](endpoint-grant.md)。

## `EndpointGrantAnnotations`

**类型：** class（final）

从 JAX-RS 实现方法与 console 契约接口解析模块归属和有效 grant。

### 方法

#### `resolveModule(Class<?> resourceClass) → EndpointModule`

先读取资源类自身注解，再读取直接实现的接口注解；未声明或参数为 `null` 时返回 `null`。

#### `isGrantResourceType(Class<?> resourceClass) → boolean`

资源类自身或直接接口存在 `@EndpointModule` 时返回 `true`。

#### `resolve(Method implementationMethod, Class<?> resourceClass) → ResolvedEndpointGrant`

解析有效 grant：

1. 先读实现方法所在类型的同名同参数方法。
2. 再依次读资源类直接接口上的同名同参数方法。
3. 无 grant 时返回 `null`。
4. 合并模块级与方法级跳过标志，OR 关系。

## `ResolvedEndpointGrant`

**类型：** record

合并模块与方法注解后的有效 endpoint 策略。

### 组件

| 名称 | 类型 | 说明 |
|------|------|------|
| `moduleKey` | `String` | entry 模块键；未声明时为空字符串 |
| `action` | `EndpointGrantAction` | 动作类型 |
| `name` | `String` | 稳定业务名 |
| `label` | `I18nLiteral` | 审计文案 |
| `recordArgs` | `boolean` | 是否记录参数 |
| `auditLog` | `boolean` | 是否写审计 |
| `ignorePermission` | `boolean` | 是否跳过权限 |
| `ignoreAuth` | `boolean` | 是否跳过登录 |

## `EndpointGrantHttpIntrospection`

**类型：** class（final）

从 JAX-RS 资源类解析 HTTP 动词与完整路径模板。

### 方法

#### `resolveRoutes(Class<?> resourceClass, Method method) → List<HttpRouteBinding>`

解析类级 `@Path` 与方法级 `@Path`，并读取 `GET`、`POST`、`PUT`、`DELETE`、`PATCH`、
`HEAD`、`OPTIONS`。类级路径只读取资源实现类；方法级路径先读实现方法，再读直接接口。
无路由时返回空列表。

## `EndpointGrantHttpIntrospection.HttpRouteBinding`

**类型：** record

| 名称 | 类型 | 说明 |
|------|------|------|
| `httpMethod` | `String` | 大写 HTTP 方法 |
| `pathTemplate` | `String` | 合并后的 JAX-RS 路径模板 |

## `EndpointGrantPathMatcher`

**类型：** class（final）

将 JAX-RS 路径模板与请求路径匹配；`{param}` 转为单段 `*` 通配。

### 方法

#### `matches(String pathTemplate, String requestPath) → boolean`

任一侧为空时返回 `false`；否则委托同包 `AntPathMatcher`。

## `AntPathMatcher`

**类型：** class（final）

简单 Ant 风格路径匹配（`*`、`**`）。Console 模块的 `ConsoleAntPathMatcher` 委托本类以保持
兼容。
