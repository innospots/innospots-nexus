# 包 `permission.authorization`

## AuthorizationContext

**类型：** record

请求鉴权通过后，传递给后续数据访问适配器的上下文。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `workspaceId` | `String` | 当前 Workspace ID。 |
| `pageKey` | `String` | 请求头中解析出的页面 key。 |
| `datasourceKey` | `String` | 根据页面、HTTP 方法和 URL 匹配出的 datasource key。 |
| `constraintDefinitions` | `List<String>` | 当前主体从角色和组织单元授权中获得的附加查询条件。 |

### 构造方法

#### `AuthorizationContext()`

- **说明：** 复制条件集合，避免线程范围内的鉴权上下文被外部修改。

## AuthorizationDecision

**类型：** record

与框架无关的请求鉴权结果。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `allowed` | `boolean` | 是否允许请求继续执行。 |
| `denyReason` | `String` | 拒绝原因；允许时为空。 |
| `context` | `AuthorizationContext` | 允许时交给后续适配器使用的鉴权上下文；拒绝时为空。 |

### 方法

#### `allow(AuthorizationContext context) → AuthorizationDecision`

- **说明：** 创建允许结果。
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
| `workspaceId` | `String` | 当前 Workspace ID。 |
| `method` | `String` | 实际 HTTP 方法。 |
| `path` | `String` | 实际请求路径，可以带查询字符串。 |
| `pageKey` | `String` | 请求头中的页面 key。 |
| `subject` | `AuthorizationSubject` | 当前登录主体及其角色、组织单元信息。 |

## AuthorizationSubject

**类型：** record

由应用安全适配器提供的当前鉴权主体。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `userId` | `String` | 当前用户 ID。 |
| `roleIds` | `Set<String>` | 当前用户拥有的角色 ID 集合。 |
| `orgUnitIds` | `Set<String>` | 当前用户所属的组织单元 ID 集合。 |
| `administrator` | `boolean` | 是否为项目管理员。管理员只绕过普通功能授权。 |

### 构造方法

#### `AuthorizationSubject()`

- **说明：** 复制角色和组织单元集合，避免鉴权判断依赖可变输入。

## AuthorizationSubjectResolver

**类型：** interface

从当前请求上下文解析鉴权主体的端口。
