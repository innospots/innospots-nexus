# Package `util`

## BeanUtils

**Type:** class

Bean property copy and conversion utility wrapping Hutool's `BeanUtil`.

## Checks

**Type:** class

Precondition checks that fail with `NexusException` and `NexusStatusCode#INVALID_PARAMETER`.

## CryptoUtils

**Type:** class

Cryptographic utilities: password hashing (BCrypt), symmetric encryption (AES-GCM), and asymmetric encryption (RSA/OAEP).

## DateTimeUtils

**Type:** class

Date and time formatting and parsing utilities.

## EnvUtils

**Type:** class

Environment property resolver with override support.

## IdGenerator

**Type:** class

ID generation utilities: Snowflake-based distributed IDs, random IDs with configurable character sets, timestamp-prefixed IDs, and batch generation.

## MetricsSnapshot

**Type:** record

A point-in-time snapshot of a metrics counter/timer.

## MetricsUtils

**Type:** class

Micrometer-based metrics facade.

## StringUtils

**Type:** class

String utilities: blank checks, placeholder replacement (`${key`} and `{{key`}}), camelCase/underscore conversion, and random key generation.
