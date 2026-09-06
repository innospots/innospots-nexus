# 内置组件目录（Component Catalog）

Pactor 1.0 内置组件 props 契约。权威来源：[Pactor DSL 参考 — 组件目录](https://pactor-docs.xp-java.workers.dev/reference/dsl/)。

组件 `type` 合法性由**运行时 Component Registry** 校验；本页定义**协议层 props 形状**，供 `ui:page` 编写与 `ui:check` 规范审查。

与 [component.md](component.md) 区分：

| 文档 | 内容 |
|------|------|
| [component.md](component.md) | 顶层 `components` 命名复用、`component:` 引用语义 |
| 本文 | 各 `type` 的 props / events 契约 |

---

## 通用能力（所有组件）

以下字段在**节点层**声明（与 `props` 同级），所有组件一致支持：

| 字段 | 说明 |
|------|------|
| `className` | 根节点 class，与内置样式合并 |
| `style` | 根节点内联样式对象，如 `{ flex: 1 }` |
| `permission` | 组件级权限，见 [permission.md](../security/permission.md) |
| `when` | 条件渲染，表达式为假时不渲染（子树一并跳过） |
| `events` | 事件名 → 动作或动作数组 |

`props` 内也可使用 `className`、`style`（与节点层语义相同，以组件实现为准）。

**优先级**：布局语义优先用组件 props（`columns`、`gap`、`justify` 等）；`style` / `className` 仅作逃生口 → 见 [layout.md](layout.md)。

**Form 内表单项**：声明 `name` 的 `Input` / `Select` 自动成为表单项（`label` / `rules` 生效，值由 Form 托管）；独立渲染时挂控件本身。

---

## Page

页面内容根容器；`children` 纵向间距约 20px。

| prop | 类型 | 说明 |
|------|------|------|
| `title` | string | 页面标题（可与 `page.title` 配合） |

```yaml
body:
  type: Page
  props:
    title: 客户列表
  children:
    - ...
```

---

## Grid

均分栏布局（CSS grid，列宽 `minmax(0, 1fr)`）。

| prop | 类型 | 说明 |
|------|------|------|
| `columns` | number（默认 1） | 列数 |
| `gap` | number | 栏间距 |

**适用**：统计卡片矩阵、等宽多列。非均分主从 → [patterns/master-detail.md](../patterns/master-detail.md)。

---

## Flex

| prop | 类型 | 说明 |
|------|------|------|
| `direction` | `horizontal`（默认） \| `vertical` | 主轴方向 |
| `gap` | number \| `small` \| `middle` \| `large` | 间距 |
| `justify` | `start` \| `center` \| `end` \| `space-between` \| `space-around` \| `space-evenly` | 主轴对齐 |
| `align` | `start` \| `center` \| `end` \| `baseline` \| `stretch` | 交叉轴对齐 |

```yaml
type: Flex
props:
  direction: horizontal
  gap: middle
  justify: space-between
  align: center
```

---

## Card

| prop | 类型 | 说明 |
|------|------|------|
| `title` | string | 卡片标题 |
| `bordered` | boolean（默认 true） | 是否有边框 |

---

## Layout 族

页面**区域骨架**组件：`Layout` / `Header` / `Sider` / `Content` / `Footer`（+ 常配合的 `Menu`）。

**语义、组合模式、与 Application Shell 边界、反模式** → 专篇 [layout-components.md](layout-components.md)。  
本节仅列 props。

### Layout

| prop | 类型 | 说明 |
|------|------|------|
| `direction` | `horizontal` \| `vertical` | 主轴方向；缺省：子节点含 `Sider` → horizontal，否则 vertical |

### Header / Content / Footer

无特有 props（通用 `className` / `style`）。`Content` 默认 `flex: 1`。

### Sider

| prop | 类型 | 说明 |
|------|------|------|
| `width` | number（默认 200） | 展开宽度 |
| `collapsible` | boolean | 渲染底部折叠触发器 |
| `collapsed` | boolean | 受控折叠态（须与 `onCollapse` 成对） |
| `collapsedWidth` | number（默认 64） | 折叠后宽度 |
| 事件 `onCollapse` | — | 传出 boolean → 典型 `setState` |

### Menu

| prop | 类型 | 说明 |
|------|------|------|
| `items` | `{ key, label, icon?, children? }[]` | 菜单树；`icon` 预留 |
| `mode` | `inline`（默认） \| `horizontal` | Sider 内 inline；Header 内 horizontal |
| `selectedKey` | string | 受控选中项 |
| 事件 `onSelect` | — | 传出 key |

---

## Button

| prop | 类型 | 说明 |
|------|------|------|
| `text` | string | 按钮文本 |
| `variant` | `default`（默认） \| `primary` \| `dashed` \| `link` \| `text` | 变体 |
| `danger` | boolean | 危险态 |
| `size` | `small` \| `middle`（默认） \| `large` | 尺寸 |
| `loading` | boolean | 加载态（可绑 `${dataStatus.x.loading}`，loading 时禁用点击） |
| `disabled` | boolean | 禁用 |
| `submit` | boolean | 作为 Form 提交按钮 |
| 事件 `onClick` | — | 点击触发动作；`event` 无参 |

---

## Alert

| prop | 类型 | 说明 |
|------|------|------|
| `message` | string | 主文案 |
| `description` | string | 辅助说明 |
| `variant` | `info`（默认） \| `success` \| `warning` \| `error` | 变体 |

---

## Link

原生锚点链接（嵌入模式的主要跳转方式之一）。

| prop | 类型 | 说明 |
|------|------|------|
| `href` | string | 链接地址（可含表达式，如 `/items/${row.id}`） |
| `text` | string | 链接文本（也可用 `children`） |
| `target` | `_self`（默认） \| `_blank` \| `_parent` \| `_top` | `_blank` 自动带 `rel="noreferrer"` |

---

## Text

普通文本渲染。

| prop | 类型 | 说明 |
|------|------|------|
| `text` | string | 文本内容（可含表达式）；也可用 `children` |
| `type` | `default`（默认） \| `secondary` \| `success` \| `warning` \| `danger` | 语义颜色 |
| `strong` | boolean | 加粗 |
| `delete` | boolean | 删除线 |
| `underline` | boolean | 下划线 |
| `italic` | boolean | 斜体 |

---

## Statistic

| prop | 类型 | 说明 |
|------|------|------|
| `title` | string | 指标标题 |
| `value` | number \| string | 数值（通常为表达式结果） |
| `precision` | number | 小数精度 |
| `suffix` | string | 后缀（如单位） |

---

## Tabs

| prop | 类型 | 说明 |
|------|------|------|
| `items` | `{ key, label, children?: DslNode[] }[]` | 页签列表，pane 内容为 DSL 子树 |
| `activeKey` | string | 当前激活 key（可绑表达式） |
| 事件 `onChange` | — | 切换时传出新 key |

---

## Table

| prop | 类型 | 说明 |
|------|------|------|
| `columns` | TableColumn[] | 列定义（见下） |
| `dataSource` | object[] | 数据行（通常 `${data.xxx}`） |
| `rowKey` | string（默认 `'id'`） | 行 key 字段 |
| `loading` | boolean | 加载态（可绑 `${dataStatus.x.loading}`） |
| `pagination` | `false` \| `{ pageSize? }` | 分页配置 |
| 事件 `onChange` | — | 分页变化，传出 `{ current, pageSize, total }` |

### TableColumn

| 字段 | 说明 |
|------|------|
| `title` | 列标题 |
| `dataIndex` | 数据字段 |
| `width` | 列宽 |
| `cell` | DslNode 行模板；作用域注入 `row` / `rowIndex` |

行内按钮、显隐、动作参数经 `${row.xxx}` 表达：

```yaml
columns:
  - title: 客户
    cell:
      type: Button
      props:
        text: ${row.name}
        variant: link
      events:
        onClick:
          - action: setState
            params: { currentId: ${row.id} }
```

---

## Form

| prop | 类型 | 说明 |
|------|------|------|
| `name` | string（默认 `'default'`） | 表单名；`form.setValues` 按名寻址；同页多表单须显式命名 |
| `layout` | `vertical`（默认） \| `horizontal` \| `inline` | 表单布局 |
| `initialValues` | object | 初始值；字段级 `value` 优先于同名字段 |
| 事件 `onSubmit` | — | 校验通过后触发；`event` 为表单 values，同时写入 `${form.}` |
| 事件 `onValuesChange` | — | 字段变化；`event` 为单键对象 `{ fieldName: value }` |

提交后 `${form.keyword}` 与 `${state.form.keyword}` 等价（`form` 是 `state.form` 的作用域别名）。

---

## Input

| prop | 类型 | 说明 |
|------|------|------|
| `name` | string | 字段名（Form 内即表单项） |
| `label` | string | 表单项标签 |
| `rules` | FieldRule[] | 校验规则 |
| `value` | string | 独立渲染时为受控值；Form 内为字段初始值 |
| `type` | `text`（默认） \| `password` | `password` 为掩码输入 |
| `placeholder` | string | 占位文案 |
| `disabled` | boolean | 禁用 |
| 事件 `onChange` | — | 传出 string 值 |

受控闭环：

```yaml
- type: Input
  props:
    value: ${state.keyword}
  events:
    onChange:
      - action: setState
        params: { keyword: ${event} }
```

---

## Select

| prop | 类型 | 说明 |
|------|------|------|
| `name` / `label` / `rules` / `placeholder` / `disabled` | 同 Input | — |
| `options` | `{ label, value, disabled? }[]` | `value` 为 string \| number \| boolean |
| `value` | string \| number \| boolean | 同 Input 初始值语义 |
| `allowClear` | boolean | 可清空 |
| 事件 `onChange` | — | 传出选中 value |

远程选项经 dataSource 的 `valueField` / `labelField` / `disabledField` 映射 → [datasource.md](../behavior/datasource.md)。

---

## 校验规则（FieldRule）

当前支持必填：

```yaml
rules:
  - required: true
    message: 请输入客户名称
```

校验失败阻止 Form 提交，并在字段下方展示 `message`。

---

## Markdown

安全 Markdown 渲染（禁 raw HTML、链接协议白名单）。需运行时注册后可用。

| prop | 类型 | 说明 |
|------|------|------|
| `content` | string | Markdown 源文本；`${data.xxx}` 等表达式由 Renderer 下发前解析 |

```yaml
- type: Markdown
  props:
    content: '当前筛选：**${state.keyword}**'
```

---

## 扩展组件（非内置目录）

以下由业务或 kit 包注册，**不在 1.0 内置目录**，编写时须确认宿主已注册：

| 组件 | 说明 |
|------|------|
| `Chat` | `@pactor-app/chat`；需 `registerChatComponent` |
| 自定义 `type` | 经 Component Registry 扩展 |

`requires.components` 可声明组件 API 版本要求（semver range），当前仅声明透传，不强制阻断。

---

## 与节点形状的关系

```text
DslNode
├── type: <ComponentCatalog 中的名称>   → ComponentNode
├── component: <components 中的 key>    → ComponentReferenceNode
└── source: ...                         → DslSourceRef
```

节点字段完整说明 → [node.md](node.md)。命名复用语义 → [component.md](component.md)。
