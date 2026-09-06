# 模板与示例（技能内自包含）

本目录提供可复制的 Page DSL 模板，**不依赖仓库内其他模块路径**。

## 场景索引

完整场景对照表 → **[scenarios.md](scenarios.md)**。

| 文件 | 场景 |
|------|------|
| [page-template-minimal.yaml](page-template-minimal.yaml) | 最小页 |
| [page-template-list.yaml](page-template-list.yaml) | 列表：搜索表单、Table、`navigate` 详情/新建、删除确认 |
| [page-template-list-drawer.yaml](page-template-list-drawer.yaml) | 列表 + 侧滑抽屉编辑（配合 edit-drawer） |
| [page-template-edit-drawer.yaml](page-template-edit-drawer.yaml) | 抽屉内编辑页（`overlay.page.pageId` 引用） |
| [page-template-form.yaml](page-template-form.yaml) | 整页表单：校验、级联 Select、提交后跳转 |
| [page-template-detail.yaml](page-template-detail.yaml) | 详情：`route.params`、加载态、返回/编辑 |
| [page-template-master-detail.yaml](page-template-master-detail.yaml) | 主从：左列表右详情 |
| [page-template-menu-layout.yaml](page-template-menu-layout.yaml) | blank 外壳：Layout + Sider + Menu + 内容区 |
| [page-template-fragment.yaml](page-template-fragment.yaml) | 根级 `children` 片段 |

## 使用方式

1. 从 [scenarios.md](scenarios.md) 选定场景
2. 复制模板，替换 `page.id`、业务域名、`service` 名
3. 按 `ui:reference` → `patterns/`、`behavior/` 调整
4. 运行 `validate.sh` 后交 `ui:check` 审查

## 规范要点（模板已遵循）

- Input/Select 字段名：`name`（非 `field`）
- Table 数据：`dataSource: ${data.xxx}`
- 提交按钮：`submit: true`
- Form 提交值：`${form}` / `${event}`（onSubmit）
- admin 内页根节点：`Page`（非 `Layout+Sider`）

## 结构模式与布局

- [structure/layout.md](../../ui-reference/references/structure/layout.md)
- [behavior/overlay.md](../../ui-reference/references/behavior/overlay.md)
- [behavior/navigation.md](../../ui-reference/references/behavior/navigation.md)
- [patterns/](../../ui-reference/references/patterns/)
