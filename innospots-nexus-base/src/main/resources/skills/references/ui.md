# UI — Dynamic UI Component & Page Definitions

## UiComponent

**Type:** class

A configurable UI component descriptor used in dynamic page definitions.

### Fields
- `String componentId` — auto-generated unique ID
- `String name` — display name
- `String type` — component type
- `String valueType` — value type
- `boolean copyable` — whether component can be copied
- `boolean deletable` — whether component can be deleted
- `Map<String, Object> settings` — settings map
- `Map<String, Object> layout` — layout hints
- `List<UiCondition> conditions` — conditional visibility rules

### Methods
- **Signature:** `named(String name, String type) → UiComponent` (static)
- **Description:** Factory method creating a component with auto-generated ID (`c_` + 8 random chars).

- **Signature:** `componentId() → String`
- **Signature:** `name() → String`
- **Signature:** `type() → String`
- **Signature:** `valueType() → String`
- **Signature:** `valueType(String valueType) → UiComponent` (fluent)
- **Signature:** `copyable() → boolean`
- **Signature:** `copyable(boolean copyable) → UiComponent` (fluent)
- **Signature:** `deletable() → boolean`
- **Signature:** `deletable(boolean deletable) → UiComponent` (fluent)
- **Signature:** `settings() → Map<String, Object>` — returns an unmodifiable copy
- **Signature:** `setting(String key, Object value) → UiComponent` (fluent)
- **Signature:** `layout() → Map<String, Object>` — returns an unmodifiable copy
- **Signature:** `layout(String key, Object value) → UiComponent` (fluent)
- **Signature:** `conditions() → List<UiCondition>`
- **Signature:** `conditions(List<UiCondition> conditions) → UiComponent` (fluent)
- **Signature:** `toMap() → Map<String, Object>`
- **Description:** Serializes component to a map representation.

---

## UiCondition

**Type:** class

A visibility/enablement condition for a UI component.

### Fields
- `String result` — result value when condition evaluates true
- `Relation relation` — logical relation between factors
- `List<Factor> factors` — list of condition factors

### Methods
- **Signature:** `when(Relation relation, List<Factor> factors, String result) → UiCondition` (static)
- **Description:** Factory method creating a condition with the given relation, factors, and result.

- **Signature:** `result() → String`
- **Signature:** `relation() → Relation`
- **Signature:** `factors() → List<Factor>`

---

## UiPage

**Type:** class

A page definition in the dynamic UI model. Groups `UiComponent` instances under a page key and URL.

### Fields
- `String pageKey` — unique page key
- `String pageName` — display name
- `String pageUrl` — page URL path
- `Boolean showNavigation` — whether to show in navigation
- `Boolean homePage` — whether this is the home page
- `Map<String, UiComponent> components` — named component map
- `Map<String, Object> configs` — page-level configuration

### Methods
- **Signature:** `of(String pageKey, String pageName, String pageUrl) → UiPage` (static)
- **Description:** Factory method creating a page with key, name, and URL.

- **Signature:** `pageKey() → String`
- **Signature:** `pageName() → String`
- **Signature:** `pageUrl() → String`
- **Signature:** `showNavigation() → Boolean`
- **Signature:** `showNavigation(Boolean showNavigation) → UiPage` (fluent)
- **Signature:** `homePage() → Boolean`
- **Signature:** `homePage(Boolean homePage) → UiPage` (fluent)
- **Signature:** `components() → Map<String, UiComponent>` — returns an unmodifiable copy
- **Signature:** `component(UiComponent component) → UiPage` (fluent)
- **Description:** Adds a component keyed by its name.
- **Signature:** `configs() → Map<String, Object>` — returns an unmodifiable copy
- **Signature:** `config(String key, Object value) → UiPage` (fluent)

---

## UiResource

**Type:** class

Aggregate model for a multi-page UI resource definition.

### Fields
- `List<UiPage> pages` — list of pages
- `Map<String, Object> schemas` — data schema map

### Methods
- **Signature:** `create() → UiResource` (static)
- **Description:** Factory method creating an empty UI resource.

- **Signature:** `pages() → List<UiPage>` — returns an unmodifiable copy
- **Signature:** `page(UiPage page) → UiResource` (fluent)
- **Description:** Adds a page to the resource.

- **Signature:** `schemas() → Map<String, Object>` — returns an unmodifiable copy
- **Signature:** `schema(String key, Object schema) → UiResource` (fluent)
- **Description:** Adds a schema entry keyed by name.