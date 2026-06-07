# Exception (`com.innospots.nexus.base.exception`)

## NexusException

**Type:** class (extends `RuntimeException`)

Base runtime exception for the platform. Carries a machine-readable error code (see `StatusCode`) and a human-readable message.

### Constructor
- **Signature:** `NexusException(String code, String message)`
- **Description:** Constructs an exception with a machine-readable code and a human-readable message.
- **Parameters:** `code` — machine-readable error code; `message` — human-readable description

### Constructor
- **Signature:** `NexusException(String code, String message, Throwable cause)`
- **Description:** Constructs an exception with a code, message, and originating cause.
- **Parameters:** `code` — machine-readable error code; `message` — human-readable description; `cause` — the originating throwable

### Static Method
- **Signature:** `build(StatusCode statusCode) → NexusException`
- **Description:** Builds an exception from a `StatusCode`, using its full code and summary message.
- **Parameters:** `statusCode` (non-null) — the status code descriptor
- **Returns:** `NexusException`
- **Throws:** `NullPointerException` if statusCode is null

### Method
- **Signature:** `code() → String`
- **Description:** Returns the machine-readable error code.
- **Parameters:** none
- **Returns:** `String` — the error code