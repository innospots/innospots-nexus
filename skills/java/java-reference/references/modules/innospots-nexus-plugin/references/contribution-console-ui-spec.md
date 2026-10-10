# 包 `console.ui.spec`

## PageDsl

**类型：** class，`@Schema(name = "PageDsl")`

Pactor Page DSL 1.0 的根文档。对应规范中描述的顶层 YAML 对象：
`dsl, requires, page, meta, state, dataSources, actions, components, lifecycle, body, children`。
完整页面优先使用 `body`，部分 DSL 片段使用根级 `children`。可变集合用于 Jackson 绑定与
程序化组装；访问器方法返回防御性副本。

### 常量

| 常量 | 说明 |
|------|------|
| `SPEC_VERSION = "1.0"` | DSL 规范版本 |

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `dsl` | `String` | 必填 DSL 版本字段（`dsl: '1.0'`） |
| `requires` | `RequiresConfig` | 运行时与组件能力要求 |
| `page` | `PageMeta` | 必填的页面标识与展示元数据 |
| `meta` | `Map<String, Object>` | 供工具使用的文档元数据；运行时不得依赖 |
| `state` | `Map<String, Object>` | 页面初始状态，运行时通过 `bindState(Map)` 更新 |
| `dataSources` | `Map<String, DataSourceConfig>` | 表达式中以 `${data.name}` 引用的命名数据源 |
| `actions` | `Map<String, ActionOrList>` | 通过 `call` 动作调用的命名可复用动作序列 |
| `components` | `Map<String, DslRenderable>` | 由 `component` 节点引用的命名可复用 UI 片段 |
| `lifecycle` | `LifecycleConfig` | 页面生命周期钩子（如 `onInit`、`onLoad`） |
| `body` | `DslNode` | 完整页面的首选 UI 根节点 |
| `children` | `Children` | 无 `body` 时部分 DSL 文档的替代根节点 |

### 构造方法

#### `PageDsl()`
- **说明：** 创建空页面 DSL，用于反序列化或组装

### 方法

#### `create() → PageDsl`（static）
- **说明：** 创建空的页面 DSL 文档
- **返回：** 空文档

#### `of(PageMeta page) → PageDsl`（static）
- **说明：** 创建带必填版本（`SPEC_VERSION`）与页面元数据的页面 DSL
- **参数：**
  - `page` — 页面元数据
- **返回：** 页面 DSL

#### `bindState(Map<String, Object> values)`
- **说明：** 将运行时值浅合并到页面状态；与 Pactor `setState` 语义一致（嵌套对象整体替换，非深度合并）
- **参数：**
  - `values` — 待合并的状态值；空/null 时忽略

#### `state() → Map<String, Object>`
- **说明：** 返回初始状态映射的不可变视图
- **返回：** 页面状态

#### `dataSources() → Map<String, DataSourceConfig>`
- **说明：** 返回命名数据源的不可变视图

#### `actions() → Map<String, ActionOrList>`
- **说明：** 返回命名页面动作的不可变视图

#### `components() → Map<String, DslRenderable>`
- **说明：** 返回命名可复用组件的不可变视图

## PageDslPageRef

**类型：** class（final）

页面 DSL 复合 `pageKey`：`{domainKey}-{moduleKey}-{xxx}`。仅有两个 `-` 作为分隔符
（`split("-", 3)`）：domain 与 module 名称本身禁止含 `-`；`xxx` 为页面后缀，可含连字符。
classpath 文件：`ui-pages/{domainKey}/{moduleKey}/{pageKey}.yaml`（`pageKey` 为完整复合键）。

### 方法

#### `encode(String domainKey, String moduleKey, String pageSuffix) → String`（static）
- **说明：** 由 domain、module 与页面后缀构建完整 `pageKey`
- **参数：**
  - `domainKey` — 领域键（须匹配 `[A-Za-z0-9][A-Za-z0-9._]*`）
  - `moduleKey` — 模块键（同上）
  - `pageSuffix` — 页面后缀（可含连字符；须匹配 `[A-Za-z0-9][A-Za-z0-9._-]*`）
- **异常：** `NexusException`（`CONFIG_ERROR`）— 任一段非法时

#### `decode(String pageKey) → PageDslPageRef`（static）
- **说明：** 解析完整 `pageKey`（`split("-", 3)`，三段均不得为空）
- **参数：**
  - `pageKey` — 完整复合键，如 `nexus-role-main`
- **返回：** 页面引用
- **异常：** `NexusException`（`CONFIG_ERROR`）— 空白或格式非法时

#### `pageKey() → String`
- **说明：** 完整复合键

#### `domainKey() → String`
- **说明：** 第一段领域键

#### `moduleKey() → String`
- **说明：** 第二段模块键

#### `pageSuffix() → String`
- **说明：** `pageKey` 中 module 之后的后缀段（可含连字符）

## HttpRequest

**类型：** class

HTTP 数据源与动态 DSL 源使用的 HTTP 请求定义。URL 与参数值可包含 Pactor 表达式，
例如 `/api/customers/${state.selectedId}`。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `method` | `String` | HTTP 方法；省略时 schema 默认为 `GET` |
| `url` | `String` | 请求 URL 或路径 |
| `params` | `Map<String, Object>` | 查询参数 |
| `headers` | `Map<String, Object>` | 请求头 |
| `body` | `Object` | POST、PUT、PATCH 请求的可选请求体 |
| `timeout` | `Integer` | 可选请求超时时间（毫秒） |

## LifecycleConfig

**类型：** class

以动作序列表达的页面生命周期钩子。每个钩子接受一个 `ActionConfig` 或有序动作列表，
与 `ActionOrList` schema 联合类型一致。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `onInit` | `ActionOrList` | 页面初始化时执行的动作序列 |
| `onLoad` | `ActionOrList` | 页面加载时执行的动作序列 |
| `onReady` | `ActionOrList` | 页面就绪时执行的动作序列 |
| `onShow` | `ActionOrList` | 页面显示时执行的动作序列 |
| `onHide` | `ActionOrList` | 页面隐藏时执行的动作序列 |
| `onDestroy` | `ActionOrList` | 页面销毁时执行的动作序列 |

## PageMeta

**类型：** class，`@Schema(name = "PageMeta")`

由 `PageDsl#page` 声明的页面标识与展示元数据。`id` 为必填（kebab-case）；`name` 为可选
（camelCase）。`parentPageKey` 声明页面在模块内的父子关系：未设置表示一级页面；设置时
表示当前页面为对应父页面的子页面（值为父页面 `id`）。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | kebab-case 唯一页面标识，例如 `customer-list`（required） |
| `name` | `String` | camelCase 程序化页面名称，例如 `customerList` |
| `title` | `String` | 人类可读的页面标题 |
| `description` | `String` | 供控制台与工具使用的可选页面描述 |
| `type` | `String` | 页面模式，如 `list`、`detail`、`form` 或 `dashboard` |
| `permission` | `PermissionConfig` | 可选的页面级访问权限 |
| `parentPageKey` | `String` | 父页面 `page.id`；为空表示一级页面 |

### 方法

#### `of(String id) → PageMeta`（static）
- **说明：** 创建带必填标识的页面元数据
- **参数：**
  - `id` — kebab-case 唯一页面标识
- **返回：** 页面元数据

#### `of(String id, String title) → PageMeta`（static）
- **说明：** 创建带标识与标题的页面元数据
- **参数：**
  - `id` — 唯一页面标识
  - `title` — 展示标题
- **返回：** 页面元数据

## PaginationConfig

**类型：** class

返回分页结果的数据源的分页绑定配置。字段值可为字面量，或绑定到页面状态的表达式。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `page` | `Object` | 当前页码或表达式 |
| `pageSize` | `Object` | 每页大小或表达式 |
| `totalField` | `String` | 后端响应中包含总数的字段名 |
| `dataField` | `String` | 后端响应中包含分页记录的字段名 |

## RequiresConfig

**类型：** class

由 `PageDsl#requires` 声明的运行时与组件能力要求。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `runtime` | `String` | 必填运行时语义版本范围，例如 `>=0.1.0` |
| `components` | `Map<String, String>` | 按组件类型名称索引的必填组件版本 |