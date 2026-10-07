# 包 `resources`

## FileResource

**类型：** record

带内容流与元数据标志的文件资源。 当 saveMeta 为 true 时，资源存储会同时持久化元数据与二进制内容。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 资源显示名称 |
| `fileName` | `String` | 文件名 |
| `contentType` | `String` | MIME 类型 |
| `inputStream` | `InputStream` | 内容输入流 |
| `saveMeta` | `boolean` | 是否同时保存元数据 |


## MetaResource

**类型：** record

已存储资源的不变元数据记录。将资源关联到模块上下文（module + moduleKey）， 并记录存储详情（URI、存储模式、创建时间）。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `resourceId` | `String` | 资源 ID |
| `resourceName` | `String` | 资源名称 |
| `mimeType` | `String` | MIME 类型 |
| `module` | `String` | 模块名称 |
| `moduleKey` | `String` | 模块键 |
| `fileSize` | `long` | 文件大小（字节） |
| `fileUri` | `String` | 文件 URI |
| `storeMode` | `String` | 存储模式 |
| `createdAt` | `Instant` | 创建时间 |


## ResourceEvent

**类型：** record

资源元数据保存/持久化时发布的领域事件。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `eventType(` | `MetaResource metaResource) implements DomainEvent {

    /**
     * 返回事件类型标识。
     *
     * @return 事件类型字符串
     */
    @Override
    public String` | — |

### 方法

#### `eventType() → String`
- **说明：** 返回事件类型标识。
- **返回：** 事件类型字符串


## ResourcePatternResolver

**类型：** class

将资源位置模式（如 classpath*:mapper/**\/*.xml）解析为 Resource 列表。 支持以下位置前缀： classpath*: — 扫描所有 classpath 根目录以匹配资源 classpath: — 仅扫描第一个匹配的 classpath 根目录 （无前缀）— 同 classpath*: 模式匹配使用 Hutool 的 AntPathMatcher，语法与 Spring AntPathMatcher 相同 （**、*、?）。

### 方法

#### `getResources(String locationPattern) → List<Resource>`
- **说明：** 使用默认类加载器创建解析器。 / public ResourcePatternResolver() { this(ClassUtil.getClassLoader()); } /** 使用指定类加载器创建解析器。
- **参数：**
  - `classLoader` — 用于 classpath 扫描的类加载器
  - `locationPattern` — 资源位置模式（如 classpath*:mapper/**\/*.xml）
- **返回：** 匹配的资源列表（永不为 null）

#### `getMatchedResources(String locationPattern) → List<MatchedResource>`
- **说明：** 返回匹配给定位置模式的 MatchedResource 列表。 与 {@link #getResources(String)} 不同，每个结果保留原始匹配的 classpath 路径。
- **参数：**
  - `locationPattern` — 资源位置模式
- **返回：** 带路径信息的匹配资源列表（永不为 null）

#### `getResource() → Resource`
- **说明：** 返回底层委托资源。
- **返回：** 委托资源


## ResourceStore

**类型：** interface

二进制资源持久化与读取的抽象接口。 实现可存储于本地（文件系统）、远程（S3、OSS）或数据库。 每个存储实现拥有唯一的 {@link #storeMode()} 标识符。
