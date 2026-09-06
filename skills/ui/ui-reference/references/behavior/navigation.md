# 页面导航与加载

页面间跳转、菜单打开对应页面、按钮触发路由——涉及 **三层配置**，不要混在一处。

权威来源：[Pactor DSL 参考 — navigate / page / 作用域](https://pactor-docs.xp-java.workers.dev/reference/dsl/)。

相关：弹层内编辑 → [overlay.md](overlay.md)；`page.id` 命名 → [naming.md](../naming.md)。

---

## 三层分工

```text
┌─────────────────────────────────────────────────────────────┐
│ 1. Bootstrap / 菜单注册（不属于 PageDsl YAML）                │
│    菜单项 pageId、路由 path、权限码 → 决定「点菜单打开哪页」    │
├─────────────────────────────────────────────────────────────┤
│ 2. PageDsl 文档（ui-pages/{module}/{page.id}.yaml）          │
│    page 元信息 + body 结构 + state/dataSources/actions       │
│    → 决定「这一页长什么样、加载什么数据」                       │
├─────────────────────────────────────────────────────────────┤
│ 3. 页面内动作（navigate / Link / Menu.onSelect）              │
│    → 决定「页内按钮/链接跳去哪」                               │
└─────────────────────────────────────────────────────────────┘
```

| 问题 | 定义在哪 | 规范文档 |
|------|----------|----------|
| 菜单点「客户列表」打开哪页？ | Bootstrap 菜单 `pageId` | 宿主菜单配置（非 PageDsl） |
| 客户列表页 UI 结构？ | `ui-pages/.../customer-list.yaml` 的 `body` | [page.md](../structure/page.md)、[patterns/](../patterns/) |
| 行内「编辑」跳详情还是开抽屉？ | 当前页 `events` / `actions` | 本文、[overlay.md](overlay.md) |
| 详情页读 URL 里的 id？ | 详情页 DSL + `${route.params}` | 本文「路由作用域」 |

---

## 页面内容结构在哪定义？

**一份页面 = 一个 PageDsl 文件**，结构入口：

```yaml
dsl: '1.0'
page:
  id: customer-list          # ← 全局寻址键，与菜单 pageId、路由一致
  title: 客户列表
  type: list

state: { ... }
dataSources: { ... }
actions: { ... }

body:                        # ← 主 UI 结构（推荐）
  type: Page
  children:
    - ...
```

| 字段 | 定义什么 |
|------|----------|
| `page` | 页面身份：id、标题、类型、页面级权限 |
| `body` / `children` | **可见 UI 树**（组件、布局、事件绑定） |
| `state` | 页内响应式状态初始值 |
| `dataSources` | 页内数据加载配置 |
| `actions` | 可复用动作序列（含 `navigate`、`overlay.open`） |
| `components` | 可复用 UI 片段 |
| `lifecycle` | 页面创建/显示时的自动动作 |

**菜单点击加载页面**时，宿主按 `pageId` 找到对应 YAML，解析 `body` 渲染到 Shell 的 Page Area；**不需要**在列表页 YAML 里定义菜单结构。

片段/嵌入场景可用根级 `children` 代替 `body` → [page.md](../structure/page.md)。

---

## navigate（整页跳转）

应用层注册动作，经路由桥接跳转（如 react-router `push`）。

```yaml
events:
  onClick:
    - action: navigate
      params:
        to: /customers/${row.id}/edit
```

| 字段 | 说明 |
|------|------|
| `to` | **必填**，目标路径；支持 `${...}` 插值 |

### 典型用法

```yaml
# 工具栏「新建」
- type: Button
  props:
    text: 新建客户
    variant: primary
  events:
    onClick:
      - action: navigate
        params:
          to: /customers/new

# 表格行链接式跳转
- type: Link
  props:
    text: ${row.name}
    href: /customers/${row.id}
```

`navigate` 与 `Link` 区别：`navigate` 走动作引擎（可配 `permission`、`condition`）；`Link` 是原生锚点，适合嵌入模式。

---

## 菜单 → 页面加载（Shell 级）

Application Shell 侧边栏菜单 **不在 PageDsl 里定义**。流程：

```text
用户点击 Shell 菜单项
    → 菜单项携带 pageId（如 customer-list）
    → 宿主路由加载 ui-pages/.../customer-list.yaml
    → 解析 PageDsl，渲染 body 到 Page Area
    → 触发 lifecycle.onInit / onLoad；autoLoad 数据源并行加载
```

PageDsl 侧只需保证：

- `page.id` 与菜单配置的 `pageId` **一致**
- 文件路径符合 [naming.md](../naming.md)
- `page.permission` 与菜单权限策略对齐（可选）

### 页内 Menu（DSL 组件）

`blank` 外壳或工作区内的 `type: Menu` **不是** Shell 菜单，需自己处理选中与跳转：

```yaml
- type: Menu
  props:
    mode: inline
    items: ${data.menus}
    selectedKey: ${state.currentMenu}
  events:
    onSelect:
      - action: setState
        params:
          currentMenu: ${event}
      - action: navigate
        params:
          to: /workspace/${event}
```

`Menu` 不读取 MenuRegistry；`items` 由 `dataSources` 或静态配置注入。

---

## 路由作用域

跳转后，目标页表达式可读路由上下文：

| 键 | 说明 | 示例 |
|----|------|------|
| `route.params` | 路径参数 | `${route.params.id}` |
| `route.query` | 查询参数 | `${route.query.tab}` |
| `params` | `route.params` 别名；overlay 内为注入参数 | `${params.id}` |
| `page` | 当前页元信息 | `${page.title}` |

### 详情页读 URL 参数

```yaml
# ui-pages/customer/customer-detail.yaml
page:
  id: customer-detail
  title: 客户详情

state:
  customerId: null

lifecycle:
  onInit:
    - action: setState
      params:
        customerId: ${route.params.id}

dataSources:
  detail:
    type: service
    service: customer.detail
    params:
      id: ${state.customerId}

body:
  type: Page
  children:
    - type: Card
      children:
        - type: Statistic
          props:
            title: 名称
            value: ${data.detail.name}
```

列表页跳详情：

```yaml
- action: navigate
  params:
    to: /customers/${row.id}/detail
```

路由表 ` /customers/:id/detail ` → `pageId: customer-detail` 由**宿主路由配置**绑定，不在单页 YAML 内声明。

---

## 页面加载与数据时机

| 机制 | 时机 |
|------|------|
| `dataSources.*.autoLoad: true` | 页面运行时初始化后自动加载（不阻塞首屏） |
| `lifecycle.onInit` | 页面创建后（与 autoLoad 并行） |
| `lifecycle.onLoad` | 首次 autoLoad 全部完成后 |
| `reload` 动作 | 手动刷新指定 dataSource |
| `navigate` 进入新页 | 新页重新走上述流程 |

避免 `autoLoad: true` 与 `onLoad` 里对同一源重复 `reload` → [lifecycle.md](lifecycle.md)。

---

## 交互选型决策

| 需求 | 推荐 |
|------|------|
| 侧栏菜单换页 | Shell 菜单 + `page.id`（不用 DSL `navigate` 定义菜单） |
| 列表 → 独立详情页 | `navigate` 或 `Link` + 详情页 YAML |
| 列表 → 行内编辑 | `overlay.open` + `drawer` + 内联 `content` 或 `page.pageId` |
| 同页筛选刷新 | `setState` + `reload`（不 navigate） |
| 新开浏览器标签 | `Link` + `target: _blank` |

```text
换页（卸载当前 PageDsl）     → navigate / Shell 菜单
浮层（当前页保持）           → overlay.open
同页数据刷新                 → reload
```

---

## 与弹层引用页的关系

| 方式 | 页面是否卸载 | 结构定义位置 |
|------|-------------|-------------|
| `navigate` | 是（路由切换） | 目标页 `body` |
| `overlay.open` + `page.pageId` | 否（浮层覆盖） | 被引用页 `body` |
| `overlay.open` + `content` | 否 | action 参数内联 DslNode |

同一 `customer-edit` 既可被抽屉引用，也可作为独立路由页——取决于宿主是否注册了对应路由。

---

## ui:check 要点

- [ ] `page.id` 与文件名、菜单 `pageId` 一致（菜单为遗留项对照）
- [ ] `navigate.params.to` 非空
- [ ] 详情页若依赖 URL 参数，`onInit` 或 dataSource `params` 使用 `route.params`
- [ ] 列表刷新用 `reload`，误用 `navigate` 回同页标警告
