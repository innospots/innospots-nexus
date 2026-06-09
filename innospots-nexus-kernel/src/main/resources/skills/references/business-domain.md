# Kernel Business Domain Packages

Each business-domain package follows the same lightweight contract pattern:

- One entity record for the core domain concept.
- One enum describing lifecycle, type, scope, effect, channel, or grant style.
- One `Operator` interface defining the domain operation port.

These packages intentionally contain no Spring, Quarkus, Servlet, interceptor,
or persistence implementation annotations.

## Packages

| Package | Responsibility |
|---------|----------------|
| `auth` | Authentication sessions and validation ports |
| `menu` | Console menu definitions and lookup ports |
| `audit` | Login audit log records and query ports |
| `user` | User identity aggregate and management ports |
| `group` | User group and department hierarchy ports |
| `role` | Role definitions and assignment lookup ports |
| `permission` | Resource/action permission definitions and evaluation ports |
| `dictionary` | Controlled dictionary values |
| `notification` | In-app/email/SMS/webhook message records |
| `config` | System configuration values |
| `i18n` | Internationalized dictionary message entries |
| `category` | Hierarchical category definitions |
