# 包 `contribution.console.ui.spec.filter`

## PageDslFilter

**类型：** interface

在渲染时准备阶段转换页面 DSL 文档。


## PageDslFilterChain

**类型：** class

有序的 PageDslFilter 实例链。

### 方法

#### `create() → PageDslFilterChain`
- **说明：** 创建空过滤器链。
- **返回：** 空过滤器链

#### `of(PageDslFilter... filters) → PageDslFilterChain`
- **说明：** 根据提供的过滤器创建过滤器链。
- **参数：**
  - `filters` — 有序过滤器
- **返回：** 过滤器链

#### `add(PageDslFilter filter) → PageDslFilterChain`
- **说明：** 返回追加了新过滤器的新链。
- **参数：**
  - `filter` — 待追加的过滤器
- **返回：** 新过滤器链

#### `process(PageDslRenderContext context) → PageDsl`
- **说明：** 对提供的上下文执行已配置的过滤器。
- **参数：**
  - `context` — 渲染上下文
- **返回：** 处理后的页面 DSL

#### `filters() → List<PageDslFilter>`
- **说明：** 返回已配置过滤器的不可变视图。
- **返回：** 过滤器列表


## PageDslRenderContext

**类型：** class

在 PageDslFilterChain 中传递的渲染时上下文。

### 方法

#### `of(String moduleKey,
            String pageKey,
            PageDsl document,
            Map<String, Object> parameters) → PageDslRenderContext`
- **说明：** 为一个模块页面创建渲染上下文。
- **参数：**
  - `moduleKey` — 所属模块键
  - `pageKey` — 与 page.id 匹配的页面键
  - `document` — 从存储加载的源页面 DSL
  - `parameters` — 运行时请求参数
- **返回：** 渲染上下文

#### `moduleKey() → String`
- **说明：** 返回所属模块键。
- **返回：** 模块键

#### `pageKey() → String`
- **说明：** 返回页面键。
- **返回：** 页面键

#### `document() → PageDsl`
- **说明：** 返回当前工作页面 DSL 文档。
- **返回：** 页面 DSL 文档

#### `withDocument(PageDsl document) → PageDslRenderContext`
- **说明：** 返回替换了工作文档的上下文视图。
- **参数：**
  - `document` — 下一工作文档
- **返回：** 供下一过滤器步骤使用的上下文

#### `parameters() → Map<String, Object>`
- **说明：** 返回请求参数的不可变视图。
- **返回：** 请求参数

#### `attribute(String key, Object value) → PageDslRenderContext`
- **说明：** 为同一链执行中的下游过滤器存储一个属性。
- **参数：**
  - `key` — 属性键
  - `value` — 属性值
- **返回：** 本上下文，支持链式调用

#### `attribute(String key) → Object`
- **说明：** 返回属性值；不存在时返回 null。
- **参数：**
  - `key` — 属性键
- **返回：** 属性值

#### `attributes() → Map<String, Object>`
- **说明：** 返回链属性的不可变视图。
- **返回：** 链属性


## StateBindingPageDslFilter

**类型：** class

将请求参数绑定到页面 DSL 的 state 映射。
