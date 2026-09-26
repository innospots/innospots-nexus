# 包 `contribution.console.ui.spec.permission`

## PermissionConfig

**类型：** class

权限声明，支持单个代码、任一匹配代码数组或详细对象形式。

### 方法

#### `code(String code) → PermissionConfig`
- **说明：** 创建单个权限代码声明。
- **参数：**
  - `code` — 权限代码
- **返回：** 权限配置

#### `anyOf(List<String> codes) → PermissionConfig`
- **说明：** 创建任一匹配权限声明。
- **参数：**
  - `codes` — 权限代码列表
- **返回：** 权限配置

#### `detailed(String code, PermissionDenied denied) → PermissionConfig`
- **说明：** 创建详细权限声明。
- **参数：**
  - `code` — 权限代码
  - `denied` — 拒绝时的行为
- **返回：** 权限配置


## PermissionConfigDeserializer

**类型：** class

从字符串、数组或对象形式反序列化 PermissionConfig。


## PermissionDenied

**类型：** enum

未授予访问权限时的拒绝行为。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `HIDDEN` | — |
| `DISABLED` | — |
| `READONLY` | — |
