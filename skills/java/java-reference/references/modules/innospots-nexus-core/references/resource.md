# Resource storage（`core.resource`）

将 `base.resources.ResourceStore`（二进制 SPI）绑定到 `nx_meta_resource` 中的持久化元数据。

## 分层

```text
base.ResourceStore          binary read/write SPI (implement in adapter/host)
        ↑
ResourceStorageRegistry     register stores by storeMode()
        ↑
MetaResourceService         save/read/delete orchestration
        ↑
MetaResourceDao             MyBatis-Plus mapper
        ↑
MetaResourceEntity          workspace-scoped metadata row
```

## ResourceStorageRegistry

**类型：** class

| 方法 | 说明 |
|--------|-------------|
| `register(store)` | 添加后端；首个注册的成为默认 |
| `defaultStoreMode(mode)` | 设置显式默认 |
| `requireStore(mode)` | 解析存储或抛出异常 |
| `findStore(mode)` | 可选查找 |

## MetaResourceService

**类型：** class

| 方法 | 说明 |
|--------|-------------|
| `save(FileResource, module, moduleKey, storeMode)` | 存储字节 + 可选元数据行 |
| `read(resourceId)` | 当元数据存在时加载字节 |
| `delete(resourceId)` | 移除元数据和二进制 |

遵循 `FileResource.saveMeta()` — 为 `false` 时跳过数据库插入。

## MetaResourceEntity

**类型：** class，继承 `WorkspaceBaseEntity`

表：`nx_meta_resource`。主键：`resourceId`（`idPrefix()` → `"res"`）。

按 `module_key` 和 `uri_key` 建立索引。

## MetaResourceDao

**类型：** interface，继承 `BaseMapper<MetaResourceEntity>`

无自定义方法；使用继承的 CRUD，或仅添加单表 `default` 方法。
