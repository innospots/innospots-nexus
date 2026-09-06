# 常用场景与模板索引

对照 [Pactor DSL 参考](https://pactor-docs.xp-java.workers.dev/reference/dsl/)，按业务场景选用模板与规范章节。

## 场景速查

| 场景 | 模板 | 规范 |
|------|------|------|
| 最小页面 | [page-template-minimal.yaml](page-template-minimal.yaml) | [page.md](../../ui-reference/references/structure/page.md) |
| 列表 + 搜索 + 跳转详情 | [page-template-list.yaml](page-template-list.yaml) | [list-page.md](../../ui-reference/references/patterns/list-page.md) |
| 列表 + 侧滑抽屉编辑 | [page-template-list-drawer.yaml](page-template-list-drawer.yaml) + [page-template-edit-drawer.yaml](page-template-edit-drawer.yaml) | [overlay.md](../../ui-reference/references/behavior/overlay.md) |
| 新建/编辑表单（整页） | [page-template-form.yaml](page-template-form.yaml) | [form-page.md](../../ui-reference/references/patterns/form-page.md) |
| 详情 + URL 参数 + 返回/编辑 | [page-template-detail.yaml](page-template-detail.yaml) | [detail-page.md](../../ui-reference/references/patterns/detail-page.md) + [navigation.md](../../ui-reference/references/behavior/navigation.md) |
| 主从（左列表右详情） | [page-template-master-detail.yaml](page-template-master-detail.yaml) | [master-detail.md](../../ui-reference/references/patterns/master-detail.md) |
| blank 外壳 + 页内菜单布局 | [page-template-menu-layout.yaml](page-template-menu-layout.yaml) | [layout-components.md](../../ui-reference/references/structure/layout-components.md) |
| UI 片段（无 body） | [page-template-fragment.yaml](page-template-fragment.yaml) | [page.md](../../ui-reference/references/structure/page.md) |

## 交互逻辑对照

### 表单布局

| 布局 | Form `layout` | 适用 |
|------|---------------|------|
| 纵向 | `vertical`（默认） | 新建/编辑页、抽屉内表单 |
| 横向标签 | `horizontal` | 字段较少、宽屏 |
| 行内搜索 | `inline` | 列表顶栏筛选 |

字段名统一用 Input/Select 的 **`name`**（非 `field`）。提交按钮用 **`submit: true`**。

### 页面导航

| 交互 | 机制 | 示例模板 |
|------|------|----------|
| Shell 侧栏菜单换页 | 菜单 `pageId` → `page.id` | 宿主配置（非 YAML） |
| 按钮/行内跳详情 | `navigate` + `params.to` | list, detail |
| 锚点链接 | `Link` + `href` | 可嵌入 list 的 cell |
| 详情读 URL id | `lifecycle.onInit` + `${route.params.id}` | detail |
| 返回列表 | `navigate` → `/examples/list` | detail, form |

### 侧滑 / 弹层

| 交互 | 机制 | 示例模板 |
|------|------|----------|
| 行内编辑抽屉 | `overlay.open` + `type: drawer` + `page.pageId` | list-drawer + edit-drawer |
| 抽屉内保存关闭 | `overlay.close` + `payload.success` | edit-drawer |
| 关闭后刷新列表 | `if` + `${actions.editResult.success}` + `reload` | list-drawer |
| 抽屉传参 | `overlay.params.id` → 子页 `${params.id}` | list-drawer, edit-drawer |

### 菜单布局

| 场景 | 做法 |
|------|------|
| admin 默认控制台 | **不写** DSL Menu；用 Shell 菜单 |
| blank 工作区页内导航 | `Layout` + `Sider` + `Menu` + `onSelect` → `navigate` | menu-layout |

## 组合关系

```text
列表 + 抽屉编辑（两文件）:
  example-list-drawer.yaml     → 主列表，actions.openEditDrawer
  example-edit-drawer.yaml     → page.id 被 drawer 引用

列表 + 整页详情（两文件 + 路由）:
  example-list.yaml            → navigate → /examples/:id/detail
  example-detail.yaml          → route.params.id 加载 detail
```

## 使用步骤

1. 从上表选最接近的模板
2. 复制并重命名 `page.id`、文件名、`service` 名
3. 按 `ui:reference` 调整布局与 props
4. `ui:check` 规范审查

覆盖度与缺口 → [../../COVERAGE.md](../../COVERAGE.md)。
