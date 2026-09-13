# 新领域实施步骤（develop 视角）

权威流程：[domain-module-initialization.md](../../java-reference/standards/domain-module-initialization.md)。
勾选清单：[domain-initialization-checklist.md](../../java-reference/references/domain-initialization-checklist.md)。
交付物总览：[develop-deliverables.md](develop-deliverables.md)。

以下将**六阶段**映射为 develop 的测试先行步骤（与 [test-conventions.md](test-conventions.md) 一致）。

---

## 步骤对照表

| 步 | 六阶段（摘要） | 先写测试 | 再写实现 | 验证 |
|----|---------------|---------|---------|------|
| 1 | 实体与表映射 | `{Domain}EntityContractsTest` | `domain/entity/*Entity.java` | 红灯 → 实现 → `mvn -pl <m> -am clean compile` |
| 2 | DAO | `{Domain}DaoContractsTest` | `dao/*Dao.java` | 同上 |
| 3 | Request/VO/枚举 | record 契约测试 | `domain/request`、`domain/vo`、`domain/enums` | 同上 |
| 4 | 状态码 | `{Domain}StatusCodeContractsTest` | `domain/enums/*StatusCode.java` | 同上 |
| 5 | Operator/Service | `{Type}Test`（拒绝路径 + 正常路径） | `operator`、`service`（按需） | 同上 |
| 6 | 端点 | `{Domain}EndpointContractsTest` | `endpoint/*Endpoint.java` | 同上 |
| 7 | 转换器（非平凡） | `{Domain}ConverterTest` | `converter/*Converter.java` | 同上 |
| 8 | 事件（按需） | 见 [domain-events.md](domain-events.md) | `domain/event`、`handler` | 同上 |
| 9 | 集成（按需） | `*IT.java` | — | 见 [integration-tests.md](integration-tests.md) |
| 10 | 交付 | — | — | `mvn -pl <m> -am test` 或 `verify` → `java:check` |

**禁止**跳步预建空包；**禁止**在无红灯记录的情况下声称「测试先行已完成」。

---

## 每步出口门禁（develop）

- [ ] 本步契约测试已写且曾失败（新类型/新约束）
- [ ] 实现通过本步聚焦测试
- [ ] 已跑 `mvn -pl <module> -am clean compile`
- [ ] 未引入 join DAO、Spring MVC 端点、JDK 业务异常

全部步骤完成后，交 [develop-deliverables.md](develop-deliverables.md) 交付清单与 `java:check`。
