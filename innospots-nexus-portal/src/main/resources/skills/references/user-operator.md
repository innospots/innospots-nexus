# Package `user.operator`

## UserOperator

**Type:** class

User data operator backed by DAO objects. It keeps persistence coordination
small: password verification, registration policy, and cross-module workflows
belong in service classes.

### findById
- **Signature:** `findById(String userId) -> Optional<UserProfileVO>`
- **Description:** Finds a user profile by identifier.
- **Parameters:** `userId` user identifier
- **Returns:** user profile when found

### pageUsers
- **Signature:** `pageUsers(UserPageRequest request) -> DataPage<UserProfileVO>`
- **Description:** Pages user profiles with optional fuzzy filters.
- **Parameters:** `request` page request with input, userName, realName, email, and mobile filters
- **Returns:** page of user profiles

### registerWithPassword
- **Signature:** `registerWithPassword(UserPasswordRegisterRequest request) -> UserProfileVO`
- **Description:** Decrypts the frontend encrypted password, hashes it with base cryptographic utilities, and inserts a separate password credential record.
- **Parameters:** `request` registration request with frontend encrypted password
- **Returns:** created user profile

### deleteUser
- **Signature:** `deleteUser(String userId) -> boolean`
- **Description:** Deletes a user profile by identifier through `UserDao`.
- **Parameters:** `userId` user identifier
- **Returns:** true when a row was deleted

### freezeUser
- **Signature:** `freezeUser(String userId) -> boolean`
- **Description:** Freezes a user by updating status to `DISABLED`.
- **Parameters:** `userId` user identifier
- **Returns:** true when a row was updated

### unfreezeUser
- **Signature:** `unfreezeUser(String userId) -> boolean`
- **Description:** Unfreezes a user by updating status to `ACTIVE`.
- **Parameters:** `userId` user identifier
- **Returns:** true when a row was updated

## UserOauthOperator

**Type:** class

OAuth user registration operator backed by user and OAuth identity DAO objects.

### registerWithOauth
- **Signature:** `registerWithOauth(UserOauthRegisterRequest request) -> UserProfileVO`
- **Description:** Inserts a user profile and a separate OAuth identity binding record.
- **Parameters:** `request` OAuth registration request
- **Returns:** created user profile
