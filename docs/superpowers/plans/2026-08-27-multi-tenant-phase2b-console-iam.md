# Multi-Tenant Governance Phase 2b Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move console-owned IAM (role/menu/permission) out of kernel, fix domain package layout, stop Group, and land AuthFacade plus platform SupportAccessGrant.

**Architecture:** Console owns the role engine, menu catalog, permission catalog/grants, credential SPI, and token issuance. Kernel keeps tenant-user storage, membership, org, workspace, and extension-driven permission *sync* (uses console DAOs). Platform keeps ops users, tenant lifecycle, and support access. Kernel and platform never depend on each other.

**Tech Stack:** Java 25, Maven, Jakarta Persistence + MyBatis-Plus, Jakarta REST, JUnit 5 + AssertJ, Lombok.

**Spec:** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §6.8, §7, §11.3–11.4, §12 Phase 2.

## Global Constraints

- Package by domain then responsibility: `endpoint`, `dao`, `operator`, `service`, `api`, `domain/{entity,request,vo,model,enums,event}`.
- Requests/VOs live under `domain.request` / `domain.vo`, never directly under the domain root or a sibling `request`/`vo` package.
- Console does not persist users. Kernel/platform do not issue tokens.
- No `ProjectBaseEntity` / `projectId`. Permission subjects: `ROLE | ORG_UNIT` only.
- Do not update module `SKILL.md`. Do not commit unless asked.
- After Java changes: `mvn clean compile`. After the slice: `mvn test`.

## Target console layout (auth example)

```text
console.auth
  ├── api/                 UserDirectory, CredentialStore, MembershipDirectory
  ├── endpoint/            PlatformAuthEndpoint, TenantAuthEndpoint
  ├── service/             AuthFacade, TokenIssuer
  └── domain
      ├── enums/           SecurityRealm
      ├── model/           AuthUser, CredentialRecord
      ├── request/         AuthLoginRequest, ...
      └── vo/              AuthTokenVo
```

Kernel leftover after the move:

```text
kernel.permission.service.PermissionResourceSyncService  (uses console DAOs + kernel ExtensionRegistry)
```

---

### Task 1: Fix auth/credential package layout

Move Phase 2a types into `api` / `endpoint` / `domain.*`. Move `VerificationType` to `credential.domain.enums`. Rename `UserProfileVO` → `UserProfileVo`.

### Task 2: Console persistence + move role/menu/permission

Add JPA / MyBatis-Plus / transaction APIs to console. Relocate kernel `role`, `menu`, `permission` (except sync service) and matching tests to `com.innospots.nexus.console.*`.

### Task 3: Role owner + role binding

Add `RoleOwnerType` (`PLATFORM|TENANT|WORKSPACE`) and `ownerType`/`ownerId` on `RoleEntity`. Replace `nx_user_role` / `UserRoleEntity` with `nx_role_binding` / `RoleBindingEntity` (`subjectType` USER|ORG_UNIT).

### Task 4: Delete Group; grant subject ORG_UNIT

Delete kernel `group` domain. `PermissionSubjectType` = `ROLE | ORG_UNIT`. `AuthorizationSubject.groupIds` → `orgUnitIds`.

### Task 5: AuthFacade token issuance

`AuthFacade` login/select-tenant/refresh/logout using directory ports + AES-GCM compact tokens (`CryptoUtils.encryptAesGcm`). No user persistence in console.

### Task 6: Platform SupportAccessGrant

`nx_support_access_grant` in platform (`support` domain, proper `domain.entity` / `dao` / `endpoint`).

### Task 7: Verify

`mvn clean compile` && `mvn test`. No kernel `role`/`menu`/`group` production packages. Console has no user entities.
