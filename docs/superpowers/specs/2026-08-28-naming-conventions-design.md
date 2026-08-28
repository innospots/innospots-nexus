# Naming Conventions Improvement Design

**Date:** 2026-08-28

**Scope:** `standards/naming.md` only.

## Goal

Turn the naming standard from a suffix reference into a practical naming
decision guide. A developer should be able to derive a name from domain
meaning, responsibility, scope, and behavior without relying on vague words or
copying a neighboring class mechanically.

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

## Document Structure

The revised document will keep its current Java, package, and file naming
sections and add the following guidance:

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
- Other standards files and module skills documentation remain unchanged.

## Review and Verification

The document will be reviewed for:

- consistency with module and DDD boundaries in `AGENTS.md`;
- consistency with `code-style.md`, `api-design.md`, and
  `domain-module-initialization.md`;
- examples that exist in or closely match the current source tree;
- absence of contradictory suffix, package, acronym, and method-verb rules;
- clear distinction between required conventions and contextual preferences;
- no placeholders, speculative architecture, or implied source renames.

Because the implementation changes Markdown only, Maven compilation is not
required. Verification will use a focused diff and Markdown/content checks.

## Completion Criteria

- A reader can explain why a proposed name was chosen, not only whether its
  casing is legal.
- Common domain and infrastructure responsibilities map to stable suffixes.
- Method, boolean, collection, identifier, time, and unit names follow
  predictable semantics.
- Abbreviations use one documented casing policy.
- Examples reinforce current Nexus terminology without treating legacy
  inconsistencies as rules.
- The change is limited to `standards/naming.md` after this design is approved.
