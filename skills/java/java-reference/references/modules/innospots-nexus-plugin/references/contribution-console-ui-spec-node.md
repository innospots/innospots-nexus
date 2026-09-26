# 包 `contribution.console.ui.spec.node`

## Children

**类型：** record

以数组或单个动态源引用声明的子可渲染节点。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `items` | `List<DslRenderable>` | — |
| `sourceRef` | `DslSourceRef` | — |

### 方法

#### `ofItems(List<DslRenderable> items) → Children`
- **说明：** 从内联可渲染列表创建子节点。
- **参数：**
  - `items` — 子可渲染节点
- **返回：** 子节点包装器

#### `ofSourceRef(DslSourceRef sourceRef) → Children`
- **说明：** 从单个动态源引用创建子节点。
- **参数：**
  - `sourceRef` — 动态源引用
- **返回：** 子节点包装器

#### `isArray() → boolean`
- **说明：** 返回子节点是否以数组形式声明。
- **返回：** 数组子节点时返回 true

#### `items() → List<DslRenderable>`
- **说明：** 返回内联子节点列表，可能为空。
- **返回：** 子节点列表


## ComponentNode

**类型：** class

以注册表 type 声明的内联组件节点。


## ComponentReferenceNode

**类型：** class

对 com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl 中声明的命名组件的引用。


## DslHttpSource

**类型：** class

HTTP 支持的动态 DSL 源。


## DslNode

**类型：** interface

以 type 或 component 声明的 UI 树节点。


## DslRenderable

**类型：** interface

可渲染 DSL 片段：内联节点或动态源。


## DslServiceSource

**类型：** class

服务支持的动态 DSL 源。


## DslSource

**类型：** interface

DslSourceRef 的动态 DSL 源定义。


## DslSourceRef

**类型：** class

从服务或 HTTP 源加载的动态 DSL 片段。
