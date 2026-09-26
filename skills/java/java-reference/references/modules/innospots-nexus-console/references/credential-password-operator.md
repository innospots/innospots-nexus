# 包 `credential.password.operator`

## UserCredentialOperator

**类型：** class

nx_user_credential 的读写与证明校验；同一表按 CredentialKind 存密码、TOTP 等行。 查询均带 ConsoleOwnershipScope 隔离。

### 方法

#### `findPassword(SecurityRealm realm, String subjectId) → Optional<CredentialRecord>`
- **说明：** 注入 DAO 与算法注册表。
- **参数：**
  - `credentialDao` — nx_user_credential Mapper
  - `algorithmRegistry` — 密码等可插拔验证算法注册表
  - `realm` — 平台或租户安全域
  - `subjectId` — 用户主体 ID（与 nx_user_credential.subject_id 一致）
- **返回：** 存在且归属匹配时返回 opaque 记录

#### `findCredential(SecurityRealm realm, CredentialKind kind, String subjectId) → Optional<CredentialRecord>`
- **说明：** 按安全域与凭据类型加载 opaque CredentialRecord。 调用场景：TOTP 注册/MFA（CredentialKind）、扩展凭据类型查询。
- **参数：**
  - `realm` — 安全域
  - `kind` — 凭据类型
  - `subjectId` — 用户主体 ID
- **返回：** 匹配行；无行或 subjectId 空白时为空

#### `find(ConsoleOwnership ownership,
            CredentialKind kind,
            String subjectId) → Optional<UserCredentialEntity>`
- **说明：** 在显式归属下查询实体行（供需要写库字段的内部流程使用）。 调用场景：{@link #saveVerifier}、租户级扩展归属 CredentialOwnershipResolver。
- **参数：**
  - `ownership` — 归属三元组
  - `kind` — 凭据类型
  - `subjectId` — 用户主体 ID
- **返回：** 实体；校验失败或不存在时为空

#### `enrollPassword(ConsoleOwnership ownership, String subjectId, String rawPassword) → void`
- **说明：** 注册或轮换密码哈希（BCrypt 等），明文仅在内存中参与编码。 调用场景：com.innospots.nexus.console.credential.password.service.CredentialService 注册/改密/重置成功后写入。
- **参数：**
  - `ownership` — 凭据行归属
  - `subjectId` — 用户主体 ID
  - `rawPassword` — 明文密码（不得落库）

#### `saveVerifier(ConsoleOwnership ownership,
            String subjectId,
            CredentialKind kind,
            String algorithm,
            String verifier,
            String verifierParams) → void`
- **说明：** 写入或轮换已编码的验证材料（禁止传入明文密码）。 调用场景：TOTP 共享密钥 AES 密文落库（CredentialKind）、自定义算法扩展。
- **参数：**
  - `ownership` — 凭据行归属
  - `subjectId` — 用户主体 ID
  - `kind` — 凭据类型
  - `algorithm` — 算法 ID
  - `verifier` — opaque 验证材料
  - `verifierParams` — 扩展参数（如 TOTP 注册待确认标记）

#### `saveState(SecurityRealm realm, CredentialRecord record) → void`
- **说明：** 更新密码行的锁定、失败次数与可选 verifier 轮换结果。 调用场景：com.innospots.nexus.console.auth.service.AuthFacade 登录失败累加次数； com.innospots.nexus.console.credential.password.service.CredentialService 清除锁定。
- **参数：**
  - `realm` — 安全域
  - `record` — 含 subjectId 与待持久化状态字段的记录

#### `clearVerifierParams(SecurityRealm realm, CredentialKind kind, String subjectId) → void`
- **说明：** 清空 verifierParams（例如 TOTP 注册确认后去掉待确认标记）。 调用场景：com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService。
- **参数：**
  - `realm` — 安全域
  - `kind` — 凭据类型
  - `subjectId` — 用户主体 ID

#### `deleteCredential(SecurityRealm realm, CredentialKind kind, String subjectId) → void`
- **说明：** 删除指定类型的凭据行。 调用场景：用户解绑 TOTP（com.innospots.nexus.console.credential.totp.service.TotpEnrollmentService）。
- **参数：**
  - `realm` — 安全域
  - `kind` — 凭据类型
  - `subjectId` — 用户主体 ID

#### `verify(CredentialRecord record, String rawProof) → boolean`
- **说明：** 用记录上的算法校验明文证明（密码或算法支持的证明类型）。 调用场景：com.innospots.nexus.console.credential.password.service.CredentialService、 已登录用户改密时校验旧密码。
- **参数：**
  - `record` — 已加载的凭据记录（含 algorithm 与 verifier）
  - `rawProof` — 用户提交的明文证明
- **返回：** 校验通过时为 true
