# Domain Identity

## UserInfo

**Type:** class

Core user identity model. Contains immutable identity fields (`userId`, `userName`, `realName`) and mutable attributes such as email, avatar, group membership, and assigned roles.

### simple
- **Signature:** `static UserInfo simple(Long userId, String userName, String realName) → UserInfo`
- **Description:** Creates a user with the core immutable identity fields.
- **Parameters:** `userId` — unique user ID; `userName` — login username; `realName` — real/display name
- **Returns:** a new UserInfo

### userId
- **Signature:** `Long userId() → Long`
- **Description:** Returns the unique user ID.
- **Returns:** user ID

### userName
- **Signature:** `String userName() → String`
- **Description:** Returns the login username.
- **Returns:** username

### realName
- **Signature:** `String realName() → String`
- **Description:** Returns the real/display name.
- **Returns:** real name

### displayName
- **Signature:** `String displayName() → String`
- **Description:** Returns the best available display name (realName if non-blank, otherwise userName).
- **Returns:** display name string

### email (getter)
- **Signature:** `String email() → String`
- **Description:** Returns the email address.
- **Returns:** email (may be null)

### email (setter)
- **Signature:** `UserInfo email(String email) → UserInfo`
- **Description:** Sets the email address.
- **Parameters:** `email` — email address
- **Returns:** this UserInfo (fluent)

### avatarKey (getter)
- **Signature:** `String avatarKey() → String`
- **Description:** Returns the avatar storage key.
- **Returns:** avatar key (may be null)

### avatarKey (setter)
- **Signature:** `UserInfo avatarKey(String avatarKey) → UserInfo`
- **Description:** Sets the avatar storage key.
- **Parameters:** `avatarKey` — avatar key identifier
- **Returns:** this UserInfo (fluent)

### lastAccessTime (getter)
- **Signature:** `LocalDateTime lastAccessTime() → LocalDateTime`
- **Description:** Returns the last access timestamp.
- **Returns:** last access time (may be null)

### lastAccessTime (setter)
- **Signature:** `UserInfo lastAccessTime(LocalDateTime lastAccessTime) → UserInfo`
- **Description:** Sets the last access timestamp.
- **Parameters:** `lastAccessTime` — timestamp of last access
- **Returns:** this UserInfo (fluent)

### status (getter)
- **Signature:** `BasicStatus status() → BasicStatus`
- **Description:** Returns the user status (default ENABLED).
- **Returns:** current status

### status (setter)
- **Signature:** `UserInfo status(BasicStatus status) → UserInfo`
- **Description:** Sets the user status. Falls back to ENABLED if null.
- **Parameters:** `status` — the status to set
- **Returns:** this UserInfo (fluent)

### group (getter)
- **Signature:** `UserGroupInfo group() → UserGroupInfo`
- **Description:** Returns the user's group.
- **Returns:** group info (may be null)

### group (setter)
- **Signature:** `UserInfo group(UserGroupInfo group) → UserInfo`
- **Description:** Sets the user's group.
- **Parameters:** `group` — user group info
- **Returns:** this UserInfo (fluent)

### roles
- **Signature:** `List<RoleInfo> roles() → List<RoleInfo>`
- **Description:** Returns an unmodifiable list of assigned roles.
- **Returns:** copy of the roles list

### role
- **Signature:** `UserInfo role(RoleInfo role) → UserInfo`
- **Description:** Adds a role assignment.
- **Parameters:** `role` — role info to add
- **Returns:** this UserInfo (fluent)

---

## UserGroupInfo

**Type:** record

A user group/team with a hierarchy (parent group), a head user, and assistant users.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `groupId` | `Long` | Unique group identifier |
| `groupName` | `String` | Display name of the group |
| `groupCode` | `String` | Programmatic code for the group |
| `parentGroupId` | `Long` | ID of the parent group (null for root groups) |
| `headUserId` | `Long` | ID of the group head/leader |
| `assistantUserIds` | `List<Long>` | IDs of assistant users (never null, defaults to empty list) |
| `status` | `BasicStatus` | Enable/disable status |

### Compact Constructor
- **Signature:** `UserGroupInfo(Long groupId, String groupName, String groupCode, Long parentGroupId, Long headUserId, List<Long> assistantUserIds, BasicStatus status) → UserGroupInfo`
- **Description:** Validates and normalizes the record. Null assistantUserIds defaults to an empty list.

---

## RoleInfo

**Type:** record

A role definition with a unique identifier, display name, and programmatic code.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `roleId` | `Long` | Unique role identifier |
| `roleName` | `String` | Display name of the role |
| `roleCode` | `String` | Programmatic code for the role |
| `status` | `BasicStatus` | Enable/disable status |