# Multi-Tenant Governance Phase 2d Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Land console dictionary persistence and JAX-RS contracts, and replace leftover role-member APIs with `nx_role_binding` contracts.

**Architecture:** Console owns dictionary catalogs isolated by `security_realm` + `workspace_id`. Types and items are separate records. Role assignment uses `RoleBindingEntity` (`USER | ORG_UNIT`), not a user-member list.

**Tech Stack:** Java 25, Maven, Jakarta Persistence + MyBatis-Plus, Jakarta REST, JUnit 5 + AssertJ, Lombok.

**Spec:** [docs/design/multi-tenant-governance-design.md](../../design/multi-tenant-governance-design.md) §9.3, §9.8, §10.1, §10.5, §12 Phase 2.

## Global Constraints

- Domain then responsibility packages; requests/VOs under `domain.request` / `domain.vo`.
- Concrete endpoint classes (not interfaces) for new domains.
- Dictionary entities extend `WorkspaceBaseEntity`. Persist `securityRealm` as String 32, not null.
- Reuse `console.auth.domain.enums.SecurityRealm`. Reuse `RoleBindingSubjectType`.
- Do not update module `SKILL.md`. Do not commit unless asked.
- After Java changes: `mvn clean compile`. After the slice: `mvn test`.

---

### Task 1: Dictionary entities

**Files:**
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/dictionary/domain/entity/DictionaryEntityContractsTest.java`
- Create: `DictionaryTypeEntity.java`, `DictionaryItemEntity.java`

`nx_dictionary_type` (`dct`): `dictionaryTypeId`, `typeCode` 64, `typeName` 128, `securityRealm` 32, `status` 32, `sortOrder`, `builtIn`. Unique `(workspace_id,security_realm,type_code)`.

`nx_dictionary_item` (`dci`): `dictionaryItemId`, `typeCode` 64, `itemValue` 64, `itemName` 128, `securityRealm` 32, `status` 32, `sortOrder`, `builtIn`. Unique `(workspace_id,security_realm,type_code,item_value)`.

- [x] Completed

### Task 2: Dictionary DAOs

`DictionaryTypeDao`, `DictionaryItemDao` extend `BaseMapper`. Contract test: interfaces + BaseMapper.

- [x] Completed

### Task 3: Dictionary requests, VOs, endpoints

`DictionaryTypeEndpoint` `@Path("/console/dictionary-types")` page/get/create/update/status/delete/options.

`DictionaryItemEndpoint` `@Path("/console/dictionary-types/{typeCode}/items")` page/create/update/status/delete.

Concrete classes throw `UnsupportedOperationException` until operators exist (same as `MenuEndpoint`).

- [x] Completed

### Task 4: Role binding endpoint replaces role members

Replace `RoleMemberEndpoint` and `RoleMember*` request/VO with `RoleBindingEndpoint` `@Path("/console/roles/{roleId}/bindings")` using `RoleBindingSubjectType`. Update `RoleEndpointContractsTest`.

- [x] Completed

### Task 5: Verify

`mvn clean compile` && `mvn test`.

- [x] Completed
