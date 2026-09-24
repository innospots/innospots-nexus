# Package `user.domain.request`

## UserPasswordRegisterRequest

**Type:** record

Request object for registering a user with local password credentials. Password
transport decryption and hashing are performed after the request reaches
`UserOperator`.

### Components
- **Signature:** `userName, displayName, realName, email, mobile, encryptedPassword`
- **Description:** User registration profile fields plus the frontend encrypted password payload.

## UserOauthRegisterRequest

**Type:** record

Request object for registering a user with an external OAuth identity.

### Components
- **Signature:** `userName, displayName, realName, email, mobile, provider, providerSubject, providerAccount, providerDisplayName, providerEmail, providerAvatarUrl, accessTokenKey, refreshTokenKey, tokenExpiresAt`
- **Description:** User registration profile fields plus OAuth provider identity and token storage metadata.

## UserPageRequest

**Type:** record

Paginated user query request with inlined pagination fields and user-specific
fuzzy filters.

### Components
- **Signature:** `input() -> String`
- **Description:** Common fuzzy search keyword.

- **Signature:** `pageNo() -> long`
- **Description:** 1-indexed page number, invalid values normalized to `1`.

- **Signature:** `pageSize() -> long`
- **Description:** Records per page, invalid values normalized to `20`.

- **Signature:** `userName() -> String`
- **Description:** User name filter.

- **Signature:** `realName() -> String`
- **Description:** Real name filter.

- **Signature:** `email() -> String`
- **Description:** Email filter.

- **Signature:** `mobile() -> String`
- **Description:** Mobile number filter.

### Constructors
- **Signature:** `UserPageRequest()`
- **Description:** No-arg constructor with all defaults (`null` strings, `1L`, `20L`).

- **Signature:** `UserPageRequest(String input, long pageNo, long pageSize, String userName, String realName, String email, String mobile)`
- **Description:** Canonical constructor.
