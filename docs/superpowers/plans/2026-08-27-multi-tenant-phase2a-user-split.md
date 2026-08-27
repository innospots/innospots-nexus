# Multi-Tenant Governance Phase 2a Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split users into platform vs tenant tables, move password credential SPI into console, and declare two-realm auth/register JAX-RS contracts. Console still does not persist users.

**Architecture:** console owns decrypt/hash/validation SPI, `UserDirectory` / `CredentialStore` ports, and `/platform/auth/**` + `/tenant/auth/**` contracts. platform stores `nx_platform_user*`. kernel stores `nx_tenant_user*` (evolved from `nx_user`). Kernel/platform implement the ports later; this slice lands entities, operators, and contracts.

**Tech Stack:** Java 25, Maven, Jakarta Persistence + MyBatis-Plus, Jakarta REST, JUnit 5 + AssertJ, Lombok.

**Spec:** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §6, §10.2, §12 Phase 2 (user split + credential/auth only).

## Global Constraints

- Console does not persist user rows. Kernel and platform do not issue tokens.
- No public `/platform/auth/register`. Tenant `POST /tenant/auth/register` creates identity only (no TenantMember).
- Do not reintroduce `ProjectBaseEntity` / `projectId`.
- Do not move menu/role/permission/extension/logger in this plan. Do not delete Group in this plan.
- Do not update module `SKILL.md`. Do not commit unless the user asks.
- After Java changes: `mvn clean compile`. After the slice: `mvn test`.

## Out of Scope

- AuthFacade token signing / refresh / logout implementation
- Group deletion, OrgUnit grant subject cutover
- Moving kernel menu/role/permission into console
- SupportAccessGrant

---

### Task 1: Console credential SPI

**Files:**
- Create: `console/credential/PasswordDecryptor.java`, `RsaPasswordDecryptor.java`, `PasswordValidator.java`
- Create: `console/credential/PasswordVerificationOperator.java`, `NullPasswordVerificationOperator.java`, `VerificationType.java`
- Test: `console/.../PasswordDecryptorTest.java`, `PasswordValidatorTest.java`
- Modify: kernel operators/tests to import console types
- Delete: kernel `user/tools/{UserPasswordDecryptor,RsaUserPasswordDecryptor,PasswordValidator}.java` and kernel decryptor test
- Delete: kernel `PasswordVerificationOperator`, `NullPasswordVerificationOperator`, `VerificationType` after console copies exist
- Modify: `UserPackageContractsTest` (no longer expects decryptor in kernel.tools)

Keep method names: `decrypt`, `isValid`, `MIN_LENGTH = 8`. RSA record takes Base64 PKCS#8 private key like the current kernel record.

- [x] **Step 1:** Failing console `PasswordDecryptorTest` (RSA round-trip via `CryptoUtils`) and `PasswordValidatorTest` (too short / missing class / valid).
- [x] **Step 2:** Run `mvn -pl innospots-nexus-console test -Dtest=PasswordDecryptorTest,PasswordValidatorTest` — missing types.
- [x] **Step 3:** Implement SPI; point kernel `UserOperator` / `PasswordOperator` at console types; delete kernel copies.
- [x] **Step 4:** Tests PASS.

---

### Task 2: Console auth ports and endpoints

**Files:**
- Create: `console/auth/SecurityRealm.java` (`PLATFORM`, `TENANT`)
- Create: `console/auth/AuthUser.java` record `(String userId, String loginName, String status, SecurityRealm realm)`
- Create: `console/auth/UserDirectory.java` — `Optional<AuthUser> findByLogin(SecurityRealm realm, String identity)`
- Create: `console/auth/CredentialRecord.java` record `(String userId, String passwordHash, String passwordSalt, String passwordAlgorithm, Integer failedAttempts, java.time.LocalDateTime lockedUntil, Boolean forceReset)`
- Create: `console/auth/CredentialStore.java` — `Optional<CredentialRecord> findPassword(SecurityRealm realm, String userId)` and `void updatePassword(SecurityRealm realm, CredentialRecord credential)`
- Create: `console/auth/MembershipDirectory.java` — `List<String> listActiveTenantIds(String tenantUserId)`
- Create: request/vo + `PlatformAuthEndpoint` `@Path("/platform/auth")` login/refresh/logout/password change+reset; **no register**
- Create: `TenantAuthEndpoint` `@Path("/tenant/auth")` register/login/select-tenant/refresh/logout/password change+reset
- Tests: contract tests for paths and record components

Login request: `login` (user_name or email or mobile), `encryptedPassword`.
Register request: `userName`, `displayName`, `email`, `mobile`, `region`, `timeZone`, `language`, `encryptedPassword`.
Token vo: `realm`, `tokenType` (`IDENTITY` | `BUSINESS`), `accessToken`, `refreshToken`, `tenantId`, `tenantMemberId`.

- [x] **Step 1–4:** TDD endpoint contracts.

---

### Task 3: Kernel tenant user tables

Evolve existing kernel user persistence:

| old | new |
|-----|-----|
| `nx_user` / `userId` prefix `usr` | `nx_tenant_user` / `tenantUserId` prefix `tus` |
| `nx_user_password` / `userId` | `nx_tenant_user_password` / `tenantUserId` |
| `nx_user_oauth` / `userId` | `nx_tenant_user_oauth` / `tenantUserId` |

`TenantUserEntity` fields per spec: `userName` 64 not null unique, `displayName` 128, `email` 128 unique, `mobile` 32 unique, `region` 32, `timeZone` 64, `language` 32, `avatarKey` 256, `registerSource` 32 not null, `status` 32 not null, `emailVerified`/`mobileVerified` Boolean not null, `lastLoginTime`, `lastLoginIp` 64. **No `realName`.** Drop `locale`.

Java type names stay `UserEntity` / `UserOperator` / `UserProfileVO` in this slice; tables and PK fields match the spec (`nx_tenant_user`, `tenantUserId`, prefix `tus`). Update register request (drop `realName`; add `region`, `timeZone`, `language`).

- [x] **Step 1:** Rewrite `UserEntityContractsTest` for tenant-user tables (will fail).
- [x] **Step 2:** Confirm RED.
- [x] **Step 3:** Replace entities/operators/tests.
- [x] **Step 4:** Kernel user tests PASS.

---

### Task 4: Platform user tables and admin create

**Files:** platform `user` domain mirroring tenant password/oauth shape with `platformUserId` prefix `pus`, table `nx_platform_user`.

Fields: `loginName` 64 unique not null, `displayName` 128, `email` 128, `mobile` 32, `employeeNo` 64, `status` 32 not null.

Password: `nx_platform_user_password`, FK `platformUserId`, prefix `ppc`.
OAuth: `nx_platform_user_oauth`, prefix `poi`.

`PlatformUserOperator.createWithPassword` — admin-only persist; uses console `PasswordDecryptor`. No public register.

`PlatformUserEndpoint` `@Path("/platform/users")` POST create, GET by id.

- [x] **Step 1–4:** TDD entities, DAOs, operator, endpoint.

---

### Task 5: Verification

- [x] `mvn clean compile`
- [x] `mvn test`
- [x] Confirm kernel has no `nx_user` table name and console has no user entities.
