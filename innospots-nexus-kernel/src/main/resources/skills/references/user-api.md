# Package `user.api`

## UserPasswordDecryptor

**Type:** interface

Contract for decrypting password ciphertext submitted by frontend registration
clients before server-side password hashing.

### Methods
- **Signature:** `decrypt(String encryptedPassword) -> String`
- **Description:** Returns the raw password string for server-side hashing.

## RsaUserPasswordDecryptor

**Type:** record

RSA implementation backed by `CryptoUtils.decryptRsa`.

### Components
- **Signature:** `privateKey`
- **Description:** Base64-encoded PKCS#8 private key used to decrypt frontend password payloads.
