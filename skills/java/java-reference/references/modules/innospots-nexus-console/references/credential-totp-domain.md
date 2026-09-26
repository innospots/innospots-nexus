# 包 `credential.totp.domain`

## TotpEnrollmentMaterial

**类型：** record

TOTP 注册阶段返回给客户端的材料；base32Secret 与 otpauthUri 仅应在受信通道展示一次。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `base32Secret` | `String` | 共享密钥明文（库中仅存加密形式） |
| `otpauthUri` | `String` | 供扫码的 provisioning URI |
