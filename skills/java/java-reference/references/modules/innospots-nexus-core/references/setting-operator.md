# 包 `setting.operator`

## SystemSettingOperator

**类型：** class

`nx_system_setting` 表读写；不含格式校验与 `SettingValueType` 门禁（见
`SystemSettingService`）。

### 构造方法

#### `SystemSettingOperator(SystemSettingDao settingDao)`
- **参数：**
  - `settingDao` — MyBatis-Plus Mapper

### 方法

#### `find(String settingDomain, SettingScope scope, String scopeId, String settingKey) → SystemSettingEntity`
- **说明：** 按域、范围与键查询唯一设置行
- **参数：**
  - `settingDomain` — 设置域
  - `scope` — 作用范围
  - `scopeId` — 范围 ID；GLOBAL 时可 null
  - `settingKey` — 设置键
- **返回：** 存在时的实体，否则 `null`

#### `insert(String settingDomain, SettingScope scope, String scopeId, String settingKey, SettingValueType valueType, String settingValue) → SystemSettingEntity`
- **说明：** 插入新设置项（含值类型，后续更新不改类型）
- **参数：**
  - `settingDomain` — 设置域
  - `scope` — 作用范围
  - `scopeId` — 范围 ID
  - `settingKey` — 设置键
  - `valueType` — 值类型
  - `settingValue` — 已 trim 的值
- **返回：** 插入后的实体（含生成主键与审计字段）

#### `updateValue(SystemSettingEntity entity, String settingValue) → SystemSettingEntity`
- **说明：** 更新已有行的 `setting_value`
- **参数：**
  - `entity` — 已加载实体
  - `settingValue` — 新值
- **返回：** 同一实体引用

### 包内辅助

- `normalizeScopeId(String scopeId)`（static，包可见）— 将 null/空白 scopeId 规范为
  `SystemSettingEntity.GLOBAL_SCOPE_ID`。