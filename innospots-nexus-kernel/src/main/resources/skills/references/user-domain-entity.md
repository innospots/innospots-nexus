# Package `user.domain.entity`

## UserEntity

**Type:** class

Persistence entity for the `nx_user` table. It stores registration profile,
identity contact fields, verification flags, lifecycle status, registration
source, and last-login metadata. It inherits audit fields from
`BaseEntity` and intentionally does not store password hash or salt fields.

### TABLE_NAME
- **Signature:** `TABLE_NAME -> String`
- **Description:** MyBatis-Plus and JPA table name, `nx_user`.

## UserPasswordCredentialEntity

**Type:** class

Persistence entity for the `nx_user_password` table. It stores
local password hash material, password algorithm metadata, password versioning,
failure/lock state, and expiry. It inherits audit fields from `BaseEntity`.

### TABLE_NAME
- **Signature:** `TABLE_NAME -> String`
- **Description:** MyBatis-Plus and JPA table name, `nx_user_password`.

## UserOauthIdentityEntity

**Type:** class

Persistence entity for the `nx_user_oauth` table. It binds one
platform user to one external OAuth provider subject and stores provider
profile metadata plus token storage keys. It inherits audit fields from
`BaseEntity`.

### TABLE_NAME
- **Signature:** `TABLE_NAME -> String`
- **Description:** MyBatis-Plus and JPA table name, `nx_user_oauth`.
