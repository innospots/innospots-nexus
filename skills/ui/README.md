# UI 技能体系（Pactor Page DSL）

对照 [Pactor DSL 参考](https://pactor-docs.xp-java.workers.dev/reference/dsl/)。覆盖度审查 → [COVERAGE.md](COVERAGE.md)。

## 技能一览

| 技能 | 目录 | 定位 |
|------|------|------|
| `ui:reference` | `ui-reference/` | 规范库：结构、布局、行为、模式、组件目录 |
| `ui:page` | `ui-page/` | 编写 `ui-pages/**/*.yaml`；[场景模板](ui-page/references/scenarios.md) |
| `ui:check` | `ui-check/` | YAML 规范审查；**脚本** `validate-page-dsl.py`（L0–L2） |

## 典型工作流

```text
ui:reference（选 pattern + 行为）
    ↓
ui:page（从 scenarios.md 选模板）
    ↓
ui:check（validate.sh L0–L2 → 评审 L3–L4）
```

## Schema 与脚本校验

| 资源 | 路径 |
|------|------|
| JSON Schema | `ui-reference/references/pactor-page-dsl.schema.yaml` |
| Schema 说明 | `ui-reference/references/schema.md` |
| 校验脚本 | `ui-check/scripts/validate-page-dsl.py` |

```bash
skills/ui/ui-check/scripts/validate.sh --check-filename path/to/customer-list.yaml
```

## 常用场景模板

| 场景 | 模板 |
|------|------|
| 列表 + 搜索 + 跳转 | `page-template-list.yaml` |
| 列表 + 侧滑编辑 | `list-drawer` + `edit-drawer` |
| 整页表单 | `page-template-form.yaml` |
| 详情 + 路由参数 | `page-template-detail.yaml` |
| 主从分栏 | `page-template-master-detail.yaml` |
| blank 页内菜单 | `page-template-menu-layout.yaml` |

完整索引 → [ui-page/references/scenarios.md](ui-page/references/scenarios.md)。

## 规范文档地图

```text
structure/          文档结构、节点、组件、布局
behavior/           state、数据源、动作、弹层、导航
patterns/           列表/表单/详情/主从等组合模式
security/           权限
ui-page/references/ 可复制 YAML 模板
```

## 规范边界

- **PageDsl 定义**：`body` 结构、`actions`（含 overlay/navigate）、`dataSources`
- **不在 PageDsl**：Shell 菜单、路由表、Action/Component 注册（见 `navigation.md`）

## 与 Java 技能

| 任务 | 技能 |
|------|------|
| 改页面 YAML | `ui:page` + `ui:check` |
| 改 ui.spec Java | `java:develop` + `java:test` |
