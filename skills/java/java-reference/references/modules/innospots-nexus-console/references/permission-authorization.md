# 包 `permission.authorization`

## AuthorizationContext

**类型：** record

请求鉴权通过后，传递给后续数据访问适配器的上下文。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `workspaceId` | `/**
         * 当前 Workspace ID。
         */
        String` | — |
| `pageKey` | `/**
         * 请求头中解析出的页面 key。
         */
        String` | — |
| `datasourceKey` | `/**
         * 根据页面、HTTP 方法和 URL 匹配出的 datasource key。
         */
        String` | — |
| `constraintDefinitions` | `/**
         * 当前主体从角色和组织单元授权中获得的附加查询条件。
         */
        List<String>` | — |


## AuthorizationDecision

**类型：** record

与框架无关的请求鉴权结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `allowed` | `/**
         * 是否允许请求继续执行。
         */
        boolean` | — |
| `denyReason` | `/**
         * 拒绝原因；允许时为空。
         */
        String` | — |
| `context` | `/**
         * 允许时交给后续适配器使用的鉴权上下文；拒绝时为空。
         */
        AuthorizationContext` | — |

### 方法

#### `allow(AuthorizationContext context) → AuthorizationDecision`
- **说明：** 是否允许请求继续执行。 / boolean allowed, /** 拒绝原因；允许时为空。 / String denyReason, /** 允许时交给后续适配器使用的鉴权上下文；拒绝时为空。 / AuthorizationContext context ) { /** 创建允许结果。
- **参数：**
  - `context` — 请求通过后形成的鉴权上下文
- **返回：** 允许结果

#### `deny(String reason) → AuthorizationDecision`
- **说明：** 创建拒绝结果，不向请求方暴露资源目录细节。
- **参数：**
  - `reason` — 拒绝原因
- **返回：** 拒绝结果


## AuthorizationRequest

**类型：** record

由 Filter 或 REST 适配器提取的、与框架无关的请求鉴权数据。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `workspaceId` | `/**
         * 当前 Workspace ID。
         */
        String` | — |
| `method` | `/**
         * 实际 HTTP 方法。
         */
        String` | — |
| `path` | `/**
         * 实际请求路径，可以带查询字符串。
         */
        String` | — |
| `pageKey` | `/**
         * 请求头中的页面 key。
         */
        String` | — |
| `subject` | `/**
         * 当前登录主体及其角色、组织单元信息。
         */
        AuthorizationSubject` | — |


## AuthorizationScope

**类型：** class

通过线程上下文向数据访问适配器传递鉴权结果。

### 方法

#### `open(AuthorizationContext context) → AuthorizationScope`
- **说明：** 为当前请求打开鉴权作用域。
- **参数：**
  - `context` — 已通过鉴权器校验的鉴权上下文
- **返回：** 可用于 try-with-resources 的作用域

#### `current() → Optional<AuthorizationContext>`
- **说明：** 返回当前线程中的请求鉴权上下文。
- **返回：** 已打开的上下文；当前线程没有鉴权作用域时为空

#### `close() → void`
- **说明：** 恢复嵌套作用域的上一级上下文，并清理最外层线程变量。 重复关闭不会产生副作用。 / public void close()


## AuthorizationSubject

**类型：** record

由应用安全适配器提供的当前鉴权主体。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `userId` | `/**
         * 当前用户 ID。
         */
        String` | — |
| `roleIds` | `/**
         * 当前用户拥有的角色 ID 集合。
         */
        Set<String>` | — |
| `orgUnitIds` | `/**
         * 当前用户所属的组织单元 ID 集合。
         */
        Set<String>` | — |
| `administrator` | `/**
         * 是否为项目管理员。管理员只绕过普通功能授权。
         */
        boolean` | — |


## AuthorizationSubjectResolver

**类型：** interface

从当前请求上下文解析鉴权主体的端口。


## ConsolePagePermissionAuthorizer

**类型：** class

控制台 catalog 页面与 datasource 代理的权限判定（框架中立）。

### 方法

#### `authorize(AuthorizationRequest request) → AuthorizationDecision`
- **说明：** 使用权限目录和授权记录存储创建请求鉴权器。 / public ConsolePagePermissionAuthorizer( ConsoleCatalogResourceDao resourceDao, PermissionGrantDao grantDao ) { this.resourceDao = resourceDao; this.grantDao = grantDao; } /** 对一个已由应用适配器提取的请求执行鉴权判定。 判定顺序固定为先校验 PAGE，再根据 method 和 URL 唯一匹配 DATASOURCE，避免接口路径在 页面权限不足时泄露 datasource 授权信息。
- **参数：**
  - `request` — 标准化请求鉴权数据
- **返回：** 允许或拒绝结果；允许时包含后续数据访问适配器使用的约束定义


## SessionAuthorizationSubjectResolver

**类型：** class

从 SessionContext 与角色绑定表解析当前鉴权主体。
