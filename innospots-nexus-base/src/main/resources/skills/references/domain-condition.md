# Domain Condition

## Mode

**Type:** enum

The target output mode for condition statements.

| Constant | Description |
|----------|-------------|
| `DB` | SQL-compatible expressions |
| `SCRIPT` | Script/expression language expressions |
| `JAVA` | Java Boolean expressions (same as SCRIPT) |

---

## Relation

**Type:** enum

Logical combinators for joining multiple `Factor` conditions. Each relation has a SQL symbol and a script symbol.

| Constant | dbSymbol | scriptSymbol |
|----------|----------|--------------|
| `AND` | `" and "` | `" && "` |
| `OR` | `" or "` | `" \|\| "` |

### symbol
- **Signature:** `String symbol(Mode mode) → String`
- **Description:** Returns the appropriate connector symbol for the given mode.
- **Parameters:** `mode` — target output mode (DB or SCRIPT/JAVA)
- **Returns:** dbSymbol if Mode.DB, otherwise scriptSymbol

### dbSymbol
- **Signature:** `String dbSymbol() → String`
- **Description:** Returns the SQL connector symbol.
- **Returns:** the SQL symbol string

### scriptSymbol
- **Signature:** `String scriptSymbol() → String`
- **Description:** Returns the script/expression connector symbol.
- **Returns:** the script symbol string

---

## Operator

**Type:** enum

Comparison operators used in filter conditions. Each operator carries separate symbols for SQL (`dbSymbol`) and script (`scriptSymbol`) contexts.

| Constant | dbSymbol | scriptSymbol | IsRange |
|----------|----------|--------------|---------|
| `GREATER` | `>` | `>` | true |
| `GREATER_EQUAL` | `>=` | `>=` | true |
| `LESS_EQUAL` | `<=` | `<=` | true |
| `LESS` | `<` | `<` | true |
| `EQUAL` | `=` | `==` | false |
| `NOT_EQUAL` | `!=` | `!=` | false |
| `IN` | `in` | `in` | false |
| `NOT_IN` | `not in` | `notin` | false |
| `LIKE` | `like` | `like` | false |
| `IS_NULL` | `is null` | `==` | false |
| `IS_NOT_NULL` | `is not null` | `!=` | false |
| `BETWEEN` | `between` | `between` | true |
| `HAS_VALUE` | `has value` | `!=` | false |

### symbol
- **Signature:** `String symbol(Mode mode) → String`
- **Description:** Returns the operator symbol appropriate for the given mode.
- **Parameters:** `mode` — target output mode
- **Returns:** dbSymbol if Mode.DB, otherwise scriptSymbol

### dbSymbol
- **Signature:** `String dbSymbol() → String`
- **Description:** Returns the SQL operator symbol.
- **Returns:** SQL symbol string

### scriptSymbol
- **Signature:** `String scriptSymbol() → String`
- **Description:** Returns the script/expression operator symbol.
- **Returns:** script symbol string

### isRange
- **Signature:** `boolean isRange() → boolean`
- **Description:** Returns true if this operator is a range comparison (BETWEEN, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL).
- **Returns:** true for range operators, false otherwise

---

## Factor

**Type:** class (with Lombok @Getter)

A single filter criterion composed of a field code, an operator, a value, and an optional value type. Values prefixed with `${...}` or `%{...}` are treated as placeholders resolved at runtime against an input map.

### Constructor
- **Signature:** `Factor(String code, Operator operator, Object value, FieldValueType valueType) → Factor`
- **Description:** Constructs a factor with explicit value type.
- **Parameters:** `code` — field identifier; `operator` — comparison operator; `value` — comparison value (may be a placeholder string like `${key}`); `valueType` — the explicit value type for formatting/quoting

### of (auto-detect type)
- **Signature:** `static Factor of(String code, Operator operator, Object value) → Factor`
- **Description:** Creates a factor by inferring the value type from the Java type of value.
- **Parameters:** `code` — field identifier; `operator` — comparison operator; `value` — comparison value
- **Returns:** a new Factor with auto-detected value type

### of (explicit type)
- **Signature:** `static Factor of(String code, Operator operator, Object value, FieldValueType valueType) → Factor`
- **Description:** Creates a factor with an explicitly specified value type.
- **Parameters:** `code`, `operator`, `value`, `valueType`
- **Returns:** a new Factor

### value
- **Signature:** `Object value() → Object`
- **Description:** Returns the raw factor value.
- **Returns:** the raw value object

### value (resolved)
- **Signature:** `Object value(Map<String, Object> input) → Object`
- **Description:** Resolves the factor value against a runtime input map. Placeholder strings (`${key}` or `%{key}`) extract the corresponding entry from the input map. Values of type `OBJECT` treat the raw value itself as a key into the input map. All other values are returned as-is.
- **Parameters:** `input` — the runtime key-value input map
- **Returns:** the resolved value, or null if input is absent

### valueKey
- **Signature:** `String valueKey() → String`
- **Description:** Extracts the placeholder key from a placeholder value string. For example, `"${userId}"` returns `"userId"`. If the value is not a placeholder, returns the string representation.
- **Returns:** the extracted key or string representation

### checkNull (instance)
- **Signature:** `boolean checkNull() → boolean`
- **Description:** Checks if this factor has any null essential properties (operator, valueType, or code).
- **Returns:** true if operator, valueType, or code is null

### checkNull (static)
- **Signature:** `static boolean checkNull(List<Factor> list) → boolean`
- **Description:** Checks if any factor in the given list has null essential properties.
- **Parameters:** `list` — list of factors to check
- **Returns:** true if any factor has null essential properties

### name
- **Signature:** `Factor name(String name) → Factor`
- **Description:** Sets the display name for this factor.
- **Parameters:** `name` — display name
- **Returns:** this Factor (fluent)

### code
- **Signature:** `String code() → String`
- **Description:** Returns the field identifier code.
- **Returns:** field code

### operator
- **Signature:** `Operator operator() → Operator`
- **Description:** Returns the comparison operator.
- **Returns:** the operator

### valueType
- **Signature:** `FieldValueType valueType() → FieldValueType`
- **Description:** Returns the value type.
- **Returns:** the field value type

---

## IFactorStatement

**Type:** interface

Strategy interface for rendering a `Factor` into a mode-specific expression string (SQL, script, or Java). Implementations handle value quoting, type conversion, and operator-specific formatting.

### normalizeValue (value + valueType + operator)
- **Signature:** `Object normalizeValue(Object value, FieldValueType valueType, Operator operator) → Object`
- **Description:** Normalizes a value for the target expression language.
- **Parameters:** `value` — raw value; `valueType` — expected value type; `operator` — operator (used for range detection)
- **Returns:** the normalized value

### normalizeValue (value + valueType)
- **Signature:** `default Object normalizeValue(Object value, FieldValueType valueType) → Object`
- **Description:** Normalizes a value without operator context (calls normalizeValue with null operator).
- **Parameters:** `value` — raw value; `valueType` — expected value type
- **Returns:** the normalized value

### normalizeValue (value only)
- **Signature:** `default Object normalizeValue(Object value) → Object`
- **Description:** Normalizes a value without operator or type (assumes STRING).
- **Parameters:** `value` — raw value
- **Returns:** the normalized value

### normalizeValue (factor)
- **Signature:** `Object normalizeValue(Factor factor) → Object`
- **Description:** Normalizes a factor's value using its own valueType.
- **Parameters:** `factor` — the factor whose value to normalize
- **Returns:** the normalized value

### statement
- **Signature:** `String statement(Factor factor) → String`
- **Description:** Renders a single factor as a complete expression string.
- **Parameters:** `factor` — the factor to render
- **Returns:** the expression string

---

## FactorStatementBuilder

**Type:** class (final, private constructor)

Factory that selects the appropriate `IFactorStatement` implementation based on the target `Mode`.

### build
- **Signature:** `static IFactorStatement build(Mode mode) → IFactorStatement`
- **Description:** Selects the appropriate implementation for the given mode.
- **Parameters:** `mode` — target output mode
- **Returns:** `DatabaseFactorStatement` for DB mode; `ScriptFactorStatement` for SCRIPT or JAVA mode

---

## DatabaseFactorStatement

**Type:** class (implements IFactorStatement)

Renders a `Factor` into a SQL expression string. Values are quoted according to their `FieldValueType`: numeric and boolean types are left unquoted; date/time types are formatted with ISO patterns; strings are single-quoted with apostrophes stripped.

### normalizeValue
- **Signature:** `String normalizeValue(Object value, FieldValueType valueType, Operator operator) → String`
- **Description:** Normalizes a value into a SQL-safe string representation. Numeric and boolean types are left unquoted; date/time types use ISO formatting; strings are single-quoted with embedded apostrophes stripped.
- **Parameters:** `value` — the raw value; `valueType` — the expected value type; `operator` — unused in SQL mode, retained for interface contract
- **Returns:** the SQL-safe string representation

### normalizeValue (factor)
- **Signature:** `Object normalizeValue(Factor factor) → Object`
- **Description:** Normalizes a factor's value using its valueType.
- **Parameters:** `factor` — the factor
- **Returns:** the normalized SQL string

### statement
- **Signature:** `String statement(Factor factor) → String`
- **Description:** Renders a single factor as a complete SQL expression like `field = 'value'` or `field IN (v1, v2)`.
- **Parameters:** `factor` — the factor to render
- **Returns:** the SQL expression string

---

## ScriptFactorStatement

**Type:** class (implements IFactorStatement)

Renders a `Factor` into a script/expression language (e.g. MVEL, SpEL) string. Numeric values are left unquoted; strings are single-quoted. `IN` and `NOT_IN` operators use `include(...)` / `notInclude(...)` helper functions; `LIKE` uses `regexMatch(...)`.

### normalizeValue
- **Signature:** `Object normalizeValue(Object value, FieldValueType valueType, Operator operator) → Object`
- **Description:** Normalizes a value for script/expression language output. The `M` suffix denotes BigDecimal literals; values with this suffix are passed through raw.
- **Parameters:** `value` — raw value; `valueType` — expected value type; `operator` — operator (used to detect range operators)
- **Returns:** the normalized value (String for quoted, Number for unquoted)

### normalizeValue (factor)
- **Signature:** `Object normalizeValue(Factor factor) → Object`
- **Description:** Normalizes a factor's value using its valueType.
- **Parameters:** `factor` — the factor
- **Returns:** the normalized value

### statement
- **Signature:** `String statement(Factor factor) → String`
- **Description:** Renders a single factor as a script expression. Comparison operators: `field == value`. IN/NOT_IN: `include(seq.set(v1,v2), field)`. LIKE: `regexMatch(pattern, field)`. BETWEEN: `field >= start && field <= end`.
- **Parameters:** `factor` — the factor to render
- **Returns:** the script expression string

---

## SimpleCondition

**Type:** class

A flat list of `Factor` conditions joined by a single `Relation`. The condition renders itself as a statement string on first access via `statement()`, caching the result until factors change.

### create
- **Signature:** `static SimpleCondition create(Mode mode, Relation relation) → SimpleCondition`
- **Description:** Creates a condition with the specified output mode and logical relation.
- **Parameters:** `mode` — output mode; `relation` — logical combinator
- **Returns:** a new SimpleCondition

### factor
- **Signature:** `SimpleCondition factor(Factor factor) → SimpleCondition`
- **Description:** Adds a factor and invalidates the cached statement.
- **Parameters:** `factor` — the factor to add
- **Returns:** this SimpleCondition (fluent)

### displayName (getter)
- **Signature:** `String displayName() → String`
- **Description:** Returns the display name.
- **Returns:** display name string

### displayName (setter)
- **Signature:** `SimpleCondition displayName(String displayName) → SimpleCondition`
- **Description:** Sets the display name.
- **Parameters:** `displayName` — display name
- **Returns:** this SimpleCondition (fluent)

### statement
- **Signature:** `String statement() → String`
- **Description:** Returns the rendered condition statement, generating it lazily if needed.
- **Returns:** the condition statement string

### initialize
- **Signature:** `void initialize() → void`
- **Description:** Generates the condition statement using the appropriate IFactorStatement for the current mode.

### merge
- **Signature:** `void merge(SimpleCondition other) → void`
- **Description:** Merges the factors from another condition into this one, invalidating the cached statement.
- **Parameters:** `other` — the other condition to merge from

### referFields
- **Signature:** `Set<String> referFields() → Set<String>`
- **Description:** Collects all field codes referenced by this condition's factors.
- **Returns:** an unmodifiable set of field code strings

### mode
- **Signature:** `Mode mode() → Mode`
- **Description:** Returns the output mode.
- **Returns:** the mode

### relation
- **Signature:** `Relation relation() → Relation`
- **Description:** Returns the logical relation.
- **Returns:** the relation

### factors
- **Signature:** `List<Factor> factors() → List<Factor>`
- **Description:** Returns an unmodifiable list of factors.
- **Returns:** copy of the factors list

---

## EmbedCondition

**Type:** class (extends SimpleCondition)

A condition that supports nesting — each `EmbedCondition` can contain child sub-conditions recursively. The final statement wraps nested groups in parentheses.

### create
- **Signature:** `static EmbedCondition create(Mode mode, Relation relation) → EmbedCondition`
- **Description:** Creates an embeddable condition with nesting support.
- **Parameters:** `mode` — output mode; `relation` — logical combinator
- **Returns:** a new EmbedCondition

### factor
- **Signature:** `EmbedCondition factor(Factor factor) → EmbedCondition`
- **Description:** Adds a factor and invalidates the cached statement. Overridden to return EmbedCondition.
- **Parameters:** `factor` — the factor to add
- **Returns:** this EmbedCondition (fluent)

### addCondition
- **Signature:** `EmbedCondition addCondition(EmbedCondition condition) → EmbedCondition`
- **Description:** Adds a nested sub-condition. On rebuild, embedded conditions are wrapped in parentheses and joined by the parent relation.
- **Parameters:** `condition` — the nested condition to add
- **Returns:** this EmbedCondition (fluent)

### merge
- **Signature:** `void merge(SimpleCondition other) → void`
- **Description:** Merges factors from another condition. If the other is an EmbedCondition, also merges its embeds.
- **Parameters:** `other` — the other condition to merge from

### rebuild
- **Signature:** `StringBuilder rebuild(IFactorStatement fs) → StringBuilder`
- **Description:** Rebuilds the statement, appending parenthesized nested conditions.
- **Parameters:** `fs` — the factor statement renderer
- **Returns:** the statement StringBuilder

### referFields
- **Signature:** `Set<String> referFields() → Set<String>`
- **Description:** Collects field codes from own factors and all nested conditions.
- **Returns:** an unmodifiable set of field code strings

### embeds
- **Signature:** `List<EmbedCondition> embeds() → List<EmbedCondition>`
- **Description:** Returns an unmodifiable list of nested sub-conditions.
- **Returns:** copy of the embeds list