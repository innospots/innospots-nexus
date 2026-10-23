# 包 `dictionary.support`

## DictionaryTypeNames

**类型：** class（final）

字典类型多语言名称的规范化、持久化与展示。`type_name_i18n` 保存完整 JSON，
`type_name` 保存截断后的简体中文摘要供检索与排序。

### 常量

| 常量 | 说明 |
|------|------|
| `LOCALE_ZH_CN` | 主名称语言标签 `zh-CN` |

### 方法

| 方法 | 说明 |
|------|------|
| `apply(DictionaryTypeEntity, I18nObject)` | 校验并写入多语言名称与中文摘要 |
| `read(DictionaryTypeEntity) → I18nObject` | 读取 JSON，缺失时回退历史单语言字段 |
| `merge(I18nObject, I18nObject) → I18nObject` | 将补丁合并到已有多语言名称 |
| `displayLabel(DictionaryTypeEntity) → String` | 返回列表/选项使用的主标签 |
