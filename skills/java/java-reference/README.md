# java:reference — Java 规范总索引

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:reference` 是 `innospots-nexus` Java 技能体系的**规范中枢**：索引权威条文、专题参考、硬性红线与八个 `java:*` 技能的路由表。

它不直接写代码，也不替代设计或实现技能；其他技能通过链接本目录消费规范，避免在各自正文中重复条文。

## 怎么用

1. **查规范** — 从 [standards-index.md](references/standards-index.md) 或下方「权威原文」定位章节，再打开 `standards/` 原文。
2. **不确定走哪个技能** — 看 [SKILL.md](./SKILL.md) 中的技能路由表，或顶层 [skills/java/README.md](../README.md)。
3. **动手前压力测试** — 重大方案走 [grill-me.md](references/grill-me.md)（跨技能，不产出代码）。
4. **查模块公共 API** — 见 `references/modules/<artifact-id>/README.md`（仅索引，非技能）。

代理或 IDE 中引用本技能时，通常附带具体问题（如「命名 record 怎么写」「异常该放哪一层」）。

## 输入

| 类型 | 示例 |
|------|------|
| 规范类问题 | 「DAO 能不能 join」「状态码格式」「public 类注释要什么」 |
| 路由类问题 | 「新建模块该用哪个技能」「升 Spring 还是升 revision」 |
| 专题上下文 | 业务域名、模块名、已有设计结论（用于查归属与包结构） |
| 显式扫描请求 | 「扫描 innospots-nexus-base 更新模块 API 索引」（少见，需开发者明确要求） |

## 输出

| 类型 | 内容 |
|------|------|
| 规范答复 | 指向 `standards/*.md` 或 `references/*.md` 的具体章节与要点摘要 |
| 路由建议 | 应使用的 `java:*` 技能及先后顺序 |
| 红线速查 | [quick-constraints.md](references/quick-constraints.md) 中与场景相关的条目 |
| 冲突裁决 | 按 SKILL 中「冲突裁决顺序」说明以哪份文档为准 |
| 模块 API 索引 | 仅在显式扫描后更新 `references/modules/`（非常规开发输出） |

**不产出**：可编译源码、POM 改动、测试代码、架构方案正文。

## 适用场景

- 编码前确认命名、异常、分层、注释等约定
- 设计或实现过程中「这条规则在哪份文档里」
- 评审时核对是否违反硬性红线
- 新成员了解规范体系与技能分工
- 重大决策前配合 `grill-me` 澄清假设

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 写/改 Java 实现 | `java:develop` |
| 产出架构或 API 设计方案 | `java:design` |
| 新建模块、改 POM、调构建 | `java:project` |
| 跑编译测试、出检查报告 | `java:check` |
| Spring 专项集成与迁移知识 | `java:spring` |
| 升工程 `${revision}` | `java:project-upgrade` |
| 升 JDK / 第三方依赖 / 框架 | `java:dependency-upgrade` |
| 随普通代码改动自动刷新模块 API 文档 | 禁止；仅显式扫描请求 |

疑义一律以 `standards/` 原文为准，不以技能摘要代替条文。

## 与上下游技能

```text
（可选）grill-me
    ↓
java:reference  ← 查规范 / 路由
    ↓
java:design | java:project | java:develop | java:spring | 升级技能
    ↓
java:check
```

## 详细参考

| 类型 | 路径 |
|------|------|
| 权威原文（7 份） | [standards/](standards/) |
| 硬性红线 | [quick-constraints.md](references/quick-constraints.md) |
| 规范章节地图 | [standards-index.md](references/standards-index.md) |
| 模块归属 | [module-ownership.md](references/module-ownership.md) |
| 包结构 | [package-structure.md](references/package-structure.md) |
| 持久化与配置 | [persistence-config.md](references/persistence-config.md) |
| 测试规范路由 | [testing-index.md](references/testing-index.md) |
| 方案压力测试 | [grill-me.md](references/grill-me.md) |
| 仓库模块职责 | 根目录 [AGENTS.md](../../../AGENTS.md) |
| 技能体系总览 | [skills/java/README.md](../README.md) |
