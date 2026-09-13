# Package `status`

## NexusStatusCode

**Type:** enum

Platform-wide status codes with bilingual (EN/ZH) messages and advice, grouped by `StatusCategory`.

## StatusCategory

**Type:** enum

Categorises status codes by concern area.

## StatusCode

**Type:** interface

Interface for a composable status code consisting of a 3-letter module code, a `StatusCategory` (2 digits), and a 4-digit local code.

## StatusCodeRules

**Type:** class

Validation rules for status code formatting.
