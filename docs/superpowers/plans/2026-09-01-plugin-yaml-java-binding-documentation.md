# Plugin YAML Java Binding Documentation Implementation Plan

> 本计划已由拆分后的 `innospots-nexus-core/docs/plugin-dsl-spec.md` 和
> `plugin-v1-implementation-plan.md` 收敛，保留为历史执行记录，不再作为现行实施依据。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Revise the unified plugin design so YAML declares JVM in-process implementations through `bind.kind: java` and an explicit implementation class, with complete rules and examples for every bind kind.

**Architecture:** Keep Java plugins bound through `CapabilityProviderFactory`. Compile YAML `java`, `http`, `process`, `mcp`, and `contract` bindings into the same capability contribution model before catalog publication. Identify multiple implementations with `providerId`, resolve Java API contracts from `type@majorVersion`, and reserve tags for runtime routing.

**Tech Stack:** Markdown design documentation, YAML examples, Java API pseudocode

---

### Task 1: Normalize binding terminology

**Files:**
- Modify: `innospots-nexus-core/docs/plugin-extension-design.md`

- [x] Replace YAML/JVM statements that prohibit class names with the scoped rule that only `bind.kind: java` accepts `class`.
- [x] Replace `inprocess` as a YAML bind kind with `java`; retain Java Factory as the Java declaration surface.
- [x] State that YAML classes are loaded explicitly without SPI, annotation scanning, or package scanning.

### Task 2: Document all bind kinds

**Files:**
- Modify: `innospots-nexus-core/docs/plugin-extension-design.md`

- [x] Add a common-field table covering `type`, `majorVersion`, `providerId`, `tags`, `bind`, and `exposures`.
- [x] Add required-field, execution, validation, and example sections for `java`, `http`, `process`, `mcp`, and `contract`.
- [x] Explain that `bind.kind` selects the implementation adapter while `exposures[].kind` publishes external entry points.

### Task 3: Align multi-provider identity and routing

**Files:**
- Modify: `innospots-nexus-core/docs/plugin-extension-design.md`

- [x] Define provider identity as `(pluginId, CapabilityKey, providerId)`.
- [x] Permit one plugin to declare multiple providers for the same `CapabilityKey` when `providerId` differs.
- [x] Keep `providerId` for identity/configuration/diagnostics and tags for runtime selection.
- [x] Update configuration namespaces, validation requirements, migration notes, and acceptance criteria consistently.

### Task 4: Verify documentation consistency

**Files:**
- Verify: `innospots-nexus-core/docs/plugin-extension-design.md`

- [x] Search for obsolete claims that YAML never contains classes, that `inprocess` is a YAML kind, or that duplicate `CapabilityKey` is always forbidden.
- [x] Run `git diff --check` and inspect the final diff; no Java compilation is required because no Java source is changed.
