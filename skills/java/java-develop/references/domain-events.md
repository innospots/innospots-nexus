# 领域事件（实现）

设计阶段决定是否发事件、载荷形状与订阅方；实现阶段按下列规则落代码。
模板见 [code-templates.md](code-templates.md) 末尾。

---

## 何时需要

| 需要 | 不需要 |
|------|--------|
| 同进程内解耦：A 域完成后通知 B 域 | 用事件绕过模块依赖方向（应改 application/adapter） |
| 设计文档已定义事件类型与载荷 | 仅为「以后可能用」预建 `event` 包 |
| 成功后发布（事务提交后） | 在失败路径或事务未提交时发布 |

---

## 落位

```text
<domain>/domain/event/     # 不可变 record，实现 DomainEvent
<domain>/handler/           # EventHandler 实现（或 listener 包，与域内约定一致）
```

- 事件由**拥有业务事实的域**定义与发布（如 `RoleCreatedEvent` 由 role 域发布）。
- **禁止**把具体业务事件塞进 `core`/`console` 仅为让两模块通信。
- `kernel` 与 `platform` **不得**通过事件互引依赖；跨边界用 application 模块协调。

---

## 实现检查

| 项 | 要求 |
|----|------|
| 载荷 | record；字段与 design 词汇一致；不暴露实体可变引用 |
| `eventType()` | 稳定字符串；改类型视为兼容面变更 |
| 发布时机 | 持久化/状态变更**成功之后**；不在 operator 内悄悄 publish 代替 service 编排 |
| Handler | 窄职责；异常不吞；订阅方在 `@PreDestroy` 或对称 API 中清理 |
| 测试 | 事件 record 形状契约；handler 对给定事件的行为单测（可 mock 下游） |

---

## 测试示例

```java
@Test
void roleCreatedEventDeclaresStableType() {
    RoleCreatedEvent event = new RoleCreatedEvent("rol-1", "ADMIN", "ws-1");

    assertThat(event.eventType()).isEqualTo("role.created");
    assertThat(event.roleId()).isEqualTo("rol-1");
}
```

同步事件不得用来重建「本应由 service 直接调用」的编排；见 `java:reference` → quick-constraints 事件条目。
