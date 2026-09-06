---
name: ui:page
display_name: Page DSL 创作
description: |
  新建或修改 Pactor Page DSL 页面 YAML。当用户要创建列表/表单/看板/主从页、
  设计页面布局（Page/Grid/Flex）、编写 ui-pages 资源时使用。
  触发词：写页面 DSL、页面布局、Grid、Flex、list-page、表单页、看板。
category: ui
version: 1.3.0
---

# Page DSL 创作

## 定位

编写符合 Pactor Page DSL 1.0 的页面 YAML。执行前加载 `ui:reference`；完成后交 `ui:check`。

## 设计工作流（布局先于组件）

```text
1. 需求分析          页面目标、数据、操作
2. 选择 Page Pattern  ui:reference → patterns/
3. 设计页面区域      Header / Filter / Main
4. 选择 Layout 原语  ui:reference → structure/layout.md
5. 设计组件树        type / component / children
6. 设计 state / dataSources / actions
7. 输出 YAML
8. ui:check
```

布局设计必须在填具体业务组件之前完成。

## 创作前检查

1. `page.id`（kebab-case）与文件名 `{page.id}.yaml` 一致
2. 资源路径：`ui-pages/{moduleKey}/{page.id}.yaml`（见 `ui:reference` → naming.md）
3. 选定对应 [Page Pattern](../ui-reference/references/patterns/)
4. 对照本目录 [references/](references/) 中的模板起步

## 写作红线

| 禁止 | 说明 |
|------|------|
| Admin 内再套 `Layout+Sider` | Application Shell 已有导航 |
| 用 `style` 代替 Grid/Flex | 优先语义布局 Props |
| `type` 与 `component` 同节点 | 节点形状互斥 |
| 未声明的 `component` 引用 | 须在 `components` 定义 |
| 顶层 `layout:` 字段 | 布局只在 UI 树内表达 |

## Pattern 速查

| 类型 | 参考 |
|------|------|
| 列表 | [list-page.md](../ui-reference/references/patterns/list-page.md) |
| 列表+抽屉 | [scenarios.md](references/scenarios.md) → list-drawer |
| 详情 | [detail-page.md](../ui-reference/references/patterns/detail-page.md) |
| 表单 | [form-page.md](../ui-reference/references/patterns/form-page.md) |
| 看板 | [dashboard-page.md](../ui-reference/references/patterns/dashboard-page.md) |
| 主从 | [master-detail.md](../ui-reference/references/patterns/master-detail.md) |
| 工作区/菜单布局 | [workspace-page.md](../ui-reference/references/patterns/workspace-page.md) |
| 弹层 | [overlay.md](../ui-reference/references/behavior/overlay.md) |
| 页面跳转 | [navigation.md](../ui-reference/references/behavior/navigation.md) |

## 本目录模板

场景索引 → **[scenarios.md](references/scenarios.md)**。

| 文件 | 用途 |
|------|------|
| [page-template-list.yaml](references/page-template-list.yaml) | 列表 + 搜索 + navigate 详情 |
| [page-template-list-drawer.yaml](references/page-template-list-drawer.yaml) | 列表 + 侧滑抽屉编辑 |
| [page-template-edit-drawer.yaml](references/page-template-edit-drawer.yaml) | 抽屉内编辑页 |
| [page-template-form.yaml](references/page-template-form.yaml) | 整页表单 |
| [page-template-detail.yaml](references/page-template-detail.yaml) | 详情 + 路由参数 |
| [page-template-master-detail.yaml](references/page-template-master-detail.yaml) | 主从分栏 |
| [page-template-menu-layout.yaml](references/page-template-menu-layout.yaml) | blank + 页内 Menu |
| [page-template-minimal.yaml](references/page-template-minimal.yaml) | 最小页面 |
| [page-template-fragment.yaml](references/page-template-fragment.yaml) | 片段 |

详见 [examples.md](references/examples.md)。

## 出口

1. 运行脚本：`skills/ui/ui-check/scripts/validate.sh --check-filename <your-page>.yaml`
2. 通过后交 `ui:check` 做 L3–L4 评审

规范细节 → `ui:reference`。
