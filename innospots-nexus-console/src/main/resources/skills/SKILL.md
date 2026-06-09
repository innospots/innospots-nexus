---
name: innospots-nexus-console
description: |
  Management-console extension contracts for Innospots Nexus.
  Capabilities include:
  - **JAX-RS API contracts**: Standard Jakarta JAX-RS declarations for console endpoints
  - **Extension metadata**: Menu and route contribution descriptors for management-platform extensions
version: 1.0.0
---

# innospots-nexus-console

## Module Overview

`innospots-nexus-console` is the management-console extension dependency. It
defines interface and declaration types that other modules can depend on when
they need to expose management-platform capabilities.

**What it can do:**

| Capability | Description |
|------------|-------------|
| **Console API Contract** | Define JAX-RS endpoint interfaces without choosing a runtime framework |
| **Extension Declaration** | Describe menu and route contributions from management-console extensions |

## Class Reference

### Package `api`

| Class | Type | Description |
|-------|------|-------------|
| `ConsoleEndpoint` | `interface` | Root management-console endpoint contract. |

### Package `extension`

| Class | Type | Description |
|-------|------|-------------|
| `ConsoleExtension` | `interface` | Extension contract that returns immutable console contribution metadata. |
| `ConsoleContribution` | `record` | Extension contribution descriptor containing menu and route declarations. |
| `ConsoleMenuDeclaration` | `record` | Menu metadata contributed by an extension. |
| `ConsoleRouteDeclaration` | `record` | Route metadata contributed by an extension. |

## References

| Package | Reference |
|---------|-----------|
| `api` | `references/api.md` |
| `extension` | `references/extension.md` |
