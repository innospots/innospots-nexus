# 包 `util`

## BeanUtils

**Type:** class

Bean 属性拷贝与转换工具，封装 Hutool 的 `BeanUtil`。

## Checks

**Type:** class

前置条件检查，失败时抛出 `NexusException` 及 `NexusStatusCode#INVALID_PARAMETER`。

## CryptoUtils

**Type:** class

加密工具：密码哈希（BCrypt）、对称加密（AES-GCM）及非对称加密（RSA/OAEP）。

## DateTimeUtils

**Type:** class

日期与时间格式化及解析工具。

## EnvUtils

**Type:** class

支持覆盖的环境属性解析器。

## IdGenerator

**Type:** class

ID 生成工具：基于 Snowflake 的分布式 ID、**`ulid(prefix)` /
`monotonicUlid(prefix)`**（主要持久化键生成）、可配置字符集的随机 ID、
带时间戳前缀的 ID 及批量生成。

## MetricsSnapshot

**Type:** record

指标计数器/计时器的时点快照。

## MetricsUtils

**Type:** class

基于 Micrometer 的指标门面。

## StringUtils

**Type:** class

字符串工具：空白检查、占位符替换（`${key`} 与 `{{key`}}）、camelCase/下划线转换及随机键生成。
