# 领域事件契约（设计）

实现见 `java:develop` → [domain-events.md](../../java-develop/references/domain-events.md)。

---

## 何时在设计中引入事件

| 需要 | 不需要 |
|------|--------|
| 同进程解耦：A 域完成后通知 B 域（依赖方向仍合法） | 用事件让 `kernel` 与 `platform` 互引 |
| 明确「成功后」副作用（审计、索引刷新） | 替代 service 编排（本可同步调用） |
| 载荷稳定、版本可演进 | 投机性「以后可能用」预建 event 包 |

---

## 设计交付（写入 L1/L2 文档或 L0 块）

| 项 | 说明 |
|----|------|
| 事件名 / `eventType()` | 稳定字符串；改类型视为兼容面变更 |
| 发布域 | 谁拥有业务事实（事件 record 放哪一域 `domain.event`） |
| 载荷字段 | record 字段表；不可变；无实体可变引用 |
| 发布时机 | 事务提交后 / 哪一层 publish |
| 订阅方 | handler 归属模块；清理策略 |
| 禁止 | 跨模块把具体业务事件塞进 `core`/`console` 只为绕过依赖 |

---

## 设计评审门禁（事件）

- [ ] 是否可用 service 直接调用代替？若可以，不设计事件
- [ ] 订阅方是否违反 kernel/platform 互依？
- [ ] 载荷是否纳入 test-scope（形状契约 + handler 行为单测）？
- [ ] 与 [exception-contract.md](exception-contract.md) 区分：失败不走事件「补偿」

未列入设计文档的事件，develop **不得**新增 `domain.event` 包。
