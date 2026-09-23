# 包 `resource.service`

## MetaResourceService

**类型：** class

协调二进制存储后端与持久化文件元数据。

### 方法

#### `save(FileResource resource, String module, String moduleKey, String storeMode) → MetaResource`

- **说明：** 存储文件，并在资源要求时持久化元数据。
- **参数：**
  - `resource` — 文件载荷
  - `module` — 所属模块名称
  - `moduleKey` — 所属模块键
  - `storeMode` — 可选存储后端模式
- **返回：** 存储后的元数据

#### `read(String resourceId) → Optional<byte[]>`

- **说明：** 按资源标识读取二进制内容。
- **参数：**
  - `resourceId` — 资源标识
- **返回：** 找到时的二进制载荷

#### `delete(String resourceId) → boolean`

- **说明：** 删除已存储资源及其元数据行。
- **参数：**
  - `resourceId` — 资源标识
- **返回：** 记录存在且删除成功时返回 {@code true}

#### `findById(String resourceId) → Optional<MetaResource>`

- **说明：** 按资源标识查找持久化元数据。
- **参数：**
  - `resourceId` — 资源标识
- **返回：** 找到时的元数据
