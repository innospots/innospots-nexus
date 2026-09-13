# java:check — Java 质量检查

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:check` 是所有 Java 工作的**统一验证出口**：编译、测试、POM 核验、规范合规、依赖卫生、安全与性能风险巡检。

它在 `java:develop`、`java:design`、两个升级技能完成之后执行，产出结构化检查报告，**不用于方案辩论或临时改设计**。

## 怎么用

1. **按层执行**（前一层失败不继续）：
   - L0 `mvn clean compile`
   - L1 `mvn validate`
   - L2 `mvn test`
   - L3 `mvn -q help:effective-pom`
   - L4 `git diff --check`
   - L5 规范 / 依赖 / 安全 / 性能清单
2. **对照清单** — [review-checklist.md](references/review-checklist.md) + `java:reference` → [quick-constraints.md](../java-reference/references/quick-constraints.md)。
3. **汇总报告** — 按 SKILL 中的「输出报告格式」填写结论、执行结果、问题清单。

向代理说明：本次变更范围、是否只做聚焦检查还是全量、已知环境问题（JDK 版本等）。

## 输入

| 类型 | 示例 |
|------|------|
| 待验证变更 | 分支 diff、模块列表、PR 范围 |
| 上游技能产物 | develop 实现、upgrade 后的 BOM、project 新模块 |
| 基线（升级场景） | 升级前的 `dependency:tree`、测试结果 |
| 检查深度 | 仅 L0–L2 或含 L5 全量评审 |

## 输出

| 类型 | 内容 |
|------|------|
| 检查结论 | 通过 / 有条件通过 / 不通过 |
| 命令结果表 | L0–L4 各层 ✅/❌ |
| 问题清单 | 级别（阻塞/警告/提示）、`path:line`、建议修正 |
| 遗留项 | 需开发者决策的未决问题 |

**不产出**：新功能代码、设计变更、为通过检查而下调 enforcer 或削弱测试断言。

## 适用场景

- 功能开发或 Bug 修复交付前
- 依赖或工程版本升级之后回归
- 代码评审、合入前核验
- 排查依赖冲突、规范违反、安全泄露风险
- 建立升级前基线（`dependency:tree`）

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 补写业务实现 | `java:develop` |
| 重新做架构方案 | `java:design` + `grill-me` |
| 执行 BOM 或 revision 升级 | `java:dependency-upgrade` / `java:project-upgrade` |
| 把阻塞项降级为警告「先过一版」 | 禁止 |
| 本地 JDK 过旧时下调 release | 应报告环境不匹配 |

`versions:display-*` 仅列候选；实际升级交给对应升级技能。

## 与上下游技能

```text
java:develop | java:project-upgrade | java:dependency-upgrade
    ↓
java:check（L0 → L5）
    ↓
通过 → 可合入 / 发版
不通过 → 回到对应技能修复
```

## 详细参考

- [verification-commands.md](references/verification-commands.md) — 命令矩阵与判读
- [review-checklist.md](references/review-checklist.md) — 分组评审清单
- [quick-constraints.md](../java-reference/references/quick-constraints.md) — 硬性红线
