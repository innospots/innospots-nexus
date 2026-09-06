# 弹层与侧滑层（Overlay）

Pactor 通过 **`overlay.open` / `overlay.close` 动作** 实现模态框（modal）与侧滑抽屉（drawer），不在 PageDsl 顶层增加 `modal:` / `drawer:` 字段。

权威来源：[Pactor DSL 参考 — overlay.open](https://pactor-docs.xp-java.workers.dev/reference/dsl/#overlayopen-的-overlay-配置)。

相关：`navigate`（整页跳转）→ [navigation.md](navigation.md)；动作总览 → [action.md](action.md)。

---

## 两种弹层类型

| `overlay.type` | 交互形态 | 典型场景 |
|----------------|----------|----------|
| `modal` | 居中对话框 | 确认、短表单、详情预览 |
| `drawer` | 侧滑面板 | 编辑、筛选高级面板、宽表单 |

类型在 **动作参数**里声明，不是独立组件 `type`。

---

## 内容结构在哪定义？

弹层里的 UI **仍是 DslNode 树**，有两种来源：

| 方式 | 定义位置 | 适用 |
|------|----------|------|
| **内联 `content`** | `overlay.open` → `params.overlay.content` | 短表单、确认区、与当前页强耦合的片段 |
| **引用页面 `page`** | `params.overlay.page.pageId` | 可复用的编辑页/详情页，独立 YAML 文件 |
| **动态 `DslSourceRef`** | `content` 子树用 `source:` | 按角色/权限远端下发结构 |

```text
当前页 PageDsl（ui-pages/.../list.yaml）
├── body                    ← 主页面结构
├── actions                 ← 可含 openEditDrawer
└── （无顶层 overlay 字段）

打开弹层时：
overlay.open.params.overlay
├── type: drawer | modal
├── title / width
├── content: { type: Form, children: [...] }   ← 内联结构
└── page: { pageId: customer-edit }            ← 或引用另一份 PageDsl
```

**主页面 `body` 只描述常驻内容**；临时浮层内容在 **触发它的 action** 或 **被引用的 pageId 对应 YAML** 中定义。

---

## overlay.open

```yaml
actions:
  openEditDrawer:
    - id: editResult
      action: overlay.open
      params:
        overlay:
          type: drawer
          title: 编辑客户
          width: 420
          params:
            id: ${row.id}
          content:
            type: Form
            props:
              name: edit
            children:
              - type: Input
                props:
                  name: name
                  label: 客户名称
            events:
              onSubmit:
                - action: overlay.close
                  params:
                    payload:
                      success: true
                      values: ${event}
```

### overlay 配置字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | `modal` \| `drawer` | 是 | 弹层形态 |
| `title` | string | 否 | 标题 |
| `width` | number \| string | 否 | 宽度（drawer 常用） |
| `content` | DslNode | 与 `page` 二选一 | 内联 DSL 子树 |
| `page` | `{ pageId: string }` | 与 `content` 二选一 | 动态加载另一份 PageDsl |
| `params` | object | 否 | 注入弹层作用域的 `params` 键 |

### 引用独立页面

```yaml
- id: editResult
  action: overlay.open
  params:
    overlay:
      type: drawer
      title: 编辑客户
      width: 480
      params:
        id: ${row.id}
      page:
        pageId: customer-edit    # 对应 ui-pages/.../customer-edit.yaml 的 page.id
```

被引用页的 `body` 即抽屉内渲染的完整 UI 树；`page.id` 与路由/菜单寻址一致 → [naming.md](../naming.md)。

---

## overlay.close

在弹层 **内部** 关闭并回传结果：

```yaml
events:
  onSubmit:
    - action: overlay.close
      params:
        payload:
          success: true
          values: ${form}
```

| 字段 | 说明 |
|------|------|
| `id` | 可选，指定关闭哪个弹层；缺省关闭栈顶 |
| `payload` | 回传给 `overlay.open` 的结果；缺省表示取消语义 |

---

## 作用域与表达式

弹层内容在 **overlay 作用域** 求值：

| 键 | 说明 | 示例 |
|----|------|------|
| `params` | `overlay.open` 注入的参数 | `${params.id}` |
| `parent` | 父页面作用域快照（只读） | `${parent.state.keyword}` |
| `state` / `data` / `form` | 与父页共享的 store，变更会驱动重渲染 | `${state.x}` |

行内按钮打开抽屉时，`${row.id}` 在 **触发处** 求值后写入 `params`。

### 关闭结果与后续动作

`overlay.open` 带 `id` 时，关闭结果写入 `${actions.<id>}`：

```yaml
- id: editResult
  action: overlay.open
  params:
    overlay: { ... }

- action: if
  params:
    condition: ${actions.editResult.success}
    then:
      - action: reload
        params:
          dataSource: customers
      - action: message
        params:
          type: success
          text: 保存成功
```

---

## 典型模式

### 列表行 → 抽屉编辑

```yaml
# 表格列
- title: 操作
  cell:
    type: Button
    props:
      text: 编辑
      variant: link
      size: small
    events:
      onClick:
        action: call
        params:
          name: openEditDrawer

actions:
  openEditDrawer:
    - id: editResult
      action: overlay.open
      params:
        overlay:
          type: drawer
          title: 编辑
          params: { id: ${row.id} }
          page: { pageId: customer-edit }
    - action: if
      params:
        condition: ${actions.editResult.success}
        then:
          - action: reload
            params: { dataSource: customers }
```

### 确认后删除（modal + confirm 组合）

删除链路常用 `confirm` → `if` → `request`，不一定需要 overlay；需要自定义 UI 时用 `modal` + 内联 `content`。

---

## 与 Layout / Page 的区别

| 机制 | 层级 | 用途 |
|------|------|------|
| `Layout` + `Sider` | 页面内**常驻**分区 | blank 外壳全屏骨架 |
| `overlay.open` | **浮层**，覆盖在当前页之上 | 编辑、详情、确认 |
| `navigate` | **整页**路由切换 | 菜单跳转、详情页 |

不要在 `body` 里用 `Card` 模拟抽屉；临时浮层统一走 `overlay.open`。

---

## 生命周期

弹层显隐可配合页面级钩子（宿主路由触发）：

| 钩子 | 说明 |
|------|------|
| `onShow` | 页面进入可视（含从其他页返回） |
| `onHide` | 页面离开可视 |

弹层打开/关闭本身**不**新增 DSL 生命周期字段，由动作链处理。

---

## ui:check 要点

- [ ] `overlay.content` 与 `overlay.page` 二选一
- [ ] `page.pageId` 对应已存在的 `page.id`（遗留项：运行时是否注册）
- [ ] 内联 `content` 满足 DslNode 规则 → [node.md](../structure/node.md)
- [ ] 关闭后刷新列表用 `id` + `if` + `reload`，避免重复打开
