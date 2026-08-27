# Extension Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Move extension contracts into `innospots-nexus-core`, remove the old console extension package, and implement page-URL role interception in `innospots-nexus-console`.

**Architecture:** Core owns framework-neutral extension descriptors, page/menu declarations, provider discovery, and lifecycle state. Console owns the active page-URL registry and a Jakarta REST request filter contract that validates the page-source header, URL template, and current user roles. Existing unrelated permission-domain work remains untouched.

**Tech Stack:** Java 25, Java records, Jakarta REST, Java SPI, JUnit 5, AssertJ, Maven.

---

### Task 1: Add core extension declaration contracts

**Files:**
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/contract/ConsoleExtensionProvider.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/contract/ConsoleExtensionEntry.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/ExtensionDescriptor.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/ExtensionModuleDeclaration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/PageDslDeclaration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/declaration/MenuDeclaration.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/extension/ExtensionDeclarationTest.java`

- [x] Write tests for immutable collections, qualified page identity, menu node validation, and module/page key validation.
- [x] Run the focused contract test before implementation; it failed because the contracts did not exist.
- [x] Implement the records and provider annotation with defensive copies and validation through `NexusException`.
- [x] Run the focused test again and verify it passes.

### Task 2: Add core provider discovery and lifecycle registry

**Files:**
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/lifecycle/ExtensionState.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/lifecycle/ExtensionRegistration.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/discovery/ExtensionProviderDiscovery.java`
- Create: `innospots-nexus-core/src/main/java/com/innospots/nexus/core/extension/lifecycle/ExtensionRegistry.java`
- Test: `innospots-nexus-core/src/test/java/com/innospots/nexus/core/extension/ExtensionRegistryTest.java`

- [x] Write tests for direct provider registration, Java SPI discovery, duplicate extension rejection, default enabled state, disable/activate transitions, and failed activation on unknown menu pages or conflicting paths.
- [x] Run the focused registry test before implementation; it failed for missing types.
- [x] Implement discovery without unrestricted classpath scanning: direct instances, `ServiceLoader`, and explicitly supplied annotated classes only.
- [x] Implement atomic registration, activation, disable, state lookup, and active descriptor snapshots.
- [x] Run the focused registry test and verify it passes.

### Task 3: Add console page-URL permission contracts

**Files:**
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlPermissionKey.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlPermissionRegistry.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/CurrentUserRoleProvider.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlRolePermissionChecker.java`
- Create: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/permission/PageUrlPermissionInterceptor.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/PageUrlPermissionRegistryTest.java`
- Test: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/permission/PageUrlPermissionInterceptorTest.java`

- [x] Write tests for URL normalization, named path-variable matching, duplicate URL deduplication within one page, independent permissions for the same URL on different pages, missing/invalid page header denial, and role-based allow/deny.
- [x] Run the focused tests before implementation; they failed for missing types.
- [x] Implement the immutable composite permission key `(moduleKey, pageKey, urlPattern)` and registry.
- [x] Implement the interceptor using `X-Nexus-Page-Key`, current roles from an injected provider, and an injected checker; query strings and HTTP methods do not affect the key.
- [x] Run focused tests and verify they pass.

### Task 4: Remove old console extension package and detach legacy references

**Files:**
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleExtension.java`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleContribution.java`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleMenuDeclaration.java`
- Delete: `innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension/ConsoleRouteDeclaration.java`
- Delete: `innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension/ConsoleExtensionTest.java`
- Modify: legacy `console/permission` references that import the deleted package.

- [x] Replace only imports and tests that directly depend on the deleted package; unrelated user changes remain untouched.
- [x] Run the console compile; no Java reference to the deleted package remains.
- [x] Run the console test suite and fix migration errors caused by the package removal.

### Task 5: Verify module boundaries and documentation

**Files:**
- Modify: `innospots-nexus-core/docs/extension-design.md` only if implementation names or boundaries differ from the approved design.

- [x] Run `mvn -pl innospots-nexus-core,innospots-nexus-console -am test`.
- [x] Run `mvn clean compile` after all Java changes.
- [x] Run `mvn validate`, `mvn test`, and `git diff --check`; effective-POM generation is blocked by the sandbox's read-only Maven resolver status path.
- [x] Confirm core has no Jakarta REST dependency and console owns the permission interceptor.
- [x] Confirm the old `console/extension` Java package and all direct Java references are gone.
