# OpenAPI 与 MicroProfile 注解规范

本文件管辖 **Jakarta REST 端点**在 **构建期 OpenAPI 3.1** 文档中的注解契约。
运行时以 `META-INF/nexus-openapi/*.yaml` 为准，**不在运行时动态扫描**端点生成规范。

技术栈：**MicroProfile OpenAPI**（`org.eclipse.microprofile.openapi.annotations.*`）+
**SmallRye Open API Maven Plugin**（`io.smallrye:smallrye-open-api-maven-plugin`）。

执行参考：注解与检查清单 [`references/openapi-contract.md`](../references/openapi-contract.md)；
对外 REST 模块 Maven 插件 [`references/openapi-maven-plugin.md`](../references/openapi-maven-plugin.md)。

---

## 1. 适用范围

| 适用 | 不适用 |
|------|--------|
| `innospots-nexus-console`、`innospots-nexus-portal`、`innospots-nexus-platform` 的 `*Endpoint` | 用 Spring MVC 注解描述 API |
| `domain.request` / `domain.vo` / 共享 `R`、`PageResult` 等出现在 OpenAPI schema 的类型 | 在 base/core 绑 Spring OpenAPI |
| `innospots-nexus-core` 的 `@NexusAuthenticatedApi`、`NexusOpenApiSecurityNames` | 把 OpenAPI 注解写在 `operator` / `dao` |

`innospots-nexus-base` 中**已进入扫描包列表**的共享类型（如 `R`、`PageResult`、`BasicStatus`）必须带 `@Schema`。

`innospots-nexus-plugin` 不配置 SmallRye 插件；被 console OpenAPI 引用的插件枚举（如 `PluginPresence`）
须在插件模块标注 `@Schema`，并在 console `scanPackages` 中包含
`PluginOpenApiSupport.INSTALLATION_DOMAIN_ENUMS` 对应包名。

---

## 2. 模块级 `@OpenAPIDefinition`

每个发布 REST 契约的 Maven 模块**必须**有一个 `*OpenApiDefinition` 类：

| 模块 | 类 | 占位 `@Path` |
|------|-----|----------------|
| console | `NexusConsoleOpenApiDefinition` | `/openapi` |
| portal | `NexusPortalOpenApiDefinition` | `/tenant` |
| platform | `NexusPlatformOpenApiDefinition` | `/platform` |

### 2.1 必须

- 类为 `public final`，**仅承载注解**，私有构造器，**不得**作为 JAX-RS 资源暴露业务方法。
- 必须带 **`@Path(...)`**，以便 JAX-RS 扫描器拾取 `@OpenAPIDefinition`（与 SmallRye 约定一致）。
- `@OpenAPIDefinition` 内 `@Info`：`title`、`version`、`description`、`contact` 与模块 POM 中
  `smallrye-open-api-maven-plugin` 的 `infoTitle` / `infoVersion` / `infoDescription` **保持一致**。
- `@OpenAPIDefinition` 内 `tags = { @Tag(...) }`：**预声明本模块所有端点将使用的 Tag 名称**（与端点类上 `@Tag(name)` 一致）。
- `@SecurityScheme`：`securitySchemeName` **必须**为 `NexusOpenApiSecurityNames.BEARER_AUTH`（`bearerAuth`），
  `type = HTTP`，`scheme = bearer`，`bearerFormat = JWT`。

### 2.2 禁止

- 使用模块内自定义的安全方案名（除非经平台级设计变更并同步 BOM + 全部 `*OpenApiDefinition`）。
- 将 `@OpenAPIDefinition` 挂在真实 `@Path` 业务 Endpoint 上（元数据类独立）。

---

## 3. 端点类（`*Endpoint`）

### 3.1 类级注解（顺序建议）

1. `@Path` — 使用模块常量（如 `ConsoleConstant.API_PREFIX + "/roles"`），禁止魔法字符串散落。
2. `@Produces(MediaType.APPLICATION_JSON)` / `@Consumes(...)`（写操作 JSON 体）。
3. `@Tag(name = "...", description = "...")` — `name` **必须**出现在同模块 `*OpenApiDefinition.tags` 中。
4. `@NexusAuthenticatedApi` — **需要 Bearer 的管理 API**（绝大多数 `/api/nexus/**` 控制台路由）。
5. 例外：公开探测、OpenAPI 目录等**明确无需登录**的资源不得加 `@NexusAuthenticatedApi`。

### 3.2 方法级

每个对外 HTTP 方法**必须**：

| 注解 | 规则 |
|------|------|
| `@Operation` | **必须**；`operationId` 全局唯一、稳定、camelCase，建议 `{资源}{动作}`（如 `rolePage`、`roleGet`）；`summary` 简短中文或英文业务描述 |
| `@Parameter` | **路径参数**、文档需要说明的 **查询参数**（尤其非 BeanParam 单参时） |

当前仓库**不强制** `@APIResponse` / `@RequestBody`；响应形状由返回类型 `R<T>` 与泛型参数推导。
若后续引入 `@APIResponse`，须在同一模块内统一，不得混用风格。

### 3.3 `operationId` 与 Maven

模块 POM 使用 `operationIdStrategy` **`CLASS_METHOD`** 时，仍应显式写 `@Operation(operationId = "...")`
以保持跨模块稳定 ID；**禁止**依赖隐式默认名而不写 `operationId`（新端点一律显式）。

### 3.4 禁止

- Spring MVC 文档注解（`io.swagger.v3.oas.annotations` 仅在与 MP 对齐且 BOM 允许时；**默认只用 MicroProfile**）。
- 无 `@Operation` 的 public JAX-RS 方法（测试用的 package-private 除外）。

---

## 4. 请求与视图类型（Schema）

### 4.1 `domain.request`（record）

- 类型上 **`@Schema(name = "XxxRequest", description = "...")`**。
- **每个 record 组件**上 **`@Schema(description = "...", required = true/false)`**；
  有示例时加 `examples = {"..."}`（单元素数组）。
- 枚举组件使用已带 `@Schema` 的枚举类型，或组件上 `description` 说明取值。

### 4.2 `domain.request`（BeanParam 类）

分页/查询 **Bean 类**（非 record，兼容 `@BeanParam`）：

- 类上 `@Schema(name, description)`。
- 每个字段：`@QueryParam` / `@PathParam` + **`@Schema(description)`**；
  默认值用 `@DefaultValue`，与 `@Schema` 描述一致。

### 4.3 `domain.vo`（record）

- 类上 `@Schema(name = "XxxVo", description = "...")`。
- 每个组件 `@Schema(description, required = ...)`；集合组件 `required = true` 时表示响应中始终存在（可为空列表）。

### 4.4 实体与其它 POJO

- **默认**：Entity **不**进入 OpenAPI（不加入 `scanPackages`）。
- 若某类型必须出现在 schema 中，改为 **vo/request record** 或在设计中标为 DTO 并加入扫描包 — 须 `java:design` 记录。

### 4.5 共享类型（base）

| 类型 | 要求 |
|------|------|
| `R<T>` | 类与组件均已 `@Schema`；失败时 `data` 可为 null 须在 description 说明 |
| `PageResult<T>` | 类与分页字段 `@Schema`，`minimum` 等与校验一致 |
| `BasicStatus` 等领域枚举 | `@Schema(description, enumeration = {...})` |

### 4.6 禁止

- 无 `description` 的 `@Schema` 裸注解（除极简单内部测试类型）。
- 在 `@Schema` 中暴露内部表名、敏感字段含义（密码明文等）；登录类字段用「前端加密」等表述。

---

## 5. 安全与 Tag 命名

| 项 | 约定 |
|----|------|
| Bearer 方案名 | 仅 `bearerAuth`（`NexusOpenApiSecurityNames.BEARER_AUTH`） |
| 需登录资源类 | `@NexusAuthenticatedApi`（继承 `@SecurityRequirement(name = "bearerAuth")`） |
| Tag `name` | PascalCase 或约定域前缀：`Role`、`TenantAuth`、`OpenApiCatalog` |
| Tag 与 Definition | 新增 Tag 时**同时**改 `*OpenApiDefinition` 与端点类 |

---

## 6. 构建与校验（开发交付）

| 步骤 | 要求 |
|------|------|
| 改端点 / request / vo | 同步补全 `@Operation` / `@Schema` / `@Parameter` |
| 编译 | `mvn clean compile` |
| 生成规范 | `mvn -pl <module> package`（`process-classes` 阶段 `generate-schema`） |
| 产物 | `META-INF/nexus-openapi/<artifactId>.yaml` 进入模块 JAR |
| 契约测试 | 模块内 `*OpenApiDefinition*` / 端点契约测试保持通过 |

对外暴露 REST 的 Maven 模块须配置 `smallrye-open-api-maven-plugin`；版本、POM 模板、
console/portal/platform 差异与验证命令见
[`references/openapi-maven-plugin.md`](../references/openapi-maven-plugin.md)。
新增 reactor 模块时另须走 `java:project` 与模块职责约定。

---

## 7. 与其它规范的关系

- REST 路径、`R<T>`、分层：[`code-style.md`](code-style.md) REST 节、[`api-design.md`](api-design.md)。
- 契约设计流程：[`references/api-contract.md`](../references/api-contract.md)。
- 专题与示例：[`references/openapi-contract.md`](../references/openapi-contract.md)。
