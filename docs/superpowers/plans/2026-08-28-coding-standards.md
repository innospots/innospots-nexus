# Coding Standards Improvement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the existing Nexus development standards into a coherent,
code-informed guide for naming, Java source construction, documentation, API
design, and domain initialization.

**Architecture:** Keep each concern in its existing authoritative standards
file, use cross-references instead of duplicating rules, and preserve all
module boundaries from `AGENTS.md`. Derive examples from current source names
without renaming source code or treating historical inconsistencies as
requirements.

**Tech Stack:** Markdown, Java 25 project conventions, Jakarta REST/JPA,
MyBatis-Plus, MapStruct, Lombok, Maven verification policy.

---

### Task 1: Expand the naming decision system

**Files:**
- Modify: `standards/naming.md`

- [ ] **Step 1: Preserve existing project-specific suffix and package rules**

Review the current type table, domain-first package layout, and file naming
rules. Retain `Endpoint`, `Dao`, `Entity`, `Request`, `Vo`, `Converter`,
`StatusCode`, `Event`, and `EventHandler` conventions.

- [ ] **Step 2: Add the naming derivation rules**

Add business vocabulary, qualifier ordering, technical responsibility
suffixes, request/view names, method verbs, fields and parameters, boolean and
collection names, time/unit names, acronym casing, persistence names, and test
names. Use current examples such as `PlatformUserPasswordEntity`,
`RoleStatusUpdateRequest`, `CapabilityRegistry`, `PluginManager`,
`ClasspathPluginDiscovery`, and `hasAvailableThread`.

- [ ] **Step 3: Add negative examples and a naming checklist**

Make vague names (`CommonUtils`, `DataManager`, `process`, `flag`) and redundant
names (`RoleBusinessService`, `RoleDataDao`) explicitly discouraged. End with a
short decision checklist.

- [ ] **Step 4: Validate the naming document**

Run:

```bash
rg -n '^## ' standards/naming.md
git diff --check -- standards/naming.md
```

Expected: all required naming sections are present and the diff check prints no
errors.

### Task 2: Clarify Java source and Lombok style

**Files:**
- Modify: `standards/code-style.md`

- [ ] **Step 1: Add source layout and multiline formatting rules**

Document member order, one declaration per line, annotation placement, method
chain wrapping, record formatting, lambda clarity, and local-variable scope.

- [ ] **Step 2: Correct and clarify Lombok rules**

Require `@Getter`/`@Setter` for mutable persistence entities and configuration
binders, allow immutable or behavior-oriented internal models to expose only
the accessors they need, and preserve `@Slf4j` plus constructor injection
guidance.

- [ ] **Step 3: Add implementation hygiene rules**

Cover dependency fields, logging, immutable collection exposure, magic values,
and avoidance of `System.out` or hidden global mutable state. Cross-reference
API design for architectural boundaries.

- [ ] **Step 4: Validate the style document**

Run:

```bash
rg -n '^## ' standards/code-style.md
git diff --check -- standards/code-style.md
```

Expected: new source layout and implementation sections exist and no whitespace
errors are reported.

### Task 3: Expand comment and Javadoc conventions

**Files:**
- Modify: `standards/code-comments.md`

- [ ] **Step 1: Define documentation ownership**

Add package-level documentation, contract/implementation documentation, record
component and enum documentation, overridden-method rules, and public API
expectations.

- [ ] **Step 2: Define useful inline and TODO comments**

Require comments for rationale, lifecycle, concurrency, security, compatibility,
and non-obvious side effects. Require focused TODOs that identify missing
behavior or boundary; prohibit commented-out code and change-history comments.

- [ ] **Step 3: Validate the comment document**

Run:

```bash
rg -n '^## ' standards/code-comments.md
git diff --check -- standards/code-comments.md
```

Expected: package, override, record/enum, and TODO guidance is discoverable and
the diff check is clean.

### Task 4: Complete API design guidance

**Files:**
- Modify: `standards/api-design.md`

- [ ] **Step 1: Clarify contracts and dependency direction**

Document when interfaces are justified, implementation naming, constructor
injection, dependency-field ownership, and public-contract stability.

- [ ] **Step 2: Clarify nullability, validation, and collection contracts**

Keep `Optional` for application-facing absence, explicitly allow nullable
framework DAO results where required, forbid `Optional` parameters and fields,
define immutable empty collections, and assign validation/normalization to the
correct boundary.

- [ ] **Step 3: Add behavior and lifecycle contracts**

Define query/command semantics, idempotency, lifecycle state, resource cleanup,
thread-safety documentation, and event/callback ownership using current plugin,
event, session, and resource patterns.

- [ ] **Step 4: Validate the API document**

Run:

```bash
rg -n '^## ' standards/api-design.md
git diff --check -- standards/api-design.md
```

Expected: contract, nullability, validation, lifecycle, and compatibility
sections exist with no whitespace errors.

### Task 5: Align the domain initialization workflow

**Files:**
- Modify: `standards/domain-module-initialization.md`

- [ ] **Step 1: Add terminology and naming decisions to the first gate**

Require domain vocabulary, ownership qualifiers, stable-key terminology, and
planned type-role suffixes before source types are created.

- [ ] **Step 2: Align later stage gates through cross-references**

Reference the authoritative naming, style, comments, and API rules when
checking entities, requests, views, converters, endpoints, operators, services,
and tests. Preserve the existing compile and full-verification commands.

- [ ] **Step 3: Validate the workflow document**

Run:

```bash
rg -n 'naming.md|code-style.md|code-comments.md|api-design.md' standards/domain-module-initialization.md
git diff --check -- standards/domain-module-initialization.md
```

Expected: all four authoritative standards are referenced and no whitespace
errors are reported.

### Task 6: Cross-file consistency and scope verification

**Files:**
- Verify: `standards/naming.md`
- Verify: `standards/code-style.md`
- Verify: `standards/code-comments.md`
- Verify: `standards/api-design.md`
- Verify: `standards/domain-module-initialization.md`
- Verify unchanged: `standards/module-skills.md`

- [ ] **Step 1: Check forbidden placeholders and stale contradictions**

Run focused searches for placeholder language, conflicting Lombok requirements,
ambiguous `Optional` requirements, and inconsistent `VO`/`Vo` spellings.

- [ ] **Step 2: Check scope and formatting**

Run:

```bash
git diff --check
git status --short
git diff --stat
git diff -- standards
```

Expected: only the five approved standards files plus design/plan tracking are
part of this task; existing unrelated Java changes remain untouched.

- [ ] **Step 3: Record verification outcome**

Confirm that no Java source changed, so the repository's mandatory
`mvn clean compile` gate is not triggered. If any Java source is unexpectedly
present in this task's diff, stop and separate it before completion.
