# 控制台认证与授权安全待办

## 审查范围与状态

以下问题来自对 `hojoai-aiomi` 的静态审查记录，尚未通过运行时验证或修复。引用路径指向原审查项目，不表示本仓库中存在相同实现。核心授权模型与设计文档基本一致，但目前不能判定权限功能安全通过；未发现普通用户仅持有 `view/update` 即可直接调用 `create/delete` 的明确静态路径。

## 待处理问题

- [ ] **P0 — 禁止使用公开的默认令牌密钥。** `AuthConfig` 和示例配置使用公开常量 `0123456789abcdef`，生产 profile 未覆盖。掌握密钥即可伪造包含用户、安全域、租户及 workspace 的管理员令牌。生产环境应外置随机密钥，未配置或使用默认值时禁止启动。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/config/AuthConfig.java:19`、`:24`；`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-sample/hojoai-aiomi-sample-console/src/main/resources/aiomi/console-auth.yaml:14`。

- [ ] **P1 — 禁用角色必须撤销权限。** 主体解析只读取角色绑定 ID，未校验 `hj_role.status`；管理员判断只检查 `administrator=true`，禁用的管理员角色仍可能全量放行。角色解析和管理员判定应过滤 `ENABLED` 状态，并验证授权查询不会继续信任已禁用角色。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/permission/authorization/SessionAuthorizationSubjectResolver.java:52`、`:97`；同目录 `ConsoleResourcePermissionAuthorizer.java:151`。

- [ ] **P1 — 修正非 PLATFORM 用户的角色绑定标识。** 令牌绑定将 opaque 用户 ID 放在 `UserSnapshot.userName()`，解析器却仅在 PLATFORM 使用该 ID，其他场景回退到数值哈希。TENANT/WORKSPACE 角色绑定可能无法命中，主要表现为权限失效。应核对绑定保存的 `TUS...` 或租户用户主键，并统一解析。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/jaxrs/support/ConsoleTokenSessionBinder.java:41`；`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/permission/authorization/SessionAuthorizationSubjectResolver.java:79`。

- [ ] **P1 — 将组织单元加入授权主体。** 解析器始终传入空 `orgUnitIds`，导致 `ORG_UNIT` 授权无法参与鉴权；设计要求其与 `ROLE` 授权取并集。应加载有效组织单元并验证并集语义。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/permission/authorization/SessionAuthorizationSubjectResolver.java:62`；`/Users/yxy/works/hojoai/hojoai-aiomi/docs/design/console-authorization-implementation.md:114`。

- [ ] **P1 — 修正 refresh 的 Filter 链。** `refresh` 仅声明 `ignoreAuth=true`，资源权限 Filter 仍要求 workspace 和 catalog 授权；默认或示例配置下，无 Bearer 调用 `/api/d/platform/auth/refresh` 会失败。应显式设置 `ignorePermission=true`，由请求体中的 refresh token 完成鉴权。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/auth/endpoint/AuthSessionEndpoint.java:35`；`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/jaxrs/filter/ConsoleResourcePermissionFilter.java:54`。

- [ ] **P1 — 明确并落实禁用用户的停权时效。** 认证 Filter 只解析并校验令牌，未重新查询用户状态；默认 access token TTL 为 7200 秒，禁用用户的旧令牌最长仍可使用约两小时。如要求即时停权，应引入用户会话版本、token version 或状态检查。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/jaxrs/filter/ConsoleAuthenticationFilter.java:54`；`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/config/AuthConfig.java:29`。

- [ ] **P2 — 防止资源方法漏声明授权。** `/api/d/**` 方法缺少 `@EndpointGrant` 时，资源 Filter 默认只认证、不授权，容易静默降级。增加全量契约测试，要求每个资源方法显式声明授权或显式跳过。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/jaxrs/filter/ConsoleResourcePermissionFilter.java:54`。

- [ ] **P2 — 按安全域过滤权限目录和资源可见性。** `permissionCatalog()` 与 `PermissionVisibilityService.visible()` 读取启用资源时未过滤 `security_realm`，且 `permission-catalog` 设置了 `ignorePermission=true`，可能泄露跨平台/租户的目录元数据。应按当前安全域过滤并覆盖跨域场景。
  - 依据：`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/permission/service/PermissionVisibilityService.java:71`；`/Users/yxy/works/hojoai/hojoai-aiomi/hojoai-aiomi-console/src/main/java/com/hojoai/aiomi/console/catalog/endpoint/ConsoleCatalogEndpoint.java:55`。

## 建议处理顺序与验证

1. 移除默认令牌密钥，生产未配置时直接启动失败。
2. 角色解析和授权查询过滤 `ENABLED`，管理员角色同样过滤。
3. 修正非 PLATFORM opaque 用户 ID 解析，补齐 `ORG_UNIT` 主体加载。
4. 给 refresh 增加 `ignorePermission=true`，由 refresh token 自身鉴权。
5. 明确禁用用户的停权时效，处理资源方法授权声明与安全域过滤。
6. 增加集成测试：仅有 view 权限访问写接口返回 403、禁用角色、禁用用户、ORG_UNIT 授权、refresh Filter 链以及跨安全域 catalog；同时增加资源方法授权声明契约测试。

## 已观察到符合设计的行为

- 资源键采用 `domainKey.moduleKey:grantName`，操作项键或分组键任一命中即放行。
- PLATFORM 会话允许无 workspace，非 PLATFORM 要求 workspace；`ignoreAuth` 与 `ignorePermission` 语义分离。
- 公共路径优先处理，携带无效 Bearer 不会降级为匿名。
- 授权保存校验 ownership、catalog 键、重复键及长度，并在事务内全量替换。
- 前端 page-access 和 action-access 基本采用失败关闭策略。
