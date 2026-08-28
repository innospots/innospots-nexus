# Exception and Status Code Standards Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a coherent, code-informed standard for exceptions, platform and
domain status codes, and safe status-code extension.

**Architecture:** Create one authoritative `exception-status-code.md` document,
then keep API and domain-initialization documents concise by linking to it and
adding their local gates. Preserve current module boundaries and existing Java
behavior; this is a documentation-only change.

**Tech Stack:** Markdown, Java 25 project conventions, `NexusException`,
`StatusCode`, `NexusStatusCode`, `PluginStatusCode`, `StatusCodeRules`, and
`R<T>`.

**Spec:** `docs/superpowers/specs/2026-08-28-exception-status-code-design.md`

## Global Constraints

- Use `NexusException` for expected application, domain, and translated
  infrastructure failures.
- Prefer typed `StatusCode` overloads; raw code strings are interop boundaries.
- Preserve original causes when translating lower-level failures.
- Full status codes use `MODULE(3) + CATEGORY(2) + LOCAL(4)` and are nine
  characters, for example `NEX080002`.
- Platform-wide codes belong to `NexusStatusCode`.
- Domain-specific codes belong to the owning domain's `domain.enums` package;
  technical module codes stay with their technical boundary.
- Do not create one exception subclass per status code.
- Do not modify Java source or `standards/module-skills.md`.

---

### Task 1: Write the authoritative exception and status-code standard

**Files:**
- Create: `standards/exception-status-code.md`

**Interfaces:**
- Consumes: current behavior of `NexusException`, `StatusCode`,
  `StatusCategory`, `StatusCodeRules`, `NexusStatusCode`, `PluginStatusCode`,
  and `R<T>`.
- Produces: the canonical rules linked by API and domain initialization docs.

- [x] **Step 1: Add exception taxonomy and construction rules**

Document expected business failures, translated infrastructure failures, pure
utility programmer misuse, interruption/cancellation, and fatal errors. Cover
`NexusException.build(StatusCode, ...)`, typed versus raw-code overloads,
message/display separation, cause preservation, and sensitive-data handling.

- [x] **Step 2: Add catch, translation, and response rules**

Define when to rethrow unchanged, when to wrap with a more specific status, how
to preserve the cause, where unknown failures become a generic system status,
how to avoid duplicate logging, and how endpoint infrastructure maps
`NexusException` to `R.fail(...)` without leaking stack traces.

- [x] **Step 3: Add status-code structure and semantic rules**

Document module allocation, category selection, four-digit local codes,
English/Chinese message and advice, HTTP mapping, enum constant naming,
uniqueness, and compatibility of full codes. Explicitly distinguish business
status from transport HTTP status.

- [x] **Step 4: Add extension procedure and tests**

Describe reuse search, ownership decision, module/local allocation, metadata,
registration/allowlist concerns for raw strings, and required contract tests for
format, uniqueness, messages, categories, HTTP status, and behavior.

- [x] **Step 5: Add review checklist and validate the document**

Run:

```bash
rg -n '^## ' standards/exception-status-code.md
git diff --check -- standards/exception-status-code.md
```

Expected: all exception, status, extension, compatibility, and checklist
sections are present with no whitespace errors.

### Task 2: Align API design with the authoritative error standard

**Files:**
- Modify: `standards/api-design.md`

**Interfaces:**
- Consumes: `standards/exception-status-code.md`.
- Produces: concise API-specific links and boundary rules.

- [x] **Step 1: Replace duplicated exception guidance with a cross-reference**

Retain the API-layer rule that business failures use `NexusException`, then
link to the authoritative document for taxonomy, wrapping, and response
mapping.

- [x] **Step 2: Clarify pure utility preconditions and status selection**

State that JDK/framework precondition exceptions are allowed only for pure
programmer misuse that does not represent caller or business input. Require a
typed reusable status code for application-visible failures and prohibit raw
string codes in ordinary in-repo calls.

- [x] **Step 3: Validate the API document**

Run:

```bash
rg -n 'exception-status-code.md|NexusException|StatusCode' standards/api-design.md
git diff --check -- standards/api-design.md
```

Expected: the API document points to the authoritative standard and has no
contradictory exception rule.

### Task 3: Add status-code gates to domain initialization

**Files:**
- Modify: `standards/domain-module-initialization.md`

**Interfaces:**
- Consumes: `standards/exception-status-code.md` and existing stage gates.
- Produces: pre-creation and verification checks for domain failures.

- [x] **Step 1: Add status ownership to the domain vocabulary gate**

Require deciding whether a failure is platform-wide, domain-specific, or
technical before adding a status enum, and require names to follow the domain
and status-code conventions.

- [x] **Step 2: Add status-code checks to the domain contract gate**

Require reuse search, unique module/local allocation, category and HTTP mapping,
bilingual message/advice, no runtime secrets in text, and no per-error exception
subclasses.

- [x] **Step 3: Add extension tests to the verification gate**

Require contract tests for full-code shape, uniqueness, metadata, and exception
translation behavior; preserve the existing compile and full-test commands.

- [x] **Step 4: Validate the workflow document**

Run:

```bash
rg -n 'exception-status-code.md|status code|StatusCode|NexusException' standards/domain-module-initialization.md
git diff --check -- standards/domain-module-initialization.md
```

Expected: status ownership and extension checks are visible in the relevant
stages and the diff check is clean.

### Task 4: Cross-file review and verification

**Files:**
- Verify: `standards/exception-status-code.md`
- Verify: `standards/api-design.md`
- Verify: `standards/domain-module-initialization.md`
- Verify unchanged: `standards/module-skills.md`

- [x] **Step 1: Check consistency and implementation alignment**

Search for conflicting code formats, `VO`/`Vo`-style status naming, raw-code
recommendations, uncaught-cause wording, and module placement that would
violate `AGENTS.md`.

- [x] **Step 2: Run repository verification**

Run:

```bash
git diff --check
mvn validate
mvn test
mvn -q help:effective-pom
git status --short
```

Expected: Maven commands succeed on Java 25, tests report zero failures, the
working tree is clean after commit, and only the intended documentation files
are changed.

- [x] **Step 3: Confirm source scope**

Run:

```bash
git diff --name-only HEAD~4..HEAD
```

Expected: no Java source and no `standards/module-skills.md` appear in the
task's final change set.
