# Module Skills

Each module must maintain a `skills/` directory under
`src/main/resources/skills/` containing a `SKILL.md` that describes the
module's capabilities and a `references/` subdirectory with per-package
reference documentation. This provides a consistent, discoverable way to
understand any module's contract without reading every source file.

## SKILL.md Format

Every `SKILL.md` must have YAML front matter and the following sections:

```yaml
---
name: <module-artifact-id>
description: |
  Concise summary followed by a bullet list of every major capability with
  enough detail to understand what the module can do.
version: <semver>
---
```

### Required Sections (in order)

| Section | Content |
|---------|---------|
| `# <Module Name>` | Module title |
| `## Module Overview` | Brief introduction + "What it can do" capability table |
| `## Class Reference` | Per-package tables: `\| Class \| Type \| Description \|` |
| `## References` | Mapping table to per-package reference files in `references/` |

The capability table in Module Overview should have two columns:
`| Capability | Description |` — one row per cohesive feature.

### Example Capability Table

```markdown
## Module Overview

**What it can do:**

| Capability | Description |
|------------|-------------|
| **Condition Engine** | Build filter/rule conditions with typed factors and operators |
| **Identity & Access** | Model users, roles, groups, and organizations |
| ... | ... |
```

### Example Class Reference

```markdown
### Package `domain.condition`

| Class | Type | Description |
|-------|------|-------------|
| `Factor` | `class` | A single filter criterion with placeholder resolution |
| `Operator` | `enum` | Comparison operators with dual DB/script symbols |
```

## References Directory

Each package in the module gets a corresponding markdown file in
`references/` named `<package-name>.md` (replace dots with hyphens,
e.g. `domain-condition.md` for `domain.condition`). The file documents
every public class/enum/interface/record/annotation in the package:

```markdown
# Package Name

## ClassName

**Type:** class/enum/interface/record/annotation

Description of the class purpose.

### Method/Field Name
- **Signature:** `methodName(ParamType param) → ReturnType`
- **Description:** what this method does
- **Parameters:** (if applicable) param name + type + description
- **Returns:** description of return value
```

## Consistency Rules

- Every Java module must have `src/main/resources/skills/SKILL.md`.
- Every package with public API must have a corresponding reference file.
- When adding a new class to a package, update both `SKILL.md` (Class
  Reference table) and the corresponding reference file.
- When adding a new package to a module, add a section to SKILL.md's
  Class Reference, create the reference file, and add a row in the
  References mapping table.