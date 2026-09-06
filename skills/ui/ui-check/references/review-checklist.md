# Page DSL 评审清单

规范硬性规则见 [validation-rules.md](validation-rules.md)。本清单覆盖布局、模式与可维护性。

## 结构与布局

- [ ] `body` 根节点为 `Page`（普通业务页）
- [ ] 未在 Application Shell 内再套 `Layout + Sider`（见 [layout-components.md](../../ui-reference/references/structure/layout-components.md)）
- [ ] 若使用 Layout 族：`Content` 承担主区，`Sider` 有 `collapsed` + `onCollapse` 闭环
- [ ] 工具栏用 `Flex`，指标卡用 `Grid`，业务块用 `Card`
- [ ] 未滥用 `style` 实现本可用 Grid/Flex Props 表达的布局
- [ ] 布局符合 `ui:reference` → [patterns/](../../ui-reference/references/patterns/) 中对齐模式

## 命名

- [ ] `page.id` kebab-case，与 `{id}.yaml` 一致
- [ ] `page.name`（若有）camelCase
- [ ] 路径形态 `ui-pages/{moduleKey}/{page.id}.yaml`
- [ ] `actions` 命名：动词 + 领域（`search`、`resetSearch`）
- [ ] `components` 命名：名词短语（`searchForm`）

## 数据与状态

- [ ] `state` 含页面所需初始值（分页、筛选、选中等）
- [ ] 业务列表/详情优先 `service` 数据源
- [ ] `static` 仅用于固定选项
- [ ] `autoLoad` 与 `lifecycle.onLoad` 不重复加载

## 行为

- [ ] `reload.params.dataSource` 均已定义
- [ ] `call.params.name` 指向已命名 action
- [ ] `navigate.params.to` 非空（若使用）
- [ ] `overlay.content` 与 `overlay.page` 二选一（若使用 overlay）
- [ ] 多步 action 顺序合理

## UI 树

- [ ] `components` 仅含真正复用片段
- [ ] `component:` 引用均有定义
- [ ] 节点层级不过深（>4 层考虑拆 `components`）
- [ ] `when` / `condition` 为布尔或表达式字符串
- [ ] 常用组件 props 符合 [component-catalog.md](../../ui-reference/references/structure/component-catalog.md)（如 Form 用 `name` 非 `field`，Flex 用 `horizontal`/`vertical`）

## 权限

- [ ] 页面级 `permission`（若需要）
- [ ] 删除/导出等危险操作有 action 或节点级 permission

## 严格形状

- [ ] 无拼写错误的字段名
- [ ] 无遗留 ui-spec 字段
- [ ] 无 `type` + `component` 同节点

## 遗留项（不阻塞规范通过）

- [ ] `service` 名在宿主后端已注册（运行时）
- [ ] 组件 `type` 在宿主 registry（运行时）
- [ ] 权限码在 IAM（产品）
