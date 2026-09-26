# 包 `credential.password.algorithm`

## BcryptCredentialAlgorithm

**类型：** class

默认密码算法：BCrypt 单向哈希，标识 CredentialAlgorithms。


## CredentialAlgorithm

**类型：** interface

可插拔凭据编码与验证；持久化层不解析 verifier 结构，由 CredentialAlgorithmRegistry 路由。


## CredentialAlgorithmRegistry

**类型：** class

按算法 ID 解析 CredentialAlgorithm；默认注册 bcrypt 密码哈希。

### 方法

#### `defaults() → CredentialAlgorithmRegistry`
- **说明：** 注册自定义算法表（单测或多算法环境）。
- **参数：**
  - `algorithms` — 算法 ID 到实现的映射
- **返回：** 仅含 CredentialAlgorithms 的注册表

#### `require(String algorithmId) → CredentialAlgorithm`
- **说明：** 按持久化算法 ID 解析实现，未知 ID 时抛出业务异常。 调用场景：加载历史凭据行后校验或重编码。
- **参数：**
  - `algorithmId` — 库中 algorithm_id
- **返回：** 对应算法实现

#### `defaultAlgorithm() → CredentialAlgorithm`
- **说明：** 当前环境的默认密码哈希算法（BCrypt v1）。 调用场景：新用户注册写入默认 CredentialAlgorithms。
- **返回：** 默认 CredentialAlgorithm


## CredentialAlgorithms

**类型：** class

内置凭据算法标识符。


## EncodedCredential

**类型：** record

算法产出的 opaque 验证材料，供 com.innospots.nexus.console.credential.password.operator.UserCredentialOperator 写入库表。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `algorithm` | `String` | 算法标识，对应 algorithm_id |
| `verifier` | `String` | 不透明验证载荷 |
| `verifierParams` | `String` | 算法私有参数（可为 null；console 不解析语义） |
