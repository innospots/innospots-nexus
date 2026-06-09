---
name: innospots-nexus-kernel
description: |
  Functional kernel domain contracts for Innospots Nexus management capabilities.
  Capabilities include:
  - **Identity and access**: Auth, users, groups, roles, and permissions
  - **Management catalog**: Menus, categories, dictionaries, and i18n dictionary entries
  - **Operations support**: Login audit logs, notifications, and system configuration
version: 1.0.0
---

# innospots-nexus-kernel

## Module Overview

`innospots-nexus-kernel` owns the functional domain contracts for management
platform capabilities. It depends on `innospots-nexus-console` for management
extension declarations and uses JPA/MyBatis-Plus entity annotations where a
kernel domain needs a stable persistence shape, but it does not bind to Spring,
Quarkus, Servlet, or a concrete web runtime.

**What it can do:**

| Capability | Description |
|------------|-------------|
| **Auth** | Model authentication sessions and authentication operations |
| **Identity** | Model users, registration credentials, OAuth identities, groups/departments, roles, and permissions |
| **Console Catalog** | Model menus and categories used by management functions |
| **Dictionary** | Model platform dictionary entries and i18n dictionary entries |
| **Operations** | Model login logs, notification messages, and system configuration |

## Class Reference

### Package `domain`

| Class | Type | Description |
|-------|------|-------------|
| `KernelDomain` | `enum` | Stable list of kernel-owned business domains. |

### Domain Packages

| Package | Classes |
|---------|---------|
| `auth` | `AuthSession`, `AuthGrantType`, `AuthOperator` |
| `menu` | `MenuItem`, `MenuItemType`, `MenuOperator` |
| `audit` | `LoginLog`, `LoginResult`, `LoginLogOperator` |
| `user` | `KernelUser`, `UserStatus` |
| `user.api` | `UserPasswordDecryptor`, `RsaUserPasswordDecryptor` |
| `user.dao` | `UserDao`, `UserPasswordCredentialDao`, `UserOauthIdentityDao` |
| `user.operator` | `UserOperator` |
| `group` | `UserGroup`, `GroupType`, `GroupOperator` |
| `role` | `Role`, `RoleScope`, `RoleOperator` |
| `permission` | `Permission`, `PermissionEffect`, `PermissionOperator` |
| `dictionary` | `DictionaryEntry`, `DictionaryState`, `DictionaryOperator` |
| `notification` | `NotificationMessage`, `NotificationChannel`, `NotificationStatus`, `NotificationOperator` |
| `config` | `SystemConfig`, `ConfigValueType`, `SystemConfigOperator` |
| `i18n` | `I18nDictionaryEntry`, `I18nEntryScope`, `I18nDictionaryOperator` |
| `category` | `Category`, `CategoryType`, `CategoryOperator` |

### Package `user.domain.entity`

| Class | Type | Description |
|-------|------|-------------|
| `UserEntity` | `class` | User registration profile and lifecycle persistence shape. |
| `UserPasswordCredentialEntity` | `class` | Local password credential storage kept separate from user profile data. |
| `UserOauthIdentityEntity` | `class` | OAuth provider identity binding for externally registered users. |

### Package `user.api`

| Class | Type | Description |
|-------|------|-------------|
| `UserPasswordDecryptor` | `interface` | Contract for decrypting frontend password ciphertext before server-side hashing. |
| `RsaUserPasswordDecryptor` | `record` | RSA decryptor backed by base module cryptographic utilities. |

### Package `user.operator`

| Class | Type | Description |
|-------|------|-------------|
| `UserOperator` | `class` | User data operator backed by DAO implementations. |

### Package `user.domain.request`

| Class | Type | Description |
|-------|------|-------------|
| `UserPasswordRegisterRequest` | `record` | Request for registering a user with a frontend encrypted password. |
| `UserOauthRegisterRequest` | `record` | Request for registering a user with an external OAuth identity. |
| `UserPageRequest` | `class` | Request for paginating users by input, user name, real name, email, and mobile. |

### Package `user.domain.vo`

| Class | Type | Description |
|-------|------|-------------|
| `UserProfileVO` | `record` | User profile read model returned by user data operations. |

### Package `user.dao`

| Class | Type | Description |
|-------|------|-------------|
| `UserDao` | `interface` | MyBatis-Plus mapper for user profile records. |
| `UserPasswordCredentialDao` | `interface` | MyBatis-Plus mapper for local password credential records. |
| `UserOauthIdentityDao` | `interface` | MyBatis-Plus mapper for OAuth identity binding records. |

### Package `user.enums`

| Class | Type | Description |
|-------|------|-------------|
| `UserRegisterSource` | `enum` | Registration source values for password and OAuth sign-up. |

## References

| Package | Reference |
|---------|-----------|
| `domain` | `references/domain.md` |
| `user.api` | `references/user-api.md` |
| `user.domain.entity` | `references/user-domain-entity.md` |
| `user.domain.request` | `references/user-domain-request.md` |
| `user.domain.vo` | `references/user-domain-vo.md` |
| `user.dao` | `references/user-dao.md` |
| `user.operator` | `references/user-operator.md` |
| `user.enums` | `references/user-enums.md` |
| Kernel business domains | `references/business-domain.md` |
