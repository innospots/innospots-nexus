# 异常与失败契约（设计）

设计阶段为每个可观察失败选定**归属边界**与**类型化 `StatusCode`**，并保证实现路径
只会抛出 `com.innospots.nexus.base.exception.NexusException`（或未来在 `base.exception`
下统一继承 `NexusException` 的专用子类型）。规范原文见
[`exception-status-code.md`](../../java-reference/standards/exception-status-code.md)。

---

## 硬约束

| 必须 | 禁止 |
|------|------|
| 应用可见失败 → `NexusException` + 类型化 `StatusCode` | `throw new RuntimeException(...)` |
| 优先 `NexusException.build(StatusCode, …)` | `throw new Exception(...)` / 受检异常向外泄漏 |
| 基础设施失败在归属边界翻译为 `NexusException`，保留 cause | `IllegalArgumentException` / `IllegalStateException` / `NullPointerException` 做业务或调用方校验 |
| 每个失败语义对应一个稳定状态码（先搜现有目录） | 为每个错误新建 `XxxException` 子类 |
| 推迟实现的端点/方法在设计中标注状态码策略 | 依赖 `UnsupportedOperationException` 作为公共契约 |

`NexusException` 是 `base` 中唯一的平台业务运行时异常基类。除该类及其在
`base.exception` 包下**显式批准**的子类型外，业务模块（`kernel`、`console`、`platform`、
`plugin` 应用路径、`core` 对外服务）**不得**向调用方抛出其他运行时或受检异常。

---

## 设计四步法中的失败设计

### 1. 定归属

- 该失败是输入校验、数据缺失、授权、工作流冲突还是基础设施问题？
- 谁拥有选择最终 `StatusCode` 的权力（endpoint / service / operator / 适配器）？

### 2. 建词汇

- 状态码的 module/category/local 与领域词汇一致；
- message/advice 双语、稳定、不含 ID/路径/SQL/用户输入。

### 3. 划边界

- helper / 领域模型 / operator / service 各自抛出还是向上传播？
- 低层只传播 `NexusException` 或原生失败；**在能选出正确语义的边界**包装为
  `NexusException.build(status, cause)`。

### 4. 定契约

- 方法 Javadoc 的 `@throws` 写 `NexusException` 与对应 `StatusCode`（或状态码枚举常量名）；
- endpoint 契约假定基础设施将 `NexusException` 映射为 `R.fail(...)`，方法内不得再 catch 后换异常类型；
- 测试断言 `NexusException` 与 `code()` / 状态枚举，而非 `IllegalArgumentException` 等。

---

## 状态码选型

| 场景 | 做法 |
|------|------|
| 可复用平台语义 | `NexusStatusCode` |
| 领域专属语义 | 领域 `domain.enums.*StatusCode`（九字符格式） |
| 插件/互操作边界 | 白名单 + 适配后仍映射为类型化 `StatusCode` |
| 未知内部失败 | 外层映射 `NexusStatusCode.SYSTEM_ERROR`（不暴露实现细节） |

新增状态码前必须搜索现有目录；见 `exception-status-code.md` 扩展流程。

---

## 推迟实现（设计约定）

有意延后的 endpoint / service 方法：

- 设计文档标明「未实现」及未来归属；
- 实现阶段使用 `TODO` + `NexusException.build(合适的 StatusCode)`，**不得**使用
  `UnsupportedOperationException`；
- 优先选用能表达「尚未提供」语义的领域或平台状态码；无专用码时临时使用
  `NexusStatusCode.SYSTEM_ERROR` 并在 TODO 中注明待分配状态码。

---

## 设计评审门禁（异常）

- [ ] 每个应用可见失败是否已有类型化 `StatusCode`？
- [ ] 契约与测试是否只承认 `NexusException`？
- [ ] 是否禁止在业务路径设计 `IllegalArgumentException` / `RuntimeException` 等兜底？
- [ ] 基础设施失败是否在明确边界翻译并保留 cause？
- [ ] 推迟实现是否已约定 `NexusException` 而非 JDK 异常？
