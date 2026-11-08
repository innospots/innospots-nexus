# EndpointGrant：REST 授权、审计与跳过策略

`EndpointGrant` 是平台 REST 资源的授权与审计元数据单一事实源（类型在 **core**
`endpoint.grant`；console/platform 过滤器与 catalog 消费）。它不替代 JAX-RS
注解，也不替代 service 中的业务校验；它只描述“这个 HTTP 方法属于哪个模块、是什么操作、
是否需要登录/权限、是否写审计”。

完整的 console@2、权限目录、角色授权、导航和页面访问链路见
[authorization-and-permissions.md](../../java-reference/references/authorization-and-permissions.md)。

**模块 API 索引：**
[`endpoint.grant`](../../java-reference/references/modules/innospots-nexus-core/references/endpoint-grant.md)
与
[`endpoint.grant.support`](../../java-reference/references/modules/innospots-nexus-core/references/endpoint-grant-support.md)
（类型在 **innospots-nexus-core**；console 提供过滤器与 catalog 组装）

## 1. 声明位置

| 资源形态 | `@EndpointModule` | `@EndpointGrant` |
|----------|-------------------|------------------|
| console 契约接口 | 当前标准不在接口上声明 | 声明在每个 HTTP 方法上 |
| 实现 console 契约的 platform/portal 类 | 在实现类声明一次 | **不得重复**接口方法注解 |
| platform/portal 自有 REST 类 | 声明在实现类上 | 声明在实现类每个 HTTP 方法上 |
| 公共 API 资源 | 模块上可设 `ignoreAuth = true` | 方法上按需设 `ignorePermission = true` |

解析优先级：

1. 资源类自身的 `@EndpointModule`；没有时读取直接实现的 console 契约接口。
2. 实现方法自身的 `@EndpointGrant`；没有时读取同名同参数的直接接口方法。
3. 模块级 `ignoreAuth` / `ignorePermission` 与方法级值按 **OR** 合并。

因此 `@EndpointModule` 放在实际 JAX-RS 资源实现类上，`@EndpointGrant` 放在 console
契约方法或独立资源方法上。不要为了“保险”在实现类复制接口 grant。复制会形成第二份
可漂移的契约。

实现 console 契约的业务类负责资源实现侧的类型级传输元数据：

- 类级 `@Path`、`@Produces`、`@Consumes` 与 `@NexusAuthenticatedApi`

契约接口保留方法级 HTTP 动词、子路径、`@PathParam` / `@BeanParam` / `@QueryParam`、
`@Operation` / `@Parameter` 与 `@EndpointGrant`。实现方法按当前代码模式直接 `@Override`，
不复制这些方法级注解；`EndpointGrantHttpIntrospection` 会从实现类及其直接接口解析
HTTP 路由。授权与审计元数据仍以 console 契约为单一事实源。

## 2. 最小示例

### console 契约接口

```java
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NexusAuthenticatedApi
public interface DictionaryTypeEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "dictionary.type.page",
            label = @I18nLiteral(pairs = {
                    "zh-CN", "分页查询字典类型",
                    "en", "Page dictionary types"
            }))
    R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(
            @BeanParam DictionaryTypePageRequest request);

    @POST
    @EndpointGrant(
            action = EndpointGrantAction.CREATE,
            name = "dictionary.type.create",
            label = @I18nLiteral(pairs = {
                    "zh-CN", "创建字典类型",
                    "en", "Create dictionary type"
            }),
            recordArgs = false,
            auditLog = true)
    R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request);
}
```

宿主实现类声明模块与具体根路径：

```java
@EndpointModule(value = "dictionary", domain = "system")
@Path("/api/d/platform/dictionary-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NexusAuthenticatedApi
public class PlatformDictionaryTypeEndpoint implements DictionaryTypeEndpoint {
    // 直接 @Override 契约方法；不重复 @EndpointGrant。
}
```

### 独立 platform 资源

```java
@EndpointModule("user")
@Path(PlatformConstant.USERS_PATH)
@NexusAuthenticatedApi
public class PlatformUserEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.user.page")
    public R<PageResult<PlatformUserVo>> pageUsers(
            @BeanParam PlatformUserPageRequest request) {
        return R.ok(platformUserService.pageUsers(request));
    }
}
```

### 公共操作

```java
@EndpointModule(value = "user", ignoreAuth = true)
public class PlatformPublicInviteEndpoint {

    @POST
    @Path("/{token}/accept")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "platform.invite.public.accept",
            recordArgs = false,
            ignorePermission = true,
            auditLog = true)
    public R<String> acceptInvite(
            @PathParam("token") String token,
            PlatformInviteAcceptRequest request) {
        return R.ok(publicInviteService.acceptInvite(token, request));
    }
}
```

`ignoreAuth` 只跳过登录解析，不自动跳过权限。公共且不需要权限的资源必须同时
声明 `ignorePermission = true`，或由公共 API 路径规则排除。

## 3. 字段语义

| 字段 | 必填 | 规则 |
|------|------|------|
| `action` | 是 | 选择最具体的 `EndpointGrantAction`；不要默认写 `WRITE` |
| `name` | 是 | 稳定业务名，建议 `domain.resource.operation`；发布后不得随实现类重命名 |
| `label` | 建议且审计时必填 | 审计与权限目录展示的多语言操作说明 |
| `recordArgs` | 否 | 默认 `true`；密钥、密码、OTP、邀请令牌、批量敏感载荷必须为 `false` |
| `auditLog` | 否 | 默认 `false`；只对需要长期审计的管理动作开启 |
| `ignorePermission` | 否 | 与模块级值 OR 合并；公共读取、登录前操作等按需使用 |
| `ignoreAuth` | 否 | 与模块级值 OR 合并；登录、注册、邀请、健康检查等按需使用 |
| `group` | 否 | READ/EXPORT 缺省为 `view`，其他动作缺省为 `operate`；细分操作组必须显式填写 |

请求鉴权会同时构造操作项键和分组键，命中任一授权即可：

```text
{domainKey}.{moduleKey}:{EndpointGrant.name}
{domainKey}.{moduleKey}:{EndpointGrant.group}
```

页面访问项则使用 MENU/MODULE 的 `entryPageKey` 作为 `grantName`。不要把两者混用。
角色页当前保存的是分组键，因此自定义 `operationGroups` 必须与
`@EndpointGrant.group` 同名。只改插件 YAML、不改注解时，目录可能展示该组，但运行时
仍使用 `operate`，最终表现为“已勾选却 403”。

## 4. Action 选择

| Action | 典型语义 |
|--------|----------|
| `READ` | 分页、详情、选项、树查询 |
| `CREATE` | 创建资源 |
| `UPDATE` | 更新主体字段 |
| `DELETE` | 删除资源 |
| `STATUS` | 启停、审批、生命周期状态变更 |
| `REPLACE` | 授权集合或配置的全量替换 |
| `EXECUTE` | 安装、同步、重试、发送等命令 |
| `EXPORT` | 导出、下载、报表 |
| `IMPORT` | 导入、上传、批量灌入 |
| `ASSIGN` | 建立成员、角色或权限绑定 |
| `REVOKE` | 解绑、回收、撤销 |
| `WRITE` | 仅存量无法细分的写操作兜底 |

目录分组中 `READ` 与 `EXPORT` 进入 `readOnly`，其余进入 `operable`。运行时缺省组：

| Action | 缺省 group | 资源组约束 |
|--------|------------|------------|
| READ / EXPORT | `view` | 自定义时与 MODULE `viewGroups` 对齐 |
| 其他全部动作 | `operate` | 使用 `create`、`update` 等细分 UI 组时，必须声明同名 `group` 并与 `operationGroups` 对齐 |

`CatalogPermissionCatalogAssembler` 还会优先使用显式 group；显式 group 未在 MODULE
`operationGroups` 声明时，该操作项不会出现在对应角色目录中。

## 5. 运行时链路

```text
ConsoleRequestContextFilter          @Priority(AUTHENTICATION - 200)
  → 请求开始/响应结束清理 SessionContext 与 TLC

ConsoleResolvedEndpointGrantFilter   @Priority(AUTHENTICATION - 100)
  → 解析 @EndpointModule + @EndpointGrant
  → 写入 ResolvedEndpointBinding

ConsoleAuthenticationFilter          @Priority(AUTHENTICATION)
  → grant.ignoreAuth()

WorkspaceScopeSessionFilter          @Priority(AUTHENTICATION + 50)
  → 绑定租户/工作区/项目作用域

ConsoleResourcePermissionFilter          @Priority(AUTHENTICATION + 100)
  → grant.ignorePermission()

ConsoleAuditLogFilter                @Priority(USER + 100)
  → 仅 grant.auditLog() 为 true 时写审计
```

`EndpointGrantManager` 还负责：

- 启动时登记资源类，形成模块级 `readOnly` / `operable` / `ignoreAuth` / `ignorePermission` 目录。
- 将类级与方法级 JAX-RS `@Path` 拼成路径模板。
- 把 `{param}` 转为单段匹配，用于 `match(httpMethod, requestPath)`。

资源没有被注册或没有 HTTP 动词时不会进入目录。没有 `@EndpointModule` 的 grant 可以解析，
但不会进入模块目录。

## 6. 路径解析边界

- 类级 `@Path` 只从运行时资源类读取。
- 方法级 `@Path` 先读实现方法，再读直接实现的接口方法。
- 实现 console 契约时，接口负责模块与 grant 元数据；接口和实现类都声明 JAX-RS /
  OpenAPI 传输注解，实现类负责类级 `@Path` 和运行逻辑。
- 路径模板中的 `{id}` 在目录匹配时视为单段通配，不表示可跨 `/`。

## 7. 必须补的测试

| 测试 | 断言 |
|------|------|
| 契约注解测试 | 每个 HTTP 方法都有 `@EndpointGrant`，`name` 唯一且稳定 |
| 模块归属测试 | 契约实现类或独立资源的 `@EndpointModule.value()` 与 MODULE `moduleKey` 一致 |
| 实现类防重复测试 | 实现 console 契约的类不得重复声明模块或方法 grant |
| 实现类 OpenAPI 测试 | console 契约每个 HTTP 方法都有 `@Operation`，实现类不复制方法级契约 |
| 目录测试 | `EndpointGrantManager.moduleCatalog()` 的只读/可操作分类与 grant group 正确 |
| 组对齐测试 | 细分 `operationGroups` 与 `@EndpointGrant.group` 同名，目录不丢操作项 |
| 路径测试 | 类路径 + 方法路径 + `{param}` 匹配正确，错误 HTTP 动词不匹配 |
| Filter 测试 | `ignoreAuth` / `ignorePermission` / `auditLog` 分别生效 |
| 审计安全测试 | `recordArgs = false` 时不记录敏感请求体或路径秘密 |

platform 的聚合契约测试可参考
`PlatformStandaloneEndpointGrantContractsTest`；console 的解析测试可参考
`EndpointGrantAnnotationsTest` 与 `EndpointGrantManagerTest`。

## 8. 反模式

| 反模式 | 后果 |
|--------|------|
| 每个方法都写 `WRITE` | 权限目录无法区分创建、状态、绑定等操作 |
| 使用 Java 方法名作为 `name` | 重构方法名会破坏已持久化授权键 |
| 在实现类复制接口 grant | 两份元数据漂移，测试与运行时可能各读一份 |
| 实现类重复 `@EndpointGrant` 或方法级 JAX-RS / OpenAPI 注解 | 两份契约漂移，授权或 OpenAPI 难以判断真源 |
| 登录接口只设 `ignoreAuth` | 仍可能被资源权限过滤器拦截 |
| 审计请求参数包含密码/令牌 | 敏感信息落库 |
| 缺少 `label` 却开启审计 | 审计展示退化为空或不稳定文本 |
| YAML 声明 `create` 组但 grant 不写 `group = "create"` | 运行时仍按 `operate` 校验，勾选后可能 403 |
| grant group 与 `viewGroups` / `operationGroups` 不一致 | 目录缺项或角色无法命中接口 |
