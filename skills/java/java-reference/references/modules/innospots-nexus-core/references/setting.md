# 系统设置（`core.setting`）

实例级键值系统设置（表 `nx_system_setting`）：按设置域（`settingDomain`）、作用范围（`SettingScope`）
与 `SettingKey` 持久化键值配置。core 只提供运行时读取/更新服务与持久化原语；
管理端 REST 由上层模块（platform、console 等）暴露。

## 分层

```text
SettingKey<T>               编译期类型化声明（域、键、范围、值类型、解析器）
        ↓
SystemSettingService        运行时读写：长度/格式/类型门禁、缺行引导插入（bootstrap）
        ↓
SystemSettingOperator       nx_system_setting 表读写（无格式校验）
        ↓
SystemSettingDao            MyBatis-Plus Mapper（无自定义 SQL）
        ↓
SystemSettingEntity         持久化实体（BaseEntity + 唯一键）
```

## 唯一性与值类型规则

- 唯一键：`setting_domain + setting_scope + scope_id + setting_key`。
- `value_type` 插入后不变，仅 `setting_value` 可更新；不同模块不得以不同
  `SettingValueType` 注册同一键（`SETTING_VALUE_TYPE_MISMATCH`）。
- `SettingScope.GLOBAL` 时 `scope_id` 使用空串 `GLOBAL_SCOPE_ID`（规避 MySQL 唯一索引
  对 NULL 的处理差异）。
- `setting_value` 长度上限 `SETTING_VALUE_MAX_LENGTH`（2048）。

事务边界由调用方（如 platform service）声明。

详细 API 见 [`setting-support.md`](setting-support.md)、[`setting-service.md`](setting-service.md)、
[`setting-operator.md`](setting-operator.md)、[`setting-domain-entity.md`](setting-domain-entity.md)、
[`setting-domain-enums.md`](setting-domain-enums.md)、[`setting-status.md`](setting-status.md)、
[`setting-dao.md`](setting-dao.md)。