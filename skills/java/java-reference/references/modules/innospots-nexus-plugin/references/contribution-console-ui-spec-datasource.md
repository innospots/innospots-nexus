# 包 `contribution.console.ui.spec.datasource`

## ComputedDataSource

**类型：** class

协议预留的计算数据源；运行时语义由实现定义。

### 方法

#### `extensions() → Map<String, Object>`
- **说明：** 返回扩展属性的 Jackson 序列化视图。
- **返回：** 扩展属性映射

#### `extension(String key, Object value) → void`
- **说明：** 设置一个扩展属性。
- **参数：**
  - `key` — 扩展属性键
  - `value` — 扩展属性值


## DataSourceConfig

**类型：** interface

命名页面数据源配置。


## HttpDataSource

**类型：** class

HTTP 后端数据源；页面需直接调用具体 HTTP 端点时使用。


## OptionMappingDataSource

**类型：** class

具体数据源类型共享的选项映射与自动加载字段。


## ResourceDataSource

**类型：** class

协议预留的资源数据源；运行时语义由实现定义。

### 方法

#### `extensions() → Map<String, Object>`
- **说明：** 返回扩展属性的 Jackson 序列化视图。
- **返回：** 扩展属性映射

#### `extension(String key, Object value) → void`
- **说明：** 设置一个扩展属性。
- **参数：**
  - `key` — 扩展属性键
  - `value` — 扩展属性值


## ServiceDataSource

**类型：** class

服务注册表支持的数据源；业务页面首选，因 DSL 不直接绑定 HTTP 端点。


## StaticDataSource

**类型：** class

文档内静态数据源，用于枚举、固定配置与演示数据。
