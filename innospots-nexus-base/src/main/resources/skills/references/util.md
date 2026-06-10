# Util — General-Purpose Utilities

## BeanUtils

**Type:** final class

Bean property copy and conversion utility wrapping Hutool's `BeanUtil`.

### Methods
- **Signature:** `copyProperties(Object source, Object target) → void`
- **Description:** Copies properties from source to target, ignoring null values and errors.

- **Signature:** `copyProperties(Object source, Class<T> targetClass) → T`
- **Description:** Creates a new instance of targetClass and copies properties from source.

- **Signature:** `copyProperties(Collection<S> sourceCollection, Class<T> targetClass) → List<T>`
- **Description:** Copies a collection of beans to a new list of target type.

- **Signature:** `toMap(Object source) → Map<String, Object>`
- **Description:** Converts a bean to a map (camelCase keys, include null values).

- **Signature:** `toMap(Object source, boolean underscore, boolean ignoreNull) → Map<String, Object>`
- **Description:** Converts a bean to a map with optional underscore naming and null skipping.

- **Signature:** `toBean(Map<String, Object> source, Class<T> targetClass) → T`
- **Description:** Converts a map to a bean of target class.

- **Signature:** `toBean(Map<String, Object> source, Class<T> targetClass, boolean underscore) → T`
- **Description:** Converts a map to a bean with optional camelCase conversion.

- **Signature:** `toBean(Collection<Map<String, Object>> sourceCollection, Class<T> targetClass) → List<T>`
- **Description:** Converts a collection of maps to a list of beans.

---

## CryptoUtils

**Type:** final class

Cryptographic utilities: password hashing (BCrypt), symmetric encryption (AES-GCM), and asymmetric encryption (RSA/OAEP).

### Methods
- **Signature:** `sha256Hex(String value) → String`
- **Description:** Computes SHA-256 hex digest.

- **Signature:** `encryptPassword(String rawPassword) → String`
- **Description:** Encrypts a raw password using BCrypt.
- **Throws:** NexusException if rawPassword is null or encryption fails

- **Signature:** `generatePasswordSalt() → String`
- **Description:** Generates a BCrypt salt for password hashing.
- **Throws:** NexusException if salt generation fails

- **Signature:** `encryptPassword(String rawPassword, String salt) → String`
- **Description:** Encrypts a raw password using BCrypt and an externally supplied salt.
- **Throws:** NexusException if rawPassword is null, salt is blank, or encryption fails

- **Signature:** `matchesPassword(String rawPassword, String encryptedPassword) → boolean`
- **Description:** Verifies a raw password against a BCrypt hash.

- **Signature:** `encryptAesGcm(String plaintext, String secret) → String`
- **Description:** Encrypts plaintext using AES-GCM with random 12-byte IV. Returns Base64-encoded IV + ciphertext.
- **Throws:** NexusException on failure

- **Signature:** `decryptAesGcm(String encrypted, String secret) → String`
- **Description:** Decrypts AES-GCM ciphertext (expects first 12 bytes as IV).
- **Throws:** NexusException on failure

- **Signature:** `generateRsaKeyPair() → AsymmetricKeyPair`
- **Description:** Generates RSA key pair with default 2048-bit key size.

- **Signature:** `generateRsaKeyPair(int keySize) → AsymmetricKeyPair`
- **Description:** Generates RSA key pair with specified key size (min 2048).
- **Throws:** NexusException if keySize < 2048

- **Signature:** `encryptRsa(String plaintext, String publicKey) → String`
- **Description:** Encrypts plaintext using RSA-OAEP with SHA-256. Supports block-mode for large data.
- **Throws:** NexusException on failure

- **Signature:** `decryptRsa(String encrypted, String privateKey) → String`
- **Description:** Decrypts RSA ciphertext using private key.
- **Throws:** NexusException on failure

### Inner Record: `AsymmetricKeyPair(String publicKey, String privateKey)`
- **Type:** record
- **Components:** `publicKey` — Base64-encoded public key; `privateKey` — Base64-encoded private key

---

## DateTimeUtils

**Type:** final class

Date and time formatting and parsing utilities with multiple predefined patterns.

### Pattern Constants
- `DEFAULT_DATE_PATTERN = "yyyy-MM-dd"`
- `DEFAULT_TIME_PATTERN = "HH:mm:ss"`
- `DEFAULT_SIMPLE_TIME_PATTERN = "HH:mm"`
- `DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"`
- `DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"`
- `DATETIME_COMPACT_MS_PATTERN = "yyyyMMddHHmmss.SSS"`
- `DATE_PATTERN_SLASH = "yyyy/MM/dd"`
- `DATE_PATTERN_DOT = "yyyy.MM.dd"`
- `DATE_PATTERN_NO_SEPARATOR = "yyyyMMdd"`
- `DATETIME_PATTERN_SLASH = "yyyy/MM/dd HH:mm:ss"`
- `DATETIME_PATTERN_DOT = "yyyy.MM.dd HH:mm:ss"`
- `DATETIME_PATTERN_NO_SEPARATOR = "yyyyMMddHHmmss"`
- `DATETIME_PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss"`
- `DATETIME_PATTERN_ISO_WITH_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS"`
- `SUPPORTED_DATE_PATTERNS` — list of 4 date patterns
- `SUPPORTED_DATETIME_PATTERNS` — list of 8 datetime patterns

### Methods
- **Signature:** `format(LocalDateTime dateTime) → String` — uses `yyyy-MM-dd HH:mm:ss`
- **Signature:** `format(LocalDateTime dateTime, String pattern) → String`
- **Signature:** `format(LocalDate date) → String` — uses `yyyy-MM-dd`
- **Signature:** `format(LocalDate date, String pattern) → String`
- **Signature:** `formatDate(Date date, String pattern) → String` — uses legacy SimpleDateFormat

- **Signature:** `parseLocalDateTime(String value) → LocalDateTime`
- **Description:** Parses using ISO instant format first, then supported datetime patterns, then date-only (midnight).

- **Signature:** `parseLocalDateTime(String value, String pattern) → LocalDateTime`
- **Signature:** `parseLocalDate(String value) → LocalDate`
- **Signature:** `parseDate(String value, String pattern) → Date` — uses legacy SimpleDateFormat

- **Signature:** `toDate(LocalDateTime dateTime) → Date`
- **Signature:** `asDate(LocalDateTime dateTime) → Date` — defaults to now if null
- **Signature:** `toLocalDateTime(Date date) → LocalDateTime`
- **Signature:** `toLocalDateTime(Long epochMillis) → LocalDateTime`
- **Signature:** `toDateTime(Long epochMillis) → LocalDateTime`

- **Signature:** `normalizeDateTime(Object value) → LocalDateTime`
- **Description:** Normalizes LocalDateTime, LocalDate (midnight), Date, Long (epoch millis), or String.

- **Signature:** `isToday(Date date) → boolean`
- **Signature:** `getDiffDay(Date day1, Date day2) → int`
- **Description:** Returns absolute difference in days.

- **Signature:** `consume(long startTime) → String` — elapsed from startTime to now
- **Signature:** `consume(long endTime, long startTime) → String`
- **Description:** Formats elapsed time as `"X hours, Y minutes, Z seconds, N ms."`.

- **Signature:** `consume(LocalDateTime endTime, LocalDateTime startTime) → String`
- **Signature:** `prettyDuration(LocalDateTime endTime, LocalDateTime startTime) → String`
- **Description:** Alias for consume.

---

## EnvUtils

**Type:** final class

Environment property resolver with override support. Lookup order: overrides → system properties → environment variables.

### Methods
- **Signature:** `value(String key) → String`
- **Signature:** `value(String key, String defaultValue) → String`
- **Description:** Resolves a value from overrides, system properties, or environment variables.

- **Signature:** `set(String key, String value) → void`
- **Description:** Sets a programmatic override. Null value clears the key.

- **Signature:** `putAll(Map<String, ?> values) → void`
- **Description:** Sets multiple overrides from a map.

- **Signature:** `clear(String key) → void`
- **Description:** Removes a programmatic override.

---

## IdGenerator

**Type:** final class

ID generation utilities: Snowflake-based distributed IDs, prefixed ULIDs,
random IDs, timestamp-prefixed IDs, and batch generation.

### Factory Methods
- **Signature:** `of(long datacenterId, long workerId) → IdGenerator`
- **Signature:** `from(String address, int port) → IdGenerator`
- **Description:** Derives IDs from an IP address and port.

### Global Singleton
- **Signature:** `configureGlobal(long datacenterId, long workerId) → void`
- **Signature:** `configureGlobal(String address, int port) → void`
- **Signature:** `next() → long` — next Snowflake ID from global generator
- **Signature:** `nextString() → String` — next Snowflake ID as string from global generator

### Instance Methods
- **Signature:** `nextId() → long` (synchronized)
- **Signature:** `nextIdString() → String`

### ULID Generation
- **Signature:** `ulid(String prefix) → String`
- **Description:** Generates a ULID string with an optional prefix.

- **Signature:** `monotonicUlid(String prefix) → String`
- **Description:** Generates a monotonic ULID string with an optional prefix.

### Random ID Generation
- **Signature:** `random(String prefix) → String`
- **Description:** Generates 8 random alphanumeric chars with optional prefix.

- **Signature:** `random(String prefix, Type type, int length) → String`
- **Description:** Generates a random ID with specified character type and length.

- **Signature:** `timestamp(String prefix, Type type, int randomLength, boolean includeMillis) → String`
- **Description:** Generates a timestamp-prefixed ID. Format: `[prefix]yyyyMMddHHmmss[SSS][random]`.

- **Signature:** `unique(String prefix, Type type) → String`
- **Description:** Generates unique ID using current millis + 6 random chars.

- **Signature:** `batch(String prefix, Type type, int length, int count) → List<String>`
- **Description:** Generates a batch of random IDs.

### Inner Enum: `IdGenerator.Type`
- `NUMERIC` — digits only
- `ALPHANUMERIC` — upper + lower + digits
- `ALPHANUMERIC_UPPER` — uppercase + digits
- `ALPHANUMERIC_LOWER` — lowercase + digits
- `HEXADECIMAL` — hex chars (0-9, A-F)

---

## MetricsSnapshot

**Type:** record

A point-in-time snapshot of a metrics counter/timer.

### Components
- `String name` — metric name (normalized)
- `Map<String, String> tags` — metric tags
- `long count` — total count (counter + timer)
- `long totalNanos` — cumulative duration in nanoseconds

### Methods
- **Signature:** `totalMillis() → double`
- **Description:** Returns total duration in milliseconds.

---

## MetricsUtils

**Type:** final class

Micrometer-based metrics facade. Provides counters, timers, and gauges with automatic name normalization (lowercase, underscore-separated).

### Methods
- **Signature:** `initialize(MeterRegistry registry) → void`
- **Description:** Sets a custom MeterRegistry (must not be null).

- **Signature:** `registry() → MeterRegistry`

- **Signature:** `increment(String name) → void` — increments counter by 1
- **Signature:** `increment(String name, Map<String, String> tags) → void`
- **Signature:** `increment(String name, Map<String, String> tags, long amount) → void`

- **Signature:** `record(String name, Callable<T> task) → T`
- **Signature:** `record(String name, Map<String, String> tags, Callable<T> task) → T`
- **Signature:** `record(String name, Runnable task) → void`
- **Signature:** `record(String name, Map<String, String> tags, Runnable task) → void`

- **Signature:** `recordWithStatus(String name, Callable<T> task) → T`
- **Signature:** `recordWithStatus(String name, Map<String, String> tags, Callable<T> task) → T`
- **Description:** Records task and increments success/failure counter based on outcome.
- **Signature:** `recordWithStatus(String name, Runnable task) → void`
- **Signature:** `recordWithStatus(String name, Map<String, String> tags, Runnable task) → void`

- **Signature:** `recordDuration(String name, Map<String, String> tags, long nanos) → void`
- **Signature:** `recordDuration(String name, Map<String, String> tags, Duration duration) → void`

- **Signature:** `countSuccess(String name, Map<String, String> tags) → void`
- **Signature:** `countFailure(String name, Map<String, String> tags) → void`
- **Signature:** `countRetry(String name, Map<String, String> tags) → void`

- **Signature:** `gauge(String name, Map<String, String> tags, Supplier<Number> supplier) → void`

- **Signature:** `snapshot(String name) → MetricsSnapshot`
- **Signature:** `snapshot(String name, Map<String, String> tags) → MetricsSnapshot`

- **Signature:** `clear() → void`
- **Description:** Clears the meter registry.

- **Signature:** `normalizeName(String name) → String`
- **Description:** Normalizes a metric name to lowercase, underscore-separated.

---

## StringUtils

**Type:** final class

String utilities: blank checks, placeholder replacement, camelCase/underscore conversion, and random key generation.

### Methods
- **Signature:** `isBlank(CharSequence value) → boolean`

- **Signature:** `defaultIfBlank(String value, String defaultValue) → String`

- **Signature:** `replacePlaceholders(String text, Map<String, ?> values) → String`
- **Description:** Replaces `${key}` and `{{key}}` placeholders with values from map. Unmatched placeholders left unchanged.

- **Signature:** `camelToUnderscore(String camelValue) → String`

- **Signature:** `underscoreToCamel(String underscoreValue) → String`

- **Signature:** `randomKey(int count) → String`
- **Description:** Generates a random alphanumeric key of specified length.

- **Signature:** `converter(Object value) → String`
- **Description:** Converts an object to string (null → "").
