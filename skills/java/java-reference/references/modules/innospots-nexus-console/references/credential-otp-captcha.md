# 包 `credential.otp.captcha`

## CaptchaPolicy

**类型：** record

图形验证码绘制参数（与 com.innospots.nexus.console.credential.otp.policy.OtpPolicy 的 TTL/重试策略独立）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `width` | `int` | 图片宽度（像素） |
| `height` | `int` | 图片高度（像素） |
| `codeCount` | `int` | 验证码字符个数 |
| `interferenceCount` | `int` | 干扰线/圆数量（GIF 样式忽略） |
| `style` | `CaptchaStyle` | 绘制样式，默认 CaptchaStyle |


## CaptchaStyle

**类型：** enum

Hutool 图形验证码样式，对应 cn.hutool.captcha 中的实现类。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `LINE` | — |
| `CIRCLE` | — |
| `SHEAR` | — |
| `GIF` | — |


## HutoolCaptchaFactory

**类型：** class

基于 hutool-all（cn.hutool.captcha）的图形验证码生成。

### 方法

#### `GeneratedCaptcha(String code, String imageBase64) → record`
- **说明：** /
- **参数：**
  - `code` — 明文验证码（入库前须规范化）
  - `imageBase64` — PNG 或 GIF 的 Base64 载荷（不含 data: 前缀）

#### `generate(CaptchaPolicy policy) → GeneratedCaptcha`
- **说明：** 按策略生成验证码图片与明文。
- **参数：**
  - `policy` — 绘制参数
- **返回：** 明文与 Base64 图片
