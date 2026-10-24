# Innospots Nexus 二次开发与对接指南

本文档面向两类读者：

- 需要在 `innospots-nexus-platform` 之上增加管理台、对外开放 API、数据表、菜单和权限的二次开发者。
- 需要把 Nexus 平台接入前端、网关、运维环境或自有业务系统的对接人员。

文档以当前仓库代码为准，不把设计稿中的“计划能力”写成已实现能力。

## 文档导航

| 文档 | 内容 |
|------|------|
| [engineering-structure.md](engineering-structure.md) | Maven reactor、平台模块职责、示例扩展包结构、依赖方向和接入步骤 |
| [entry-and-resources.md](entry-and-resources.md) | console Entry 的定义、Java SPI/YAML 引入方式、资源目录与新增资源的位置 |
| [permissions-and-openapi.md](permissions-and-openapi.md) | 认证与授权链路、授权键、页面权限现状、OpenAPI 构建和访问方式 |
| [menus-and-configuration.md](menus-and-configuration.md) | catalog、meta-sitemap、前端 `sitemap.yaml` 的区别和全部配置说明 |

## 先理解四条主线

```text
console@2 Entry
  -> 插件贡献 MODULE / MENU
  -> nx_console_catalog_resource
  -> 权限目录 + 导航菜单

@EndpointModule + @EndpointGrant
  -> 模块归属与操作授权
  -> nx_permission_grant
  -> HTTP 资源权限与审计

@Path + @Operation + @Schema
  -> 构建期 SmallRye OpenAPI
  -> META-INF/nexus-openapi/<artifactId>.yaml
  -> /openapi/specs 与 /openapi/ui

application*.yaml + nexus/console-*.yaml
  -> Spring 宿主配置
  -> 安全、认证、注册、插件、OpenAPI、数据源
```

`entryPageKey` 只是稳定的页面入口身份，不是页面文件，也不是前端组件。Entry 不会自动创建页面、REST 实现或数据库表。

## 当前示例工程

当前可运行平台入口是：

```text
innospots-nexus-sample/innospots-nexus-sample-spring-platform
```

主类：

```text
com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer
```

它只直接依赖 `innospots-nexus-spring-platform` 和 H2：

```text
innospots-nexus-spring-platform
  -> innospots-nexus-platform
  -> innospots-nexus-console
  -> innospots-nexus-plugin
  -> innospots-nexus-core
  -> innospots-nexus-base
```

同级的 `innospots-nexus-sample-platform` 是**无 Spring 的扩展样板库**，展示了
`core`、`console`、`inbound` 三个交付面。当前 `sample-spring-platform` **没有依赖它，也没有装配它的 Endpoint、DAO 或 Service**。阅读时要把它当作结构参考，而不是已经生效的业务代码。

## 本地启动

前置条件：

- JDK 25 或项目要求的更高版本。
- Maven。

在仓库根目录构建相关模块：

```bash
mvn -pl :innospots-nexus-sample-spring-platform -am clean package -DskipTests
```

推荐在 IDE 中直接运行 `SamplePlatformConsoleServer`。默认 profile 为 `local`，端口为 `8082`。

启动后可访问：

| 地址 | 用途 |
|------|------|
| `http://localhost:8082/openapi/ui` | Scalar OpenAPI 文档页 |
| `http://localhost:8082/openapi/specs` | 已打包规范列表 |
| `http://localhost:8082/openapi/specs/innospots-nexus-platform` | 平台 OpenAPI JSON |
| `http://localhost:8082/api/d/platform/navigation/menus` | 当前用户可见导航菜单 |
| `http://localhost:8082/actuator/health` | 健康检查 |

当前示例配置关闭了请求侧安全并启用了固定 dev session：

```yaml
nexus.console.web.security.enabled: false
nexus.console.web.security.dev-session.enabled: true
```

这只适合本地联调。生产环境至少要做到：

1. 设置 `nexus.console.web.security.enabled: true`。
2. 设置 `dev-session.enabled: false`。
3. 替换 `console-auth.yaml` 中的示例密钥和 RSA 密钥。
4. 配置 `nexus.console.credential.totp.master-key`。
5. 配置生产数据库驱动和迁移脚本，不能依赖 H2 schema 脚本。

## 二次开发最小闭环

新增一个业务能力时，按下面顺序落地：

1. 在 `core.<domain>` 定义实体、DAO、Operator、Service 和领域模型。
2. 在 `console.<domain>` 或 `inbound.<domain>` 定义 REST 边界，并按交付面调用 `core`。
3. 给管理端契约声明 `@EndpointModule` 和 `@EndpointGrant`。
4. 需要出现在权限目录和导航时，增加或修改 `console@2` Entry。
5. 需要前端导航兜底时，修改宿主工程 `src/main/resources/console/meta-sitemap.yaml`。
6. 前端页面在 UI 工程的 `sitemap.yaml` 与页面 YAML 中注册。
7. 增加 SQL、配置、OpenAPI 扫描和集成测试。

不要只增加 Entry 就期待页面可用，也不要只增加 `sitemap.yaml` 就期待后端授权生效。Entry、页面、权限和 REST 是四个独立契约。

