# 页面操作授权设计

## 归属与边界

平台权限端点归 `innospots-nexus-platform`，通用授权逻辑复用 console 已有服务及 core 的 EndpointGrantManager；Spring 仅负责组装。前端页面转换在 console UI 的 Next.js 服务端完成。依赖方向符合 AGENTS.md，不新增模块、数据库表或基础库公开契约。

## 契约和处理

认证会话调用平台批量检查接口，提交页面 domain/module 和 method、URL 列表；响应包含逐请求布尔结果和页面允许的 grantGroup 集合。仅允许本地 API 路径，按注册的 JAX-RS 路径模板匹配；unknown、歧义、表达式不能安全解析或服务失败均拒绝。公开与仅登录资源复用现有认证及 EndpointGrant 策略。调用者无法指定主体，服务从会话解析。接口本身不授予目标资源权限，实际请求仍走既有安全过滤器。

Next.js 递归扫描页面 DSL、表格单元和内嵌弹层的 request 动作，收集并去重请求；无权限的按钮移除，非按钮事件禁用或移除；多请求任一必要请求拒绝则拒绝组件，未决/动态资源不放行。自定义组件和动作需显式声明请求或 grantGroup 依赖；grants 以页面运行时状态提供给 `when`，不覆盖已有判断。无权限和检查失败分开呈现；敏感页面 DSL 不向匿名用户泄露。

## 测试

验证 POST 角色创建、GET 角色列表、动态路径、公开及仅登录接口、未知路径、主体与作用域、管理员和普通用户；前端验证嵌套弹窗、表格和表单、多请求、请求失败及不可用，保留页面已有条件并使用 grantGroup。以 admin、opt01、user01 端到端验证。无实体、枚举或状态码变更；异常沿用既有契约。

## 页面 DSL 编写约定

页面 state 注入 `grantGroupList`（授权分组数组）和 `grantGroups`（按分组名索引的布尔对象）；例如 `when: ${state.grantGroups.create}`。`when` 不替代请求校验。若组件或自定义 action 在 JavaScript 内部发请求，必须在对应 DSL 节点声明：

```yaml
access:
  requests:
    - { method: POST, url: /api/d/platform/users }
  # 可选：grantGroups: [create]
  # 自定义组件需保留只读内容时：mode: disable（组件须支持 disabled）
```

`access` 仅用于服务端页面构建，返回 DSL 前移除；同一组件声明的必要请求或组有任一拒绝即禁止该组件操作；支持 `disabled` 的自定义组件可用 `mode: disable` 保留只读内容。`${params.id}` 和 `${row.id}` 等独立路径段按模板检查，有歧义时拒绝。公开页面跳过 page-access 和页面操作过滤，其实际请求仍由后端各接口鉴权。

当前已发布页面中，直接声明 `action: request` 的组件类型为 `Button` 和 `Form`；内嵌弹层中的请求也归属触发按钮检查。自定义 React 组件或自定义 action 内的网络请求无法从 `action: request` 自动发现，需要显式声明。管理员、运营人员与成员的请求结果仍以实际后端鉴权为准。

## 合并页面访问与操作检查（2026-11-04）

页面的 `page.access-mode` 为 `public`、`authenticated` 或 `grant`，缺失或无效时拒绝。公开页在 Next.js 直接加载，不调用后端；其他页加载 DSL 时仅调用一次开放的 `/api/public/page-access/verify`，提交 `accessMode`、页面 catalog 定位和 `requests`。接口的调用者不选择主体，会话由服务端解析；authenticated 要求有效主体，grant 还校验页面 catalog 组。响应同时包含 `allowed`、`reason`、`requestPermissions`（method:url 到布尔值）和 `grantGroups`。该结果仅用于页面构建，不授予业务 API 权限。未知或含糊接口、缺失结果、服务异常拒绝；无效页面配置拒绝。浏览器不再单独调用页面访问端点。保留原四参数请求构造器用于已有 Java 消费者，旧 action-access HTTP 端点移除。测试覆盖匿名、认证、授权拒绝、动态请求以及公开页不调用。
