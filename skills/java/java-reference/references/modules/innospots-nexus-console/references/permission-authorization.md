# 包 `permission.authorization`

## `AuthorizationContext`

**类型：** record

请求授权通过后传给数据访问适配器的上下文。

| 组件 | 类型 | 说明 |
|------|------|------|
| `workspaceId` | `String` | 当前 Workspace ID |
| `pageKey` | `String` | 入口页 pageKey |
| `datasourceKey` | `String` | 兼容字段；当前 catalog v2 授权器固定返回 `null` |
| `constraintDefinitions` | `List<String>` | 兼容字段；当前授权器固定返回空列表 |

构造器复制 `constraintDefinitions`，避免线程上下文暴露可变集合。

## `AuthorizationDecision`

**类型：** record

框架无关的允许/拒绝结果。

| 组件 | 类型 | 说明 |
|------|------|------|
| `allowed` | `boolean` | 是否允许 |
| `denyReason` | `String` | 拒绝原因；允许时为空 |
| `context` | `AuthorizationContext` | 允许上下文；拒绝时为空 |

### 静态方法

- `allow(AuthorizationContext context)`
- `deny(String reason)`

## `AuthorizationRequest`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `workspaceId` | `String` | 当前 Workspace ID |
| `method` | `String` | HTTP 方法；当前页面授权器不消费 |
| `path` | `String` | 请求路径；当前页面授权器不消费 |
| `pageKey` | `String` | 入口页 pageKey |
| `subject` | `AuthorizationSubject` | 当前登录主体 |

## `AuthorizationScope`

**类型：** class（final，`AutoCloseable`）

通过线程上下文传递 `AuthorizationContext`，支持嵌套作用域。

### 方法

- `open(AuthorizationContext context) → AuthorizationScope`
- `current() → Optional<AuthorizationContext>`
- `close() → void`：恢复父作用域；最外层关闭时清理线程变量，重复关闭幂等。

## `AuthorizationSubject`

**类型：** record

| 组件 | 类型 | 说明 |
|------|------|------|
| `userId` | `String` | 当前用户 ID |
| `roleIds` | `Set<String>` | 当前用户角色 |
| `orgUnitIds` | `Set<String>` | 当前用户组织单元 |
| `administrator` | `boolean` | 是否管理员 |

集合构造时复制为空集合并冻结。

## `AuthorizationSubjectResolver`

**类型：** interface

从当前请求上下文解析主体：`resolve() → Optional<AuthorizationSubject>`。

## `ConsolePagePermissionAuthorizer`

**类型：** class（final）

catalog v2 页面入口授权器。

### 构造方法

#### `ConsolePagePermissionAuthorizer(ConsoleCatalogResourceDao resourceDao, PermissionGrantDao grantDao)`

### 方法

#### `authorize(AuthorizationRequest request) → AuthorizationDecision`

按 `pageKey` 唯一匹配启用的叶子 MODULE/MENU，再以该资源的 view grant key 检查
ROLE / ORG_UNIT 授权。管理员绕过普通授权。

不执行 datasource 路由匹配；返回的 `AuthorizationContext` 中 `datasourceKey = null`、
`constraintDefinitions = []`。

## `SessionAuthorizationSubjectResolver`

**类型：** class（final，implements `AuthorizationSubjectResolver`）

从 `SessionContext`、角色绑定与角色管理员标记解析主体。当前实现返回角色集合与
`administrator`，`orgUnitIds` 为空集合；解析异常按未认证处理。
