# 多租户治理 Phase 2a 实施计划

> **面向 agent 工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐步实施本计划。步骤使用 checkbox（`- [ ]`）语法跟踪进度。

**目标：** 将 user 拆分为 platform 与 tenant 表，将 password credential SPI 迁入 console，并声明双 realm auth/register JAX-RS 契约。Console 仍不持久化 users。

**架构：** console 拥有 decrypt/hash/validation SPI、`UserDirectory` / `CredentialStore` port，以及 `/platform/auth/**` + `/tenant/auth/**` 契约。platform 存储 `nx_platform_user*`。kernel 存储 `nx_tenant_user*`（由 `nx_user` 演进）。Kernel/platform 稍后实现 port；本 slice 落地 entity、operator 与契约。

**技术栈：** Java 25、Maven、Jakarta Persistence + MyBatis-Plus、Jakarta REST、JUnit 5 + AssertJ、Lombok。

**规格：** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §6、§10.2、§12 Phase 2（仅 user split + credential/auth）。

## 全局约束

- Console 不持久化 user 行。Kernel 与 platform 不签发 token。
- 无公开 `/platform/auth/register`。Tenant `POST /tenant/auth/register` 仅创建 identity（无 TenantMember）。
- 不得重新引入 `ProjectBaseEntity` / `projectId`。
- 本计划不迁移 menu/role/permission/extension/logger。本计划不删除 Group。
- 不更新模块 `SKILL.md`。除非用户要求，否则不 commit。
- Java 变更后：`mvn clean compile`。本 slice 完成后：`mvn test`。

## 不在范围

- AuthFacade token 签名 / refresh / logout 实现
- Group 删除、OrgUnit grant subject 切换
- 将 kernel menu/role/permission 迁入 console
- SupportAccessGrant

---

### Task 1：Console credential SPI

**文件：**
- Create: `console/credential/PasswordDecryptor.java`, `RsaPasswordDecryptor.java`, `PasswordValidator.java`
- Create: `console/credential/PasswordVerificationOperator.java`, `NullPasswordVerificationOperator.java`, `VerificationType.java`
- Test: `console/.../PasswordDecryptorTest.java`, `PasswordValidatorTest.java`
- Modify: kernel operator/test 改为 import console 类型
- Delete: kernel `user/tools/{UserPasswordDecryptor,RsaUserPasswordDecryptor,PasswordValidator}.java` 及 kernel decryptor test
- Delete: console 副本存在后删除 kernel `PasswordVerificationOperator`, `NullPasswordVerificationOperator`, `VerificationType`
- Modify: `UserPackageContractsTest`（不再期望 decryptor 在 kernel.tools）

保留方法名：`decrypt`、`isValid`、`MIN_LENGTH = 8`。RSA record 接受 Base64 PKCS#8 private key，与当前 kernel record 相同。

- [x] **Step 1：** 失败的 console `PasswordDecryptorTest`（通过 `CryptoUtils` 的 RSA round-trip）与 `PasswordValidatorTest`（过短 / 缺 class / 有效）。
- [x] **Step 2：** 运行 `mvn -pl innospots-nexus-console test -Dtest=PasswordDecryptorTest,PasswordValidatorTest` — 类型缺失。
- [x] **Step 3：** 实现 SPI；kernel `UserOperator` / `PasswordOperator` 指向 console 类型；删除 kernel 副本。
- [x] **Step 4：** 测试 PASS。

---

### Task 2：Console auth port 与 endpoint

**文件：**
- Create: `console/auth/SecurityRealm.java`（`PLATFORM`, `TENANT`）
- Create: `console/auth/AuthUser.java` record `(String userId, String loginName, String status, SecurityRealm realm)`
- Create: `console/auth/UserDirectory.java` — `Optional<AuthUser> findByLogin(SecurityRealm realm, String identity)`
- Create: `console/auth/CredentialRecord.java` record `(String userId, String passwordHash, String passwordSalt, String passwordAlgorithm, Integer failedAttempts, java.time.LocalDateTime lockedUntil, Boolean forceReset)`
- Create: `console/auth/CredentialStore.java` — `Optional<CredentialRecord> findPassword(SecurityRealm realm, String userId)` 与 `void updatePassword(SecurityRealm realm, CredentialRecord credential)`
- Create: `console/auth/MembershipDirectory.java` — `List<String> listActiveTenantIds(String tenantUserId)`
- Create: request/vo + `PlatformAuthEndpoint` `@Path("/platform/auth")` login/refresh/logout/password change+reset；**无 register**
- Create: `TenantAuthEndpoint` `@Path("/tenant/auth")` register/login/select-tenant/refresh/logout/password change+reset
- Tests: path 与 record component 的契约测试

Login request: `login`（user_name 或 email 或 mobile）、`encryptedPassword`。
Register request: `userName`, `displayName`, `email`, `mobile`, `region`, `timeZone`, `language`, `encryptedPassword`。
Token vo: `realm`, `tokenType`（`IDENTITY` | `BUSINESS`）, `accessToken`, `refreshToken`, `tenantId`, `tenantMemberId`。

- [x] **Step 1–4：** TDD endpoint 契约。

---

### Task 3：Kernel tenant user 表

演进现有 kernel user 持久化：

| 旧 | 新 |
|-----|-----|
| `nx_user` / `userId` prefix `usr` | `nx_tenant_user` / `tenantUserId` prefix `tus` |
| `nx_user_password` / `userId` | `nx_tenant_user_password` / `tenantUserId` |
| `nx_user_oauth` / `userId` | `nx_tenant_user_oauth` / `tenantUserId` |

`TenantUserEntity` 字段按规格：`userName` 64 not null unique, `displayName` 128, `email` 128 unique, `mobile` 32 unique, `region` 32, `timeZone` 64, `language` 32, `avatarKey` 256, `registerSource` 32 not null, `status` 32 not null, `emailVerified`/`mobileVerified` Boolean not null, `lastLoginTime`, `lastLoginIp` 64。**无 `realName`。** 删除 `locale`。

本 slice 中 Java 类型名仍为 `UserEntity` / `UserOperator` / `UserProfileVO`；表与 PK 字段符合规格（`nx_tenant_user`, `tenantUserId`, prefix `tus`）。更新 register request（删除 `realName`；添加 `region`, `timeZone`, `language`）。

- [x] **Step 1：** 重写 `UserEntityContractsTest` 以适配 tenant-user 表（将失败）。
- [x] **Step 2：** 确认 RED。
- [x] **Step 3：** 替换 entity/operator/test。
- [x] **Step 4：** Kernel user 测试 PASS。

---

### Task 4：Platform user 表与 admin create

**文件：** platform `user` domain，镜像 tenant password/oauth 形状，使用 `platformUserId` prefix `pus`，表 `nx_platform_user`。

字段：`loginName` 64 unique not null, `displayName` 128, `email` 128, `mobile` 32, `employeeNo` 64, `status` 32 not null。

Password: `nx_platform_user_password`, FK `platformUserId`, prefix `ppc`。
OAuth: `nx_platform_user_oauth`, prefix `poi`。

`PlatformUserOperator.createWithPassword` — 仅 admin 持久化；使用 console `PasswordDecryptor`。无公开 register。

`PlatformUserEndpoint` `@Path("/platform/users")` POST create, GET by id。

- [x] **Step 1–4：** TDD entity、DAO、operator、endpoint。

---

### Task 5：验证

- [x] `mvn clean compile`
- [x] `mvn test`
- [x] 确认 kernel 无 `nx_user` 表名，console 无 user entity。
