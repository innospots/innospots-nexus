# innospots-nexus-console — 模块 API 索引

> **模块 API 索引**，由 `java:reference` 消费；**不是**可安装的 Cursor 技能。
> 仅开发者显式请求模块扫描时生成或刷新（见 `skills/java/java-reference/standards/module-skills.md`）。

快照版本：0.1.0-SNAPSHOT

## 模块概览

基于 `innospots-nexus-core` 与 `innospots-nexus-plugin` 的管理控制台 **API 表面**：Jakarta REST、request/response record、目录索引、权限运行时、导航组装、平台域认证编排、凭据（密码/OTP/TOTP）、OpenAPI 规范目录，以及内置控制台入口插件。

**能力一览：**

| 能力 | 说明 |
|------|------|
| **REST 表面** | `/console/**` 与 `/openapi/specs`（租户/平台认证在 portal/platform） |
| **归属与作用域** | `ConsoleOwnership*`、`SessionScopeBinder` 端口 |
| **目录索引** | 插件贡献同步到 `nx_console_catalog_resource` |
| **权限运行时** | `ConsolePagePermissionAuthorizer`、`PermissionGrantService` |
| **导航** | `NavigationMenuAssembler` + `/console/navigation/menus` |
| **凭据** | 密码、OTP、TOTP 与 `nx_user_credential` |
| **插件管理** | `PluginManagementEndpoint` |
| **OpenAPI** | 构建期 bundled 规范目录与 Scalar 文档钩子 |
| **内置入口** | 六个 `*EntryPlugin` + Page DSL 资源 |
| **审计** | `nx_audit_log` 与调用日志管道 |

**不包含：** Spring Boot 自动配置、Servlet 绑定、租户 `/tenant/**` 与平台 `/platform/**` 认证端点实现（归属 portal/platform）、
插件规范 / Page DSL 定义。

## 边界契约

### 控制台 vs 插件 vs portal/platform

| 关注点 | 归属 | 控制台提供 |
|---------|-------|------------------|
| 插件发现、贡献解码、Page DSL | **plugin** | 消费 plugin API 做目录同步 |
| 目录持久化索引（`nx_console_catalog_resource`） | **console** | `ConsoleCatalogSyncService`、`ConsoleCatalogService` |
| 权限授权（`nx_permission_grant`） | **console** | 实体、DAO、`PermissionGrantService`、`ConsolePagePermissionAuthorizer` |
| 用户 / 成员持久化 | **portal** / **platform** | `UserDirectory`、`MembershipDirectory` 等端口 |
| 租户 / 工作空间 / 项目令牌链 | **portal** / **platform** | `/tenant/auth`、`/tenant/scope` 端点与 `TenantAuthFacade` |
| 平台运维登录 | **platform** | `/platform/auth` |
| 控制台归属列 | **console** + **core** | `ConsoleOwnership*`、`OwnershipEntity`（凭据/字典等） |
| HTTP 运行时绑定 | **adapter/application** | 过滤器、`SessionScopeBinder` 实现、`AuthorizationSubjectResolver` |

### 分层（强制）

```text
endpoint → service → operator → dao
```

- 端点：`jakarta.ws.rs`；返回 `R<T>`（分页为 `R<PageResult<T>>`）。
- `domain.request` / `domain.vo`：**record**。
- 事务：`jakarta.transaction.Transactional` 仅在 service/operator。
- 需 Bearer 的管理 API 使用 `@NexusAuthenticatedApi`（core OpenAPI 契约）。

### 持久化表（控制台归属）

| 表 | 实体 | 基类 / 归属 |
|-------|--------|----------------|
| `nx_console_catalog_resource` | `ConsoleCatalogResourceEntity` | `BaseEntity` + 目录键 |
| `nx_permission_grant` | `PermissionGrantEntity` | `OwnershipEntity` |
| `nx_role` | `RoleEntity` | `BaseEntity` + `ownerType`/`ownerId`/`securityRealm` 列 |
| `nx_role_binding` | `RoleBindingEntity` | `OwnershipEntity` |
| `nx_menu` | `MenuEntity` | `OwnershipEntity`（无 `/console/menus` REST） |
| `nx_dictionary_*` | `DictionaryTypeEntity` / `DictionaryItemEntity` | `OwnershipEntity` |
| `nx_user_credential` | `UserCredentialEntity` | `OwnershipEntity` |
| `nx_audit_log` | `AuditLogEntity` | `OwnershipEntity` |

## 扩展指南

| 需求 | 使用控制台中的 | 实现在 |
|------|----------------|--------|
| 平台域登录/刷新 | `AuthFacade`、`AuthTokenPairIssuer` | platform + adapter |
| 租户登录/注册/作用域 | 端口与 VO 形状 | **portal** |
| 会话绑定 TLC | `SessionScopeBinder` 端口、`SessionScopeTlc` | adapter |
| 控制台归属解析 | `ConsoleOwnershipScope`、`ConsoleOwnershipGuard` | console operator |
| 页面/数据源授权 | `ConsolePagePermissionAuthorizer` | adapter 过滤器 |
| 当前用户主体 | `AuthorizationSubjectResolver`（默认 `SessionAuthorizationSubjectResolver`） | adapter 可替换 |
| 目录同步 | `ConsoleCatalogSyncService`、`ConsoleCatalogSyncStartupTask` | 宿主注册启动任务 |
| OpenAPI 规范目录 | `OpenApiCatalogEndpoint` | console + 构建期 bundled specs |
| 新管理 REST 域 | endpoint + request/vo + service/operator/dao | console 模块内 |

**不要**在 console 中放置插件规范、贡献约束或 Spring 绑定。

## REST 端点索引（console 模块内）

完整方法表：[endpoint-contracts.md](references/endpoint-contracts.md)。

| 路径前缀 | 端点 | 说明 |
|-------------|----------|------|
| `/console` | `ConsoleEndpoint` | 健康/状态 |
| `/console/catalog` | `ConsoleCatalogEndpoint` | 目录树与同步 |
| `/console/plugins` | `PluginManagementEndpoint` | 插件安装生命周期 |
| `/console/navigation/menus` | `NavigationMenuEndpoint` | 授权过滤后的侧栏 |
| `/console/me/permissions` | `CurrentAuthorizationEndpoint` | 当前用户可见资源 |
| `/console/roles` | `RoleEndpoint` | 角色 CRUD（可继承） |
| `/console/roles/{roleId}/bindings` | `RoleBindingEndpoint` | 角色绑定 |
| `/console` | `GrantManagementEndpoint` | 角色/组织单元授权替换 |
| `/console/dictionary-types` | `DictionaryTypeEndpoint` | 字典类型 |
| `/console/dictionary-types/{typeCode}/items` | `DictionaryItemEndpoint` | 字典项 |
| `/openapi/specs` | `OpenApiCatalogEndpoint` | OpenAPI YAML 目录 |

租户 `/tenant/**`、平台 `/platform/**` 契约由 **portal** / **platform** 实现，见对应模块文档。

## 技能用法（java:reference）

本目录**不是**可安装技能。通过 **`java:reference`** 消费：扩展 portal/platform 端口、在 console 增加 REST 契约、判断逻辑归属 console 还是 plugin。

```text
java:reference → module-ownership.md + this README
java:design     → boundary + endpoint contracts
java:develop    → portal/platform/adapter 实现；console 保持契约中立
java:check      → mvn clean compile / test
```


## 类参考

### 包 `auth.api`

| 类 | 类型 | 说明 |
|------|------|------|
| `UserDirectory` | `interface` | 查找登录身份 |

### 包 `auth.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `SecurityRealm` | `enum` | 令牌与用户目录的安全域 |

### 包 `auth.domain.model`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuthUser` | `record` | 用户目录端口返回的域中立用户身份 |
| `CredentialRecord` | `record` | 域用户的鉴权凭据快照（opaque verifier，由 algorithm 解释） |
| `TokenClaims` | `record` | 加密进访问或刷新令牌的紧凑声明 |

### 包 `auth.domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuthCaptchaIssueRequest` | `record` | 登录前图形验证码发放请求 |
| `AuthLoginRequest` | `record` | 两个域共用的密码登录载荷 |
| `PasswordChangeRequest` | `record` | 已认证用户的密码修改 |
| `PasswordResetRequest` | `record` | 使用验证码重置密码 |
| `TokenRefreshRequest` | `record` | 刷新令牌交换 |

### 包 `auth.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuthCaptchaVo` | `record` | 登录图形验证码发放结果（REST 形状） |
| `AuthTokenVo` | `record` | 登录、注册、刷新或租户选择后返回的令牌对 |

### 包 `auth.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuthFacade` | `class` | 运维平台域登录与令牌刷新编排 |
| `AuthSessionScope` | `record` | 已签发认证令牌对携带的作用域 |
| `AuthTokenPairIssuer` | `class` | 从会话作用域签发访问与刷新令牌对 |
| `LoginCaptchaGate` | `class` | 登录前图形验证码策略与编排；实现委托 CaptchaChallengeService |
| `TokenIssuer` | `class` | 签发并解析 AES-GCM 紧凑令牌 |

### 包 `catalog.bootstrap`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleCatalogSyncStartupTask` | `class` | 启动后将 ACTIVE 插件贡献同步到宿主级目录索引 |

### 包 `catalog.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleCatalogResourceDao` | `interface` | 宿主级 Console 目录索引 DAO |

### 包 `catalog.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleCatalogResourceEntity` | `class` | 宿主级 Console 插件贡献目录索引 |

### 包 `catalog.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `CatalogResourceType` | `enum` | Console 插件贡献向目录索引暴露的资源类型 |

### 包 `catalog.domain.model`

| 类 | 类型 | 说明 |
|------|------|------|
| `CatalogSyncResult` | `record` | Console 目录显式同步结果 |

### 包 `catalog.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `CatalogNodeVo` | `record` | 权限设置页的插件功能树节点 |

### 包 `catalog.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleCatalogEndpoint` | `class` | 权限设置页目录树与显式同步接口 |

### 包 `catalog.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleCatalogService` | `class` | 从宿主级目录索引组装权限设置树 |
| `ConsoleCatalogSyncService` | `class` | 将已激活 Console Contribution 和 PageDsl 同步为宿主级目录索引 |

### 包 `config`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuthConfig` | `class` | 控制台认证的令牌签发设置 |

### 包 `credential.otp.captcha`

| 类 | 类型 | 说明 |
|------|------|------|
| `CaptchaPolicy` | `record` | 图形验证码绘制参数（与 com.innospots.nexus.console.credential.otp.policy.OtpPolicy 的 TTL/重试策略独立） |
| `CaptchaStyle` | `enum` | Hutool 图形验证码样式，对应 cn.hutool.captcha 中的实现类 |
| `HutoolCaptchaFactory` | `class` | 基于 hutool-all（cn.hutool.captcha）的图形验证码生成 |

### 包 `credential.otp.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `OtpChallengeDao` | `interface` | OtpChallengeEntity 的 MyBatis-Plus Mapper；无自定义 SQL，由 com.innospots.nexus.console.credential.otp.service.OtpChallengeService 通过 com.baomidou.mybatisplus.core.con… |

### 包 `credential.otp.domain`

| 类 | 类型 | 说明 |
|------|------|------|
| `CaptchaIssueCommand` | `record` | 发放图形验证码 |
| `CaptchaIssueResult` | `record` | 图形验证码发放结果 |
| `CaptchaVerifyCommand` | `record` | 校验用户提交的图形验证码 |
| `OtpIssueCommand` | `record` | 发放 OTP 的不可变命令对象；由 API 层组装后交给 com.innospots.nexus.console.credential.otp.service.OtpChallengeService |
| `OtpVerifyCommand` | `record` | 校验 OTP 的不可变命令；查找挑战时使用与发放相同的四维键： realm + purpose + channel + 规范化 destination |

### 包 `credential.otp.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `OtpChallengeEntity` | `class` | 短生命周期 OTP 挑战行（表 {@value #TABLE_NAME}）；库内仅存 codeVerifier 哈希，明文码仅经 com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent 投递 |

### 包 `credential.otp.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `OtpChannel` | `enum` | OTP 下发通道；决定地址规范化规则、com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent 后缀，以及与 legacy VerificationType 的互转（仅 EMAIL/MOBILE） |
| `OtpPurpose` | `enum` | OTP 业务用途；持久化为 com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity 字符串 |

### 包 `credential.otp.event`

| 类 | 类型 | 说明 |
|------|------|------|
| `OtpSendRequestedEvent` | `record` | OTP 明文码投递请求；adapter 订阅后按 {@link #channel} 与 {@link #templateId} 渲染并发送 |

### 包 `credential.otp.policy`

| 类 | 类型 | 说明 |
|------|------|------|
| `OtpPolicy` | `record` | OTP 发放与校验的可配置策略（不可变 record） |

### 包 `credential.otp.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `CaptchaChallengeService` | `class` | 图形验证码（captchaCode）发放与校验门面，底层复用 OtpChallengeService 挑战表 |
| `OtpChallengeService` | `class` | OTP 挑战的唯一样式入口：发放、校验、作废，并在发放后发布 OtpSendRequestedEvent |
| `OtpPasswordVerificationOperator` | `class` | 将 PasswordVerificationOperator 映射到 OTP 子域，且用途固定为 com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose |

### 包 `credential.otp.status`

| 类 | 类型 | 说明 |
|------|------|------|
| `OtpStatusCode` | `enum` | OTP 子域业务状态码（模块标识 OTP，完整码形如 OTP-0001） |

### 包 `credential.ownership`

| 类 | 类型 | 说明 |
|------|------|------|
| `CredentialOwnershipResolver` | `class` | 将 com.innospots.nexus.console.auth.domain.enums.SecurityRealm 映射为凭据/OTP 行的 com.innospots.nexus.console.scope.ConsoleOwnership（身份级 TENANT/PLATFORM 时 ownerId 可为… |

### 包 `credential.password`

| 类 | 类型 | 说明 |
|------|------|------|
| `CredentialKind` | `enum` | nx_user_credential.credential_kind 取值；与 com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithm 解耦 |
| `PasswordDecryptor` | `interface` | 解密前端客户端提交的密码密文；由 portal/platform/auth 注入，实现见 RsaPasswordDecryptor |
| `PasswordValidator` | `class` | 密码强度校验器 |
| `PasswordVerificationOperator` | `interface` | 忘记密码等场景的验证码端口；OTP 实现见 com.innospots.nexus.console.credential.otp.service.OtpPasswordVerificationOperator |
| `RsaPasswordDecryptor` | `record` | 前端 RSA 加密密码的 PasswordDecryptor 实现 |
| `VerificationType` | `enum` | 忘记密码等流程中验证码的投递通道（与 com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel 在 EMAIL/MOBILE 上互转） |

### 包 `credential.password.algorithm`

| 类 | 类型 | 说明 |
|------|------|------|
| `BcryptCredentialAlgorithm` | `class` | 默认密码算法：BCrypt 单向哈希，标识 CredentialAlgorithms |
| `CredentialAlgorithm` | `interface` | 可插拔凭据编码与验证；持久化层不解析 verifier 结构，由 CredentialAlgorithmRegistry 路由 |
| `CredentialAlgorithmRegistry` | `class` | 按算法 ID 解析 CredentialAlgorithm；默认注册 bcrypt 密码哈希 |
| `CredentialAlgorithms` | `class` | 内置凭据算法标识符 |
| `EncodedCredential` | `record` | 算法产出的 opaque 验证材料，供 com.innospots.nexus.console.credential.password.operator.UserCredentialOperator 写入库表 |

### 包 `credential.password.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `UserCredentialDao` | `interface` | UserCredentialEntity MyBatis-Plus Mapper |

### 包 `credential.password.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `UserCredentialEntity` | `class` | 按控制台归属存储的用户鉴证凭据行（表 {@value #TABLE_NAME}） |

### 包 `credential.password.operator`

| 类 | 类型 | 说明 |
|------|------|------|
| `UserCredentialOperator` | `class` | nx_user_credential 的读写与证明校验；同一表按 CredentialKind 存密码、TOTP 等行 |

### 包 `credential.password.policy`

| 类 | 类型 | 说明 |
|------|------|------|
| `LoginLockPolicy` | `record` | 登录密码失败后的锁定策略 |

### 包 `credential.password.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `CredentialService` | `class` | 租户/平台用户密码生命周期：注册、修改、重置；校验强度后委托 UserCredentialOperator |

### 包 `credential.totp`

| 类 | 类型 | 说明 |
|------|------|------|
| `MfaAuthenticator` | `interface` | MFA 动态码校验端口；默认实现见 com.innospots.nexus.console.credential.totp.adapter.DefaultMfaAuthenticator |
| `TotpCredentials` | `class` | TOTP 凭据行 verifierParams 约定与判定辅助 |
| `TotpMasterKeyProvider` | `interface` | 提供 TOTP 共享密钥静态加密用的主密钥（应用或 adapter 注入，须为 16/24/32 字节 AES 材料） |
| `TotpProvisioningUriBuilder` | `class` | 构建 otpauth:// 扫码 URI，参数与 com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithm 一致（SHA1、8 位、30 秒） |
| `TotpSecretProtector` | `class` | TOTP 共享密钥的 Base32 生成与 AES-GCM 静态加密；主密钥由 TotpMasterKeyProvider 提供 |

### 包 `credential.totp.adapter`

| 类 | 类型 | 说明 |
|------|------|------|
| `DefaultMfaAuthenticator` | `class` | 从 nx_user_credential 加载已激活 TOTP 行并校验动态码 |

### 包 `credential.totp.algorithm`

| 类 | 类型 | 说明 |
|------|------|------|
| `TotpCredentialAlgorithm` | `class` | RFC 6238 TOTP（HMAC-SHA1，30 秒步长，8 位数字）；校验允许 ±1 个时间窗 |
| `TotpCredentialAlgorithms` | `class` | TOTP 算法标识 |

### 包 `credential.totp.domain`

| 类 | 类型 | 说明 |
|------|------|------|
| `TotpEnrollmentMaterial` | `record` | TOTP 注册阶段返回给客户端的材料；base32Secret 与 otpauthUri 仅应在受信通道展示一次 |

### 包 `credential.totp.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `DefaultTotpEnrollmentService` | `class` | 默认 TOTP 注册：密钥经 com.innospots.nexus.console.credential.totp.TotpSecretProtector 加密后写入 com.innospots.nexus.console.credential.password.CredentialKind 行，待确认状态见 co… |
| `TotpEnrollmentService` | `interface` | TOTP 注册两阶段：{@link #beginEnrollment} 返回扫码材料，{@link #confirmEnrollment} 校验首码后激活行 |
| `TotpVerificationSupport` | `class` | TOTP 动态码校验（注册确认与登录 MFA 共用）；失败抛出 com.innospots.nexus.console.credential.totp.status.TotpStatusCode |

### 包 `credential.totp.status`

| 类 | 类型 | 说明 |
|------|------|------|
| `TotpStatusCode` | `enum` | TOTP/MFA 子域 com.innospots.nexus.base.status.StatusCode（模块 MFA） |

### 包 `dictionary.converter`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryConverter` | `class` | 字典实体与 VO 的转换 |

### 包 `dictionary.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItemDao` | `interface` | 字典项记录的 MyBatis-Plus Mapper |
| `DictionaryTypeDao` | `interface` | 字典类型记录的 MyBatis-Plus Mapper |

### 包 `dictionary.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItemEntity` | `class` | 租户级字典项；归属与字典类型一致 |
| `DictionaryTypeEntity` | `class` | 租户级字典类型；默认 ownerType=TENANT，ownerId=tenantId |

### 包 `dictionary.domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItemCreateRequest` | `record` | 在类型编码下创建字典项的请求 |
| `DictionaryItemPageRequest` | `class` | 由管理控制台查询参数绑定的分页字典项查询 |
| `DictionaryItemStatusUpdateRequest` | `record` | 启用或禁用字典项的请求 |
| `DictionaryItemUpdateRequest` | `record` | 更新可变字典项字段的请求；项值不可变 |
| `DictionaryTypeCreateRequest` | `record` | 创建字典类型的请求 |
| `DictionaryTypePageRequest` | `class` | 由管理控制台查询参数绑定的分页字典类型查询 |
| `DictionaryTypeStatusUpdateRequest` | `record` | 启用或禁用字典类型的请求 |
| `DictionaryTypeUpdateRequest` | `record` | 更新可变字典类型字段的请求；类型编码不可变 |

### 包 `dictionary.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItemVo` | `record` | 管理控制台字典项视图 |
| `DictionaryTypeOptionVo` | `record` | 用于选择器的字典类型精简选项 |
| `DictionaryTypeVo` | `record` | 管理控制台字典类型视图 |

### 包 `dictionary.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItemEndpoint` | `class` | 字典项管理 REST 资源 |
| `DictionaryTypeEndpoint` | `class` | 字典类型管理 REST 资源 |

### 包 `dictionary.entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryEntryPlugin` | `class` | 贡献字典管理主页面的内置 entry 插件 |

### 包 `dictionary.operator`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryItemOperator` | `class` | 租户级字典项持久化与查询 |
| `DictionaryTypeOperator` | `class` | 租户级字典类型持久化与查询 |

### 包 `dictionary.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryService` | `class` | 字典类型与字典项的应用服务 |

### 包 `dictionary.status`

| 类 | 类型 | 说明 |
|------|------|------|
| `DictionaryStatusCode` | `enum` | 字典域状态码 |

### 包 `endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleEndpoint` | `interface` | 根管理控制台端点契约 |

### 包 `entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `BuiltinConsoleEntryPlugins` | `class` | 内置控制台 entry 插件身份常量 |
| `ConsoleModuleDescriptor` | `record` | 单个内置控制台模块 entry 插件的不可变元数据 |
| `ConsoleModuleEntrySupport` | `class` | 内置控制台模块 entry 插件的共享组装辅助工具 |

### 包 `logger`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuditLog` | `@interface` | 标记方法为审计日志操作 |
| `InvocationLogHandler` | `interface` | 通用拦截处理器端口 |
| `LogExecutor` | `class` | 所有框架适配器共享的可复用拦截例程 |

### 包 `logger.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuditLogDao` | `interface` | 仅追加审计日志记录的单表 Mapper |

### 包 `logger.domain.context`

| 类 | 类型 | 说明 |
|------|------|------|
| `InvocationLogContext` | `record` | 单次被拦截调用的框架无关描述 |

### 包 `logger.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuditLogEntity` | `class` | 管理控制台操作的仅追加审计日志；归属列记录操作发生的控制台上下文 |

### 包 `logger.entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `LoggerEntryPlugin` | `class` | 贡献审计日志管理主页面的内置 entry 插件 |

### 包 `logger.handler`

| 类 | 类型 | 说明 |
|------|------|------|
| `PersistenceInvocationLogHandler` | `class` | InvocationLogHandler 的持久化实现 |

### 包 `logger.operator`

| 类 | 类型 | 说明 |
|------|------|------|
| `InvocationLogOperator` | `class` | 将被拦截调用持久化到审计日志领域的数据操作器 |

### 包 `menu.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `MenuDao` | `interface` | 项目菜单的 MyBatis-Plus 持久化 Mapper |

### 包 `menu.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `MenuEntity` | `class` | 管理控制台菜单节点；默认 WORKSPACE 归属 |

### 包 `menu.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `MenuOpenMode` | `enum` | 打开菜单目标时使用的浏览器目标 |
| `MenuType` | `enum` | 结构与导航菜单节点类型 |

### 包 `menu.domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `MenuCreateRequest` | `record` | 创建菜单节点所需的数据 |
| `MenuOrderRequest` | `record` | 同一父节点下的有序同级菜单标识符 |
| `MenuStatusUpdateRequest` | `record` | 菜单生命周期状态更新 |
| `MenuTreeRequest` | `record` | 由查询参数绑定的管理菜单树过滤器 |
| `MenuUpdateRequest` | `record` | 现有菜单节点的可变档案数据 |

### 包 `menu.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `MenuOptionVo` | `record` | 用于父级选择器与树形控件菜单精简选项 |
| `MenuVo` | `record` | 管理控制台菜单详情及嵌套子节点 |
| `NavigationMenuVo` | `record` | 交付给管理前端的只读导航节点 |

### 包 `menu.entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `MenuEntryPlugin` | `class` | 贡献菜单管理主页面的内置 entry 插件 |

### 包 `navigation.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `NavigationMenuEndpoint` | `class` | 当前用户可见的侧边栏导航接口 |

### 包 `navigation.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `NavigationMenuAssembler` | `class` | 从持久化权限目录组装当前用户可见的导航菜单树 |

### 包 `openapi`

| 类 | 类型 | 说明 |
|------|------|------|
| `NexusConsoleOpenApiDefinition` | `class` | 控制台 OpenAPI 全局元数据（构建期扫描；非 JAX-RS Application，以便与 portal/platform 共宿主） |

### 包 `openapi.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `OpenApiSpecItemVo` | `record` | classpath 上的单个 OpenAPI 模块规范 |

### 包 `openapi.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `OpenApiCatalogEndpoint` | `class` | 构建期 OpenAPI 规范目录：列表与按 specId 读取 YAML |

### 包 `openapi.internal`

| 类 | 类型 | 说明 |
|------|------|------|
| `OpenApiBundledSpecs` | `class` | 读取 META-INF/nexus-openapi/.yaml 构建期产物 |

### 包 `openapi.operator`

| 类 | 类型 | 说明 |
|------|------|------|
| `OpenApiCatalogOperator` | `class` | 按文件名加载 OpenApiBundledSpecs 下的 OpenAPI YAML |

### 包 `openapi.scalar`

| 类 | 类型 | 说明 |
|------|------|------|
| `OpenApiScalarDocumentation` | `class` | Scalar 文档 UI 的框架无关配置与渲染，供 Spring、Quarkus 等运行时挂载 HTTP 路由时复用 |

### 包 `permission.api`

| 类 | 类型 | 说明 |
|------|------|------|
| `OrganizationUnitDirectory` | `interface` | 校验组织单元是否属于指定租户；由 portal 或 adapter 提供实现 |

### 包 `permission.authorization`

| 类 | 类型 | 说明 |
|------|------|------|
| `AuthorizationContext` | `record` | 请求鉴权通过后，传递给后续数据访问适配器的上下文 |
| `AuthorizationDecision` | `record` | 与框架无关的请求鉴权结果 |
| `AuthorizationRequest` | `record` | 由 Filter 或 REST 适配器提取的、与框架无关的请求鉴权数据 |
| `AuthorizationScope` | `class` | 通过线程上下文向数据访问适配器传递鉴权结果 |
| `AuthorizationSubject` | `record` | 由应用安全适配器提供的当前鉴权主体 |
| `AuthorizationSubjectResolver` | `interface` | 从当前请求上下文解析鉴权主体的端口 |
| `ConsolePagePermissionAuthorizer` | `class` | 控制台 catalog 页面与 datasource 代理的权限判定（框架中立） |
| `SessionAuthorizationSubjectResolver` | `class` | 从 SessionContext 与角色绑定表解析当前鉴权主体 |

### 包 `permission.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionGrantDao` | `interface` | 角色和组织单元授权记录的单表数据访问接口 |

### 包 `permission.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionGrantEntity` | `class` | 角色或组织单元对单个权限资源的授权记录；归属列表示授权生效的工作区（或租户）上下文 |

### 包 `permission.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionSubjectType` | `enum` | 可以接收权限授权的主体类型 |

### 包 `permission.domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionGrantItemRequest` | `record` | 角色或组织单元权限全量替换请求中的一条资源授权 |
| `PermissionGrantReplaceRequest` | `record` | 一个角色或组织单元最终应拥有的完整资源授权集合 |

### 包 `permission.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionResourceSyncVo` | `record` | 显式同步扩展和 UiSpec 权限目录后的处理结果 |
| `PermissionResourceVo` | `record` | 面向管理端和前端的权限资源目录视图 |

### 包 `permission.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `CurrentAuthorizationEndpoint` | `class` | 当前用户可见权限资源的 REST 资源 |
| `GrantManagementEndpoint` | `class` | 角色与组织单元权限全量替换的管理 REST 资源 |

### 包 `permission.entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `PermissionEntryPlugin` | `class` | 贡献权限管理主页面的内置 entry 插件 |

### 包 `permission.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `GrantSubjectAccess` | `class` | 校验授权主体属于当前控制台归属上下文 |
| `PermissionGrantService` | `class` | 管理角色和组织单元授权，并以全量替换方式保存授权结果 |
| `PermissionVisibilityService` | `class` | 按当前主体构建菜单、页面、action 和 datasource 的可见资源视图 |

### 包 `plugin.converter`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginManagementConverter` | `interface` | 使用 MapStruct 将 Core 管理聚合视图转换为 Console VO |

### 包 `plugin.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginManagementVo` | `record` | 插件管理页面使用的正交安装事实、运行状态和来源视图 |

### 包 `plugin.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginManagementEndpoint` | `class` | 管理端插件查询、安装、启停和失败重试接口；不提供 JAR 删除或卸载操作 |

### 包 `plugin.entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `PluginManagementEntryPlugin` | `class` | 贡献插件管理主页面的内置 entry 插件 |

### 包 `role.converter`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleConverter` | `interface` | 角色实体与控制台 VO 的 MapStruct 转换 |

### 包 `role.dao`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingDao` | `interface` | 角色绑定记录的 MyBatis-Plus Mapper |
| `RoleDao` | `interface` | 角色记录的 MyBatis-Plus Mapper |

### 包 `role.domain.entity`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingEntity` | `class` | 将 USER 或 ORG_UNIT 主体绑定到角色；行归属与角色工作区一致 |
| `RoleEntity` | `class` | 控制台角色；通过 ownerType / ownerId / securityRealm 表达归属与可见性 |

### 包 `role.domain.enums`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingSubjectType` | `enum` | 可绑定到角色的主体 |
| `RoleOwnerType` | `enum` | 拥有角色定义的层级 |

### 包 `role.domain.request`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingAddRequest` | `record` | 向角色添加 USER 或 ORG_UNIT 主体的请求 |
| `RoleBindingPageRequest` | `class` | 绑定到角色的主体的分页查询 |
| `RoleCreateRequest` | `record` | 创建由平台、租户或工作区节点拥有的角色的请求 |
| `RolePageRequest` | `class` | 由管理控制台查询参数绑定的分页角色查询 |
| `RoleStatusUpdateRequest` | `record` | 启用或禁用角色的请求 |
| `RoleUpdateRequest` | `record` | 更新可变角色档案字段的请求 |

### 包 `role.domain.vo`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingVo` | `record` | 分配管理中展示的角色绑定 |
| `RoleOptionVo` | `record` | 用于选择器与分配表单的角色精简选项 |
| `RoleVo` | `record` | 管理控制台角色视图 |

### 包 `role.endpoint`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingEndpoint` | `class` | 角色绑定 REST 资源 |
| `RoleEndpoint` | `class` | 角色生命周期与查询 REST 资源，可直接继承以扩展路由或响应包装 |

### 包 `role.entry`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleEntryPlugin` | `class` | 贡献角色管理主页面的内置 entry 插件 |

### 包 `role.operator`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleBindingOperator` | `class` | 角色与主体绑定关系维护，可供子类扩展校验或同步逻辑 |
| `RoleOperator` | `class` | 工作空间作用域内角色持久化与查询，可供子类扩展过滤或校验规则 |

### 包 `role.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleService` | `class` | 角色与绑定管理工作流入口，子类可覆写方法以组合扩展 operator 行为 |

### 包 `role.status`

| 类 | 类型 | 说明 |
|------|------|------|
| `RoleStatusCode` | `enum` | 角色与角色绑定域状态码 |

### 包 `scope`

| 类 | 类型 | 说明 |
|------|------|------|
| `ConsoleOwnership` | `record` | 控制台行级归属三元组，由会话或业务规则解析 |
| `ConsoleOwnershipGuard` | `class` | 控制台 API 入口校验会话是否满足数据归属层级要求 |
| `ConsoleOwnershipLevel` | `enum` | 控制台数据默认归属层级（业务可见性与会话要求） |
| `ConsoleOwnershipScope` | `class` | 基于 ownerType / ownerId / securityRealm 的查询隔离 |

### 包 `scope.service`

| 类 | 类型 | 说明 |
|------|------|------|
| `SessionScopeBinder` | `interface` | 将认证身份与作用域快照绑定到会话上下文；由 portal / platform 装配提供具体实现 |
| `SessionScopeTlc` | `class` | 会话 TLC / SessionContext 绑定的共享辅助方法 |


## 包参考

| 领域 | 文件 |
|------|------|
| REST 路径与方法 | [endpoint-contracts.md](references/endpoint-contracts.md) |
| 认证、作用域、凭据 | [auth-scope.md](references/auth-scope.md) |
| 目录与权限 | [catalog-permission.md](references/catalog-permission.md) |
| 角色、字典、菜单域 | [management-domains.md](references/management-domains.md) |
| 插件与入口 | [plugin-entry.md](references/plugin-entry.md) |
| 审计日志 | [logger-overview.md](references/logger-overview.md) |

## 相关模块

| 主题 | 模块 |
|-------|--------|
| 传输快照、`TLC` | `innospots-nexus-base` |
| `OwnershipEntity`、`@NexusAuthenticatedApi` | `innospots-nexus-core` |
| 插件运行时 | `innospots-nexus-plugin` |
| 租户认证、`/tenant/**` | `innospots-nexus-portal` |
| 平台认证 | `innospots-nexus-platform` |

### 全部包（`references/`）

| 包 | 参考 |
|------|------|
| `auth.api` | [`references/auth-api.md`](references/auth-api.md) |
| `auth.domain.enums` | [`references/auth-domain-enums.md`](references/auth-domain-enums.md) |
| `auth.domain.model` | [`references/auth-domain-model.md`](references/auth-domain-model.md) |
| `auth.domain.request` | [`references/auth-domain-request.md`](references/auth-domain-request.md) |
| `auth.domain.vo` | [`references/auth-domain-vo.md`](references/auth-domain-vo.md) |
| `auth.service` | [`references/auth-service.md`](references/auth-service.md) |
| `catalog.bootstrap` | [`references/catalog-bootstrap.md`](references/catalog-bootstrap.md) |
| `catalog.dao` | [`references/catalog-dao.md`](references/catalog-dao.md) |
| `catalog.domain.entity` | [`references/catalog-domain-entity.md`](references/catalog-domain-entity.md) |
| `catalog.domain.enums` | [`references/catalog-domain-enums.md`](references/catalog-domain-enums.md) |
| `catalog.domain.model` | [`references/catalog-domain-model.md`](references/catalog-domain-model.md) |
| `catalog.domain.vo` | [`references/catalog-domain-vo.md`](references/catalog-domain-vo.md) |
| `catalog.endpoint` | [`references/catalog-endpoint.md`](references/catalog-endpoint.md) |
| `catalog.service` | [`references/catalog-service.md`](references/catalog-service.md) |
| `config` | [`references/config.md`](references/config.md) |
| `credential.otp.captcha` | [`references/credential-otp-captcha.md`](references/credential-otp-captcha.md) |
| `credential.otp.dao` | [`references/credential-otp-dao.md`](references/credential-otp-dao.md) |
| `credential.otp.domain` | [`references/credential-otp-domain.md`](references/credential-otp-domain.md) |
| `credential.otp.domain.entity` | [`references/credential-otp-domain-entity.md`](references/credential-otp-domain-entity.md) |
| `credential.otp.domain.enums` | [`references/credential-otp-domain-enums.md`](references/credential-otp-domain-enums.md) |
| `credential.otp.event` | [`references/credential-otp-event.md`](references/credential-otp-event.md) |
| `credential.otp.policy` | [`references/credential-otp-policy.md`](references/credential-otp-policy.md) |
| `credential.otp.service` | [`references/credential-otp-service.md`](references/credential-otp-service.md) |
| `credential.otp.status` | [`references/credential-otp-status.md`](references/credential-otp-status.md) |
| `credential.ownership` | [`references/credential-ownership.md`](references/credential-ownership.md) |
| `credential.password` | [`references/credential-password.md`](references/credential-password.md) |
| `credential.password.algorithm` | [`references/credential-password-algorithm.md`](references/credential-password-algorithm.md) |
| `credential.password.dao` | [`references/credential-password-dao.md`](references/credential-password-dao.md) |
| `credential.password.domain.entity` | [`references/credential-password-domain-entity.md`](references/credential-password-domain-entity.md) |
| `credential.password.operator` | [`references/credential-password-operator.md`](references/credential-password-operator.md) |
| `credential.password.policy` | [`references/credential-password-policy.md`](references/credential-password-policy.md) |
| `credential.password.service` | [`references/credential-password-service.md`](references/credential-password-service.md) |
| `credential.totp` | [`references/credential-totp.md`](references/credential-totp.md) |
| `credential.totp.adapter` | [`references/credential-totp-adapter.md`](references/credential-totp-adapter.md) |
| `credential.totp.algorithm` | [`references/credential-totp-algorithm.md`](references/credential-totp-algorithm.md) |
| `credential.totp.domain` | [`references/credential-totp-domain.md`](references/credential-totp-domain.md) |
| `credential.totp.service` | [`references/credential-totp-service.md`](references/credential-totp-service.md) |
| `credential.totp.status` | [`references/credential-totp-status.md`](references/credential-totp-status.md) |
| `dictionary.converter` | [`references/dictionary-converter.md`](references/dictionary-converter.md) |
| `dictionary.dao` | [`references/dictionary-dao.md`](references/dictionary-dao.md) |
| `dictionary.domain.entity` | [`references/dictionary-domain-entity.md`](references/dictionary-domain-entity.md) |
| `dictionary.domain.request` | [`references/dictionary-domain-request.md`](references/dictionary-domain-request.md) |
| `dictionary.domain.vo` | [`references/dictionary-domain-vo.md`](references/dictionary-domain-vo.md) |
| `dictionary.endpoint` | [`references/dictionary-endpoint.md`](references/dictionary-endpoint.md) |
| `dictionary.entry` | [`references/dictionary-entry.md`](references/dictionary-entry.md) |
| `dictionary.operator` | [`references/dictionary-operator.md`](references/dictionary-operator.md) |
| `dictionary.service` | [`references/dictionary-service.md`](references/dictionary-service.md) |
| `dictionary.status` | [`references/dictionary-status.md`](references/dictionary-status.md) |
| `endpoint` | [`references/endpoint.md`](references/endpoint.md) |
| `entry` | [`references/entry.md`](references/entry.md) |
| `logger` | [`references/logger.md`](references/logger.md) |
| `logger.dao` | [`references/logger-dao.md`](references/logger-dao.md) |
| `logger.domain.context` | [`references/logger-domain-context.md`](references/logger-domain-context.md) |
| `logger.domain.entity` | [`references/logger-domain-entity.md`](references/logger-domain-entity.md) |
| `logger.entry` | [`references/logger-entry.md`](references/logger-entry.md) |
| `logger.handler` | [`references/logger-handler.md`](references/logger-handler.md) |
| `logger.operator` | [`references/logger-operator.md`](references/logger-operator.md) |
| `menu.dao` | [`references/menu-dao.md`](references/menu-dao.md) |
| `menu.domain.entity` | [`references/menu-domain-entity.md`](references/menu-domain-entity.md) |
| `menu.domain.enums` | [`references/menu-domain-enums.md`](references/menu-domain-enums.md) |
| `menu.domain.request` | [`references/menu-domain-request.md`](references/menu-domain-request.md) |
| `menu.domain.vo` | [`references/menu-domain-vo.md`](references/menu-domain-vo.md) |
| `menu.entry` | [`references/menu-entry.md`](references/menu-entry.md) |
| `navigation.endpoint` | [`references/navigation-endpoint.md`](references/navigation-endpoint.md) |
| `navigation.service` | [`references/navigation-service.md`](references/navigation-service.md) |
| `openapi` | [`references/openapi.md`](references/openapi.md) |
| `openapi.domain.vo` | [`references/openapi-domain-vo.md`](references/openapi-domain-vo.md) |
| `openapi.endpoint` | [`references/openapi-endpoint.md`](references/openapi-endpoint.md) |
| `openapi.internal` | [`references/openapi-internal.md`](references/openapi-internal.md) |
| `openapi.operator` | [`references/openapi-operator.md`](references/openapi-operator.md) |
| `openapi.scalar` | [`references/openapi-scalar.md`](references/openapi-scalar.md) |
| `permission.api` | [`references/permission-api.md`](references/permission-api.md) |
| `permission.authorization` | [`references/permission-authorization.md`](references/permission-authorization.md) |
| `permission.dao` | [`references/permission-dao.md`](references/permission-dao.md) |
| `permission.domain.entity` | [`references/permission-domain-entity.md`](references/permission-domain-entity.md) |
| `permission.domain.enums` | [`references/permission-domain-enums.md`](references/permission-domain-enums.md) |
| `permission.domain.request` | [`references/permission-domain-request.md`](references/permission-domain-request.md) |
| `permission.domain.vo` | [`references/permission-domain-vo.md`](references/permission-domain-vo.md) |
| `permission.endpoint` | [`references/permission-endpoint.md`](references/permission-endpoint.md) |
| `permission.entry` | [`references/permission-entry.md`](references/permission-entry.md) |
| `permission.service` | [`references/permission-service.md`](references/permission-service.md) |
| `plugin.converter` | [`references/plugin-converter.md`](references/plugin-converter.md) |
| `plugin.domain.vo` | [`references/plugin-domain-vo.md`](references/plugin-domain-vo.md) |
| `plugin.endpoint` | [`references/plugin-endpoint.md`](references/plugin-endpoint.md) |
| `plugin.entry` | [`references/plugin-entry.md`](references/plugin-entry.md) |
| `role.converter` | [`references/role-converter.md`](references/role-converter.md) |
| `role.dao` | [`references/role-dao.md`](references/role-dao.md) |
| `role.domain.entity` | [`references/role-domain-entity.md`](references/role-domain-entity.md) |
| `role.domain.enums` | [`references/role-domain-enums.md`](references/role-domain-enums.md) |
| `role.domain.request` | [`references/role-domain-request.md`](references/role-domain-request.md) |
| `role.domain.vo` | [`references/role-domain-vo.md`](references/role-domain-vo.md) |
| `role.endpoint` | [`references/role-endpoint.md`](references/role-endpoint.md) |
| `role.entry` | [`references/role-entry.md`](references/role-entry.md) |
| `role.operator` | [`references/role-operator.md`](references/role-operator.md) |
| `role.service` | [`references/role-service.md`](references/role-service.md) |
| `role.status` | [`references/role-status.md`](references/role-status.md) |
| `scope` | [`references/scope.md`](references/scope.md) |
| `scope.service` | [`references/scope-service.md`](references/scope-service.md) |
