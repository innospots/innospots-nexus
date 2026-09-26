# 设计：innospots-nexus-sample 平台扩展

设计产出仍遵循 [design-four-steps.md](design-four-steps.md) 与
[design-deliverables.md](design-deliverables.md)。本文仅补充 **sample 扩展库** 在步骤 1、3、4 的判定与契约。

**工程与包树权威：** [sample-extension-layout.md](../../java-reference/references/sample-extension-layout.md)。

**勿误解：** sample 仓库内 **同时存在** 多种可运行子模块是为对照与测试；设计新能力时先定最小模块集（§1.1、§2.2），**不要**把「sample 里有的模块」当作默认清单。

---

## 步骤 1 定归属（扩展场景）

在核对 [`AGENTS.md`](../../../../AGENTS.md) 平台/portal 职责后，追加：

| 问题 | 若「是」 | 若「否」 |
|------|----------|----------|
| 能力是否已是 platform 产品域（租户、企业、平台用户…）？ | 设计在 `innospots-nexus-platform`，**不要**放 `sample-platform` | 可放在 `innospots-nexus-sample-platform` 或外部产品模块 |
| 是否需要租户侧用户/权限/菜单？ | 归属 `portal` 或外部产品 portal 模块 | 继续 platform 扩展 |
| 是否仅需演示「运营域增量」？ | `innospots-nexus-sample-platform` + 运行模块装配 | — |

**Maven：** 扩展逻辑默认 **一个** `innospots-nexus-sample-platform` JAR；不为每个交付面再拆 Maven 模块，除非 grill-me 证明独立发布边界。

---

## 步骤 3 划边界（交付面）

对每个领域画三张切片（可写在 L1/L2 设计文档）：

```text
core.<domain>     — 数据与编排真源
console.<domain>  — 管理台 API + Console*Service
inbound.<domain>  — 对外 API + Inbound*Service
```

- 功能子模块（如 `support.assignment`）写在 **core** 下优先；若仅 inbound 可见的薄适配，可放 `inbound.<domain>.<function>`。
- 禁止技术层优先：`endpoint.announcement`、`dao.support`（在交付面之下仍 **领域优先**）。

---

## 步骤 4 定契约（路径与分层）

### HTTP

| 面 | 前缀 |
|----|------|
| 管理台 | `/platform/console/sample/...` |
| 对外 | `/platform/api/sample/...` |

设计文档须列出：**资源名、动词、request/vo record、主要错误码**。

### 分层

- 端点 interface 在扩展库；`R<T>` 仅出现在 endpoint。
- service/operator 返回领域 vo 或实体，不返回 `R<T>`（见 [api-contract.md](../../java-reference/references/api-contract.md)）。

### 持久化

- 表名 `nx_sample_*`；迁移脚本归属 **可运行 sample 模块** 的资源目录，不写进 marker 类。

---

## 设计评审追加项（sample 扩展）

- [ ] §6 对照表：未把 platform 产品能力永久放在 sample
- [ ] 每个领域已标明 console / inbound 是否都需要，或仅一侧
- [ ] loader/启动任务已标明注册模块（spring-platform / quarkus-platform）
- [ ] 与内置 platform OpenAPI 扫描包无歧义（`scanPackages` 若扩展须单独列出）

---

## 交接

- 结构/POM → `java:project`（[project-deliverables.md](../../java-project/references/project-deliverables.md) § sample）
- 实现与测试 → `java:develop`（[sample-extension-development.md](../../java-develop/references/sample-extension-development.md)）
