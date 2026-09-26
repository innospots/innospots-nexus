# 包 `contribution.console.ui.spec`

## HttpRequest

**类型：** class

HTTP 数据源与动态 DSL 源使用的 HTTP 请求定义。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private String` | `HTTP 方法；省略时 schema 默认为 {@code GET}。 */
    private String method = "GET";

    /** 请求 URL 或路径。` | HTTP 方法；省略时 schema 默认为 GET。 private String method = "GET"; /** 请求 URL 或路径。 private String url; |
| `private Integer` | `可选请求超时时间（毫秒）。` | 可选请求超时时间（毫秒）。 private Integer timeout; |


## LifecycleConfig

**类型：** class

以动作序列表达的页面生命周期钩子。


## PageDsl

**类型：** class

Pactor Page DSL 1.0 的根文档。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private RequiresConfig` | `运行时与组件能力要求。` | 运行时与组件能力要求。 private RequiresConfig requires; |
| `private PageMeta` | `必填的页面标识与展示元数据。` | 必填的页面标识与展示元数据。 private PageMeta page; |

### 方法

#### `create() → PageDsl`
- **说明：** DSL 规范版本，必须为 {@link #SPEC_VERSION}。 public static final String SPEC_VERSION = "1.0"; /** 必填 DSL 版本字段（dsl: '1.0'）。 private String dsl; /** 运行时与组件能力要求。 private RequiresConfig requires; /** 必填的页面标识与展示元数据。 private PageMeta page; /** 供工具使用的文档元数据；运行时不得依赖此对象。 private Map meta = new LinkedHashMap<>(); /** 页面初始状态，运行时通过 {@link #bindState(Map)} 更新。 private Map state = new LinkedHashMap<>(); /** 表达式中以 ${data.name} 引用的命名数据源。 private Map dataSources = new LinkedHashMap<>(); /** 通过 call 动作调用的命名可复用动作序列。 private Map actions = new LinkedHashMap<>(); /** 由 component 节点引用的命名可复用 UI 片段。 private Map components = new LinkedHashMap<>(); /** 页面生命周期钩子，如 onInit 与 onLoad。 private LifecycleConfig lifecycle; /** 完整页面的首选 UI 根节点。 private com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslNode body; /** 无 body 时，部分 DSL 文档的替代根节点。 private Children children; /** 创建空页面 DSL，用于反序列化或组装。 public PageDsl() { } /** 创建空的页面 DSL 文档。
- **返回：** 空文档

#### `of(PageMeta page) → PageDsl`
- **说明：** 创建带必填版本与页面元数据的页面 DSL。
- **参数：**
  - `page` — 页面元数据
- **返回：** 页面 DSL

#### `bindState(Map<String, Object> values) → void`
- **说明：** 将运行时值浅合并到页面状态。 与 Pactor setState 语义一致：嵌套对象整体替换，而非深度合并。
- **参数：**
  - `values` — 待合并的状态值

#### `state() → Map<String, Object>`
- **说明：** 返回初始状态映射的不可变视图。
- **返回：** 页面状态

#### `dataSources() → Map<String, DataSourceConfig>`
- **说明：** 返回命名数据源的不可变视图。
- **返回：** 数据源

#### `actions() → Map<String, ActionOrList>`
- **说明：** 返回命名页面动作的不可变视图。
- **返回：** 页面动作

#### `components() → Map<String, DslRenderable>`
- **说明：** 返回命名可复用组件的不可变视图。
- **返回：** 命名组件


## PageMeta

**类型：** class

由 PageDsl 声明的页面标识与展示元数据。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private String` | `kebab-case 唯一页面标识，例如 {@code customer-list}。` | kebab-case 唯一页面标识，例如 customer-list。 private String id; |
| `private String` | `camelCase 程序化页面名称，例如 {@code customerList}。` | camelCase 程序化页面名称，例如 customerList。 private String name; |
| `private String` | `人类可读的页面标题。` | 人类可读的页面标题。 private String title; |
| `private String` | `供控制台与工具使用的可选页面描述。` | 供控制台与工具使用的可选页面描述。 private String description; |
| `private String` | `页面模式，如 {@code list}、{@code detail}、{@code form} 或 {@code dashboard}。` | 页面模式，如 list、detail、form 或 dashboard。 private String type; |
| `private PermissionConfig` | `可选的页面级访问权限。` | 可选的页面级访问权限。 private PermissionConfig permission; |

### 方法

#### `of(String id) → PageMeta`
- **说明：** kebab-case 唯一页面标识，例如 customer-list。 private String id; /** camelCase 程序化页面名称，例如 customerList。 private String name; /** 人类可读的页面标题。 private String title; /** 供控制台与工具使用的可选页面描述。 private String description; /** 页面模式，如 list、detail、form 或 dashboard。 private String type; /** 可选的页面级访问权限。 private PermissionConfig permission; /** 创建空页面元数据。 public PageMeta() { } /** 创建带必填标识的页面元数据。
- **参数：**
  - `id` — kebab-case 唯一页面标识
- **返回：** 页面元数据

#### `of(String id, String title) → PageMeta`
- **说明：** 创建带标识与标题的页面元数据。
- **参数：**
  - `id` — 唯一页面标识
  - `title` — 展示标题
- **返回：** 页面元数据


## PaginationConfig

**类型：** class

返回分页结果的数据源的分页绑定配置。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private Object` | `当前页码或表达式。` | 当前页码或表达式。 private Object page; |
| `private Object` | `每页大小或表达式。` | 每页大小或表达式。 private Object pageSize; |
| `private String` | `后端响应中包含总数的字段名。` | 后端响应中包含总数的字段名。 private String totalField; |
| `private String` | `后端响应中包含分页记录的字段名。` | 后端响应中包含分页记录的字段名。 private String dataField; |


## RequiresConfig

**类型：** class

由 PageDsl 声明的运行时与组件能力要求。

### 成员变量

| 名称 | 类型 | 说明 |
|------|------|------|
| `private String` | `必填运行时语义版本范围，例如 {@code >=0.1.0}。` | 必填运行时语义版本范围，例如 >=0.1.0。 private String runtime; |
