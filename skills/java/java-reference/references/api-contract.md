# 契约设计规则

关注「方法、类、模块之间约定什么」。风格与命名回源
[`standards/code-style.md`](../standards/code-style.md) 与
[`standards/naming.md`](../standards/naming.md)。

本文件是 `java:reference` 的契约专题参考；`java:design` 与 `java:develop`
通过链接复用，不在各自 SKILL 正文中重复。

---

## 方法签名

| 规则 | 示例 |
|------|------|
| 优先静态工厂而非 public 构造器 | `DataPage.of(records, pageNo, pageSize, total)` |
| 返回不可变集合 | `return List.copyOf(items);` |
| 不暴露内部可变引用 | 访问器返回副本或不可变视图 |
| `Optional<T>` 仅用于应用侧单值结果 | `Optional<Role> findByCode(String code)` |
| `Optional` 禁用于参数/字段/record 组件/集合元素/集合返回 | ❌ `Optional<List<Role>> listRoles()` |
| 简单数据载体用 record | `record RoleOptionVo(String roleId, String roleName)` |
| 分页返回 `PageResult<T>` | service / operator 层 |
| service / operator **不得**返回 `R<T>` | `R` 只属于端点边界 |

### 参数设计

- 分页查询收**请求对象**，而非散落的 pageNo / pageSize / 过滤参数
- 跨层传递时参数名表达调用方概念，而非被调框架的内部术语
- 构造器参数若可变，内部防御性拷贝：`this.context = Map.copyOf(context)`

---

## 不可变性

| 层面 | 规则 |
|------|------|
| 字段 | 尽量 `final`；依赖字段必须 `final` |
| 集合 | 访问器返回不可变副本或快照 |
| 构造器 | 可变入参防御拷贝；`null` 归一化为不可变空集合 |
| record | 紧凑构造器做校验与拷贝 |
| 快照语义 | 注册表、路由表、配置、指标对外返回不可变快照 |

```java
public ExecutionRecord(Map<String, Object> context) {
    this.context = context == null ? Map.of() : Map.copyOf(context);
}
```

Lombok 只消除访问器样板，**不替代**领域类的显式行为方法。
领域对象应自己拥有校验、状态转换与业务行为，不要把所有逻辑搬进 service。

---

## 空值与缺失

| 场景 | 处理 |
|------|------|
| 必填为空或非法 | `throw NexusException.build(StatusCode)` |
| 可选参数为 null | `null → 默认值`（setter）/ `null → 跳过`（集合构建） |
| 集合返回值 | 返回不可变空集合，**绝不返回 null** |
| 嵌套缺失 | 禁止 `Optional<List<T>>`，返回空列表 |
| DAO 可空返回 | Javadoc 声明；operator/service 边界归一化或拒绝 |

禁止用 `Objects.requireNonNull` / `IllegalArgumentException` /
`NullPointerException` / `IllegalStateException` / `RuntimeException` /
`Exception` / `UnsupportedOperationException` 表达调用方或业务校验。
一律 `NexusException` + 类型化 `StatusCode`，或 `Checks.*`。详见
[`exception-status-code.md`](../standards/exception-status-code.md) 与
`java:design` → `exception-contract.md`、`java:develop` → `exception-handling.md`。

---

## 校验归属

「规则放在拥有它的最窄边界」：

| 边界 | 负责 |
|------|------|
| record 紧凑构造器 / 领域类型 | 每个合法实例都必须成立的不变量；集合防御拷贝 |
| 请求 `validate()` | 不需要持久化或其他域的字段组合校验 |
| operator | 直接数据操作前置条件；把 mapper 缺失翻译成恰当状态码 |
| service | 工作流、授权、跨记录、跨领域规则 |
| endpoint | 仅 Jakarta REST 绑定无法表达的传输层问题 |

---

## 查询与命令语义

| 类别 | 规则 |
|------|------|
| 查询 | 不改业务状态。`find` → 可选单值；`list` → 有限集合；`page` → `PageResult<T>`；`count` → 数字 |
| create | 遇重复稳定键失败，除非契约显式幂等；**不得静默把 create 当 update** |
| update | 只改文档化的可变属性；不接受不可变稳定键 |
| replace | 把传入值/关联集视为完整；必须定义「省略是否删除既有成员」 |
| delete | 必须定义「目标缺失算成功还是未找到」，并在同一公共资源边界内保持一致 |
| 生命周期 | `register`/`subscribe`/`start`/`stop`/`close` 必须定义重复调用行为 |

---

## 分层契约

```text
endpoint → service → operator → dao
```

| 类型 | 职责 | 禁止 |
|------|------|------|
| `Endpoint` | 传输边界；返回 `R<T>` | 直接依赖 DAO；编排事务与持久化；返回裸实体 |
| `Service` | 非平凡工作流、跨 operator 协调、跨领域、事务 | 返回 `R<T>` |
| `Operator` | 面向 DAO 的直接数据操作；可跨多 DAO 但须简单内聚 | 依赖 service 或另一个 operator |
| `Dao` | 单表操作 | join、XML、跨表编排 |

模块依赖：`base → core → plugin → console → {kernel, platform}`。

---

## 事务

| 规则 | 说明 |
|------|------|
| 只用 `jakarta.transaction.Transactional` | 禁止 Spring 事务注解 |
| 方法级优先 | 落在最小写操作上，不默认类级 |
| 多 DAO 写入或跨表协调必须声明 | |
| 简单单表读不加事务 | 除非有具体的一致性需求 |

---

## 生命周期、并发与兼容性

- 生命周期：`initialize` → `start` → `stop` → `destroy/close`；创建者负责清理
- 并发：可变 public 类型声明线程安全策略；持锁时不得回调未知代码
- 兼容面：public 类型/签名、REST、表结构、稳定键、事件类型串、插件 ID 等改动须先出迁移方案

完整条文见 [`standards-index.md`](standards-index.md) → `api-design.md` 各节。

---

## 契约评审门禁

- [ ] 抽象有真实契约边界，而非机械加接口
- [ ] 空值、缺失、空集合、所有权、可变性都已显式
- [ ] 校验放在拥有该规则的边界
- [ ] 查询与命令的名字与其结果和副作用匹配
- [ ] 事务、幂等、生命周期、清理、重复调用均已定义
- [ ] 并发访问安全或约束已声明
- [ ] 领域事件只在成功状态变更后发布，且清理责任明确
- [ ] 所有受影响的公共标识符都检查过兼容影响
- [ ] 服务/operator 未返回 `R<T>`，端点未直接依赖 DAO
- [ ] 契约与实现路径仅使用 `NexusException`，无 JDK 通用异常作为业务失败
