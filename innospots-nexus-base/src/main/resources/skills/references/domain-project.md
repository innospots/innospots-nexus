# Domain Project

## ProjectInfo

**Type:** class

A project within an organization. Projects scope workflows, resources, and execution records.

### named
- **Signature:** `static ProjectInfo named(Long projectId, String projectName, String projectCode) → ProjectInfo`
- **Description:** Creates a project with the core immutable fields.
- **Parameters:** `projectId` — unique project ID; `projectName` — display name; `projectCode` — programmatic code
- **Returns:** a new ProjectInfo

### projectId
- **Signature:** `Long projectId() → Long`
- **Description:** Returns the unique project ID.
- **Returns:** project ID

### projectName
- **Signature:** `String projectName() → String`
- **Description:** Returns the display name.
- **Returns:** project name

### projectCode
- **Signature:** `String projectCode() → String`
- **Description:** Returns the programmatic code.
- **Returns:** project code

### organizationId (getter)
- **Signature:** `Long organizationId() → Long`
- **Description:** Returns the owning organization ID.
- **Returns:** organization ID (may be null)

### organizationId (setter)
- **Signature:** `ProjectInfo organizationId(Long organizationId) → ProjectInfo`
- **Description:** Sets the owning organization ID.
- **Parameters:** `organizationId` — organization ID
- **Returns:** this ProjectInfo (fluent)

### description (getter)
- **Signature:** `String description() → String`
- **Description:** Returns the project description.
- **Returns:** description (may be null)

### description (setter)
- **Signature:** `ProjectInfo description(String description) → ProjectInfo`
- **Description:** Sets the project description.
- **Parameters:** `description` — description text
- **Returns:** this ProjectInfo (fluent)

### status (getter)
- **Signature:** `BasicStatus status() → BasicStatus`
- **Description:** Returns the project status (default ENABLED).
- **Returns:** current status

### status (setter)
- **Signature:** `ProjectInfo status(BasicStatus status) → ProjectInfo`
- **Description:** Sets the project status. Falls back to ENABLED if null.
- **Parameters:** `status` — the status to set
- **Returns:** this ProjectInfo (fluent)