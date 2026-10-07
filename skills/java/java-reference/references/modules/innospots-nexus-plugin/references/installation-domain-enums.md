# 包 `installation.domain.enums`

## PluginPresence

**类型：** enum

插件定义在当前有效目录中的存在性。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `PRESENT` | — |
| `MISSING` | — |


## PluginSourceType

**类型：** enum

插件定义来源类型。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `JAVA` | — |

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| ` ` | `Java SPI 或 classpath 实现类。 */
    JAVA,

    /** YAML 资源声明。` | Java SPI 或 classpath 实现类。 JAVA, /** YAML 资源声明。 YAML; |

### 方法

#### `from(String value) → PluginSourceType`
- **说明：** Java SPI 或 classpath 实现类。 JAVA, /** YAML 资源声明。 YAML; /** 将来源文本转换为枚举。
- **参数：**
  - `value` — 持久化或声明中的来源文本
- **返回：** 匹配的枚举值；输入为 null 时返回 null
