# Package `user.domain.entity`

## UserEntity

**Type:** class

Persistence entity for the `nx_user` table. It stores registration profile,
identity contact fields, verification flags, lifecycle status, registration
source, and last-login metadata. It inherits audit fields from
`BaseEntity` and intentionally does not store password hash or salt fields.
Primary key `userId` is a 32-character string column with `IdType.INPUT`;
operators assign a prefixed ULID before insertion.

### TABLE_NAME
- **Signature:** `TABLE_NAME -> String`
- **Description:** MyBatis-Plus and JPA table name, `nx_user`.

### Indexes
- `uk_nx_user_user_name` unique index on `user_name`
- `idx_nx_user_real_name` index on `real_name`
- `idx_nx_user_email` index on `email`
- `idx_nx_user_mobile` index on `mobile`
- `idx_nx_user_status` index on `status`

## UserPasswordCredentialEntity

**Type:** class

Persistence entity for the `nx_user_password` table. It stores
local password hash material, password algorithm metadata, password versioning,
failure/lock state, and expiry. It inherits audit fields from `BaseEntity`.
Primary key `credentialId` and foreign key `userId` are 32-character string
columns. Operators assign a prefixed ULID before insertion.

### TABLE_NAME
- **Signature:** `TABLE_NAME -> String`
- **Description:** MyBatis-Plus and JPA table name, `nx_user_password`.

### Indexes
- `uk_nx_user_password_user_id` unique index on `user_id`

## UserOauthIdentityEntity

**Type:** class

Persistence entity for the `nx_user_oauth` table. It binds one
platform user to one external OAuth provider subject and stores provider
profile metadata plus token storage keys. It inherits audit fields from
`BaseEntity`.
Primary key `identityId` and foreign key `userId` are 32-character string
columns. Operators assign a prefixed ULID before insertion.

### TABLE_NAME
- **Signature:** `TABLE_NAME -> String`
- **Description:** MyBatis-Plus and JPA table name, `nx_user_oauth`.

### Indexes
- `idx_nx_user_oauth_user_id` index on `user_id`
- `uk_nx_user_oauth_provider_subject` unique index on `provider, provider_subject`
