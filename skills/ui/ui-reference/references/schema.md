# Page DSL Schema（JSON Schema）

Pactor Page DSL 1.0 的**机器可读约束**定义，供 `ui:check` 脚本与工具链校验 YAML。

## 权威文件

**唯一源**（`ui:reference` 内自包含）：

```text
skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml
```

修改 schema 时**只改此文件**，无需同步其他模块副本。

## 格式

- **标准**：JSON Schema [Draft 2020-12](https://json-schema.org/draft/2020-12/schema)
- **编码**：YAML（`$schema` / `$id` / `$defs` 结构）
- **校验器**：`jsonschema`（Python）或任意 Draft 2020-12 兼容实现

```yaml
$schema: "https://json-schema.org/draft/2020-12/schema"
$id: "https://pactor.dev/schema/page-dsl-1.0.schema.yaml"
title: Pactor Page DSL
type: object
required: [dsl, page]
additionalProperties: false
```

## 顶层结构

| 字段 | Schema | 说明 |
|------|--------|------|
| `dsl` | `const: "1.0"` | 规范版本，必填 |
| `page` | `$ref: page` | 页面元信息，必填 |
| `requires` | object | 能力声明 |
| `meta` | object | 扩展元数据（运行时不消费） |
| `state` | object | 初始状态 |
| `dataSources` | object | 命名数据源 |
| `actions` | object | 命名动作 |
| `components` | object | 命名 UI 子树 |
| `lifecycle` | object | 生命周期钩子 |
| `body` | `$ref: node` | UI 树根（推荐） |
| `children` | `$ref: children` | 片段根 |

`additionalProperties: false` — 未知顶层字段**校验失败**。

## 核心 $defs

| 定义 | 约束 |
|------|------|
| `node` | `type` 与 `component` **互斥**（oneOf） |
| `renderable` | `node` \| `sourceRef`（动态 DSL） |
| `children` | 数组 \| `sourceRef` |
| `action` / `actionOrList` | 动作形状 |
| `dataSource` | static \| service \| http \| computed \| resource |
| `permission` | string \| array \| `{ code, denied }` |
| `componentNode.type` | `^[A-Z][A-Za-z0-9._-]*$` |

## Schema 校验范围

### 覆盖（L1）

- 字段存在性与类型
- 必填字段（如 `page.id`、`static.value`、`service.service`）
- 节点 union（不能同时有 `type` + `component`）
- `permission` / `expressionOrBoolean` 形态
- HTTP `request.method` 枚举（在 schema 层）

### 不覆盖（L2 脚本 / 运行时）

| 项 | 处理 |
|----|------|
| `reload.params.dataSource` 引用闭合 | `validate-page-dsl.py` L2 |
| `component:` 引用存在于 `components` | L2 |
| `action.id` 唯一 | L2 |
| 组件 `type` 是否注册 | 运行时遗留项 |
| `service` 是否存在 | 运行时遗留项 |
| 组件 `props` 契约 | Component Registry（非 core schema） |

Core schema **故意不校验**各组件特有 `props`（见 schema 内 `componentNode.props` 注释）。

## 与 Pactor 官方文档

人类可读说明：[Pactor DSL 参考](https://pactor-docs.xp-java.workers.dev/reference/dsl/)

| 文档 | 受众 |
|------|------|
| Pactor 官方参考 | 语义、props、交互 |
| 本 schema | 结构形状、机器校验 |
| [validation-rules.md](../../ui-check/references/validation-rules.md) | L2 交叉引用规则摘要 |

## 校验命令

```bash
skills/ui/ui-check/scripts/validate.sh path/to/page.yaml
skills/ui/ui-check/scripts/validate.sh --check-filename path/to/customer-list.yaml
skills/ui/ui-check/scripts/validate.sh --json path/to/page.yaml
```

详见 [ui-check/scripts/README.md](../../ui-check/scripts/README.md)。
