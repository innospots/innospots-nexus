# 包 `mapstruct`

## BaseBeanConverter

**类型：** interface

领域模型与持久化实体之间的 MapStruct 风格转换器基接口。 提供列表转换、JSON 字符串与 Map/List 互转以及日期时间格式化的默认方法。


## BaseMapperConfig

**类型：** interface

所有领域 Mapper 共享的 MapStruct 配置。 配置仅访问器集合映射、始终启用空值检查，并静默忽略未映射的目标字段。


## BaseMapperSupport

**类型：** class

通过映射函数转换集合的工具类。

### 方法

#### `mapList(List<S> source, Function<S, T> mapper) → List<T>`
- **说明：** 将源列表中的每个元素通过映射函数转换为目标列表。
- **参数：**
  - `source` — 源列表
  - `mapper` — 映射函数
  - `<S>` — 源元素类型
  - `<T>` — 目标元素类型
- **返回：** 转换后的列表；源为空时返回空列表
