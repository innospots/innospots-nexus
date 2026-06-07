# Config

## NexusConfig

**Type:** class (final)

Immutable configuration store that wraps a flat key-value map. Keys must be non-blank strings; null values are skipped during construction. Typed accessor methods perform automatic conversion via Hutool's `Convert` utility.

### of
- **Signature:** `static NexusConfig of(Map<String, ?> source) → NexusConfig`
- **Description:** Creates an immutable config from a source map. Null values are omitted; blank keys cause an immediate failure.
- **Parameters:** `source` — the source key-value pairs
- **Returns:** a new NexusConfig with copied values

### get
- **Signature:** `Optional<String> get(String key) → Optional<String>`
- **Description:** Returns the raw value for the given key.
- **Parameters:** `key` — config key
- **Returns:** Optional containing the value, or empty if absent

### get (with default)
- **Signature:** `String get(String key, String defaultValue) → String`
- **Description:** Returns the value for the given key, falling back to a default if absent.
- **Parameters:** `key` — config key; `defaultValue` — fallback value
- **Returns:** the value or defaultValue

### getBoolean
- **Signature:** `boolean getBoolean(String key, boolean defaultValue) → boolean`
- **Description:** Returns the boolean value for the given key, falling back to a default. Conversion via `Convert.toBool`.
- **Parameters:** `key` — config key; `defaultValue` — fallback value
- **Returns:** the boolean value or defaultValue

### getInt
- **Signature:** `int getInt(String key, int defaultValue) → int`
- **Description:** Returns the int value for the given key, falling back to a default. Conversion via `Convert.toInt`.
- **Parameters:** `key` — config key; `defaultValue` — fallback value
- **Returns:** the int value or defaultValue

### asMap
- **Signature:** `Map<String, String> asMap() → Map<String, String>`
- **Description:** Returns an unmodifiable view of the underlying config map.
- **Returns:** the internal values map