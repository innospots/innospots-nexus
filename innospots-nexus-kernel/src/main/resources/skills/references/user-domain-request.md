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

**Type:** class

Request object for paginated user queries. It extends `BasePageRequest` for
`input`, `pageNo`, and `pageSize`.

### Fields
- **Signature:** `input, pageNo, pageSize, userName, realName, email, mobile`
- **Description:** Common pagination fields plus user-specific fuzzy filters.
