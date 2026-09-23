# 包 `mapstruct`

## BaseBeanConverter

**类型：** interface

领域模型与持久化实体之间的 MapStruct 风格转换器基接口。 提供列表转换、JSON 字符串与 Map/List 互转以及日期时间格式化的默认方法。

## BaseMapperConfig

**类型：** interface

所有领域 Mapper 共享的 MapStruct 配置。 配置仅访问器集合映射、始终启用空值检查，并静默忽略未映射的目标字段。
