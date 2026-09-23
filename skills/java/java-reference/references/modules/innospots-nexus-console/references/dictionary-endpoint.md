# 包 `dictionary.endpoint`

## DictionaryItemEndpoint

**类型：** class

嵌套在类型编码下的字典项管理控制台端点。 方法工作流延后至字典服务与 Operator 边界在此实现。

## DictionaryTypeEndpoint

**类型：** class

字典类型目录的管理控制台端点。 方法工作流延后至字典服务与 Operator 边界在此实现。

### 方法

#### `pageDictionaryTypes(@BeanParam DictionaryTypePageRequest request) → R<PageResult<DictionaryTypeVo>>`

- **说明：** 使用管理过滤器分页查询字典类型。
- **参数：**
  - `request` — 字典类型分页查询
- **返回：** 匹配的 字典类型分页结果

#### `createDictionaryType(DictionaryTypeCreateRequest request) → R<DictionaryTypeVo>`

- **说明：** 创建字典类型。
- **参数：**
  - `request` — 字典类型创建数据
- **返回：** created 字典类型

#### `listDictionaryTypeOptions() → R<List<DictionaryTypeOptionVo>>`

- **说明：** 列出用于选择器的字典类型精简选项。
- **返回：** type 选项列表
