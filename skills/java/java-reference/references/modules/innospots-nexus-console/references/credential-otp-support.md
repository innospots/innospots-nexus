# 包 `credential.otp.support`

## RegistrationOtpDebugLogger

**类型：** class（final）

联调期注册 OTP 日志输出。仅在 `RegistrationConfig.OtpDelivery.debugLogEnabled` 开启时记录
challengeId、投递地址与明文验证码；生产环境禁止长期启用。
