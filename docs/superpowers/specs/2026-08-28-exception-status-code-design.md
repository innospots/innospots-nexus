# Exception and Status Code Standards Design

**Date:** 2026-08-28

**Scope:** Add a single source of truth for exception and status-code design,
then add focused cross-references from API and domain initialization standards.
No Java source is renamed or modified by this task.

## Goal

Make failures predictable across domain, infrastructure, REST, plugin, and
extension boundaries. A reader should be able to determine which exception to
throw, which status code to select or add, how to preserve the original cause,
how to expose the failure to a caller, and how to extend the code system without
breaking module ownership or compatibility.

## Source of Truth

The rules are derived from the current implementations:

- `NexusException` is the shared runtime exception carrying a machine-readable
  code, summary message, optional localized display, and optional cause.
- `StatusCode` defines module, category, four-digit local code, bilingual
  message/advice, and HTTP status metadata.
- `NexusStatusCode` owns platform-wide `NEX` codes.
- `PluginStatusCode` owns plugin-runtime `PLG` codes as a technical module.
- `StatusCodeRules` validates module/category/local-code shape.
- `R<T>` converts a `NexusException` into a transport failure response.

The documentation will distinguish current implementation behavior from
recommended use. It will not silently authorize source changes that are outside
this standards task.

## Target Documents

### `standards/exception-status-code.md`

Create the authoritative standard with these sections:

1. failure taxonomy and exception ownership;
2. `NexusException` construction, cause preservation, display messages, and
   sensitive-data rules;
3. catch/rethrow/wrap and boundary translation rules;
4. status-code structure, category selection, HTTP mapping, and naming;
5. platform-wide status-code rules;
6. domain and technical module extension rules;
7. module-code and local-code allocation procedure;
8. compatibility, i18n, serialization, and response mapping;
9. contract-test requirements and review checklists.

### `standards/api-design.md`

Keep API-level exception and status guidance concise, link to the new source of
truth, and clarify the existing rule that application/business failures use
`NexusException` while pure utility programmer-precondition failures may use
the framework-appropriate exception only when they do not cross an application
boundary.

### `standards/domain-module-initialization.md`

Add status-code and exception checks to the domain contract and verification
gates. The workflow will require reuse checks, domain ownership, stable code
allocation, bilingual messages/advice, and status-code contract tests before a
new domain failure is introduced.

## Exception Model

The standard will define four practical failure classes:

- expected caller or business failures, represented by `NexusException` and a
  reusable `StatusCode`;
- translated infrastructure/external failures, represented by
  `NexusException` with the relevant status and preserved cause;
- programmer misuse of a pure lower-level utility, which may retain the
  framework/JDK precondition exception if it never represents user or business
  input at an application boundary;
- cancellation/interruption and fatal JVM errors, which must not be swallowed
  or mislabeled as ordinary business failures.

No exception subclass is created per status code. `NexusException.build(...)`
overloads are preferred over raw code strings; raw strings remain an explicit
interop/extension boundary and must be validated and allowlisted.

## Status-Code Model

Document the canonical format as:

```text
MODULE(3 uppercase letters) + CATEGORY(2 digits) + LOCAL(4 digits)
```

For example, `NEX080002` is the configuration error code in the `NEX` module.
The full code is nine characters. A category communicates failure semantics;
the HTTP status communicates transport behavior and does not replace the
business code.

Platform-wide codes belong to `NexusStatusCode`. A domain-specific business
code belongs to the owning domain's `domain.enums` package and implements
`StatusCode`. A reusable technical module may keep a module-local status enum
near that module's technical boundary, as `core.plugin.status.PluginStatusCode`
does. Sibling business modules must not import each other's status enums merely
to share errors.

## Extension Procedure

Before adding a status code:

1. search for an existing code with the same meaning and reuse it when the
   scope and remediation are compatible;
2. decide whether the failure is platform-wide, domain-specific, or technical;
3. select the module code and category from the owning boundary;
4. allocate a four-digit local code unique within that module's status family;
5. provide stable English and Chinese message/advice text without runtime
   values or secrets;
6. map to the narrowest correct HTTP status;
7. add format, uniqueness, metadata, and behavior contract tests;
8. update consumers and documentation in the same compatibility change.

The extension must not introduce a new exception subclass, duplicate an
existing status under a different enum, reuse a code for a different meaning,
or change an existing full code/message/event contract casually.

## Compatibility and Verification

Full codes, enum constant names, HTTP mapping, event/configuration identifiers,
and localized default messages are compatibility surfaces. Renaming or
re-numbering requires an explicit migration or version boundary. Existing
source-level deviations can be corrected in a separate implementation change;
this task changes documentation only.

Verification consists of Markdown consistency checks, a focused review against
the source implementations, and the repository's normal Maven validation. No
module skill scan is triggered.

## Completion Criteria

- Exception selection and wrapping rules are unambiguous.
- `NexusException`, raw-code interop, cause preservation, and response mapping
  are documented consistently.
- Status-code format, category semantics, module ownership, local allocation,
  messages, advice, and HTTP mapping are explicit.
- Domain and technical extensions have separate, legal placement rules.
- Compatibility and contract-test requirements are visible before extension.
- Only the intended standards/design/plan documents change; Java sources and
  `standards/module-skills.md` remain untouched.
