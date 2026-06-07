# JSON (`com.innospots.nexus.base.json`)

## Jsons

**Type:** final class

Central JSON utility facade built on Jackson. Provides two `ObjectMapper` instances: a default mapper and a `maskedMapper()` with the `MaskingModule` registered for field-level value conversion and masking.

### Static Method
- **Signature:** `mapper() → ObjectMapper`
- **Description:** Returns the default Jackson `ObjectMapper` (fails on unknown properties disabled, dates as timestamps disabled).
- **Parameters:** none
- **Returns:** `ObjectMapper`

### Static Method
- **Signature:** `maskedMapper() → ObjectMapper`
- **Description:** Returns an `ObjectMapper` with `MaskingModule` registered, so that fields annotated with `@ValueConverter` or `@MaskValue` are automatically transformed during serialization.
- **Parameters:** none
- **Returns:** `ObjectMapper`

### Static Method
- **Signature:** `toJson(Object value) → String`
- **Description:** Serializes an object to JSON using the default mapper.
- **Parameters:** `value` — the object to serialize
- **Returns:** `String` — JSON string

### Static Method
- **Signature:** `toMaskedJson(Object value) → String`
- **Description:** Serializes an object to JSON, applying field conversion and masking where `@ValueConverter` or `@MaskValue` annotations are present.
- **Parameters:** `value` — the object to serialize
- **Returns:** `String` — JSON string (with masked/converted fields)

### Static Method
- **Signature:** `fromJson(String json, Class<T> type) → T`
- **Description:** Deserializes a JSON string to an object of the specified type.
- **Parameters:** `json` — JSON string; `type` — target class
- **Returns:** `T` — deserialized object

### Static Method
- **Signature:** `fromJsonList(String json, Class<T> elementType) → List<T>`
- **Description:** Deserializes a JSON string to a typed List.
- **Parameters:** `json` — JSON string; `elementType` — list element class
- **Returns:** `List<T>`

### Static Method
- **Signature:** `fromJsonSet(String json, Class<T> elementType) → Set<T>`
- **Description:** Deserializes a JSON string to a typed Set.
- **Parameters:** `json` — JSON string; `elementType` — set element class
- **Returns:** `Set<T>`

### Static Method
- **Signature:** `toMap(String json) → Map<String, Object>`
- **Description:** Deserializes a JSON string to a `Map<String, Object>`.
- **Parameters:** `json` — JSON string
- **Returns:** `Map<String, Object>`

---

## MaskStrategy

**Type:** enum

Predefined masking strategies for sensitive data during JSON serialization. Each strategy defines how a string value is transformed, keeping a fixed number of leading and/or trailing characters visible and masking the rest with asterisks.

### Values
- `PHONE` — `138****1234` (keep first 3, last 4)
- `EMAIL` — `a***@example.com` (keep first 1 before @, full domain)
- `ID_CARD` — `320***********1234` (keep first 3, last 4)
- `BANK_CARD` — `6222****1234` (keep first 4, last 4)
- `NAME` — `张*` / `张三*` (keep first char, or first two if length > 2)
- `PASSWORD` — `******` (fully masked)
- `HIDE` — `***` (fully replaced)
- `CUSTOM` — uses `MaskValue.keepHead()` and `MaskValue.keepTail()`

### Method
- **Signature:** `apply(String value) → String`
- **Description:** Apply the masking transformation to the given value.
- **Parameters:** `value` — the original string (may be null)
- **Returns:** `String` — the masked string, or null if input is null

### Static Method
- **Signature:** `mask(String s, int head, int tail) → String`
- **Description:** Mask a string keeping the first `head` and last `tail` characters, filling the middle with asterisks (up to 4).
- **Parameters:** `s` — the original string; `head` — leading chars to keep; `tail` — trailing chars to keep
- **Returns:** `String` — masked string, or null if input is null

### Static Method
- **Signature:** `mask(String s, int head, int tail, int asteriskCount) → String`
- **Description:** Fixed-length masking with a configurable number of asterisks.
- **Parameters:** `s` — the original string; `head` — leading chars to keep; `tail` — trailing chars to keep; `asteriskCount` — number of asterisks
- **Returns:** `String` — masked string, or null if input is null

---

## MaskValue

**Type:** `@interface`

Marks a field or accessor for masking during JSON serialization. Masking is activated only when the `MaskingModule` is registered on the Jackson `ObjectMapper`.

### Elements
- **Signature:** `value() → MaskStrategy` (default: `HIDE`)
- **Description:** The masking strategy to apply.
- **Signature:** `keepHead() → int` (default: 0)
- **Description:** Number of leading characters to keep visible. Only used when `value()` is `CUSTOM`.
- **Signature:** `keepTail() → int` (default: 0)
- **Description:** Number of trailing characters to keep visible. Only used when `value()` is `CUSTOM`.

---

## MaskedSerializer

**Type:** class (package-private, extends `StdSerializer<Object>`)

Jackson serializer that applies field-level masking. Automatically wired by `MaskingModule`.

### Method
- **Signature:** `serialize(Object value, JsonGenerator gen, SerializerProvider provider) → void`
- **Description:** Writes the masked string value to the JSON generator. Null values produce a JSON null.
- **Parameters:** `value` — the original field value; `gen` — JSON generator; `provider` — serializer provider
- **Returns:** void

---

## ValueConverter

**Type:** `@interface`

Marks a field or accessor for value conversion during JSON serialization. The converter class must provide a public no-argument constructor. It receives the original field value and returns the value that Jackson should serialize.

### Elements
- **Signature:** `value() → Class<? extends Function<?, ?>>`
- **Description:** Function implementation used to transform the original value.

---

## ValueConvertingSerializer

**Type:** class (package-private, extends `StdSerializer<Object>`)

Jackson serializer that applies field-level value conversion. A delegate serializer may be provided to serialize the converted value, for example to apply masking after conversion.

### Constructors
- **Signature:** `ValueConvertingSerializer(ValueConverter valueConverter)`
- **Signature:** `ValueConvertingSerializer(ValueConverter valueConverter, JsonSerializer<Object> delegate)`

### Method
- **Signature:** `serialize(Object value, JsonGenerator gen, SerializerProvider provider) → void`
- **Description:** Applies the converter, then delegates to the optional delegate serializer or default serialization.
- **Parameters:** `value` — the original field value; `gen` — JSON generator; `provider` — serializer provider
- **Returns:** void

---

## MaskingModule

**Type:** final class (extends `Module`)

Jackson `Module` that activates field-level value conversion and masking. Register this module on your `ObjectMapper` to enable automatic conversion of fields annotated with `@ValueConverter` and masking of fields annotated with `@MaskValue` during serialization. Without this module, these annotations are ignored.

### Method
- **Signature:** `getModuleName() → String`
- **Description:** Returns the module name: `"innospots-nexus-masking"`.
- **Parameters:** none
- **Returns:** `String`

### Method
- **Signature:** `version() → Version`
- **Description:** Returns the module version: `0.1.0`.
- **Parameters:** none
- **Returns:** `Version`

### Method
- **Signature:** `setupModule(SetupContext context) → void`
- **Description:** Registers a `BeanSerializerModifier` that rewires properties annotated with `@ValueConverter` or `@MaskValue` to use dedicated serializers.
- **Parameters:** `context` — module setup context
- **Returns:** void