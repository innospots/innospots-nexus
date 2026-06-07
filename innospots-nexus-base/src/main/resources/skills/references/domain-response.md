# Domain Response

## R

**Type:** record

Generic API response wrapper with success/failure status, result code, message, and optional data payload.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `success` | `boolean` | Whether the operation succeeded |
| `code` | `String` | Result code |
| `message` | `String` | Result description |
| `data` | `T` | Response data payload (may be null) |

### ok (no data)
- **Signature:** `static <T> R<T> ok() → R<T>`
- **Description:** Returns a success response with null data.
- **Returns:** R with success=true, code="OK", message="OK", data=null

### ok (with data)
- **Signature:** `static <T> R<T> ok(T data) → R<T>`
- **Description:** Returns a success response wrapping the given data.
- **Parameters:** `data` — the data payload
- **Returns:** R with success=true, code="OK", message="OK", data=data

### fail (code + message)
- **Signature:** `static <T> R<T> fail(String code, String message) → R<T>`
- **Description:** Returns a failure response with an error code and message.
- **Parameters:** `code` — error code; `message` — error description
- **Returns:** R with success=false, data=null

### fail (code + message + data)
- **Signature:** `static <T> R<T> fail(String code, String message, T data) → R<T>`
- **Description:** Returns a failure response with an error code, message, and data payload.
- **Parameters:** `code` — error code; `message` — error description; `data` — additional error data
- **Returns:** R with success=false

---

## PageResult

**Type:** record

Paginated API response wrapper. Validates page bounds at construction and computes the total page count from total record count and page size.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `records` | `List<T>` | Page records (never null) |
| `pageNo` | `long` | 1-indexed page number |
| `pageSize` | `long` | Number of records per page |
| `total` | `long` | Total record count across all pages |
| `pages` | `long` | Total number of pages |

### Compact Constructor
- **Signature:** `PageResult(List<T> records, long pageNo, long pageSize, long total, long pages) → PageResult`
- **Description:** Validates pagination parameters (pageNo > 0, pageSize > 0, total >= 0) and ensures records is never null.
- **Throws:** `IllegalArgumentException` if validation fails

### of
- **Signature:** `static <T> PageResult<T> of(List<T> records, long pageNo, long pageSize, long total) → PageResult<T>`
- **Description:** Creates a page result with auto-calculated total pages.
- **Parameters:** `records` — page records (null-safe); `pageNo` — 1-indexed page number; `pageSize` — records per page; `total` — total record count
- **Returns:** a new PageResult

### empty
- **Signature:** `static <T> PageResult<T> empty(long pageNo, long pageSize) → PageResult<T>`
- **Description:** Returns an empty page result for the given page number and size.
- **Parameters:** `pageNo` — page number; `pageSize` — page size
- **Returns:** a PageResult with empty records and total=0

### hasNext
- **Signature:** `boolean hasNext() → boolean`
- **Description:** Returns true if there is a next page available.
- **Returns:** true if pageNo < pages

### hasPrevious
- **Signature:** `boolean hasPrevious() → boolean`
- **Description:** Returns true if there is a previous page available.
- **Returns:** true if pageNo > 1 and pages > 0