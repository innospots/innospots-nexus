# innospots-nexus-console

管理控制台的 **API 表面**与**框架中立运行时契约**模块。在 `innospots-nexus-core` 与
`innospots-nexus-plugin` 之上，为租户管理端提供 Jakarta JAX-RS 端点形态、请求/响应 record、
目录索引、权限判定、导航组装、认证/作用域编排，以及内置控制台入口插件。

本模块 **不包含** Spring Boot 自动配置或 Servlet 绑定（归属 Spring/Quarkus 适配与应用装配层）。
用户/成员持久化、租户业务数据等由 **portal** / **platform** 实现控制台定义的端口；插件规范、
贡献解码与 Page DSL 归属 **plugin** 模块。

## 在工程中的位置

```text
innospots-nexus-base
        ↓
innospots-nexus-core
        ↓
innospots-nexus-plugin
        ↓
innospots-nexus-console          ← 本模块
        ↓
innospots-nexus-portal / innospots-nexus-platform（并行）
        ↓
adapter / application（HTTP 运行时绑定）
```

Maven 依赖：`innospots-nexus-core`、`innospots-nexus-plugin`、`jakarta.ws.rs-api`、
`jakarta.transaction-api`。

分层约定（强制）：`endpoint → service → operator → dao`。端点仅使用 JAX-RS；`domain.request` /
`domain.vo` 使用 record；事务注解放在 service/operator 层。

## 功能域说明

| 包 / 域 | 用途 | 说明 |
|--------|------|------|
| **auth** | 跨域认证契约 | 共享 `UserDirectory`、令牌工具与 **平台域** `AuthFacade`；租户域 `TenantAuthFacade`、成员关系与 `/tenant/auth` 请求形状归属 **portal**。 |
| **scope** | 控制台归属解析 | `ConsoleOwnership*` 与 `SessionScopeBinder` 端口；租户 `/tenant/scope` 与 `ScopeFacade` 归属 **portal**。 |
| **credential** | 归属化鉴权凭据 | 统一表 `nx_user_credential`；`CredentialService`（登录 `authenticate` + 生命周期）与 OTP/TOTP 子包。 |
| **catalog** | 控制台目录索引 | 将插件 `console@1` 贡献与 Page DSL 同步到表 `nx_console_catalog_resource`（`ConsoleCatalogSyncService`）；为权限 UI 提供目录树读取（`ConsoleCatalogService`）。启动钩子：`ConsoleCatalogSyncStartupTask`。 |
| **permission** | 授权运行时 | 持久化 `nx_permission_grant`；`PermissionGrantService` 管理角色/组织单元的授权替换；`PermissionVisibilityService` 计算当前用户可见资源；`RequestAuthorizer` 基于目录与授权做 PAGE/DATASOURCE 判定（框架中立，由 adapter 过滤器调用）。REST：授权管理、当前用户权限等。 |
| **navigation** | 运行时导航 | `NavigationMenuAssembler` 结合目录与授权，组装侧边栏用的 `NavigationMenuVo`（与管理端菜单 CRUD 的 `MenuVo` 区分）。REST：`/console/navigation/menus`。 |
| **menu** | 菜单能力（无管理 REST） | 实体 `MenuEntity`、DAO 与领域类型供库内能力预留；运行时侧栏走 **navigation** + **catalog**。不提供 `/console/menus`。内置 `MenuEntryPlugin`。 |
| **role** | 角色与绑定 | 表 `nx_role`、`nx_role_binding`；`service` / `operator` / `endpoint` 均在 console，端点为可继承的具体类。内置 `RoleEntryPlugin`。 |
| **dictionary** | 租户级字典 | `DictionaryType` / `DictionaryItem` Operator、Service 与 REST（TENANT ownership）。内置 `DictionaryEntryPlugin`。 |
| **plugin** | 插件管理 API | `PluginManagementEndpoint`：安装、启用、禁用、重试等，委托 `PluginInstallationManager`（plugin 模块）。内置 `PluginManagementEntryPlugin`。 |
| **logger** | 审计与调用日志 | `nx_audit_log` 实体、调用日志上下文与 `InvocationLogHandler` 管道，记录管理 API 调用轨迹。内置 `LoggerEntryPlugin`。 |
| **entry** | 内置控制台入口 | `ConsoleModuleDescriptor`、`BuiltinConsoleEntryPlugins`：六个必选内置模块的 entry 插件 ID 与 Page/菜单元数据；`src/main/resources/ui-pages/` 下对应 Page DSL YAML。 |

### 内置控制台模块（entry 插件）

宿主必须启用以下插件 ID（见 `BuiltinConsoleEntryPlugins.REQUIRED_PLUGIN_IDS`）：

| 插件 ID | 模块键 | 典型用途 |
|---------|--------|----------|
| `com.innospots.nexus.console.menu` | menu | 菜单配置与管理界面入口 |
| `com.innospots.nexus.console.dictionary` | dictionary | 数据字典维护入口 |
| `com.innospots.nexus.console.logger` | logger | 审计/调用日志查看入口 |
| `com.innospots.nexus.console.permission` | permission | 权限与授权配置入口 |
| `com.innospots.nexus.console.role` | role | 角色与绑定管理入口 |
| `com.innospots.nexus.console.plugin-management` | plugin | 插件安装与生命周期管理入口 |

## REST 路径概览

| 前缀 | 典型能力 |
|------|----------|
| `/console/**` | 目录、插件、导航、角色/菜单/字典管理契约、当前用户权限 |
| `/tenant/auth`、`/tenant/scope` | 租户域注册、登录、刷新、作用域选择 |
| `/platform/auth` | 运维平台域登录（无公开自注册） |

安全域通过 `SecurityRealm` 分离 **PLATFORM** 与 **TENANT**。完整方法表与 record 索引见下方「进一步阅读」。

OpenAPI 在 **`mvn package`** 时由 `smallrye-open-api-maven-plugin` 扫描各 `*.endpoint` 实现类（JAX-RS + `@Operation`）生成，产物打包为 `META-INF/nexus-openapi/innospots-nexus-console.yaml`（`schemaFilename` 与模块 `artifactId` 一致）；运行时 Spring/Quarkus 按模块暴露对应文件，不再动态扫描。Portal、Platform 模块同理，文件名分别为 `innospots-nexus-portal.yaml`、`innospots-nexus-platform.yaml`。

## 边界速查

| 关注点 | 归属模块 |
|--------|----------|
| 插件发现、贡献解码、Page DSL 规范 | `innospots-nexus-plugin` |
| `nx_console_catalog_resource` 索引同步与读取 | **本模块** `catalog` |
| 权限授权存储与 `RequestAuthorizer` | **本模块** `permission` |
| 用户/角色/菜单/字典 **业务工作流**与多数端点 **实现** | `innospots-nexus-portal`（platform 负责运维域数据） |
| JAX-RS Bean 注册、鉴权过滤器、`AuthorizationSubjectResolver` 实现 | adapter / application |

## 进一步阅读

| 主题 | 位置 |
|------|------|
| 模块 API 索引（端点、类表、扩展指南） | [`skills/java/java-reference/references/modules/innospots-nexus-console/README.md`](../skills/java/java-reference/references/modules/innospots-nexus-console/README.md) |
| 仓库模块职责与依赖规则 | [`AGENTS.md`](../AGENTS.md) |
| 插件与 `console@1` 贡献 | [`innospots-nexus-plugin/docs/README.md`](../innospots-nexus-plugin/docs/README.md) |
| Core 持久化基类与启动 SPI | [`innospots-nexus-core/docs/README.md`](../innospots-nexus-core/docs/README.md) |

## 本地验证

在 JDK 满足工程 `release` 要求时：

```bash
mvn -pl innospots-nexus-console clean test
```
