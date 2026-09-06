# skills/ui 覆盖度与缺口审查

对照 [Pactor DSL 参考](https://pactor-docs.xp-java.workers.dev/reference/dsl/)，截至本审查的技能体系状态。

## 技能分工（完整）

| 技能 | 职责 | 状态 |
|------|------|------|
| `ui:reference` | 只读规范库 | ✅ 结构/行为/模式/组件目录已覆盖 |
| `ui:page` | 编写 YAML | ✅ 模板已扩展至常用场景 |
| `ui:check` | YAML 规范审查 | ✅ 脚本 L0–L2 + 评审 L3–L4 |

## 规范域覆盖矩阵

| Pactor 域 | 规范文档 | 模板示例 | 缺口/说明 |
|-----------|----------|----------|-----------|
| 顶层 PageDsl | `structure/page.md` | minimal | — |
| 节点 DslNode | `structure/node.md` | 各模板 | — |
| components 复用 | `structure/component.md` | list, list-drawer | — |
| 内置组件 props | `structure/component-catalog.md` | 各模板 | `Chat` 等扩展组件仅索引 |
| Layout 族 | `structure/layout-components.md` | menu-layout | — |
| 布局选用 | `structure/layout.md` | 各 pattern | — |
| state | `behavior/state.md` | 全部 | — |
| dataSources | `behavior/datasource.md` | list, detail | `computed`/`resource` 协议保留、未实现标注 |
| actions | `behavior/action.md` | 全部 | `request`/`sequence` 示例偏少 |
| 表达式作用域 | `behavior/expression.md` | detail, drawer | — |
| lifecycle | `behavior/lifecycle.md` | list | — |
| permission | `security/permission.md` | list | — |
| **弹层 overlay** | `behavior/overlay.md` | list-drawer, edit-drawer | — |
| **页面导航** | `behavior/navigation.md` | detail, menu-layout | 路由表不在 PageDsl |
| 列表页 pattern | `patterns/list-page.md` | list, list-drawer | — |
| 表单页 pattern | `patterns/form-page.md` | form | 已修正 props |
| 详情页 pattern | `patterns/detail-page.md` | detail | 已去掉未内置 `Descriptions` |
| 主从 pattern | `patterns/master-detail.md` | master-detail | — |
| 看板/工作区 | `patterns/dashboard-page.md` 等 | — | 无独立 YAML 模板（pattern 文档足够） |
| **JSON Schema** | `schema.md` + `pactor-page-dsl.schema.yaml` | 全部模板 | `validate-page-dsl.py` 自动校验 |

## Schema 与脚本

| 文件 | 说明 |
|------|------|
| `ui-reference/references/pactor-page-dsl.schema.yaml` | **唯一** JSON Schema 权威源 |
| `ui-reference/references/schema.md` | Schema 结构说明 |
| `ui-check/scripts/validate-page-dsl.py` | L0–L2 自动校验 |
| `ui-check/scripts/validate.sh` | 一键运行（自动 venv） |

## 明确不属于 PageDsl（规范已标注）

| 能力 | 定义位置 | 技能内说明 |
|------|----------|------------|
| Application Shell / 侧栏菜单 | Bootstrap `layout.type` | `layout-components.md`, `navigation.md` |
| 菜单项 → pageId 绑定 | 宿主菜单注册 | `navigation.md` |
| 路由表 path → pageId | 宿主路由配置 | `navigation.md` |
| Action / Component Registry | 运行时注册 | `ui:check` 遗留项 |
| `navigate` / `overlay.*` / `message` | 应用层 action | `action.md`（非 schema 枚举） |

## 已修复的规范偏差

| 问题 | 修正 |
|------|------|
| Input 使用 `field` | 统一为 `name`（Pactor Form 字段名） |
| Button `htmlType: submit` | 改为 `submit: true` |
| Form `model: ${state.form}` | 删除；Form 提交写入 `${form.}` / `${state.form.}` |
| Table `dataSource: items` | 改为 `${data.items}` |
| 详情页 `Descriptions` 组件 | 改用 `Statistic` / `Text`（内置目录无 Descriptions） |
| list 模板 `onLoad` + `autoLoad` 重复 | 仅保留 `autoLoad: true` |

## 场景 → 模板索引

见 [ui-page/references/scenarios.md](ui-page/references/scenarios.md)。

## 后续可增强（非阻塞）

- `request` + `confirm` + `if` 完整删除链路示例
- `DslSourceRef` 动态片段独立模板
- `dashboard-page` 独立 YAML 模板
