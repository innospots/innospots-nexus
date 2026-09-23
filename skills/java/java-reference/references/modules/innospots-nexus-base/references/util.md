# 包 `util`

## AsymmetricKeyPair

**类型：** record

RSA 不对称密钥对，公钥与私钥均为 Base64 编码字符串。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `publicKey` | `String` | Base64 编码的公钥 |
| `privateKey` | `String` | Base64 编码的私钥 |

## MetricsSnapshot

**类型：** record

指标计数器/计时器的时点快照，记录指标名称、标签、总次数及累计耗时（纳秒）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | — |
| `tags` | `Map<String, String>` | — |
| `count` | `long` | — |
| `totalNanos` | `long` | — |

### 方法

#### `totalMillis() → double`

- **说明：** 返回累计耗时的毫秒表示。
- **返回：** 总耗时（毫秒）

## Type

**类型：** enum

随机 ID 字符集类型。
