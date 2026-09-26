# 包 `contribution.console.ui.spec.action`

## ActionConfig

**类型：** class

在运行时动作注册表中注册的一次动作调用。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private String` | `可选结果名称，供后续表达式引用。` | 可选结果名称，供后续表达式引用。 private String id; |
| `private String` | `动作注册表名称，例如 {@code reload}、{@code setState} 或 {@code call}。` | 动作注册表名称，例如 reload、setState 或 call。 private String action; |
| `private PermissionConfig` | `可选动作级权限。` | 可选动作级权限。 private PermissionConfig permission; |


## ActionOrList

**类型：** record

单个动作或有序动作序列。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `actions` | `List<ActionConfig>` | — |

### 方法

#### `isEmpty() → boolean`
- **说明：** 创建动作列表包装器。
- **参数：**
  - `actions` — 有序动作列表
- **返回：** 存在至少一个动作时返回 true

#### `toJsonValue() → List<ActionConfig>`
- **说明：** 序列化为单个动作对象或动作数组，与 YAML 联合类型一致。
- **返回：** 动作步骤列表
