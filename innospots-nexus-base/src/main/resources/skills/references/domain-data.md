# Domain Data

## DataBody

**Type:** class

A self-timing data payload. Automatically records the start time at construction and computes elapsed millis when `end()` is called. Carries optional `DataSchema` metadata and a free-form meta map.

### of
- **Signature:** `static <T> DataBody<T> of(T data) → DataBody<T>`
- **Description:** Wraps data into a new DataBody with automatic start-time recording.
- **Parameters:** `data` — the data payload
- **Returns:** a new DataBody

### data (getter)
- **Signature:** `T data() → T`
- **Description:** Returns the data payload.
- **Returns:** the data

### data (setter)
- **Signature:** `DataBody<T> data(T data) → DataBody<T>`
- **Description:** Sets the data payload.
- **Parameters:** `data` — new data value
- **Returns:** this DataBody (fluent)

### schema (getter)
- **Signature:** `DataSchema schema() → DataSchema`
- **Description:** Returns the associated schema.
- **Returns:** the schema (may be null)

### schema (setter)
- **Signature:** `DataBody<T> schema(DataSchema schema) → DataBody<T>`
- **Description:** Sets the schema metadata.
- **Parameters:** `schema` — schema definition
- **Returns:** this DataBody (fluent)

### message (getter)
- **Signature:** `String message() → String`
- **Description:** Returns the message.
- **Returns:** message string

### message (setter)
- **Signature:** `DataBody<T> message(String message) → DataBody<T>`
- **Description:** Sets the message.
- **Parameters:** `message` — message text
- **Returns:** this DataBody (fluent)

### elapsedMillis
- **Signature:** `long elapsedMillis() → long`
- **Description:** Returns the elapsed milliseconds (valid after `end()` is called).
- **Returns:** elapsed time in milliseconds

### end
- **Signature:** `DataBody<T> end() → DataBody<T>`
- **Description:** Stops the timer and records elapsed milliseconds since construction.
- **Returns:** this DataBody (fluent)

### meta (getter)
- **Signature:** `Map<String, Object> meta() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of the metadata map.
- **Returns:** copy of the meta map

### meta (setter)
- **Signature:** `DataBody<T> meta(String key, Object value) → DataBody<T>`
- **Description:** Sets a metadata entry.
- **Parameters:** `key` — metadata key; `value` — metadata value
- **Returns:** this DataBody (fluent)

---

## DataOperation

**Type:** enum

The type of data operation to perform on a target datasource.

| Constant | Description |
|----------|-------------|
| `QUERY` | Read/select operation |
| `CREATE` | Insert operation |
| `UPDATE` | Update operation |
| `DELETE` | Delete operation |
| `EXECUTE` | Generic execution (e.g. stored procedure) |

---

## DataPage

**Type:** record

Immutable paginated data container. Validates page bounds at construction and provides convenience methods for pagination navigation.

### Record Components
| Component | Type | Description |
|-----------|------|-------------|
| `records` | `List<T>` | Page records (never null) |
| `pageNo` | `long` | 1-indexed page number |
| `pageSize` | `long` | Number of records per page |
| `total` | `long` | Total record count across all pages |
| `pages` | `long` | Total number of pages |

### Compact Constructor
- **Signature:** `DataPage(List<T> records, long pageNo, long pageSize, long total, long pages) → DataPage`
- **Description:** Validates pagination parameters (pageNo > 0, pageSize > 0, total >= 0) and ensures records is never null.
- **Throws:** `IllegalArgumentException` if validation fails

### of
- **Signature:** `static <T> DataPage<T> of(List<T> records, long pageNo, long pageSize, long total) → DataPage<T>`
- **Description:** Creates a page with auto-calculated total pages.
- **Parameters:** `records` — page records (null-safe); `pageNo` — 1-indexed page number; `pageSize` — records per page; `total` — total record count
- **Returns:** a new DataPage

### empty
- **Signature:** `static <T> DataPage<T> empty(long pageNo, long pageSize) → DataPage<T>`
- **Description:** Returns an empty page for the given page number and size.
- **Parameters:** `pageNo` — page number; `pageSize` — page size
- **Returns:** a DataPage with empty records and total=0

### hasNext
- **Signature:** `boolean hasNext() → boolean`
- **Description:** Returns true if there is a next page available.
- **Returns:** true if pageNo < pages

### hasPrevious
- **Signature:** `boolean hasPrevious() → boolean`
- **Description:** Returns true if there is a previous page available.
- **Returns:** true if pageNo > 1 and pages > 0

---

## DataRequest

**Type:** class

A request to perform a `DataOperation` on a named target datasource. Supports an optional request body, pagination parameters, a free-form query map, and a metadata map.

### create
- **Signature:** `static <T> DataRequest<T> create(String target, DataOperation operation) → DataRequest<T>`
- **Description:** Creates a data request for the given target and operation.
- **Parameters:** `target` — datasource or entity identifier; `operation` — the type of operation
- **Returns:** a new DataRequest

### target
- **Signature:** `String target() → String`
- **Description:** Returns the target datasource identifier.
- **Returns:** target string

### operation
- **Signature:** `DataOperation operation() → DataOperation`
- **Description:** Returns the operation type.
- **Returns:** the operation

### credentialKey (getter)
- **Signature:** `String credentialKey() → String`
- **Description:** Returns the credential key used for authentication.
- **Returns:** credential key (may be null)

### credentialKey (setter)
- **Signature:** `DataRequest<T> credentialKey(String credentialKey) → DataRequest<T>`
- **Description:** Sets the credential key.
- **Parameters:** `credentialKey` — authentication credential key
- **Returns:** this DataRequest (fluent)

### body (getter)
- **Signature:** `T body() → T`
- **Description:** Returns the request body.
- **Returns:** the body (may be null)

### body (setter)
- **Signature:** `DataRequest<T> body(T body) → DataRequest<T>`
- **Description:** Sets the request body.
- **Parameters:** `body` — request data payload
- **Returns:** this DataRequest (fluent)

### pageNo
- **Signature:** `int pageNo() → int`
- **Description:** Returns the current page number (default 1).
- **Returns:** page number

### pageSize
- **Signature:** `int pageSize() → int`
- **Description:** Returns the current page size (default 20).
- **Returns:** page size

### page
- **Signature:** `DataRequest<T> page(int pageNo, int pageSize) → DataRequest<T>`
- **Description:** Sets pagination parameters. Both values must be positive.
- **Parameters:** `pageNo` — 1-indexed page number; `pageSize` — records per page
- **Throws:** `IllegalArgumentException` if values are less than 1
- **Returns:** this DataRequest (fluent)

### query (getter all)
- **Signature:** `Map<String, Object> query() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of the query parameters.
- **Returns:** copy of the query map

### query (getter by key)
- **Signature:** `Object query(String key) → Object`
- **Description:** Returns a specific query parameter by key.
- **Parameters:** `key` — query parameter key
- **Returns:** the query value, or null if absent

### query (setter)
- **Signature:** `DataRequest<T> query(String key, Object value) → DataRequest<T>`
- **Description:** Sets a query parameter.
- **Parameters:** `key` — query key; `value` — query value
- **Returns:** this DataRequest (fluent)

### meta (getter)
- **Signature:** `Map<String, Object> meta() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of the metadata map.
- **Returns:** copy of the meta map

### meta (setter)
- **Signature:** `DataRequest<T> meta(String key, Object value) → DataRequest<T>`
- **Description:** Sets a metadata entry.
- **Parameters:** `key` — metadata key; `value` — metadata value
- **Returns:** this DataRequest (fluent)

---

## DataResponse

**Type:** class

A generic response envelope for data operations. Distinguishes success from failure via the `success` flag and carries an optional `DataSchema`, data payload, and metadata.

### ok
- **Signature:** `static <T> DataResponse<T> ok(T data) → DataResponse<T>`
- **Description:** Creates a success response with the given data payload.
- **Parameters:** `data` — the data payload
- **Returns:** a success DataResponse

### fail
- **Signature:** `static <T> DataResponse<T> fail(String code, String message) → DataResponse<T>`
- **Description:** Creates a failure response with an error code and message.
- **Parameters:** `code` — error code; `message` — error description
- **Returns:** a failure DataResponse with null data

### success
- **Signature:** `boolean success() → boolean`
- **Description:** Returns true if the operation succeeded.
- **Returns:** success flag

### code
- **Signature:** `String code() → String`
- **Description:** Returns the result code.
- **Returns:** code string

### message
- **Signature:** `String message() → String`
- **Description:** Returns the result message.
- **Returns:** message string

### data
- **Signature:** `T data() → T`
- **Description:** Returns the data payload.
- **Returns:** the data (may be null on failure)

### schema (getter)
- **Signature:** `DataSchema schema() → DataSchema`
- **Description:** Returns the associated schema.
- **Returns:** the schema (may be null)

### schema (setter)
- **Signature:** `DataResponse<T> schema(DataSchema schema) → DataResponse<T>`
- **Description:** Sets the schema metadata.
- **Parameters:** `schema` — schema definition
- **Returns:** this DataResponse (fluent)

### meta (getter)
- **Signature:** `Map<String, Object> meta() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of the metadata map.
- **Returns:** copy of the meta map

### meta (setter by key)
- **Signature:** `DataResponse<T> meta(String key, Object value) → DataResponse<T>`
- **Description:** Sets a single metadata entry.
- **Parameters:** `key` — metadata key; `value` — metadata value
- **Returns:** this DataResponse (fluent)

### meta (setter by map)
- **Signature:** `DataResponse<T> meta(Map<String, Object> meta) → DataResponse<T>`
- **Description:** Merges the given map into the existing meta.
- **Parameters:** `meta` — map of metadata entries to merge
- **Returns:** this DataResponse (fluent)

---

## DataSchema

**Type:** class

Describes the structure of a data payload: a list of `DomainField`s plus free-form configuration entries. Used to convey field metadata alongside data responses.

### named
- **Signature:** `static DataSchema named(String code, String name) → DataSchema`
- **Description:** Creates a schema with a programmatic code and display name.
- **Parameters:** `code` — programmatic identifier; `name` — display name
- **Returns:** a new DataSchema

### code
- **Signature:** `String code() → String`
- **Description:** Returns the schema code.
- **Returns:** code string

### name
- **Signature:** `String name() → String`
- **Description:** Returns the display name.
- **Returns:** name string

### fields
- **Signature:** `List<DomainField> fields() → List<DomainField>`
- **Description:** Returns an unmodifiable list of fields.
- **Returns:** copy of the fields list

### field (add)
- **Signature:** `DataSchema field(DomainField field) → DataSchema`
- **Description:** Adds a field to the schema definition.
- **Parameters:** `field` — the domain field to add
- **Returns:** this DataSchema (fluent)

### field (lookup)
- **Signature:** `Optional<DomainField> field(String code) → Optional<DomainField>`
- **Description:** Looks up a field by its programmatic code.
- **Parameters:** `code` — the field code to search for
- **Returns:** Optional containing the field, or empty if not found

### configs
- **Signature:** `Map<String, Object> configs() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of configuration.
- **Returns:** copy of the configs map

### config (setter)
- **Signature:** `DataSchema config(String key, Object value) → DataSchema`
- **Description:** Sets a configuration property on this schema.
- **Parameters:** `key` — config key; `value` — config value
- **Returns:** this DataSchema (fluent)

### config (getter)
- **Signature:** `Object config(String key) → Object`
- **Description:** Gets a configuration property by key.
- **Parameters:** `key` — config key
- **Returns:** the config value, or null if absent