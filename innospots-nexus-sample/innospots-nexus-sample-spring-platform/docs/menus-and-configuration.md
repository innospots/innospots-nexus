# 菜单设置与配置说明

## 1. 三种“菜单”必须分开

| 名称 | 所在位置 | 事实源 | 当前用途 |
|------|----------|--------|----------|
| console catalog | `nx_console_catalog_resource` | ACTIVE 插件的 `console@2` 贡献 | 权限目录和受授权导航 |
| 后端 meta-sitemap | 运行模块 `src/main/resources/console/meta-sitemap.yaml` | 手工维护的 classpath YAML | 导航兜底/默认菜单 |
| 前端 sitemap | UI 工程 `src/server/yaml/sitemap.yaml` | 前端页面包 | 页面、布局和前端导航 |

另外存在 `nx_menu` 表和 `MenuDao`，但当前没有菜单 CRUD Service 或 Endpoint。它目前不是管理台菜单管理功能的可用事实源。

`entryPageKey` 不指向页面文件。只有前端工程中的 `pages[]` 和页面 YAML 才定义实际页面。

## 2. Catalog 菜单

### 2.1 来源

```text
ACTIVE Plugin
  -> ConsolePluginContribution
  -> ConsoleCatalogSyncService
  -> nx_console_catalog_resource
```

叶子 MODULE 和 MENU 都可成为导航节点。目录导航行为：

- 只读取 `ENABLED` 资源。
- 管理员可见所有启用资源。
- 普通用户根据授权裁剪。
- 当前可见性按模块授权键后缀匹配，属于模块级可见性。
- 节点顺序来自 `sortOrder`。

### 2.2 路由

带 `entryPageKey` 的目录节点生成：

```text
/page/{domainKey}/{moduleKey}/{entryPageKey}
```

`NavigationMenuVo` 会携带：

- `menuKey`：通常为 `entryPageKey`
- `routePath`：上述 `/page/...` 路径
- `resourceId`：catalog 资源主键
- `ownerPluginId`
- `moduleKey`
- `pageKey`：当前实现填 `entryPageKey`

前端必须能够处理这条路由，或把目录菜单映射到自己的页面路由。

## 3. 后端 meta-sitemap

### 3.1 加载位置

Spring 配置固定使用：

```text
classpath:console/meta-sitemap.yaml
```

在示例工程中对应：

```text
innospots-nexus-sample-spring-platform/src/main/resources/console/meta-sitemap.yaml
```

发布包会把 `src/main/resources` 复制到 `config/`，启动脚本把 `config/` 放入 classpath，所以发布后对应：

```text
<发行包>/config/console/meta-sitemap.yaml
```

当前没有 `nexus.console.navigation.sitemap-path` 之类的 Spring 配置项，不能只改配置属性切换路径。需要自定义装配时，应替换 `SitemapConfigLoader` Bean，并使用 `SitemapYamlConfig.ofResourcePath(...)`。

### 3.2 文件结构

后端只解析站点元数据和 `menus[]`：

```yaml
id: platform-console
title: Innospots Platform Console
description: 运营管理平台
version: 1
updatedAt: "2026-10-21T00:00:00Z"

menus:
  - id: home
    type: page
    pageId: dashboard
    title: 工作台
    icon: home
    layout: admin
    order: 1

  - id: operations
    type: group
    title: 运营管理
    icon: users
    order: 2
    children:
      - id: nav-users
        type: page
        pageId: platform-user-list
        title: 用户列表
        layout: admin
```

字段：

| 字段 | 适用节点 | 后端行为 |
|------|----------|----------|
| `id` | page/group | 输出为导航 `menuKey`，应保持唯一且稳定 |
| `type` | 全部 | 仅 `page`、`group` 被转换；其他值被忽略 |
| `pageId` | page | 转换为 `NavigationMenuVo.pageKey` |
| `title` | 全部 | 导航显示名 |
| `layout` | page | 后端解析但导航转换不输出 |
| `icon` | 全部 | 导航图标 |
| `order` | 全部 | 当前转换器不排序；输出顺序以 YAML 数组顺序为准 |
| `public` | page | 后端导航过滤不使用，属于前端页面契约 |
| `hidden` | 全部 | 为 `true` 时后端直接剔除 |
| `activeMenu` | page | 后端解析但导航转换不输出 |
| `roles` | 全部 | 与当前用户 `roleId` 精确匹配；空表示不限；管理员放行 |
| `children` | group | 递归过滤和转换 |

后端转换后的 page 节点：

```text
routePath = null
pageKey = pageId
openMode = INTERNAL
```

前端应使用 `pageKey` 或自己的路由映射决定实际跳转，不要期待后端 meta-sitemap 返回 `routePath`。

### 3.3 裁剪规则

`MetaSitemapMenuFilter`：

- `hidden=true`：剔除。
- `roles` 非空：用户至少拥有一个相同 `roleId` 才保留。
- 管理员：绕过 `roles` 限制。
- `group` 的所有子项都被过滤后：整个 group 剔除。

`public` 不参与后端导航过滤。匿名路由和页面公开访问由前端/页面契约处理。

### 3.4 与 catalog 合并

`NavigationMenuComposer` 的规则：

1. catalog 可见菜单在前。
2. meta-sitemap 菜单在后追加。
3. catalog 为空时只返回 meta-sitemap。
4. meta-sitemap 为空时只返回 catalog。
5. 当前不按 `id`、`menuKey` 去重。

因此如果 catalog 和 meta-sitemap 描述同一入口，可能返回两个菜单。新增目录 Entry 后，应及时清理重复的兜底菜单。

## 4. 前端 `sitemap.yaml`

前端文件：

```text
innospots-nexus-ui-console/src/server/yaml/sitemap.yaml
```

根字段通常包括：

```yaml
id: site
resourceType: sitemap
version: 1
dslVersion: "1.0"
app: { name: Nexus Console }
auth: { loginPath: /login }
pages: []
menus: []
layouts: {}
```

后端 meta-sitemap 与前端 `sitemap.yaml` 的 `menus[]` 共享 `id`、`type`、`pageId`、`title`、
`icon`、`order`、`children` 等节点形状，但不是同一个契约：后端额外解析 `roles` 并忽略
`public`，前端 BFF 则使用 `public` / `permission` 做可见性过滤。后端 meta-sitemap 不能替代以下
前端内容：

- `pages[]`：页面 ID、路径、是否公开、是否懒加载。
- `layouts`：Shell、侧栏、Header、Content 等布局 DSL。
- `auth`、`app`、`dslVersion` 等前端运行配置。

实际加载链路：

```text
GET /api/sitemap
  -> app/api/sitemap/route.ts
  -> resolveSessionUser(cookies)
  -> buildSitemap(user)
  -> src/server/yaml/sitemap.yaml
  -> src/server/yaml/**/<pageId>.yaml
```

`buildSitemap()` 会按会话过滤 `pages[]` 和 `menus[]`，注入 `user`、`permissions` 与
`updatedAt`。页面 DSL 文件名必须等于 `pages[].id`；`lazy: true` 时不下发 `pageDsl`，由
`GET /api/public/pages/{id}` 按需加载。

### 4.1 页面声明

```yaml
pages:
  - id: platform-user-list
    path: /ops/platform-users
    title: 用户列表
    version: 1
    public: false
    permission: user:view
    lazy: true
```

页面 DSL 放在：

```text
src/server/yaml/**/<pageId>.yaml
```

页面可见性规则：

| 字段 | 当前 BFF 行为 |
|------|---------------|
| `public: true` | 匿名和已登录用户都可见 |
| `permission` | 当前会话的 `permissions[]` 命中任一权限码才可见 |
| 未设置 `public` / `permission` | 匿名不可见；登录后可见 |
| `lazy` | `true` 时不内联页面 DSL，改由 `/api/public/pages/{id}` 加载 |

`permission` 可以是单个权限码、权限码数组或 `{ code: ... }`。当前权限码来自 UI BFF 的会话构造，
不是后端 `nx_permission_grant` 的自动投影。

### 4.2 前端菜单声明

```yaml
menus:
  - id: operations
    type: group
    title: 运营管理
    icon: users
    order: 2
    children:
      - id: nav-platform-users
        type: page
        pageId: platform-user-list
        title: 用户列表
        layout: admin
        permission: user:view
```

菜单字段：

| 字段 | 前端 BFF 行为 |
|------|---------------|
| `id` | 菜单节点稳定身份 |
| `type` | `page` 或 `group`；`group` 递归处理 `children` |
| `pageId` | 对应 `pages[].id`，前端据此路由和加载页面 DSL |
| `title` / `icon` / `layout` | 前端展示和布局属性 |
| `order` | 菜单顺序属性；BFF 不重排，保留 YAML 数组顺序 |
| `public: true` | 匿名可见；优先于 `permission` |
| `permission` | 登录后仍需命中当前会话权限码 |
| `hidden` | 由前端渲染层决定是否显示；BFF 的 `filterMenus()` 不处理该字段 |
| `activeMenu` | 详情页等隐藏页面用于高亮父菜单 |
| `children` | 子菜单；组内没有可见子项时整组被移除 |

`filterMenus()` 的规则与 `pages[]` 相同：先看 `public`，再看登录状态和 `permission`，最后递归过滤
`group.children`。因此“页面已注册”不代表“菜单自动出现”，`pages[]` 和 `menus[]` 必须分别维护。

当前 UI 的权限码是 BFF 会话中的占位集合（例如 `report:view`、`user:view`），不是
`nx_permission_grant.grantKey`。前端菜单既不会自动读取后端
`/api/d/platform/navigation/menus`，也不会自动读取 `console/meta-sitemap.yaml`。若要让后端授权驱动
前端导航，需要改造 BFF，把后端导航或当前用户权限转换为 `sitemap.yaml` 对应的 `menus[]` /
`permission` 结构。

当前 BFF 的 `resolveSessionUser()` 只检查平台 access-token Cookie 是否存在，不在 BFF 内验证令牌
签名或后端权限。因此前端可见性不是安全边界，不能替代 API 鉴权。新增页面仍必须由后端 Endpoint
的认证和资源授权保护。

## 5. 新增菜单的完整步骤

### 方案 A：插件目录驱动

适用于需要进入权限目录、由后端授权和导航裁剪的模块。

1. 实现或修改 console Entry，声明 MODULE/MENU 与 `entryPageKey`。
2. 注册插件并确认状态为 `ACTIVE`。
3. 启动或手动调用 `POST /api/d/nexus/catalog/sync`。
4. 为角色配置对应的页面授权键。
5. 前端创建页面并处理 `/page/{domain}/{module}/{entryPageKey}` 路由，或做映射。
6. 如有必要，删除 meta-sitemap 中重复的兜底菜单。

### 方案 B：meta-sitemap 兜底

适用于暂未进入插件 catalog 的默认导航。

1. 修改运行模块的 `console/meta-sitemap.yaml`。
2. 确保 `pageId` 与前端 `pages[].id` 一致。
3. 前端创建并注册对应页面。
4. 添加 `roles`/`hidden` 时验证不同角色和匿名场景。
5. 不要把它当作后端权限事实源；它只负责导航展示和简单角色裁剪。

### 方案 C：前端本地菜单

仅适用于纯前端页面，不参与后端 catalog 和导航 API。

1. 修改 UI 工程 `src/server/yaml/sitemap.yaml`。
2. 添加页面 YAML。
3. 为 `pages[]` / `menus[]` 设置 `public`、`permission`，或在 BFF 中接入真实权限投影。

三种方案不要混用到同一个入口上，除非明确接受重复菜单和两套授权语义。

## 6. 配置文件

### 6.1 配置导入

`application.yaml` 默认导入：

```yaml
spring:
  config:
    import:
      - optional:classpath:nexus/console-auth.yaml
      - optional:classpath:nexus/console-registration.yaml
      - optional:classpath:nexus/console-credential.yaml
      - optional:classpath:nexus/console-web.yaml
      - optional:classpath:nexus/console-openapi.yaml
      - optional:classpath:nexus/console-plugins.yaml
```

`optional:` 表示文件不存在时不阻止启动。当前没有把 `optional:file:./config/nexus/...` 放入导入列表；注释只是示例。若需要外部文件覆盖，必须显式加入，例如：

```yaml
spring:
  config:
    import:
      - optional:classpath:nexus/console-web.yaml
      - optional:file:./config/nexus/console-web.yaml
```

后导入的属性文档在相同键上优先。

### 6.2 `application.yaml`

| 配置 | 当前值/说明 |
|------|-------------|
| `spring.profiles.default` | `local` |
| `spring.application.name` | `nexus-platform-console` |
| `spring.jersey.type` | `servlet`，由 Jersey 处理 JAX-RS 和 Scalar 路由 |
| `server.port` | `8082` |
| `management.endpoints.web.exposure.include` | `info,health,metrics,prometheus` |

### 6.3 profiles

| Profile | 数据源 | Schema |
|---------|--------|--------|
| `local` | H2 文件库 `./out/db/nexus-platform-console` | `always`，执行 4 个 H2 脚本 |
| `test` | H2 内存库 | `embedded`，执行 4 个 H2 脚本 |
| `prod` | MySQL | `never`，必须由正式迁移流程维护 |

本地/测试 schema 顺序：

```text
nexus-core-h2.sql
nexus-plugin-h2.sql
nexus-console-h2.sql
nexus-platform-h2.sql
```

生产环境变量：

| 变量 | 用途 |
|------|------|
| `DB_DRIVER` | 默认 `com.mysql.cj.jdbc.Driver` |
| `DB_IP` | 数据库主机 |
| `DB_PORT` | 端口，默认 `3306` |
| `DB_NAME` | 数据库名 |
| `DB_USER` | 用户名 |
| `DB_PASSWORD` | 密码 |
| `DB_TIMEZONE` | 连接时区，默认 `GMT+8` |

示例运行包默认只带 H2 驱动；生产需要自行提供 MySQL JDBC 驱动。

### 6.4 `nexus/console-auth.yaml`

| 键 | 作用 |
|----|------|
| `nexus.console.auth.token-secret` | Token AES 密钥；生产必须替换 |
| `access-token-ttl-seconds` | 访问令牌 TTL，默认 7200 秒 |
| `refresh-token-ttl-seconds` | 刷新令牌 TTL，默认 604800 秒 |
| `login-captcha-enabled` | 登录验证码 |
| `login-max-failed-attempts` | 失败锁定阈值 |
| `login-lock-minutes` | 锁定时长 |
| `rsa.public-key` | 前端 RSA-OAEP 加密公钥 |
| `rsa.private-key` | 后端解密私钥 |

重要：`PlatformAuthConfiguration` 整体受
`nexus.console.auth.rsa.private-key` 条件控制。缺少私钥时，平台认证相关 Bean 和关联 DAO 不会装配，平台登录链路不可用。

### 6.5 `nexus/console-registration.yaml`

| 键 | 作用 |
|----|------|
| `mode` | `OPEN`、`INVITE`、`APPROVAL`、`CLOSED` |
| `otp-verification` | `EMAIL`、`MOBILE`、`NONE` |
| `otp.debug-log-enabled` | 把 OTP 明文写入 INFO 日志，只用于联调 |
| `otp.email.*` | SMTP 地址、账号、密码、发件人、TLS |
| `otp.mobile.*` | 短信网关 URL 和 API Key |

示例值包含默认密码和示例地址，生产必须覆盖。

### 6.6 `nexus/console-credential.yaml`

```yaml
nexus:
  console:
    credential:
      totp:
        master-key: change-me-in-production
```

生产必须配置 TOTP 主密钥，并使用密钥管理系统注入。

### 6.7 `nexus/console-web.yaml`

| 键 | 作用 |
|----|------|
| `nexus.console.web.enabled` | 是否注册控制台 Filter、CORS 和异常映射 |
| `security.enabled` | Bearer 认证和资源权限总开关 |
| `security.dev-session.*` | 安全关闭时可选的固定会话 |
| `security.permit-all-patterns` | 完全跳过 Bearer 校验的 Ant 路径 |
| `security.console-path-patterns` | 执行资源授权 Filter 的 Ant 路径，默认 `/api/d/**` |
| `cors.enabled` | 是否输出 CORS 头 |
| `cors.allowed-origins` | 允许来源 |
| `cors.allow-credentials` | 是否允许 Cookie；启用时来源不能使用 `*` |

生产基线：

```yaml
nexus:
  console:
    web:
      security:
        enabled: true
        dev-session:
          enabled: false
      cors:
        allowed-origins:
          - https://console.example.com
```

### 6.8 `nexus/console-openapi.yaml`

绑定 `scalar.*`：

| 键 | 默认值 | 作用 |
|----|--------|------|
| `scalar.specs-base` | `/openapi/specs` | 规范 JSON 根路径 |
| `scalar.path` | `/openapi/ui` | Scalar HTML 页面 |
| `scalar.script-segment` | `scalar.js` | 页面脚本段 |
| `scalar.enabled` | `true` | 是否注册文档 UI |
| `scalar.page-title` | `Nexus API Reference` | 页面标题 |
| `scalar.layout` | `modern` | Scalar 布局 |
| `scalar.theme` | `default` | Scalar 主题 |
| `scalar.hide-test-request-button` | `false` | 是否隐藏 Try it |
| `scalar.persist-auth` | `true` | 是否本地保存鉴权 |

修改 `specs-base` 时必须同时保证它与 `OpenApiCatalogEndpoint` 的路径一致；当前端点路径是固定常量。

### 6.9 `nexus/console-plugins.yaml`

| 键 | 作用 |
|----|------|
| `nexus.plugin.auto-install` | 首次发现是否自动安装 |
| `nexus.plugins.required` | 启动必须 ACTIVE 的插件 ID |
| `nexus.plugins.disabled` | 禁止自动启动的插件 ID |
| `plugins.<pluginId>.<key>` | 插件运行配置 |

示例：

```yaml
nexus:
  plugins:
    required:
      - com.innospots.nexus.platform.console
    disabled: []

plugins:
  com.example.message-wecom:
    corpId: ww-demo
```

### 6.10 发行包

assembly 把运行模块 `src/main/resources` 全部复制到：

```text
<发行包>/config/
```

启动脚本设置：

```text
CLASSPATH=<发行包>/lib/*:<发行包>/config
-Dspring.config.additional-location=optional:file:<发行包>/config/
```

因此：

- `config/nexus/*.yaml` 可被 `classpath:nexus/*.yaml` 导入。
- `config/console/meta-sitemap.yaml` 可被固定 classpath 路径读取。
- `config/schema/*.sql` 仍作为 classpath 资源保留。
- 发布前不要手工修改 JAR 内的 YAML；优先替换发布包 `config/` 下文件。

## 7. 配置覆盖优先级

通用顺序从低到高：

```text
代码默认值
-> application.yaml
-> profile 文件
-> spring.config.import 的后续文件
-> 外部 config/ 文件或 additional-location
-> 环境变量
-> JVM 系统属性
-> 命令行参数
```

插件配置还有自己的优先级：

```text
schema 默认值
-> 宿主配置
-> ConfigSource
-> NEXUS_PLUGIN_* 环境变量
-> JVM 系统属性
-> runtimeVariables
```

## 8. 配置与菜单检查表

- [ ] 新增配置文件已加入 `spring.config.import`。
- [ ] profile 的数据源与 schema 初始化策略匹配。
- [ ] 生产已配置 MySQL 驱动和正式迁移脚本。
- [ ] 生产已替换 Token 密钥、RSA 密钥和 TOTP 主密钥。
- [ ] `security.enabled=true`，`dev-session.enabled=false`。
- [ ] `required` 插件均已部署，可选插件未误设为 required。
- [ ] catalog 与 meta-sitemap 没有重复菜单。
- [ ] meta-sitemap 的 `pageId` 在前端 `pages[]` 中存在。
- [ ] 前端路由能处理 catalog `/page/...` 或完成显式映射。
- [ ] 修改配置后已在目标 profile、发行包和网关链路下验证。

