# 包 `credential.password.domain.entity`

## UserCredentialEntity

**类型：** class

按控制台归属存储的用户鉴证凭据行（表 {@value #TABLE_NAME}）。 同一主体在同一安全域下每种 com.innospots.nexus.console.credential.password.CredentialKind 至多一行（见唯一索引 uk_nx_user_credential_subject）。 verifier / verifierParams 为 opaque，由 algorithm 与种类解释，不得存明文密码、OTP 或 TOTP 共享密钥。 读写编排见 com.innospots.nexus.console.credential.password.operator.UserCredentialOperator； 登录快照见 com.innospots.nexus.console.auth.domain.model.CredentialRecord。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private LocalDateTime` | `* 账户锁定截止时间；{@code null} 表示未锁定。与 {@link #failedAttempts} 配合实现登录防暴力。` | 账户锁定截止时间；null 表示未锁定。与 {@link #failedAttempts} 配合实现登录防暴力。 / private LocalDateTime lockedUntil; |
| `private LocalDateTime` | `* 凭据过期时间；{@code null} 表示无过期策略（密码/TOTP 通常为空，预留临时凭据场景）。` | 凭据过期时间；null 表示无过期策略（密码/TOTP 通常为空，预留临时凭据场景）。 / private LocalDateTime expiredAt; |
