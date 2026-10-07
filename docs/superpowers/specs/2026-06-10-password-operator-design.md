# Password Operator 设计

## 概述

在 portal user 模块新增 `PasswordOperator`，支持密码修改（需旧密码）与
密码重置（通过验证码）。声明 `PasswordVerificationOperator` SPI；默认占位实现
抛出 `UnsupportedOperationException`。

## 根因

用户需要在知道当前密码时修改密码，在无法登录时通过 email / mobile 验证码
重置密码。当前 `UserOperator` 仅提供注册与生命周期管理，不含密码修改。

## 目录影响

```
innospots-nexus-portal/src/main/java/com/innospots/nexus/portal/user/operator/
├── PasswordOperator.java          (new)
├── PasswordVerificationOperator.java  (new)
├── PasswordValidator.java         (new)
└── VerificationType.java          (new)
```

## 设计

### PasswordOperator

`com.innospots.nexus.portal.user.operator.PasswordOperator` 是拥有密码更新逻辑的
具体 service 类。它不继承或实现任何其他 interface——上层直接依赖它。

**依赖**

| 字段 | 类型 | 来源 |
|-------|------|--------|
| `userDao` | `UserDao` | 现有 |
| `passwordCredentialDao` | `UserPasswordCredentialDao` | 现有 |
| `passwordDecryptor` | `UserPasswordDecryptor` | 现有 |
| `verificationOperator` | `PasswordVerificationOperator` | 新增 |
| `validator` | `PasswordValidator` | 新增 |

**方法**

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

### 密码修改流程

1. 按 `userId` 查找 `UserEntity`。
2. 按 `userId` 查找 `UserPasswordCredentialEntity`。
3. 对照存储 hash 验证 `oldPassword`（算法与
   `UserPasswordCredentialDao.selectByUserId` 相同）。
4. 若 `oldPassword` 等于 `newPassword` 则拒绝。
5. 通过 `PasswordValidator` 校验 `newPassword` 强度。
6. Hash 并保存新密码（递增 `passwordVersion`，成功时重置
   `failedAttempts`，清除 `lockedUntil`）。

### 密码重置流程

1. 按 `identity`（userName / email / mobile）解析 `UserEntity`。
2. 通过 `PasswordVerificationOperator` 验证验证码。
3. 若验证码无效/过期，抛出 `VerificationCodeException`。
4. 校验新密码强度。
5. Hash 并保存新密码（设置 `forceReset = true`）。

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

默认实现（同包 `NullPasswordVerificationOperator`）对所有方法抛出
`UnsupportedOperationException`。未来实现位于 portal 模块之外。

### VerificationType enum

```java
public enum VerificationType {
    EMAIL,
    MOBILE
}
```

表示验证码传输通道。后续接入真实 verification service 时，`identity` 分别
映射到 email 或 mobile。

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

强度要求：长度 >= 8，必须包含大写 + 小写 + 数字。

### 异常类型

| 异常 | 时机 |
|-----------|------|
| `PasswordMismatchException` | 旧密码与存储 hash 不匹配 |
| `PasswordTooYoungException` | 新密码等于旧密码 |
| `PasswordStrengthException` | 新密码未通过 `PasswordValidator` |
| `VerificationCodeException` | 验证码无效 / 过期 |
| `PasswordResetNotSupportedError` | 调用 `NullPasswordVerificationOperator` |

### 事务策略

`changePassword` 与 `resetPassword` 均为 `@Transactional`。它们仅触及
`nx_user`（查询）与 `nx_user_password`（credential 更新），
单一 Peloton 隔离级别即可。

### 非功能决策

- portal 层不做 rate-limiting 或 brute-force 防护 — 由 authentication gateway 处理。
- 不做 password history check（禁止复用最近 N 个密码）— 延后到未来增强。
- `PasswordValidator` 常量（`MIN_LENGTH`、字符类要求）
  为 public `static final` 字段，供上层测试读取。
