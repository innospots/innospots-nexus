# New Domain Module Initialization

This document defines the required workflow for initializing a business domain
inside an existing Nexus Java module. It applies to domains such as `role`,
`menu`, `permission`, `dictionary`, and other management capabilities.

The workflow is stage-gated. Complete and review each stage before continuing
to the next one. Do not generate the entire package tree first and attempt to
justify it afterward.

## 1. Scope and Principles

### 1.1 Confirm Ownership

Before creating files, determine:

- which Maven module owns the business capability;
- whether the concept is business-specific or reusable infrastructure;
- whether records are platform-wide or project-scoped;
- which adjacent domains interact with it;
- which behavior is required now and which behavior is intentionally deferred.

Concrete management domains normally belong in `innospots-nexus-kernel`.
Business-neutral console contracts belong in `innospots-nexus-console`.
Database and middleware foundations that do not contain concrete business
concepts belong in `innospots-nexus-core`.

Do not create a new Maven module unless its boundary, dependency direction, and
independent testability are clear.

### 1.2 Use Legacy Projects as Behavioral References

A legacy project may be inspected to understand:

- business terminology;
- common management operations;
- lifecycle rules;
- tree, membership, ordering, and status behavior;
- data that must remain stable across updates;
- historical edge cases and protected records.

A legacy project must not be used as a source template. Never:

- copy or move legacy source files;
- reproduce its package structure mechanically;
- preserve obsolete framework annotations or dependencies;
- carry forward denormalized or duplicated fields without a current reason;
- combine concepts that now belong to separate domains;
- reproduce legacy POM structure.

Every field, type, endpoint, and dependency must be justified against the
current module boundaries and developer intent.

### 1.3 Keep the Initial Surface Minimal

Create only packages with an immediate responsibility. A typical domain may
contain:

```text
<domain>
  ├── dao
  ├── domain
  │   ├── entity
  │   ├── enums
  │   ├── request
  │   └── vo
  └── endpoint
```

Add `model`, `converter`, `operator`, `service`, `event`, `handler`, or other
packages only when their responsibilities are required by the current task.
Do not create empty architectural layers or placeholder types.

## 2. Stage One: Define the Entity Data Structure

Entity design is the first implementation stage because it establishes domain
identity, ownership, persistence scope, uniqueness, relationships, lifecycle,
and the data required by later contracts.

### 2.1 Inventory Domain Concepts

List the domain records before writing Java:

- aggregate or primary record;
- association records;
- parent-child relationships;
- stable business keys;
- lifecycle and visibility fields;
- ordering fields;
- protected or built-in flags;
- audit and project ownership inherited from shared entities.

Do not merge an adjacent domain merely because the legacy project stored the
data together. For example, menu navigation and permission resources must
remain separate when permission is independently owned.

### 2.2 Select Persistence Scope

Use `ProjectBaseEntity` by default for project-owned business records.
Use `BaseEntity` only when the record is explicitly platform-wide.

Never redeclare inherited fields such as:

- `projectId`;
- `createdTime`;
- `updatedTime`;
- `createdBy`;
- `updatedBy`.

### 2.3 Define Identity and Stable Keys

Each concrete persistence entity must:

- use a `String` primary key;
- use `@TableId(type = IdType.ASSIGN_UUID)`;
- use Jakarta Persistence `@Id`;
- use `@Column(length = 32, nullable = false)`;
- declare `public static final String TABLE_NAME`;
- use the same constant in `@Table` and `@TableName`.

Differentiate the technical primary key from stable business keys. A stable
key such as `roleCode` or `menuKey` should normally:

- be unique within its ownership scope;
- remain immutable after creation;
- be excluded from update requests when mutation would break references.

Override `BaseEntity.idPrefix()` with a short, stable domain prefix. Primary
keys are generated centrally by `DbPrimaryGenerator` through
`IdGenerator.ulid(prefix)`; operators must not generate primary keys manually.

### 2.4 Review Every Field

For each proposed field, document or determine:

- business meaning;
- required or optional status;
- Java type;
- persistence length;
- default value responsibility;
- whether it is mutable;
- whether it participates in an index;
- whether it belongs to this domain.

Use powers of two for String lengths. Use `@Lob` for intentionally unbounded
text. Store enum values as Strings unless the project establishes another
explicit mapping.

Do not add:

- duplicated inherited audit or project fields;
- transient response-only fields to entities;
- cached ancestor paths without a demonstrated query need;
- HTTP methods or permission actions to unrelated navigation entities;
- framework-specific transport objects;
- fields retained only because the legacy entity contained them.

### 2.5 Define Indexes from Access Patterns

Declare explicit Jakarta Persistence indexes for:

- stable unique lookups;
- foreign-key or parent lookups;
- common status and page-query filters;
- association uniqueness;
- sibling ordering where tree queries require it.

Index names must be explicit and table-prefixed. Project-scoped uniqueness
normally includes `project_id`.

### 2.6 Entity Gate

Do not proceed until all answers are yes:

- Is the entity in the correct module and domain?
- Is platform versus project scope correct?
- Are primary and stable business keys clearly separated?
- Are required fields present?
- Have obsolete or adjacent-domain fields been removed?
- Are nullability and String lengths intentional?
- Do indexes match expected query paths?
- Does the entity use Lombok `@Getter` and `@Setter`?
- Are JPA and MyBatis-Plus annotations both complete?

Add entity contract tests before implementing the entity. Tests should verify
the table, inheritance, identifier annotations, required fields, lengths,
nullability, and indexes. Run the test and confirm that it fails for the
expected missing contract before adding production code.

## 3. Stage Two: Define DAO Contracts

Create the DAO after the entity contract is stable.

### 3.1 DAO Rules

- Place DAOs under `<domain>.dao`.
- Use the `*Dao` suffix.
- Extend `BaseMapper<EntityType>`.
- Prefer inherited CRUD operations.
- Do not create mapper XML files.
- Do not put business workflows or transaction orchestration in a DAO.
- Every DAO method must access exactly one table.
- Do not use SQL joins in wrappers, annotation SQL, XML, or handwritten
  persistence statements.

Add custom DAO methods only when a known access pattern cannot be expressed
clearly by the inherited operations. Prefer:

- `default` methods with MyBatis-Plus lambda wrappers for reusable predicates;
- annotation SQL for explicit single-table queries that are clearer as SQL.

Do not invent speculative queries during initialization.

Cross-table data must be assembled outside DAOs:

1. query the owning or association table;
2. collect all required identifiers or stable keys;
3. batch-query each related table independently;
4. map the records in memory;
5. return the assembled domain view.

N+1 query patterns are prohibited. Never query a related table once for every
row in an earlier result.

Cross-table writes, cascading cleanup, relationship integrity, and stable-key
renames must be coordinated by a service with a transaction. For example, when
an association stores `tagName`, renaming the tag definition must update both
tables through separate DAO operations in one transaction.

### 3.2 DAO Gate

Verify:

- every DAO maps exactly one persistence entity;
- associations have their own DAO when they have their own entity;
- no DAO depends on a service or operator;
- custom methods describe direct database operations only;
- every method is demonstrably single-table;
- result assembly uses batch queries rather than N+1 reads;
- a contract test confirms the `BaseMapper` generic entity.

## 4. Stage Three: Define the Domain Contract

After the persistence shape is known, define the business-facing data
structures needed by endpoints and future services.

### 4.1 Enumerations

Place domain-specific enums under `domain.enums`.

An enum should represent a closed business concept, not a transport detail.
Reuse platform enums such as `BasicStatus` when their semantics match.

Do not combine categories owned by another domain. For example, menu types may
describe directory, page, and external-link navigation without also defining
button or API permissions.

### 4.2 Request Records

Place endpoint input types under `domain.request` and use Java records with the
`Request` suffix.

Create separate requests when operations have different mutation rights:

- create request;
- update request;
- status request;
- page or tree query request;
- ordering request;
- association add or replace request.

Do not reuse the entity as an endpoint request.

Creation requests may include stable business keys. Update requests should
exclude immutable keys and protected system fields.

For query-bound requests:

- use explicit Jakarta REST annotations such as `@QueryParam`;
- use `@DefaultValue` where appropriate;
- provide a no-argument constructor when required by bean binding;
- normalize invalid pagination values to shared defaults.

For collection components:

- convert `null` to an empty immutable collection when that meaning is valid;
- use `List.copyOf`, `Set.copyOf`, or `Map.copyOf`;
- never expose caller-owned mutable collections.

### 4.3 View Records

Place response types under `domain.vo` and use Java records with the `Vo`
suffix.

Separate views by use case:

- detail or tree view;
- compact selector option;
- member or association view;
- runtime/navigation view.

Do not expose persistence entities directly. A VO may contain:

- enum types instead of persisted enum Strings;
- derived counts;
- child views for trees;
- audit timestamps needed by management screens;
- compact fields tailored to a read-only runtime boundary.

Defensively copy child and collection fields in compact constructors.

### 4.4 Models, Events, and Status Codes

Create `domain.model` only when internal business behavior needs a model that
is neither a persistence entity nor a transport record.

Create domain events only for an actual decoupled interaction. The publishing
domain owns the event contract.

Create a domain-specific status-code enum only when existing platform status
codes cannot express the business failure accurately.

### 4.5 Domain Gate

Verify:

- requests and VOs are records;
- names reflect their use cases;
- create and update mutation rights differ where necessary;
- collections are immutable;
- persistence-only Strings are exposed as domain enums where appropriate;
- no permission, user, role, menu, or other adjacent concern leaked across
  domain ownership;
- no speculative model or event package was created.

## 5. Stage Four: Define Conversion Boundaries

Create a MapStruct converter when structural mapping is required among
requests, models, entities, and VOs.

- Place it under `<domain>.converter`.
- Use the `*Converter` suffix.
- Use `@Mapper(config = BaseMapperConfig.class)`.
- Extend `BaseBeanConverter<ModelType, EntityType>` when converting a model and
  entity pair.

Do not create a converter merely to copy one or two scalar values. Do not write
large field-by-field mappings in endpoints, services, or operators.

When methods remain intentionally unimplemented and no conversion occurs yet,
the converter package may be deferred.

## 6. Stage Five: Define Endpoint Boundaries

Endpoints define transport boundaries. They do not own persistence or business
workflows.

### 6.1 Split by Cohesive Resource Boundary

Start with one endpoint for one cohesive resource lifecycle. Split endpoints
when operations have different:

- resource ownership;
- path hierarchy;
- authorization context;
- read/write characteristics;
- consumers;
- service dependencies;
- expected rate or lifecycle.

Common split examples include:

- role lifecycle versus role membership;
- menu management versus current-user navigation;
- permission definitions versus role permission assignment;
- user profile versus authentication credentials.

Do not create one endpoint that accumulates every operation related to a
broad business noun.

### 6.2 Initial Endpoint Form

During domain initialization, create Endpoint types as concrete classes unless
a developer explicitly requests a transport contract interface.

When endpoint behavior is deferred:

- declare the complete Jakarta REST annotations and method signatures;
- return `R<T>`, `R<PageResult<T>>`, or `R<Void>` as appropriate;
- add a focused `TODO` describing the future service boundary;
- throw `UnsupportedOperationException` with a specific message;
- do not return fabricated success responses or empty data.

This makes deferred behavior explicit and prevents an incomplete endpoint from
appearing operational.

### 6.3 Endpoint Method Rules

- Use Jakarta REST annotations only.
- Put resource paths at class level and operation paths at method level.
- Annotate all path, query, header, and bean-bound parameters explicitly.
- Keep request validation, transactions, persistence, and orchestration out of
  endpoints.
- Use one request record when an operation has structured input.
- Use nouns and HTTP semantics consistently.
- Keep stable identifiers in paths for single-resource operations.
- Prefer a dedicated ordering request over repeated scalar parameters.

### 6.4 Endpoint Size Review

Split an endpoint when one or more of these signals appear:

- it manages both lifecycle and membership/assignment;
- it mixes management writes with current-user runtime reads;
- unrelated methods would depend on different future services;
- its class-level path no longer describes all methods naturally;
- operation-level authorization would be fundamentally different;
- adding another method requires a vague name to avoid collision.

Method count alone is not the deciding factor, but more than approximately
seven cohesive operations should trigger a boundary review.

### 6.5 Endpoint Gate

Verify:

- the endpoint boundary is cohesive;
- management and runtime concerns are split when appropriate;
- every method returns the shared `R` wrapper;
- request and response types are domain records;
- all parameters are explicitly annotated;
- deferred methods fail explicitly;
- no endpoint directly depends on a DAO;
- no permission-resource operations were inserted into another domain.

Add reflection-based endpoint contract tests for class form, paths, HTTP
annotations, method signatures, request/VO records, and explicit deferred
behavior.

## 7. Stage Six: Add Operator and Service Only When Needed

Do not scaffold operators and services automatically.

Create an operator when implementing straightforward data operations primarily
against one DAO. An operator may coordinate multiple DAOs only when the
operation remains simple and cohesive.

Create a service when implementing:

- non-trivial workflow or validation;
- transactions across multiple writes;
- coordination across multiple operators;
- cross-domain behavior;
- tree cascade, protected-record, or lifecycle orchestration;
- authorization-aware assembly.

Dependency direction must remain:

```text
endpoint -> service -> operator -> dao
```

Permitted simplifications depend on behavior, for example:

```text
endpoint -> operator -> dao
service -> dao
```

An operator must not depend on another operator or a service. Endpoints must
not orchestrate DAOs directly.

## 8. Test-First Initialization Workflow

Use focused contract tests to establish the intended shape before production
types exist.

Recommended order:

1. Write entity contract tests.
2. Run them and confirm failure for missing entity contracts.
3. Implement the entity.
4. Write DAO generic-binding tests.
5. Implement the DAO.
6. Write request, VO, enum, and endpoint contract tests.
7. Run them and confirm failure for missing contracts.
8. Implement the domain records and endpoints.
9. Re-run focused tests until they pass.
10. Run full project verification.

Do not weaken a valid test merely to accommodate an accidental implementation.
When a test failure reveals a pre-existing unrelated problem, distinguish it
from the expected red state and use an appropriate reactor build such as
`-am` when local dependency artifacts are stale.

## 9. Mandatory Compile Gate

After every batch of Java source changes, immediately run:

```bash
mvn clean compile
```

This command is mandatory. Do not defer compilation until the end of the task.
Do not lower the configured Java release to match an older local JDK.

If compilation fails:

1. identify whether the failure is caused by the new domain changes;
2. correct imports, signatures, annotations, generics, or module dependencies;
3. rerun `mvn clean compile`;
4. do not continue adding features while the source tree is uncompilable.

## 10. Full Verification Gate

After focused tests and compilation pass, run:

```bash
mvn validate
mvn test
mvn -q help:effective-pom
git diff --check
```

Also inspect:

- `git status --short` for unintended files;
- the domain tree for empty or unnecessary packages;
- imports for forbidden framework coupling;
- entity fields for missing or unrelated data;
- Endpoint paths and method count for boundary drift;
- the diff for copied legacy code or mechanical package reproduction.

If the local JDK is older than the configured release, report the mismatch
instead of changing the project baseline.

## 11. Skills Documentation Is Out of Scope

Ordinary domain initialization must not create, edit, synchronize, or remove:

- module `SKILL.md` files;
- files under skills `references/`;
- generated skills indexes.

Skills documentation is updated only when a developer explicitly requests a
complete scan and identifies the module or project scope. Domain code changes
must not trigger incremental skills documentation maintenance.

## 12. Completion Checklist

### Boundary

- [ ] The owning Maven module is correct.
- [ ] The domain does not duplicate base, core, or console responsibilities.
- [ ] Legacy code was used only to understand behavior.
- [ ] Adjacent domains remain separate.

### Entity

- [ ] Persistence scope and superclass are correct.
- [ ] Primary key annotations and length are complete.
- [ ] Stable business keys and mutation rules are explicit.
- [ ] Required fields, nullability, and lengths were reviewed.
- [ ] Indexes match uniqueness and query paths.
- [ ] Inherited fields are not duplicated.

### DAO and Domain

- [ ] DAOs extend the correct `BaseMapper` type.
- [ ] Every DAO method accesses one table and contains no join.
- [ ] Cross-table views are designed for separate batch queries and in-memory
      assembly.
- [ ] Cross-table writes and stable-key propagation are assigned to a
      transactional service.
- [ ] Requests and VOs are immutable records.
- [ ] Collection components are defensively copied.
- [ ] Enums express this domain only.
- [ ] No speculative layers or empty packages were created.

### Endpoint

- [ ] Endpoint boundaries are cohesive and deliberately split.
- [ ] Jakarta REST paths and parameter annotations are explicit.
- [ ] Every method returns the shared response wrapper.
- [ ] Deferred methods use `TODO` and `UnsupportedOperationException`.
- [ ] Endpoint methods contain no persistence or workflow implementation.

### Verification

- [ ] Contract tests were observed failing before implementation.
- [ ] Focused tests pass.
- [ ] `mvn clean compile` passes after Java changes.
- [ ] `mvn validate` passes.
- [ ] `mvn test` passes.
- [ ] `mvn -q help:effective-pom` passes.
- [ ] `git diff --check` passes.
- [ ] No skills documentation was generated or modified.
