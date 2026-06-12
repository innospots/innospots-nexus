# Package `domain.request`

## SimpleQueryRequest

**Type:** record

Immutable paginated query request with built-in defaults. Subclasses are not
supported — domain-specific page requests should inline the fields or compose
this record.

### Components
- **Signature:** `input() -> String`
- **Description:** Common fuzzy search keyword, may be `null`.

- **Signature:** `pageNo() -> long`
- **Description:** 1-indexed page number, defaults to `1`. Invalid values are normalized to `1`.

- **Signature:** `pageSize() -> long`
- **Description:** Records per page, defaults to `20`. Invalid values are normalized to `20`.

### Constants
- `DEFAULT_PAGE_NO = 1L`
- `DEFAULT_PAGE_SIZE = 20L`

### Constructors
- **Signature:** `SimpleQueryRequest()`
- **Description:** No-arg constructor with defaults (`null`, `1`, `20`).

- **Signature:** `SimpleQueryRequest(String input, long pageNo, long pageSize)`
- **Description:** Canonical constructor. Invalid page number or size are normalized.
