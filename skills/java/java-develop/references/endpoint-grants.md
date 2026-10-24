# EndpointGrant：REST 授权、审计与跳过策略

`EndpointGrant` 是平台 REST 资源的授权与审计元数据单一事实源（类型在 **core**
`endpoint.grant`；console/platform 过滤器与 catalog 消费）。它不替代 JAX-RS
注解，也不替代 service 中的业务校验；它只描述“这个 HTTP 方法属于哪个模块、是什么操作、
是否需要登录/权限、是否写审计”。

**模块 API 索引：**
[`endpoint.grant`](../../java-reference/references/modules/innospots-nexus-core/references/endpoint-grant.md)
与
[`endpoint.grant.support`](../../java-reference/references/modules/innospots-nexus-core/references/endpoint-grant-support.md)
（类型在 **innospots-nexus-core**；console 提供过滤器与 catalog 组装）

## 1. 声明位置

| 资源形态 | `@EndpointModule` | `@EndpointGrant` |
|----------|-------------------|------------------|
| console 契约接口 | 声明在接口类上 | 声明在每个 HTTP 方法上 |
| 实现 console 契约的 platform/portal 类 | **不得重复** | **不得重复** |
| platform/portal 自有 REST 类 | 声明在实现类上 | 声明在实现类每个 HTTP 方法上 |
| 公共 API 资源 | 模块上可设 `ignoreAuth = true` | 方法上按需设 `ignorePermission = true` |

解析优先级：

1. 资源类自身的 `@EndpointModule`；没有时读取直接实现的 console 契约接口。
2. 实现方法自身的 `@EndpointGrant`；没有时读取同名同参数的直接接口方法。
3. 模块级 `ignoreAuth` / `ignorePermission` 与方法级值按 **OR** 合并。

因此不要为了“保险”在实现类上复制接口注解。复制会形成第二份可漂移的契约。

`@EndpointModule` / `@EndpointGrant` 的“不重复”只约束授权与审计元数据。实现 console
契约的业务类仍必须重复以下内容：

- 类级 `@Path`、`@Produces`、`@Consumes` 与 `@NexusAuthenticatedApi`
- 每个 HTTP 方法的动词、子路径、`@PathParam` / `@BeanParam` / `@QueryParam`
- `@Operation`、`@Parameter` 与必要的 `@SecurityRequirement`

原因是 SmallRye 构建期 JAX-RS 扫描器不会把接口方法上的 JAX-RS / OpenAPI 注解投影到
实现类。只保留接口注解会导致运行时 grant 正常，但 OpenAPI 缺失路径、请求体或 schema。
实现类重复的是**传输契约**，授权单一事实源仍是 console 接口。

## 2. 最小示例

### console 契约接口

```java
@EndpointModule("dictionary")
@Path("/api/d/nexus/dictionary-types")
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
| `group` | 否 | 当前 `ResolvedEndpointGrant` 尚未消费该字段，目录实际仍归入 `default` 组 |

`name` 会进入 catalog 授权键：

```text
<EndpointGrant.name>:<domainKey>.<moduleKey>
```

页面访问项则使用 MENU/MODULE 的 `entryPageKey` 作为 `grantName`。不要把两者混用。

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

目录分组中 `READ` 与 `EXPORT` 进入 `readOnly`，其余进入 `operable`。

## 5. 运行时链路

```text
ConsoleResolvedEndpointGrantFilter   @Priority(AUTHENTICATION - 100)
  → 解析 @EndpointModule + @EndpointGrant
  → 写入 ResolvedEndpointBinding

ConsoleAuthenticationFilter          @Priority(AUTHENTICATION)
  → grant.ignoreAuth()

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
| 模块归属测试 | 契约接口或独立资源的 `@EndpointModule.value()` 与 MODULE `moduleKey` 一致 |
| 实现类防重复测试 | 实现 console 契约的类不得重复声明模块或方法 grant |
| 实现类 OpenAPI 测试 | 每个实现 HTTP 方法都有 `@Operation`，参数绑定与接口一致 |
| 目录测试 | `EndpointGrantManager.moduleCatalog()` 的只读/可操作分类正确 |
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
| 实现类只写运行逻辑、不重复 JAX-RS / OpenAPI 注解 | 构建期 OpenAPI 缺失路径或 schema |
| 登录接口只设 `ignoreAuth` | 仍可能被资源权限过滤器拦截 |
| 审计请求参数包含密码/令牌 | 敏感信息落库 |
| 缺少 `label` 却开启审计 | 审计展示退化为空或不稳定文本 |
| 依赖 `group` 做分组 | 当前运行时尚未消费该字段，操作项仍进入 `default` |
