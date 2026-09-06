# 规范检查流程

`ui:check` 对照 Pactor Page DSL 1.0 **规范**审查 YAML。

## 0. 自动脚本校验（L0–L2，必须先执行）

```bash
skills/ui/ui-check/scripts/validate.sh --check-filename path/to/page.yaml
```

- 脚本说明：[scripts/README.md](../scripts/README.md)
- Schema：[pactor-page-dsl.schema.yaml](../../ui-reference/references/pactor-page-dsl.schema.yaml)
- **退出码非 0 → 直接判定不通过**，不继续 L3–L4

模板文件（`page-template-*.yaml`）的 `page.id` 与文件名故意不一致，批量验模板时**省略** `--check-filename`：

```bash
skills/ui/ui-check/scripts/validate.sh skills/ui/ui-page/references/
```

## 准备（L3–L4）

1. 确认脚本已通过
2. 打开 [validation-rules.md](validation-rules.md)、[review-checklist.md](review-checklist.md)

## L0：文档与命名

> L0 部分项由脚本 `--check-filename` 自动检查。

- [ ] `dsl: '1.0'`
- [ ] `page.id` 存在、非空、kebab-case
- [ ] 文件名 `{page.id}.yaml`（生产页面；模板可例外）
- [ ] 目标路径形态 `ui-pages/{moduleKey}/{page.id}.yaml`

## L1：Schema 形状

> **由 `validate-page-dsl.py` 自动执行**（JSON Schema Draft 2020-12）。

人工复核仅在脚本报错时对照 schema 修正。规则摘要：

- [ ] 仅使用 schema 允许的顶层字段
- [ ] 节点为 `type` / `component` / `source` 三种形状之一，不混用
- [ ] `permission` / `lifecycle` / `dataSources` 形态合法

## L2：交叉引用（结构闭合）

> **由 `validate-page-dsl.py` 自动执行**。

- [ ] 每个 `reload.params.dataSource` 在 `dataSources` 中存在
- [ ] 每个 `component:` 在 `components` 中有定义
- [ ] `ComponentNode` 均有 `type`
- [ ] 同作用域 `action.id` 不重复

规则明细 → [validation-rules.md](validation-rules.md)。

## L3：布局与 Page Pattern

对照 `ui:reference` → [structure/layout.md](../../ui-reference/references/structure/layout.md) 与 [patterns/](../../ui-reference/references/patterns/)：

- [ ] 普通业务页 `body` 根为 `Page`
- [ ] 未在 Application Shell 内再套完整 `Layout + Sider`
- [ ] 工具栏 → `Flex`；指标横排 → `Grid`；业务块 → `Card`
- [ ] 页面类型与 pattern 一致（如 list 页符合 list-page 结构）
- [ ] 优先布局 Props，非必要不用 `style` 排版
- [ ] overlay：`overlay.content` 与 `overlay.page` 二选一（若使用）
- [ ] navigate：`params.to` 非空（若使用）；详情页通过 `route.params` 取 id
- [ ] Form 字段用 `name`；Table 绑定 `${data.xxx}`

对照场景模板 → [scenarios.md](../../ui-page/references/scenarios.md)。

## L4：评审清单

逐项过 [review-checklist.md](review-checklist.md)。

## 结论判定

| 结论 | 条件 |
|------|------|
| **通过** | L0–L2 无阻塞项；L3–L4 无严重偏离 |
| **有条件通过** | L0–L2 通过；L3–L4 有警告但可接受 |
| **不通过** | L0–L2 任一项违规 |

## 遗留项

以下写入报告「遗留项」，**不影响** L0–L2 结论：

- 组件 `type` 是否在宿主 registry
- `service` 名是否已注册
- 表达式运行时语义
- 权限码是否在 IAM
