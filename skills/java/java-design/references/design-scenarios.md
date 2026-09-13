# 设计场景与产出级别

按常见场景选择 **L0–L3 产出形态** 与四步法侧重点。门禁逐步见
[design-four-steps.md](design-four-steps.md)。

---

## 场景速查

| 场景 | 建议级别 | grill-me | 四步法侧重 | 下游 |
|------|---------|----------|-----------|------|
| 单端点字段 / 行为澄清 | **L0** | 可跳过 | ②③ 简写；④ 增量契约 | develop |
| 模块内新领域 | **L1** | 必经 | ①–④ 完整 | develop |
| 模块内大领域扩展（多表、多端点） | **L1** | 必经 | ③ 功能子模块；④ 完整 | develop |
| 跨模块 / 新中间件 / 存储大改 | **L2** | 必经 | ① + architecture-decision；十二节文档 | develop（+ project 若需） |
| 单项技术选型 | **L3 ADR** | 按需 | 可并入 L2 §12 或独立 ADR | 视决策 |
| 需新建 Maven 模块 | **L1/L2** + | 必经 | ① 判定模块类型；**project** | project → develop |
| Console 插件贡献 | **L1/L2** | 必经 | ① plugin vs console；见 plugin 设计 doc | develop |
| 破坏公共兼容面 | **L2** | 必经 | ④ + §9 迁移 | develop |
| 仅改设计文档 / 无契约变 | — | 否 | 更新既有 `*-design.md` | 无 develop |

---

## 新领域（L1）最小设计面

文档或 L0 块须包含：

- 归属模块 + 领域包名
- 词汇表（5～15 行）
- 包树（领域优先，初始最小面）
- 端点表 + record 骨架
- 表 / Dao 清单（无 join）
- 状态码矩阵（或「无新增失败」）
- 测试类清单（契约 + 行为）

存放：`<module>/docs/<domain>-design.md`。

---

## 扩域 / 加功能（L0 或 L1）

| 条件 | 级别 |
|------|------|
| 无新状态码、无新表、无兼容面 | **L0** PR 块 |
| 新端点 / 新字段 / 新拒绝路径 | **L0** 或修订 L1 文档章节 |
| 新表 / 新状态码 / 新事件 | **L1** 文档增补 |

---

## 破坏性变更（L2）

触发 [architecture-decision.md](architecture-decision.md)「必须先出方案」任一条时：

- 独立 `docs/design/` 或 `docs/superpowers/specs/YYYY-MM-DD-*.md`
- 必须 §9 兼容与迁移
- 状态「已接受」后再 develop

---

## 插件与 Console

| 能力 | 设计归属文档 |
|------|-------------|
| 插件运行时、Page DSL、安装 | `innospots-nexus-plugin/docs/plugin/design/` |
| Catalog 索引、管理台 REST 契约 | console 域 + `docs/design/` 或 console `docs/` |
| 租户侧用户/权限实现 | kernel |

设计阶段须写明：**贡献声明** vs **持久化索引** vs **kernel 业务** 分工，禁止混在单一「菜单」概念下。

---

## 与 project / develop 的分流

```text
design 结论：仅新 Java 包     → java:develop
design 结论：新 Maven 模块     → java:project → java:develop
design 结论：仅 yaml 配置键    → develop（无新模块）
```

详见 [design-deliverables.md](design-deliverables.md) 交接节。
