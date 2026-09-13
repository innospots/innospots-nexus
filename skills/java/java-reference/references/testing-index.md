# 测试规范索引

`java:reference` 不重复测试写法条文；本页路由到**设计 / 实现 / 检查**各技能的测试文档与
`standards/` 中的契约要求。

---

## 按阶段

| 阶段 | 文档 | 内容 |
|------|------|------|
| **设计** — 测什么 | `java:design` → [test-scope.md](../../java-design/references/test-scope.md) | 契约/行为/集成边界；不测范围 |
| **实现** — 怎么写 | `java:develop` → [test-conventions.md](../../java-develop/references/test-conventions.md) | 命名、断言、Mock、独立性 |
| **实现** — 契约测试 | `java:develop` → [contract-tests.md](../../java-develop/references/contract-tests.md) | 实体/DAO/端点/状态码反射契约 |
| **实现** — 集成测试 | `java:develop` → [integration-tests.md](../../java-develop/references/integration-tests.md) | `*IT`、跨层、真实中间件边界 |
| **实现** — 六阶段顺序 | [domain-initialization-checklist.md](domain-initialization-checklist.md) + [standards/domain-module-initialization.md](../standards/domain-module-initialization.md) §8 | 测试先行、红灯→绿灯 |
| **检查** | `java:check` | 交付前 `mvn test` 全量验证 |

---

## standards 中的硬性测试要求

| 来源 | 要求 |
|------|------|
| [exception-status-code.md](../standards/exception-status-code.md) §8 | 状态码 13 项契约测试性质 |
| [domain-module-initialization.md](../standards/domain-module-initialization.md) §8–10 | 契约测试先于实现；`mvn clean compile` / `mvn test` 门禁 |
| [quick-constraints.md](quick-constraints.md) §1、§11 | 每批编译；新功能须有单测/契约测试；禁止削弱断言 |

---

## 默认期望（摘要）

| 层 | 至少 |
|----|------|
| Entity / DAO / Endpoint / StatusCode | 契约测试（结构、绑定、路径、`R<T>`） |
| Service / Operator | 正常路径 + 每种业务拒绝路径单测 |
| 持久化 | 无 mapper.xml；Dao 清单与 [persistence-config.md](persistence-config.md) 一致 |
| 集成 | 仅当设计或 develop 文档声明需要 `*IT` |

命名：`{Type}Test` 行为单测；`{Concept}ContractsTest` 或契约族；测试方法 lowerCamelCase **无 `test` 前缀**（见 [naming.md](../standards/naming.md)）。
