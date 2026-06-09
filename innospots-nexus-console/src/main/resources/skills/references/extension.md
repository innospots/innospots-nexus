# Package `extension`

## ConsoleExtension

**Type:** interface

Contract implemented by modules that contribute management-console metadata.

### contribution
- **Signature:** `contribution() -> ConsoleContribution`
- **Description:** Returns immutable contribution metadata.

## ConsoleContribution

**Type:** record

Immutable descriptor with extension key, display name, menus, and routes.

### of
- **Signature:** `of(String, String, List<ConsoleMenuDeclaration>, List<ConsoleRouteDeclaration>) -> ConsoleContribution`
- **Description:** Creates a descriptor with immutable declaration lists.

## ConsoleMenuDeclaration

**Type:** record

Menu metadata containing key, title, path, and order.

## ConsoleRouteDeclaration

**Type:** record

Route metadata containing key, path, and logical component identifier.
