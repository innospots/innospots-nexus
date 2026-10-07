# 包 `resource.storage`

## ResourceStorageRegistry

**类型：** class

按 ResourceStore 索引的 ResourceStore 实现注册表。

### 方法

#### `register(ResourceStore store) → ResourceStorageRegistry`
- **说明：** 注册存储后端。
- **参数：**
  - `store` — 存储实现
- **返回：** 当前注册表，支持链式调用

#### `defaultStoreMode(String storeMode) → ResourceStorageRegistry`
- **说明：** 设置未显式指定时使用的默认存储模式。
- **参数：**
  - `storeMode` — 已注册的存储模式
- **返回：** 当前注册表，支持链式调用

#### `requireStore(String storeMode) → ResourceStore`
- **说明：** 按模式解析存储后端；模式为空时回退到默认值。
- **参数：**
  - `storeMode` — 可选存储模式
- **返回：** 匹配的存储后端

#### `findStore(String storeMode) → Optional<ResourceStore>`
- **说明：** 按模式查找已注册的存储后端。
- **参数：**
  - `storeMode` — 存储模式
- **返回：** 已注册时的存储后端
