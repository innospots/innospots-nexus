# Password Operator Design

## Overview

Add `PasswordOperator` to the kernel user module for password change (with
old password) and password reset (via verification code). A
`PasswordVerificationOperator` SPI is declared; the default placeholder
throws `UnsupportedOperationException`.

## Root Cause

Users need to change their password when they know the current one, and reset
their password when they are locked out (via email / mobile verification code).
Current `UserOperator` only provides registration and lifecycle management, not
password modification.

## Directory Impact

```
innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/user/operator/
├── PasswordOperator.java          (new)
├── PasswordVerificationOperator.java  (new)
├── PasswordValidator.java         (new)
└── VerificationType.java          (new)
```

## designs

### PasswordOperator

`com.innospots.nexus.kernel.user.operator.PasswordOperator` is the concrete
service class that owns password update logic. It does not extend or
implement any other interface—upper layers depend on it directly.

**Dependencies**

| Field | Type | Source |
|-------|------|--------|
| `userDao` | `UserDao` | existing |
| `passwordCredentialDao` | `UserPasswordCredentialDao` | existing |
| `passwordDecryptor` | `UserPasswordDecryptor` | existing |
| `verificationOperator` | `PasswordVerificationOperator` | new |
| `validator` | `PasswordValidator` | new |

**Methods**

```java
public class PasswordOperator {

    /**
     * Change password: verify old password, apply new password.
     */
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * Reset password: verify code against identity, apply new password with
     * force reset flag.
     */
    @Transactional
    public void resetPassword(String identity, String verificationCode, String newPassword);
}
```

### Password change flow

1. Look up `UserEntity` by `userId`.
2. Look up `UserPasswordCredentialEntity` by `userId`.
3. Verify `oldPassword` against stored hash (same algorithm as
   `UserPasswordCredentialDao.selectByUserId`).
4. Reject if `oldPassword` equals `newPassword`.
5. Validate `newPassword` strength via `PasswordValidator`.
6. Hash and save the new password (increment `passwordVersion`, reset
   `failedAttempts`, clear `lockedUntil` on success).

### Password reset flow

1. Resolve `UserEntity` by `identity` (userName / email / mobile).
2. Verify the verification code via `PasswordVerificationOperator`.
3. If code is invalid/expired, throw `VerificationCodeException`.
4. Validate `newPassword` strength.
5. Hash and save the new password (set `forceReset = true`).

### PasswordVerificationOperator interface

```java
public interface PasswordVerificationOperator {

    /**
     * Send a verification code to {@code identity} via the given transport.
     */
    void sendVerificationCode(String identity, VerificationType type);

    /**
     * Verify a code for the given identity and type. Returns false or throws
     * {@link VerificationCodeException} when the code is invalid or expired.
     */
    boolean verifyVerificationCode(String identity, VerificationType type,
                                    String code);

    /**
     * Expire an already-used code.
     */
    void expireVerificationCode(String identity, VerificationType type);
}
```

Default implementation (`NullPasswordVerificationOperator` in the same
package) throws `UnsupportedOperationException` for every method. Future
implementations live outside the kernel module.

### VerificationType enum

```java
public enum VerificationType {
    EMAIL,
    MOBILE
}
```

Indicates the transport channel used for the verification code. When the
real verification service is added later, `identity` maps to email or
mobile respectively.

### PasswordValidator

```java
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    /**
     * Returns true when the password meets all strength requirements.
     */
    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (hasUpper && hasLower && hasDigit) return true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
```

Strength requirement: length >= 8, must contain uppercase + lowercase + digit.

### Exception types

| Exception | When |
|-----------|------|
| `PasswordMismatchException` | Old password does not match stored hash |
| `PasswordTooYoungException` | New password equals old password |
| `PasswordStrengthException` | New password fails `PasswordValidator` |
| `VerificationCodeException` | Verification code invalid / expired |
| `PasswordResetNotSupportedError` | `NullPasswordVerificationOperator` invoked |

### Transaction strategy

Both `changePassword` and `resetPassword` are `@Transactional`. They only
touch `nx_user` (for lookups) and `nx_user_password` (credential update),
so a single Peloton isolation level suffices.

### Non-functional decisions

- No rate-limiting or brute-force protection in the kernel layer — handled
  by the authentication gateway.
- No password history check (prevent reuse of last N passwords) — deferred
  to a future enhancement.
- `PasswordValidator` constants (`MIN_LENGTH`, character class requirements)
  are public `static final` fields so they can be read by upper-layer tests.
