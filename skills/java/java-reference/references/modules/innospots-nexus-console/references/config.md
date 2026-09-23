# 包 `config`

## AuthConfig

**类型：** class

控制台认证的令牌签发设置。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `DEFAULT_TOKEN_SECRET` | `String` | 测试与本地开发使用的默认 AES 密钥。 |
| `tokenSecret` | `String` | 紧凑令牌 AES 加密密钥。 |
| `accessTokenTtlSeconds` | `long` | 访问令牌有效期（秒）。 |
| `refreshTokenTtlSeconds` | `long` | 刷新令牌有效期（秒）。 |
