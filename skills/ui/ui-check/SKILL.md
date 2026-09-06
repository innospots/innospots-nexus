---
name: ui:check
display_name: Page DSL 规范检查
description: |
  检查 Pactor Page DSL 页面 YAML 是否符合规范：JSON Schema 形状、交叉引用、
  布局与页面模式、命名约定。通过 validate-page-dsl.py 脚本自动校验 L0–L2。
  触发词：检查页面 DSL、验证 YAML、规范检查、DSL 评审、schema 校验。
category: ui
version: 3.0.0
---

# Page DSL 规范检查

## 定位

`ui:page` 产出的 YAML **规范符合性**统一出口。

检查对象是 **Page DSL 文档本身**，不是 Java 源码。核心校验通过 **脚本** 执行，不依赖 `mvn test`。

## 第一步：运行校验脚本（L0–L2，阻塞）

```bash
skills/ui/ui-check/scripts/validate.sh --check-filename path/to/page.yaml

# 批量
skills/ui/ui-check/scripts/validate.sh --check-filename skills/ui/ui-page/references/
```

脚本说明 → [scripts/README.md](scripts/README.md)

| 层 | 脚本实现 |
|----|----------|
| L0 | YAML 解析、`page.id` kebab-case、`--check-filename` |
| L1 | [pactor-page-dsl.schema.yaml](../ui-reference/references/pactor-page-dsl.schema.yaml) JSON Schema |
| L2 | dataSource / component / action.id 交叉引用 |

Schema 说明 → [schema.md](../ui-reference/references/schema.md)

**脚本失败 = 不通过**，不继续做 L3–L4 宣称通过。

## 第二步：评审清单（L3–L4，警告）

脚本通过后，按人工/Agent 清单审查：

- [check-procedure.md](references/check-procedure.md)
- [review-checklist.md](references/review-checklist.md)
- [validation-rules.md](references/validation-rules.md)

## 检查分层

```text
L0  文档与命名      ← 脚本（--check-filename）
L1  Schema 形状     ← 脚本（JSON Schema）
L2  交叉引用        ← 脚本（交叉引用规则）
L3  布局与模式      ← 评审清单
L4  质量与可维护性  ← 评审清单
```

## 规范依据

| 资源 | 路径 |
|------|------|
| JSON Schema | `ui-reference/references/pactor-page-dsl.schema.yaml` |
| 校验脚本 | `ui-check/scripts/validate-page-dsl.py` |
| 行为/布局规范 | `ui-reference/references/` |

## 不属于本技能（遗留项）

- 组件 `type` 是否在 registry 注册
- `service` 是否在后端存在
- 表达式求值、权限码 IAM

## 输出报告格式

```markdown
## Page DSL 检查结论

通过 / 有条件通过 / 不通过

## 脚本校验（L0–L2）

\`\`\`bash
skills/ui/ui-check/scripts/validate.sh --check-filename <file>
\`\`\`

结果：PASS / FAIL（附脚本输出）

## 评审（L3–L4）

| 层 | 结果 |
|----|------|
| L3 布局与模式 | ✅ / ⚠️ |
| L4 评审清单 | ✅ / ⚠️ |

## 问题清单

| 级别 | 位置 | 问题 | 建议 |
|------|------|------|------|

## 遗留项（运行时）
```

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 跳过脚本直接宣称 L1/L2 通过 | 必须先 `validate.sh` |
| 用 Maven/Java 测试代替 schema 校验 | 脚本 + 评审清单 |
| 把运行时未注册项当作规范通过 | 写入遗留项 |

## 详细参考

- [scripts/README.md](scripts/README.md) — 脚本用法
- [schema.md](../ui-reference/references/schema.md) — Schema 结构说明
- [check-procedure.md](references/check-procedure.md) — 完整流程
