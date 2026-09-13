# 异常实现规则

实现阶段**唯一**允许向业务调用方传播的应用异常是
`com.innospots.nexus.base.exception.NexusException`。权威条文见
[`exception-status-code.md`](../../java-reference/standards/exception-status-code.md)；
设计阶段失败归属见 [`java:design` → exception-contract.md](../../java-design/references/exception-contract.md)。

---

## 抛出

```java
// 业务 / 校验 / 状态检查
throw NexusException.build(RoleStatusCode.ROLE_NOT_FOUND);

// 带 cause 的基础设施翻译
throw NexusException.build(NexusStatusCode.SYSTEM_ERROR, cause);

// 参数前置（优先用 Checks，内部同样是 NexusException）
Checks.notBlank(roleCode, "roleCode");
```

| 禁止 | 替代 |
|------|------|
| `throw new IllegalArgumentException(...)` | `NexusException.build(NexusStatusCode.INVALID_PARAMETER, …)` 或 `Checks.*` |
| `throw new IllegalStateException(...)` | 带业务语义的 `StatusCode` + `NexusException.build` |
| `throw new NullPointerException(...)` | `Checks.notNull` / 显式 `NexusException` |
| `throw new RuntimeException(...)` / `throw new Exception(...)` | 在边界 `NexusException.build(status, cause)` |
| `throw new UnsupportedOperationException(...)` | `NexusException.build(StatusCode, "…尚未实现")` + `TODO` |
| 为每个错误 `extends RuntimeException` 新子类 | `NexusException` + 领域 `*StatusCode` 枚举 |

**不要**为业务错误创建 `RoleNotFoundException` 等平行异常体系；用 `StatusCode` 区分语义。

---

## 捕获与翻译

```java
try {
    provider.load();
} catch (SomeProviderException ex) {
    throw NexusException.build(NexusStatusCode.EXTERNAL_FAILURE, ex);
}
```

| 必须 | 禁止 |
|------|------|
| 已有 `NexusException` 原样重抛（除非换更准确状态） | 捕获后改抛 `RuntimeException` |
| 最窄类型捕获 + `NexusException.build(status, cause)` | `catch (Exception)` 后返回伪造成功 |
| `InterruptedException` 恢复中断标志再处理 | 吞掉异常或 `catch (Throwable)` 掩盖致命错误 |

endpoint **不得**在方法内把 `NexusException` 转成其他异常；全局异常映射负责 `R.fail(...)`。

---

## 推迟实现

```java
@GET
@Path("/{roleId}")
public R<RoleVo> getRole(@PathParam("roleId") String roleId) {
    // TODO 查询契约确定后委托 RoleService。
    throw NexusException.build(NexusStatusCode.SYSTEM_ERROR, "角色查找尚未实现");
}
```

---

## 测试

```java
assertThatThrownBy(() -> operator.requireRole("missing"))
        .isInstanceOf(NexusException.class)
        .satisfies(ex -> assertThat(((NexusException) ex).code())
                .isEqualTo(RoleStatusCode.ROLE_NOT_FOUND.fullCode()));
```

不要断言 `IllegalArgumentException` 或 `RuntimeException` 作为业务失败契约。

---

## 实现自检

- [ ] 新增/修改的 `throw` 是否全部为 `NexusException`（或 `Checks` 间接抛出）？
- [ ] 是否未向 endpoint 响应路径泄漏 JDK 异常类型？
- [ ] catch 块是否只做翻译/添加上下文，而非换异常族？
- [ ] 推迟实现是否已去掉 `UnsupportedOperationException`？
