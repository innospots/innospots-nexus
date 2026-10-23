# 包 `installation.domain.enums`

## PluginPresence

**类型：** enum

插件定义在当前有效 classpath 目录中的存在性。

| 常量 | 说明 |
|------|------|
| `PRESENT` | 当前发现结果中存在该插件 |
| `MISSING` | 安装记录存在，但当前发现结果中不存在 |


## PluginSourceType

**类型：** enum

插件定义来源类型。

| 常量 | 说明 |
|------|------|
| `JAVA` | Java SPI 或 classpath 实现类 |
| `YAML` | classpath `META-INF/nexus/plugin.yaml` 资源声明 |

### 方法

#### `from(String value) → PluginSourceType`

忽略大小写转换为枚举；输入为 `null` 时返回 `null`，未知文本抛出
`IllegalArgumentException`。
