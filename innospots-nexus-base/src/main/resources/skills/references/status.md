# Status — Status Codes & Categorisation

## StatusCategory

**Type:** enum

Categorises status codes by concern area. Each category has a 2-digit code embedded in the full status code string and a priority level.

### Constants

| Constant | Code | Label | Priority |
|---|---|---|---|
| `GENERAL` | 00 | General | L |
| `INPUT_VALIDATION` | 01 | Input validation | H |
| `BUSINESS_RULE` | 02 | Business rule | H |
| `INTERNAL_ERROR` | 03 | Internal error | C |
| `PERMISSION_SECURITY` | 04 | Permission or security | C |
| `TRANSACTION_CONFLICT` | 05 | Transaction conflict | M |
| `EXTERNAL_FAILURE` | 06 | External failure | C |
| `DATA_CONSISTENCY` | 07 | Data consistency | C |
| `CONFIGURATION` | 08 | Configuration | H |
| `COMPLIANCE` | 09 | Compliance | B |
| `RESOURCE_LIMIT` | 10 | Resource limit | C |
| `BATCH_JOB` | 11 | Batch job | H |
| `CHANNEL_INTERACTION` | 12 | Channel interaction | M |
| `RESOURCE_DATA` | 13 | Resource data | M |
| `DATA_OPERATION` | 14 | Data operation | M |
| `FILE_OPERATION` | 15 | File operation | M |
| `MIDDLEWARE` | 16 | Middleware | C |
| `CRYPTO` | 17 | Cryptography | C |
| `SCRIPT` | 18 | Script | C |
| `DATA_CONNECTION` | 19 | Data connection | C |
| `DATA_SCHEMA` | 20 | Data schema | C |
| `SQL_EXECUTION` | 21 | SQL execution | C |

### Methods
- `code()` → `String` — 2-digit category code
- `label()` → `String` — human-readable label
- `priority()` → `String` — priority level (L/M/H/B/C)

---

## StatusCode

**Type:** interface

Composable status code consisting of a 3-letter module code, a `StatusCategory` (2 digits), and a 4-digit local code.

### Methods
- **Signature:** `module() → String`
- **Returns:** 3-letter module code (e.g. `"NEX"`)

- **Signature:** `category() → StatusCategory`

- **Signature:** `localCode() → String`
- **Returns:** 4-digit local code

- **Signature:** `message() → I18nObject`

- **Signature:** `advice() → I18nObject`

- **Signature:** `httpStatusCode() → int`
- **Returns:** HTTP status code (e.g. 200, 400, 404, 500)

- **Signature:** `name() → String`

- **Signature:** `bisCode() → String` (default)
- **Description:** Returns `module + category.code + localCode` after validation.
- **Throws:** IllegalArgumentException if format is invalid

- **Signature:** `fullCode() → String` (default)
- **Description:** Alias for `bisCode()`.

- **Signature:** `summary() → String` (default)
- **Description:** Returns `"message, advice"` or just `message` if advice is blank.

---

## StatusCodeRules

**Type:** final class

Validation rules for status code formatting.

### Method
- **Signature:** `requireValid(String module, StatusCategory category, String localCode) → void`
- **Description:** Validates that module is 3 uppercase letters, category is non-null, localCode is 4 digits.
- **Throws:** IllegalArgumentException on invalid format

---

## NexusStatusCode

**Type:** enum implementing `StatusCode`

Platform-wide status codes with bilingual (EN/ZH) messages and advice. Full code format: `NEX + category(2) + local(4)`.

### Constants
| Constant | Local Code | Category | HTTP Status | Full Code |
|---|---|---|---|---|
| `SUCCESS` | 0000 | GENERAL | 200 | NEX000000 |
| `INVALID_PARAMETER` | 0001 | INPUT_VALIDATION | 400 | NEX010001 |
| `CONFIG_ERROR` | 0002 | CONFIGURATION | 500 | NEX080002 |
| `SERIALIZATION_FAILED` | 0003 | INTERNAL_ERROR | 500 | NEX030003 |
| `DATA_NOT_FOUND` | 0004 | DATA_OPERATION | 404 | NEX140004 |
| `RESOURCE_NOT_FOUND` | 0005 | RESOURCE_DATA | 404 | NEX130005 |
| `NO_PERMISSION` | 0006 | PERMISSION_SECURITY | 403 | NEX040006 |
| `AUTHENTICATION_FAILED` | 0007 | PERMISSION_SECURITY | 401 | NEX040007 |
| `PASSWORD_ERROR` | 0008 | PERMISSION_SECURITY | 400 | NEX040008 |
| `USER_NOT_FOUND` | 0009 | PERMISSION_SECURITY | 404 | NEX040009 |
| `EXECUTION_FAILED` | 0010 | BATCH_JOB | 500 | NEX110010 |
| `BUSINESS_ERROR` | 0011 | BUSINESS_RULE | 400 | NEX020011 |
| `LIMIT_EXCEEDED` | 0012 | RESOURCE_LIMIT | 429 | NEX100012 |
| `RETRY_FAILED` | 0013 | EXTERNAL_FAILURE | 500 | NEX060013 |
| `OPTIMISTIC_LOCK_FAILED` | 0014 | TRANSACTION_CONFLICT | 409 | NEX050014 |
| `COMPILE_FAILED` | 0015 | SCRIPT | 500 | NEX180015 |
| `INITIALIZATION_FAILED` | 0016 | INTERNAL_ERROR | 500 | NEX030016 |
| `SYSTEM_ERROR` | 0017 | INTERNAL_ERROR | 500 | NEX030017 |

### Methods
- `module()` → `"NEX"`
- `category()` → `StatusCategory`
- `localCode()` → `String`
- `message()` → `I18nObject`
- `advice()` → `I18nObject`
- `httpStatusCode()` → `int`
- **Signature:** `findByFullCode(String fullCode) → Optional<NexusStatusCode>` (static)
- **Description:** Looks up a status code by its full code string (e.g. `NEX000000`).