# 包 `jaxrs.support`

## ConsoleAntPathMatcher

**类型：** class（final，静态工具）

简单 Ant 风格路径匹配（`*`、`**`）；路径与模式均规范化（补前导 `/`、去末尾 `/`）。

### 方法

#### `matches(String pattern, String path) → boolean`（static）
- **说明：** 单模式匹配；`**` 全匹配、`xxx/**` 前缀匹配、段级 `*` 匹配
- **参数：**
  - `pattern` — Ant 模式
  - `path` — 请求路径
- **返回：** 是否命中；任一参数为 `null` 时 `false`

#### `matchesAny(Collection<String> patterns, String path) → boolean`（static）
- **说明：** 任一模式命中即 `true`；空集合返回 `false`

## ConsolePermitAllPaths

**类型：** class（final，常量容器）

`ConsoleWebSecuritySettings.getPermitAllPatterns()` 默认 Ant 模式。命中路径在鉴权
Filter 中与 OpenAPI 一样**完全跳过** Bearer 校验（含公共开放 API）。

### 常量

| 常量 | 说明 |
|------|------|
| `OPENAPI = "/openapi/**"` | OpenAPI 规范与文档 UI |
| `PUBLIC_API_LEGACY` | 控制台公共 API 前缀（`ConsolePublicApiPaths.LEGACY_PUBLIC_PATTERN`） |
| `PUBLIC_API_DOMAIN` | 各业务域公共 API（`ConsolePublicApiPaths.DOMAIN_PUBLIC_PATTERN`） |
| `HEALTH = "/health"` | 应用健康检查 |
| `ACTUATOR_HEALTH = "/actuator/health/**"` | Spring Boot Actuator 健康端点 |
| `ROOT = "/"` | 站点根路径（`MainRootEndpoint` 重定向） |

### 方法

#### `defaultPatterns() → List<String>`（static）
- **说明：** 返回默认免登录路径模式（可变副本，供 `ConsoleWebSecuritySettings` 初始化或重置）

## ConsolePublicApiPaths

**类型：** class（final，静态工具）

公共开放 API 路径匹配（Jersey 请求路径）。

### 常量

| 常量 | 说明 |
|------|------|
| `LEGACY_PUBLIC_PATTERN` | `ConsoleConstant.PUBLIC_API_PREFIX` 及其子路径（如 `/api/public/pages/...`） |
| `DOMAIN_PUBLIC_PATTERN = "/api/d/*/public/**"` | `/api/d/{domain}/public` 及其子路径，`domain` 为领域键（如 `nexus`、`platform`、`nexmux`） |

### 方法

#### `matches(String normalizedPath) → boolean`（static）
- **说明：** 是否为公共开放 API 路径
- **参数：**
  - `normalizedPath` — 以 `/` 开头的请求路径

## ConsoleTokenSessionBinder

**类型：** class（final，静态工具）

将访问令牌声明绑定到 `SessionContext`。

### 方法

#### `bindAccessToken(TokenClaims claims)`（static）
- **说明：** 校验 access 用途与过期时间后绑定用户、`TLC.SECURITY_REALM`、租户成员/租户/
  工作区/项目快照（快照 ID 以 claims 中的 ID 充当）
- **参数：**
  - `claims` — 访问令牌声明
- **异常：** `NexusException`（`AUTHENTICATION_FAILED`）— claims 为空、用途不符、过期或 userId 缺失时

#### `clear()`（static）
- **说明：** 清空用户/租户/工作区/项目绑定与 `TLC`（响应过滤器在请求结束时调用）

## ConsoleDevSessionBinder

**类型：** class（final，静态工具）

在关闭请求侧安全时，将配置的 dev 身份绑定到 `SessionContext`。

### 方法

#### `bind(ConsoleWebDevSessionSettings devSession)`（static）
- **说明：** 绑定 dev 用户、`TLC.SECURITY_REALM`、租户成员 ID 及租户/工作区/项目快照；
  `devSession` 为 `null` 时直接返回
- **参数：**
  - `devSession` — 开发会话配置