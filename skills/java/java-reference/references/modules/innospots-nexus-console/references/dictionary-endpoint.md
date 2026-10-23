# 包 `dictionary.endpoint`

## DictionaryTypeEndpoint

**类型：** interface，`@EndpointModule("dictionary")`

字典类型管理 JAX-RS 契约。类级路径由业务域实现绑定；实现类重复方法级 JAX-RS / OpenAPI
注解供构建期扫描，但不重复 grant。字典项随类型详情、创建和更新请求体维护，不提供独立字典项资源。

### 端点

| 方法 | 相对路径 | 返回 |
|------|----------|------|
| GET `pageDictionaryTypes` | `/` | `R<PageResult<DictionaryTypeVo>>` |
| GET `getDictionaryType` | `/{dictionaryTypeId}` | `R<DictionaryTypeVo>` |
| POST `createDictionaryType` | `/` | `R<DictionaryTypeVo>` |
| PUT `updateDictionaryType` | `/{dictionaryTypeId}` | `R<DictionaryTypeVo>` |
| PUT `updateDictionaryTypeStatus` | `/{dictionaryTypeId}/status` | `R<Void>` |
| DELETE `deleteDictionaryType` | `/{dictionaryTypeId}` | `R<Void>` |
| GET `listDictionaryTypeOptions` | `/options` | `R<List<DictionaryTypeOptionVo>>` |
