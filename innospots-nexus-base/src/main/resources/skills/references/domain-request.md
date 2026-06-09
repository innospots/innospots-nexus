# Package `domain.request`

## BasePageRequest

**Type:** class

Base request object for paginated queries. Higher-level modules should extend
it for domain-specific page filters.

### Fields
- `input` — common fuzzy search input
- `pageNo` — 1-indexed page number, default `1`
- `pageSize` — page size, default `20`

### Methods
- **Signature:** `getInput() -> String`
- **Description:** Returns the common fuzzy search input.

- **Signature:** `getPageNo() -> Long`
- **Description:** Returns a normalized page number, defaulting invalid values to `1`.

- **Signature:** `getPageSize() -> Long`
- **Description:** Returns a normalized page size, defaulting invalid values to `20`.
