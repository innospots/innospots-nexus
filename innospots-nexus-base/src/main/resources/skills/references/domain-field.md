# Domain Field

## DomainField

**Type:** class

Describes a field within a domain schema or data structure. Carries an identifier, display name, programmatic code, value type, scope, optional comment, and a list of selectable options.

### named
- **Signature:** `static DomainField named(String name, String code, String valueType) → DomainField`
- **Description:** Creates a field with the given display name, programmatic code, and value type name.
- **Parameters:** `name` — display name; `code` — programmatic identifier; `valueType` — value type name (e.g. "String", "Integer")
- **Returns:** a new DomainField

### fieldId (getter)
- **Signature:** `String fieldId() → String`
- **Description:** Returns the unique field identifier.
- **Returns:** field ID (may be null)

### fieldId (setter)
- **Signature:** `DomainField fieldId(String fieldId) → DomainField`
- **Description:** Sets the unique field identifier.
- **Parameters:** `fieldId` — field ID to set
- **Returns:** this DomainField (fluent)

### name
- **Signature:** `String name() → String`
- **Description:** Returns the display name.
- **Returns:** name string

### code
- **Signature:** `String code() → String`
- **Description:** Returns the programmatic code.
- **Returns:** code string

### valueType
- **Signature:** `String valueType() → String`
- **Description:** Returns the value type name.
- **Returns:** value type string

### scope (getter)
- **Signature:** `FieldScope scope() → FieldScope`
- **Description:** Returns the field scope (default METADATA).
- **Returns:** the field scope

### scope (setter)
- **Signature:** `DomainField scope(FieldScope scope) → DomainField`
- **Description:** Sets the field scope. Falls back to METADATA if null.
- **Parameters:** `scope` — the scope to set
- **Returns:** this DomainField (fluent)

### comment (getter)
- **Signature:** `String comment() → String`
- **Description:** Returns the field comment/description.
- **Returns:** comment string (may be null)

### comment (setter)
- **Signature:** `DomainField comment(String comment) → DomainField`
- **Description:** Sets the field comment.
- **Parameters:** `comment` — comment text
- **Returns:** this DomainField (fluent)

### options
- **Signature:** `List<SelectOption> options() → List<SelectOption>`
- **Description:** Returns an unmodifiable list of selectable options.
- **Returns:** copy of the options list

### option
- **Signature:** `DomainField option(SelectOption option) → DomainField`
- **Description:** Adds a selectable option to this field (e.g. for dropdowns).
- **Parameters:** `option` — the option to add
- **Returns:** this DomainField (fluent)

---

## ParamField

**Type:** class (extends DomainField)

A parameter field with a specific `FieldValueType`, required flag, and optional default value. Its scope is automatically set to `FieldScope.PARAMETER`.

### of
- **Signature:** `static ParamField of(String code, FieldValueType valueType) → ParamField`
- **Description:** Creates a parameter field for the given code and value type. The name is set to the code; the value type name is derived from `valueType.javaType().getSimpleName()`.
- **Parameters:** `code` — field code and name; `valueType` — the specific field value type
- **Returns:** a new ParamField

### required (getter)
- **Signature:** `boolean required() → boolean`
- **Description:** Returns whether this field is required.
- **Returns:** true if required

### fieldValueType
- **Signature:** `FieldValueType fieldValueType() → FieldValueType`
- **Description:** Returns the specific FieldValueType enum.
- **Returns:** the field value type

### required (setter)
- **Signature:** `ParamField required(boolean required) → ParamField`
- **Description:** Sets the required flag.
- **Parameters:** `required` — required flag
- **Returns:** this ParamField (fluent)

### defaultValue (getter)
- **Signature:** `Object defaultValue() → Object`
- **Description:** Returns the default value.
- **Returns:** default value (may be null)

### defaultValue (setter)
- **Signature:** `ParamField defaultValue(Object defaultValue) → ParamField`
- **Description:** Sets the default value.
- **Parameters:** `defaultValue` — default value
- **Returns:** this ParamField (fluent)

---

## FieldScope

**Type:** enum

The role or boundary a field belongs to within a domain schema.

| Constant | Description |
|----------|-------------|
| `INPUT` | Input parameter field |
| `OUTPUT` | Output/result field |
| `PARAMETER` | General parameter field |
| `METADATA` | Metadata/descriptive field |

---

## FieldValueType

**Type:** enum

Supported value types for domain fields. Each type maps to a Java `Class` and provides a `convert(Object)` method for string-to-typed-value coercion.

| Constant | Java Type |
|----------|-----------|
| `STRING` | `String.class` |
| `INTEGER` | `Integer.class` |
| `LONG` | `Long.class` |
| `DOUBLE` | `Double.class` |
| `DECIMAL` | `BigDecimal.class` |
| `BOOLEAN` | `Boolean.class` |
| `DATE` | `LocalDate.class` |
| `TIME` | `LocalTime.class` |
| `DATE_TIME` | `LocalDateTime.class` |
| `OBJECT` | `Object.class` |
| `ARRAY` | `Object[].class` |

### javaType
- **Signature:** `Class<?> javaType() → Class<?>`
- **Description:** Returns the associated Java class for this value type.
- **Returns:** the Java class

### convert
- **Signature:** `Object convert(Object value) → Object`
- **Description:** Converts a raw value (typically a string) to the target Java type. If the value is already an instance of the target type, it is returned as-is.
- **Parameters:** `value` — the raw input value
- **Returns:** the converted typed value, or null if input is null
- **Throws:** `IllegalArgumentException` if the string cannot be parsed

---

## SelectOption

**Type:** record

A selectable option with a stored value and a display label.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `value` | `String` | The stored option value |
| `label` | `String` | The display label shown to users |