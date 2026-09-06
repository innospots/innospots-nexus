# Page DSL 校验脚本

对 Pactor Page DSL YAML 执行 **L0–L2** 自动校验（不依赖 Maven / Java）。

## 依赖

- Python 3.10+
- `PyYAML`、`jsonschema`（`validate.sh` 首次运行自动创建 `.venv` 并安装）

手动安装：

```bash
pip install -r skills/ui/ui-check/scripts/requirements.txt
```

## 用法

```bash
# 单文件
skills/ui/ui-check/scripts/validate.sh path/to/page.yaml

# 目录（递归 *.yaml / *.yml）
skills/ui/ui-check/scripts/validate.sh skills/ui/ui-page/references/

# L0：文件名须与 page.id 一致
skills/ui/ui-check/scripts/validate.sh --check-filename path/to/customer-list.yaml

# 指定 schema
skills/ui/ui-check/scripts/validate.sh \
  --schema skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml \
  path/to/page.yaml

# JSON 输出（单文件）
skills/ui/ui-check/scripts/validate.sh --json path/to/page.yaml

# 直接调用 Python
python3 skills/ui/ui-check/scripts/validate-page-dsl.py path/to/page.yaml
```

## 校验层

| 层 | 实现 | 阻塞 |
|----|------|------|
| L0 | `--check-filename`、YAML 解析 | 是 |
| L1 | JSON Schema（`pactor-page-dsl.schema.yaml`） | 是 |
| L2 | 交叉引用（dataSource、component、action id） | 是 |
| L3–L4 | 人工 / Agent 按 review-checklist | 否 |

L1 失败时跳过 L2（文档结构无效）。

## Schema 路径

唯一权威源：

```text
skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml
```

## 退出码

| 码 | 含义 |
|----|------|
| 0 | 全部通过 |
| 1 | 存在校验错误 |
| 2 | 环境或参数错误 |

## ui:check 集成

`ui:check` 执行规范审查时**必须先运行本脚本**：

```bash
skills/ui/ui-check/scripts/validate.sh --check-filename <yaml-files>
```

脚本通过后，再按 [check-procedure.md](../references/check-procedure.md) 做 L3–L4 人工/Agent 评审。

## 示例输出

```text
PASS  skills/ui/ui-page/references/page-template-list.yaml
```

```text
FAIL  broken.yaml
  [L1] body/children[0]: 'type' is a required property
  [L2] body.children[0]: unknown component reference: missingForm
  (2 issue(s): L1=1, L2=1)
```
