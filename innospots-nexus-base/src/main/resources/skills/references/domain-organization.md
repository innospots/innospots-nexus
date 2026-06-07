# Domain Organization

## OrganizationInfo

**Type:** record

An organization/tenant with locale, currency, and branding preferences.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `organizationId` | `Long` | Unique organization/tenant identifier |
| `organizationName` | `String` | Display name of the organization |
| `organizationCode` | `String` | Programmatic code for the organization |
| `defaultLocale` | `String` | Default locale (e.g. "zh-CN", "en-US") |
| `defaultCurrency` | `String` | Default currency (e.g. "CNY", "USD") |
| `logo` | `String` | Logo storage key or URL |
| `status` | `BasicStatus` | Enable/disable status |