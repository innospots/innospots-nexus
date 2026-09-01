# 契约设计规则

关注「方法、类、模块之间约定什么」。风格与命名回源
`standards/code-style.md` 与 `standards/naming.md`。

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
`NullPointerException` / `IllegalStateException` 表达调用方或业务校验。

```java
public void validateRoleCode(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
    }
}

public SimpleCondition factor(Factor factor) {
    if (factor != null) {
        factors.add(factor);
    }
    return this;
}
```

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

- 归一化必须**确定性**且**有文档**：传输默认值、大小写、空白、分页、集合空性
- 不得在调用方需要知道契约被违反时静默修复非法值
- 校验方法拒绝输入时抛 `NexusException`；布尔探针用 `isValid` 命名且**不得改状态**
- 不要只依赖数据库约束做应用层能表达清楚的校验，但并发下的完整性仍要保留数据库约束

---

## 查询与命令语义

方法名与返回类型必须让操作形态可预测。

| 类别 | 规则 |
|------|------|
| 查询 | 不改业务状态。`find` → 可选单值；`list` → 有限集合；`page` → `PageResult<T>`；`count` → 数字 |
| create | 遇重复稳定键失败，除非契约显式幂等；**不得静默把 create 当 update** |
| update | 只改文档化的可变属性；不接受不可变稳定键 |
| replace | 把传入值/关联集视为完整；必须定义「省略是否删除既有成员」 |
| delete | 必须定义「目标缺失算成功还是 not-found」，并在同一公共资源边界内保持一致 |
| 生命周期 | `register`/`subscribe`/`start`/`stop`/`close` 必须定义重复调用行为 |
| 隐藏成本 | 方法不得把昂贵的 I/O、阻塞、发布或持久化藏在看起来像属性的名字后面 |

### 幂等

重试是正常边界行为的地方要提供幂等（声明式同步、注册等）。
幂等 = 相同有效输入重复执行产生相同的外部可见状态，**不要求**返回同一对象实例。
幂等必须靠稳定键与数据库/运行时唯一性保护，不能只靠「先读再写」。

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

需要协调多个 operator 的逻辑必须放 service，不允许 operator 间互相依赖。

### 依赖注入

- 构造器注入 + `final` 字段 + `@RequiredArgsConstructor`
- 需要校验、防御拷贝、归一化或解释时用显式构造器
- 禁止字段注入、禁止依赖 setter
- 必需的协作者必须显式；不得为简化测试把必需依赖表示为可空字段

---

## 事务

| 规则 | 说明 |
|------|------|
| 只用 `jakarta.transaction.Transactional` | 禁止 `org.springframework.transaction.annotation.Transactional` |
| 方法级优先 | 落在最小写操作上，不默认类级 |
| 多 DAO 写入或跨表协调必须声明 | |
| 简单单表读不加事务 | 除非有具体的一致性需求 |
| 版本来源 | `jakarta.transaction-api` 在 BOM 管版本，模块不内联版本 |

---

## 生命周期与资源所有权

拥有线程、执行器、订阅、类加载器、调度器、网络客户端或其他可关闭资源的类型，
必须暴露并文档化清晰的生命周期。

| 阶段 | 语义 |
|------|------|
| 构造 | 建立合法局部状态；除非工厂契约声明，否则不得静默启动后台长期工作 |
| `initialize` | 准备依赖与注册 |
| `start` | 开始实际工作 |
| `stop` | 停止工作，支持的场景保留可重启状态 |
| `destroy` / `close` | 永久释放资源 |

- 生命周期操作定义允许状态与重复调用行为；清理优先做成安全幂等
- 创建或注册资源者负责清理，除非 API 显式转移所有权
- 有依赖时逆序释放
- **不得吞掉清理失败**：保留主失败，附带或记录次级失败及其上下文
- 状态转换成功前不得发布 started/stopped 成功事件
- 需要词法或显式清理时用 `AutoCloseable` 或 `Subscription` 之类的句柄
- 避免 finalizer，不依赖 GC 释放外部资源

---

## 线程安全与并发

| 规则 | 说明 |
|------|------|
| 不可变 record 与快照优先跨线程传递 | |
| 可变 public 类型必须声明 | 线程安全 / 单线程封闭 / 需外部同步 |
| 一种不变量一种同步机制 | 不得混用 synchronized、原子类、并发集合而不解释各自保护什么 |
| **持锁时不得回调未知代码** | 先拷出注册项再调用插件/事件处理器/回调 |
| 多字段协调的状态转换对调用方原子 | |
| 返回不可变快照 | 注册表、路由表、配置、指标 |
| 中断与取消必须传播或有意恢复 | 不得静默吞掉 `InterruptedException` |

---

## 公共契约兼容性

### 兼容面清单

以下跨越模块、插件、持久化或传输边界的内容都是兼容面：

- public 类型名、包名、方法签名、泛型边界与声明语义
- REST 路径、参数名、请求/响应字段、枚举值、状态码
- 表名列名、稳定业务键、实体 ID 前缀、索引支撑的唯一性假设
- 事件类型串、配置键、插件 ID、能力键、标签名、序列化字段名

### 规则

- 不得把改动这些面作为机械重命名或内部重构的一部分
- 先出迁移方案、兼容适配器、数据迁移或版本边界
- 追加式改动仍须为旧调用方与既有持久化数据定义默认值
- 废弃：`@Deprecated` + Javadoc `@deprecated` **同时**使用，指明替代方案与明确约定的兼容期
- 不得无限期保留废弃别名而没有移除决定

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
