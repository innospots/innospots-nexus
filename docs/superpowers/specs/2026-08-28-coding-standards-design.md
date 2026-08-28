# Coding Standards Improvement Design

**Date:** 2026-08-28

**Scope:** The ordinary development standards under `standards/`, with naming
as the primary focus. `module-skills.md` remains unchanged because module skill
documentation is governed by its own developer-triggered scan policy.

## Goal

Make the coding standards usable as a coherent decision system derived from
the current Nexus codebase. A developer should be able to choose names,
structure Java source, document contracts, design APIs, and initialize domains
without resolving contradictions between files or copying neighboring code
mechanically.

## Source of Truth

The update derives conventions from the current `base`, `core`, `console`,
`kernel`, and `platform` source trees. Representative established patterns
include:

- domain roles such as `RoleEntity`, `RoleDao`, `RoleCreateRequest`,
  `RoleOptionVo`, and `RoleCreatedEvent`;
- platform infrastructure roles such as `PluginManager`, `CapabilityRegistry`,
  `CapabilityRouter`, `CapabilityProvider`, and `ClasspathPluginDiscovery`;
- behavior-oriented methods such as `findByUserId`, `listActiveTenantIds`,
  `publishSync`, `hasAvailableThread`, and `validatePassword`;
- scoped concepts such as `PlatformUser`, `TenantMember`, `WorkspaceBaseEntity`,
  and `CurrentAuthorizationEndpoint`.

Existing names are evidence of project vocabulary, not automatically good
examples. The standard will prefer clear full words and consistent semantics
over preserving every historical abbreviation or irregularity.

## Files and Responsibilities

### `standards/naming.md`

Keep the current Java, package, and file naming sections and add:

1. A naming decision order: business concept, scope, responsibility, then
   technical form.
2. Vocabulary rules covering precise nouns, one concept per term, contextual
   qualifiers, and avoidance of vague names.
3. Type-role rules for domain objects and technical roles, including
   `Manager`, `Registry`, `Router`, `Provider`, `Factory`, `Resolver`,
   `Discovery`, `Loader`, `Parser`, `Validator`, `Builder`, `Repository`,
   `Facade`, and `Holder`.
4. Method verb rules for reads, collections, creation, mutation, lifecycle,
   validation, conversion, and boolean predicates.
5. Field and parameter rules for identifiers, collections, booleans, time,
   counts, units, and paired values.
6. Abbreviation and acronym casing rules, including established terms such as
   `Id`, `Url`, `Http`, `Json`, `Rsa`, `Dao`, `Vo`, `Api`, and `Ui`.
7. Request/VO naming rules that put the business resource before the operation
   or view purpose.
8. Persistence naming rules for Java fields, table/column names, indexes, and
   identifier prefixes.
9. Test naming rules for test classes and behavior-oriented test methods.
10. Positive and negative examples plus a final naming checklist.

### `standards/code-style.md`

Clarify source-layout and implementation conventions that recur in the current
code:

- declaration and member ordering;
- annotation, record, fluent-call, lambda, and multiline formatting;
- appropriate Lombok use for mutable entities, configuration binders, and
  internal models instead of treating every domain class identically;
- dependency fields, constructor injection, logging, collection handling, and
  avoidance of hidden mutable state;
- separation of formatting rules from API and architectural rules, replacing
  duplicated sections with focused cross-references where useful.

### `standards/code-comments.md`

Extend comment rules for package documentation, records and record components,
enums, overridden methods, contract versus implementation Javadocs, TODOs, and
comments that explain lifecycle, concurrency, security, or compatibility
constraints. Examples will reinforce intent-focused comments without requiring
noise on self-evident private implementation details.

### `standards/api-design.md`

Resolve and extend API guidance around:

- interface/implementation boundaries and dependency injection;
- nullability and `Optional`, including framework-facing DAO exceptions;
- command/query method semantics, collection return types, and boolean
  predicates;
- validation ownership and normalization boundaries;
- lifecycle, resource ownership, idempotency, thread safety, and public API
  compatibility where the current plugin, event, resource, and session code
  demonstrates a real need;
- explicit cross-references to naming and source-style rules instead of
  duplicating them.

### `standards/domain-module-initialization.md`

Keep the existing stage-gated workflow. Make only targeted consistency updates:

- require terminology and naming decisions before types are created;
- align domain models, converters, services, operators, and endpoint gates with
  the clarified naming and API rules;
- make documentation and verification checkpoints reference the authoritative
  standards rather than restating divergent variants.

### Excluded files

`standards/module-skills.md` will not be changed. This task is not a requested
module or project skill scan, and ordinary code documentation work must not
refresh skill documentation.

## Naming Decision Model

Names will be constructed from left to right using only qualifiers needed to
remove ambiguity:

```text
[scope or variant] + business concept + [operation or view purpose] + responsibility
```

For example, `PlatformUserPasswordEntity` identifies the platform scope, user
concept, password sub-concept, and persistence role. `RoleStatusUpdateRequest`
identifies the role concept, the narrowly allowed mutation, and the transport
input role. Qualifiers must describe a real distinction; redundant terms such
as `RoleBusinessService`, `RoleDataDao`, or `CommonUtils` will be discouraged.

## Compatibility and Scope

- No Java type, member, package, database object, or API contract is renamed.
- No new build-time naming checker is introduced.
- Existing project-specific conventions such as `Dao`, `Vo`, `Endpoint`,
  `domain.enums`, and approved interface names are retained.
- Historical names that conflict with the improved guidance may remain until
  separately refactored; they will not be promoted as examples.
- Existing requirements are preserved unless they conflict with the current
  module boundaries or with another standard. Any corrected conflict will be
  made explicit rather than silently deleting the rule.
- Standards describe the intended baseline. Current code may contain older
  deviations; this task does not authorize source-code cleanup.
- Module skills documentation remains unchanged.

## Review and Verification

The standards set will be reviewed for:

- consistency with module and DDD boundaries in `AGENTS.md`;
- consistency with `code-style.md`, `api-design.md`, and
  `domain-module-initialization.md`;
- examples that exist in or closely match the current source tree;
- absence of contradictory suffix, package, acronym, method-verb, Lombok,
  nullability, and layering rules;
- clear distinction between required conventions and contextual preferences;
- no placeholders, speculative architecture, or implied source renames.

Because the implementation changes Markdown only, Maven compilation is not
required by the repository's Java-source compile gate. Verification will use
focused diffs, cross-file searches, Markdown/content checks, and a final check
that no Java or module skill documentation was modified.

## Completion Criteria

- A reader can explain why a proposed name was chosen, not only whether its
  casing is legal.
- Common domain and infrastructure responsibilities map to stable suffixes.
- Method, boolean, collection, identifier, time, and unit names follow
  predictable semantics.
- Abbreviations use one documented casing policy.
- Source layout, Lombok, comments, nullability, validation, lifecycle, and
  compatibility rules have one unambiguous authoritative statement.
- Cross-references replace material duplication where two standards currently
  describe the same concern.
- Domain initialization gates direct developers to the clarified rules and do
  not introduce a competing convention.
- Examples reinforce current Nexus terminology without treating legacy
  inconsistencies as rules.
- All changes remain inside the approved standards and design/plan documents;
  Java sources and module skill documentation remain untouched.
