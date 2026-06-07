# Domain Enums

## BasicStatus

**Type:** enum

Universal enable/disable status used across domain entities.

| Constant | Description |
|----------|-------------|
| `ENABLED` | Active/enabled state |
| `DISABLED` | Inactive/disabled state |

---

## Visibility

**Type:** enum

Visibility scope for resources and domain entities.

| Constant | Description |
|----------|-------------|
| `PRIVATE` | Visible only to the owner |
| `ORGANIZATION` | Visible within the organization/tenant |
| `PROJECT` | Visible within the project scope |
| `PUBLIC` | Visible to everyone |