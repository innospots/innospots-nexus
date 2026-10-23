# OpenAPI 契约与开发指南

权威条文：[`standards/openapi.md`](../standards/openapi.md)。

本文件说明**如何在本仓库落地**：注解模板、构建产物、运行时目录、与 console/portal/platform 分工。

---

## 架构概览

```text
开发期：*Endpoint + @Schema record/vo
           ↓  mvn package (smallrye-open-api-maven-plugin, JAX-RS scanner)
构建产物：META-INF/nexus-openapi/innospots-nexus-{console|portal|platform}.yaml
运行期：OpenApiCatalogEndpoint / OpenApiBundledSpecs 提供目录与 YAML 下载
文档 UI：OpenApiScalarDocumentation（Spring/Quarkus 装配路由）
```

- **OpenAPI 3.1**；扫描器仅 **JAX-RS**（`jakarta.ws.rs`）。
- 运行时**不**重新扫描 classpath 生成规范。

模块 API 索引：

- Core 安全注解：[`innospots-nexus-core` → openapi.md](modules/innospots-nexus-core/references/openapi.md)
- Console 目录与 Scalar：[`innospots-nexus-console` → openapi*.md](modules/innospots-nexus-console/README.md)
- 总览：[`innospots-nexus-openapi` 模块索引](modules/innospots-nexus-openapi/README.md)

---

## 模块级定义（模板）

参考 `NexusConsoleOpenApiDefinition`：

```java
@Path("/openapi")
@OpenAPIDefinition(
        info = @Info(
                title = "Innospots Nexus Console API",
                version = "1.0.0",
                description = "…",
                contact = @Contact(name = "Innospots Nexus")
        ),
        tags = {
                @Tag(name = "Role", description = "角色与绑定"),
                // 与端点 @Tag(name) 一致
        }
)
@SecurityScheme(
        securitySchemeName = NexusOpenApiSecurityNames.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "紧凑访问令牌（IDENTITY 或 BUSINESS）"
)
public final class NexusConsoleOpenApiDefinition {
    private NexusConsoleOpenApiDefinition() {
    }
}
```

Portal / Platform 使用各自 `title`、`tags` 与占位 `@Path`（`/tenant`、`/platform`）。

---

## 端点类（模板）

参考 `RoleEndpoint`：

```java
@Path(ConsoleConstant.API_PREFIX + "/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Role", description = "角色与绑定")
@NexusAuthenticatedApi
public class RoleEndpoint {

    @GET
    @Operation(operationId = "rolePage", summary = "分页查询角色")
    public R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request) {
        // ...
    }

    @GET
    @Path("/{roleId}")
    @Operation(operationId = "roleGet", summary = "查询角色详情")
    public R<RoleVo> getRole(
            @Parameter(description = "角色 ID", required = true) @PathParam("roleId") String roleId) {
        // ...
    }
}
```

| 检查项 | 说明 |
|--------|------|
| `operationId` | 稳定、唯一；契约测试与前端生成代码依赖 |
| `@NexusAuthenticatedApi` | 管理 API 默认需要 |
| `@BeanParam` 查询类 | 字段级 `@Schema` + `@QueryParam`（见 `RolePageRequest`） |

### console 契约接口 + platform/portal 实现

当 console 声明 `interface *Endpoint`、业务模块提供实现类时，SmallRye 的构建期 JAX-RS
扫描器**不会继承接口方法注解**。接口保留 `@EndpointModule` / `@EndpointGrant` 作为运行时
授权单一事实源；实现类必须重复传输契约：

```java
// console：契约与 grant 单一事实源
public interface RoleEndpoint {

    @GET
    @EndpointGrant(action = EndpointGrantAction.READ, name = "role.page")
    @Operation(operationId = "rolePage", summary = "分页查询角色")
    R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request);
}

// platform/portal：供 JAX-RS 与 OpenAPI 构建期扫描
@Path(PlatformConstant.ROLES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NexusAuthenticatedApi
public class PlatformRoleEndpoint implements RoleEndpoint {

    @Override
    @GET
    @Operation(operationId = "platformRolePage", summary = "分页查询角色")
    public R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request) {
        // ...
    }
}
```

不得在实现类重复 `@EndpointModule` / `@EndpointGrant`；它们仍由 console 接口解析。

---

## Request / VO Schema（模板）

**Record 请求：**

```java
@Schema(name = "RoleCreateRequest", description = "创建角色请求")
public record RoleCreateRequest(
        @Schema(description = "显示名称", required = true)
        String roleName,
        @Schema(description = "归属范围内唯一的稳定编码", required = true)
        String roleCode
) {
}
```

**Record VO：** 同上，类名 `XxxVo`。

**枚举：**

```java
@Schema(description = "启用/禁用状态", enumeration = {"ENABLED", "DISABLED"})
public enum BasicStatus { ... }
```

**统一响应 `R`：** 在 `innospots-nexus-base` 已定义；端点返回 `R<T>` 时 OpenAPI 展开为 `R` + 嵌套 `T` 的 schema。

---

## Maven 配置要点

**完整说明**（插件版本、两种打包模式、POM 模板、新建模块 checklist、验证命令）见
[`openapi-maven-plugin.md`](openapi-maven-plugin.md)。

摘要（以 `innospots-nexus-console/pom.xml` 为准）：

| 配置项 | 典型值 |
|--------|--------|
| 插件坐标 | `io.smallrye:smallrye-open-api-maven-plugin`（版本由 `innospots-nexus-parent` 管理） |
| execution | `generate-openapi` / `process-classes` / `generate-schema` |
| `outputDirectory` | 模式 A：`…/META-INF/nexus-openapi`；模式 B：`target/generated/openapi` + copy |
| `schemaFilename` | `${project.artifactId}` → `<artifactId>.yaml` |
| `scanners` | `JAX-RS` |
| `scanPackages` | 本模块根包 + 被引用的 `base` / `console` 契约类型包 |
| `scanDependenciesDisable` | `false` |
| `openApiVersion` | `3.1.0` |
| `operationIdStrategy` | `CLASS_METHOD` |
| `infoTitle` / `infoVersion` / … | 与模块 `*OpenApiDefinition` 的 `@Info` 保持一致 |

**扩展 `scanPackages` 时**：只加入必须在文档中出现的**稳定契约类型**；不要扫描整个 `base` 或 `core`。

platform/portal 实现 console 契约时，至少把实际出现在方法签名中的依赖类型包加入
`scanPackages`。例如 platform 引用 console 的 auth、role、permission、dictionary、
logger 请求/VO 时，应显式加入：

```xml
<scanPackages>
    com.innospots.nexus.platform,
    com.innospots.nexus.base.domain.response,
    com.innospots.nexus.base.i18n,
    com.innospots.nexus.base.domain.enums,
    com.innospots.nexus.console.auth.domain,
    com.innospots.nexus.console.dictionary.domain,
    com.innospots.nexus.console.logger.domain,
    com.innospots.nexus.console.menu.domain.vo,
    com.innospots.nexus.console.permission.domain,
    com.innospots.nexus.console.permission.pageaccess,
    com.innospots.nexus.console.role.domain
</scanPackages>
```

portal 当前通过 console auth 契约暴露登录、验证码、令牌刷新等端点，因此其
`scanPackages` 也必须包含 `com.innospots.nexus.console.auth.domain`：

```xml
<scanPackages>
    com.innospots.nexus.portal,
    com.innospots.nexus.console.auth.domain,
    com.innospots.nexus.base.domain.response,
    com.innospots.nexus.base.i18n,
    com.innospots.nexus.base.domain.enums
</scanPackages>
```

若构建日志出现 `SROAP04005: Could not find schema class in index`，说明依赖契约包未纳入扫描；
不能把退化后的 `type: object` 当作完成。

| 模块 | 打包模式 |
|------|----------|
| console、platform | A — 直接写入 `META-INF/nexus-openapi` |
| portal | B — `generated/openapi` + `maven-resources-plugin` `package-openapi` |

---

## 运行时 HTTP（console）

| 路径 | 说明 |
|------|------|
| `GET /openapi/specs` | `R<List<OpenApiSpecItemVo>>` 规范列表 |
| `GET /openapi/specs/{specId}` | `application/yaml` 单文件内容 |

`specId` 与打包文件名对应（如 `innospots-nexus-console`）。

---

## 开发检查清单（`java:develop`）

新增或修改 REST 契约时：

- [ ] 端点类 `@Tag` 已加入 `*OpenApiDefinition.tags`
- [ ] 每个 HTTP 方法有 `@Operation(operationId, summary)`
- [ ] 路径参数有 `@Parameter(description, required = true)`
- [ ] 新建/修改的 request/vo record（或 BeanParam 类）有类级与字段级 `@Schema`
- [ ] 需认证资源类有 `@NexusAuthenticatedApi`
- [ ] console 契约实现类重复类级与方法级 JAX-RS / OpenAPI 传输注解，但不重复 grant
- [ ] 构建期 `scanPackages` 覆盖方法签名引用的依赖契约类型
- [ ] `mvn -pl <module> clean compile`
- [ ] `mvn -pl <module> package` 后检查 `META-INF/nexus-openapi/*.yaml` 是否包含新 operation

设计阶段（`java:design`）须在端点表中列出 **operationId** 与 **Tag**，与实现一致。

---

## 与插件清单区分

| 文档 | 规范入口 |
|------|----------|
| 插件清单 `plugin.yaml` | `innospots-nexus-plugin/docs/plugin/design/plugin-dsl-spec.md` |
| REST OpenAPI | 本文件 + `standards/openapi.md` |
