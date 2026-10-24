# 权限设置与 OpenAPI

## 1. 认证与授权不是同一件事

| 层次 | 解决的问题 | 主要组件 |
|------|------------|----------|
| 认证 | 请求者是谁，Bearer Token 是否有效 | `ConsoleAuthenticationFilter`、`TokenIssuer` |
| 资源授权 | 当前主体是否有权调用这个 Endpoint | `ConsoleResourcePermissionFilter`、`ConsoleResourcePermissionAuthorizer` |
| 页面访问 | 当前主体是否可打开某个管理页面 | `GrantGroupPageAccessVerifier` |
| 导航裁剪 | 当前主体能看到哪些菜单 | `PermissionVisibilityService`、`NavigationMenuAssembler` |
| 审计 | 是否记录本次管理操作 | `ConsoleAuditLogFilter`、`AuditLogRecorder` |

公共 API、登录和 OpenAPI 默认由路径规则跳过认证；资源授权则由 `@EndpointGrant` 和 `console-path-patterns` 决定。

## 2. 权限元数据

### 2.1 声明位置

| 资源形态 | `@EndpointModule` | `@EndpointGrant` |
|----------|-------------------|------------------|
| console 契约接口 | 接口类上声明 | 每个 HTTP 方法声明 |
| 实现 console 契约的 platform/portal 类 | 不重复 | 不重复 |
| platform/portal 自有 REST 类 | 实现类上声明 | 每个 HTTP 方法声明 |
| 匿名 API | 模块级可设 `ignoreAuth = true` | 方法级按需设 `ignorePermission = true` |

实现 console 契约时仍必须重复传输注解：

- `@Path`
- `@GET`、`@POST` 等 HTTP 动词
- `@PathParam`、`@QueryParam`、`@BeanParam`
- `@Operation`、`@Parameter`
- `@NexusAuthenticatedApi`

原因是构建期 SmallRye JAX-RS 扫描不会把接口方法注解投影到实现类。只写接口会导致运行时授权正常，但 OpenAPI 缺路径或 schema。

### 2.2 最小示例

console 契约：

```java
@EndpointModule("announcement")
@Path("/api/d/nexus/announcements")
@NexusAuthenticatedApi
public interface AnnouncementEndpoint {

    @POST
    @EndpointGrant(
            action = EndpointGrantAction.CREATE,
            name = "announcement.create",
            label = @I18nLiteral(pairs = {
                    "zh-CN", "创建公告",
                    "en", "Create announcement"
            }),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "announcementCreate", summary = "创建公告")
    R<AnnouncementVo> create(AnnouncementCreateRequest request);
}
```

platform 实现：

```java
@Path("/api/d/platform/announcements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformAnnouncement")
@NexusAuthenticatedApi
public class PlatformAnnouncementEndpoint implements AnnouncementEndpoint {

    @Override
    @POST
    @Operation(operationId = "platformAnnouncementCreate", summary = "创建公告")
    public R<AnnouncementVo> create(AnnouncementCreateRequest request) {
        // ...
    }
}
```

不要为了“保险”在实现类复制 `@EndpointModule` 或 `@EndpointGrant`。

### 2.3 字段选择

- `name` 使用稳定业务名，推荐 `domain.resource.operation`。
- `action` 选择最具体的动作，不要所有写操作都用 `WRITE`。
- `recordArgs = false` 用于密码、令牌、OTP、邀请码和敏感负载。
- `auditLog = true` 只用于需要长期留痕的管理动作。
- `ignoreAuth = true` 只跳过登录解析。
- `ignorePermission = true` 只跳过资源授权。
- 匿名且无需权限的接口通常两者都要处理，或由公共路径规则排除。

## 3. 授权键

统一格式：

```text
<grantName>:<domainKey>.<moduleKey>
```

示例：

```text
platform.user.page:platform.user
```

`entryPageKey` 用于目录节点身份和 `/page/...` 路由，不用于拼授权键。当前权限目录的访问项由
MODULE 下的只读 Endpoint Grant 组成，`grantName` 来自 `@EndpointGrant.name`：

```text
@EndpointGrant(action = READ, name = "platform.user.page")
-> platform.user.page:platform.user
```

操作项由可变更/执行类 Endpoint Grant 组成，同样使用 `@EndpointGrant.name`：

```text
@EndpointGrant(action = CREATE, name = "platform.user.create")
platform.user.create:platform.user
```

分类规则：

- `READ`、`EXPORT` 进入访问侧（`readOnly`）。
- 其他动作进入操作侧（`operable`）。
- `CatalogPermissionCatalogAssembler` 当前把所有项目放入 `default` 分组；即使 Entry 声明了其他
  `viewGroups` / `operationGroups`，这些组当前也不会形成独立的权限项集合。

管理员主体绕过普通资源授权。普通主体通过角色或组织单元获得授权，授权记录保存在 `nx_permission_grant`。

## 4. 运行时过滤链

```text
ConsoleResolvedEndpointGrantFilter    AUTHENTICATION - 100
  -> 解析 @EndpointModule / @EndpointGrant

ConsoleAuthenticationFilter          AUTHENTICATION
  -> 校验 Bearer Token 或跳过公共路径

ConsoleDevSessionFilter              AUTHENTICATION + 10
  -> 仅在 security.enabled=false 且 dev-session.enabled=true 时注入固定会话

ConsoleResourcePermissionFilter      AUTHENTICATION + 100
  -> 命中 /api/d/** 且 grant 未 ignorePermission 时查授权

ConsoleAuditLogFilter                USER + 100
  -> grant.auditLog=true 时记录审计
```

`ConsoleResourcePermissionFilter` 的判定步骤：

1. 从请求读取 `ResolvedEndpointGrant`。
2. 通过 `moduleKey` 在 catalog 中定位唯一的启用 MODULE，取得 `domainKey`。
3. 组装 `grantName:domainKey.moduleKey`。
4. 查询当前 workspace、角色和组织单元的授权记录。
5. 未授权返回 `NO_PERMISSION`，授权通过后写入请求授权上下文。

## 5. 权限接口

平台示例常用接口：

| 方法与路径 | 用途 |
|------------|------|
| `GET /api/d/nexus/catalog/permission-catalog` | 权限设置页的 MODULE/MENU 与可授权项 |
| `POST /api/d/nexus/catalog/sync` | 手动同步活动插件贡献 |
| `GET /api/d/platform/roles/{roleId}/permissions` | 查询角色授权 |
| `PUT /api/d/platform/roles/{roleId}/permissions` | 全量替换角色授权 |
| `GET /api/d/platform/organization-units/{unitId}/permissions` | 查询组织单元授权 |
| `PUT /api/d/platform/organization-units/{unitId}/permissions` | 全量替换组织单元授权 |
| `GET /api/d/platform/me/permissions` | 当前平台用户可见权限资源 |
| `POST /api/public/page-access/verify` | 页面访问校验 |

替换请求中的 `grants[].resourceKey` 必须是 `permission-catalog` 中存在的授权键。当前只支持 `grantGroup=default`，其他分组失败关闭。

### 5.1 当前页面权限限制

当前 `GrantGroupPageAccessVerifier` 的实际行为是：

1. 校验请求中的 `domainKey`、`moduleKey`、`pageKey` 非空。
2. 不按 `pageKey` 查询具体的叶子 MODULE/MENU，也不校验它是否等于 `entryPageKey`。
3. 只要主体的任一授权键以 `:<domainKey>.<moduleKey>` 结尾，就认为页面可访问。
4. 管理员直接通过；非 `default` 分组拒绝。

`PermissionVisibilityService` 的导航裁剪也使用模块后缀匹配。因此当前实现更接近**模块级页面授权**，不是严格的页面级隔离。

影响：

- 同一模块下的页面不能仅靠当前授权键实现互相隔离。
- 操作授权和页面授权如果落在同一模块，当前后缀匹配无法可靠区分用途。
- 页面访问校验不会根据 `pageKey` 或 `entryPageKey` 找到唯一的权限项。
- 设计文档中“按 `entryPageKey` 精确校验页面”的目标尚未在当前实现中完成。

在实现精确页面校验前，不应向业务方承诺“同模块不同页面可独立授权”。高风险页面仍应在服务层增加业务校验。

## 6. 本地 dev session

当前示例：

```yaml
nexus:
  console:
    web:
      security:
        enabled: false
        dev-session:
          enabled: true
          user-id: "1"
          tenant-id: dev-tenant
          project-id: dev-project
          realm: PLATFORM
          session-scope: PLATFORM
```

注意：

- `security.enabled=false` 时认证和资源权限 Filter 都不执行。
- `dev-session` 只在安全关闭时生效。
- `realm` 和 `session-scope` 必须与目标域一致；平台示例使用 `PLATFORM`。
- 生产必须关闭 dev session，并启用安全。

## 7. OpenAPI 构建

### 7.1 产物

构建期使用 SmallRye 和 JAX-RS scanner：

```text
META-INF/nexus-openapi/<artifactId>.yaml
```

当前已有：

```text
innospots-nexus-console.yaml
innospots-nexus-platform.yaml
（启用 portal 时）innospots-nexus-portal.yaml
```

运行时只读取打包 YAML，不重新扫描端点。

### 7.2 Maven 配置

以 platform 为例：

```xml
<plugin>
    <groupId>io.smallrye</groupId>
    <artifactId>smallrye-open-api-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-openapi</id>
            <phase>process-classes</phase>
            <goals>
                <goal>generate-schema</goal>
            </goals>
            <configuration>
                <outputDirectory>
                    ${project.build.outputDirectory}/META-INF/nexus-openapi
                </outputDirectory>
                <schemaFilename>${project.artifactId}</schemaFilename>
                <outputFileTypeFilter>YAML</outputFileTypeFilter>
                <encoding>UTF-8</encoding>
                <scanners>
                    <scanner>JAX-RS</scanner>
                </scanners>
                <scanPackages>
                    com.example.product.platform,
                    com.innospots.nexus.base.domain.response,
                    com.innospots.nexus.base.i18n,
                    com.innospots.nexus.base.domain.enums
                </scanPackages>
                <scanDependenciesDisable>false</scanDependenciesDisable>
                <openApiVersion>3.1.0</openApiVersion>
                <operationIdStrategy>CLASS_METHOD</operationIdStrategy>
            </configuration>
        </execution>
    </executions>
</plugin>
```

要求：

- `schemaFilename` 必须等于 `artifactId`。
- `scanPackages` 覆盖本模块 REST 根包和响应中实际出现的共享类型。
- 不要扫描整个 `base`、`core` 或 `console`。
- console/platform 可直接写入 `META-INF/nexus-openapi`；portal 当前先生成到 `target/generated/openapi`，再复制到最终目录。
- `info*` 配置应与模块的 `@OpenAPIDefinition` 保持一致。

### 7.3 为新增 REST 模块生成规范

1. 增加 `*OpenApiDefinition`，声明标题、版本、Tag 和 `bearerAuth`。
2. 每个 Endpoint 声明稳定的 `@Tag`。
3. 每个 HTTP 方法声明稳定的 `@Operation(operationId, summary)`。
4. 请求、VO、枚举补充 `@Schema`。
5. 需要认证的资源加 `@NexusAuthenticatedApi`。
6. 配置 SmallRye 插件并运行 `package`。
7. 检查 JAR 中确实存在目标 YAML，并包含新增路径。

```bash
mvn -pl <rest-module> -am clean package -DskipTests
jar tf <rest-module>/target/<artifact>-*.jar | grep 'META-INF/nexus-openapi'
```

`META-INF/nexus-openapi` 下的 YAML 是构建产物，不手写、不直接编辑。

### 7.4 扩展库没有独立 OpenAPI 时

无框架扩展库可以只提供 JAX-RS 契约，但这样不会自动产生规范。两种接入方式：

- 推荐：为产品 API 建独立 REST/adaptor 模块，配置自己的 SmallRye 产物。
- 或：把扩展的契约包加入某个 REST 模块的 `scanPackages`，重新生成该模块 YAML。这样规范 ID 仍然是该 REST 模块的 `artifactId`。

不要假设运行时自动扫描 classpath 中的新 Endpoint。

## 8. OpenAPI 运行时

| 路径 | 说明 |
|------|------|
| `GET /openapi/specs` | 规范列表，返回 `R<List<OpenApiSpecItemVo>>` |
| `GET /openapi/specs/{specId}` | 将打包 YAML 解析后以 JSON 返回 |
| `GET /openapi/ui` | Scalar 文档页 |
| `GET /openapi/ui/scalar.js` | Scalar 页面脚本 |

默认免登录路径包括：

```text
/
/openapi/**
/api/public/**
/api/d/public/**
/health
/actuator/health/**
```

因此 `/openapi/**` 默认完全跳过 Bearer 解析。生产环境是否需要限制文档访问，应由网关或自定义 permit 规则决定。

## 9. 权限与 OpenAPI 检查表

- [ ] console 契约有 `@EndpointModule`，每个方法有 `@EndpointGrant`。
- [ ] 实现类没有复制 grant 元数据。
- [ ] 实现类重复了 JAX-RS 与 OpenAPI 传输注解。
- [ ] `grant.name` 稳定且唯一，敏感参数关闭记录。
- [ ] 新增权限资源已通过 catalog 同步。
- [ ] 页面/导航的模块级授权限制已被业务方知悉。
- [ ] OpenAPI YAML 已由构建生成，路径和 schema 完整。
- [ ] 生产启用了 security，关闭了 dev session，并替换了示例密钥。

