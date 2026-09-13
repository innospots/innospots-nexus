# 硬性约束速查表

`java:reference` 的跨技能复用速查表。按**工作场景**组织最常违反、代价最高的约束。
完整规则回源 [`../standards/`](../standards/) 对应文件（通过 [standards-index.md](standards-index.md) 定位）。
其他 `java:*` 技能链接本文件，不在各自正文中重复。

---

## 1. 每次改完 Java 代码

| 约束 | 说明 |
|------|------|
| 立即 `mvn clean compile` | 不得延后到任务末尾，不得在源码不可编译时继续加功能 |
| 不得降低 `maven.compiler.release` | 本地 JDK 更旧时**报告环境不匹配**，而不是下调基线 |
| 本地依赖产物过期 | 用 `mvn -am` 之类的 reactor 构建，而不是削弱测试 |

违反代价：整条流水线阻塞；基线被悄悄降级后长期无法回滚。

---

## 2. 包结构（领域优先）

| 必须 | 禁止 |
|------|------|
| 业务包先领域后职责：`role/endpoint`、`role/dao`、`role/domain/entity` | 技术层优先：`endpoint/role`、`dao/menu`、`domain/permission/entity` |
| 同一领域代码聚在一个领域根下（如 `kernel/role/**`） | 为「分层」把多领域塞进同一 `endpoint/`、`dao/` 顶层包 |
| 大领域按功能子模块：`permission/authorization`、`grant/service` | 模块根 `service` 或单包堆满全部 `*Service` |
| 单包 ≤15 个 `.java`（达 12 规划拆分） | 单包 16+ 类仍平铺 |
| `domain` 子包仅用 `entity`/`request`/`vo`/`model`/`enums`/`event` | `role/entity` 缺 `domain` 层；或 `domain/<领域名>/` 套娃 |
| 初始面最小，按需加 `service`/`operator`/`converter` | 空包、`impl`/`common`/`misc` 逃避归属 |

完整说明：[package-structure.md](package-structure.md)。

---

## 3. 写实体（Entity）

| 必须 | 禁止 |
|------|------|
| 默认继承 `WorkspaceBaseEntity`（或 `TenantBaseEntity` / `BaseEntity` 按范围） | 未经设计批准继承 `ProjectBaseEntity`；在无项目语义的表上加 `projectId` |
| 设计评审批准后可继承 `ProjectBaseEntity`（core 提供） | 自建隔离基类或重复声明继承字段 |
| 主键 `String` + `@TableId(type = IdType.ASSIGN_UUID)` + `@Id` + `@Column(length = 32, nullable = false)` | 手动赋主键（由 `DbPrimaryGenerator` 经 `IdGenerator.ulid(prefix)` 统一生成） |
| `public static final String TABLE_NAME`，且 `@Table` 与 `@TableName` 同时引用该常量 | 在注解里重复字面量 |
| 字符串长度为 2 的幂（16/32/64/128/256/512/1024），无界文本用 `@Lob` | 任意大长度 |
| 显式 `@Table(indexes = ...)`，索引名 `uk_`/`idx_` + 表概念 + 用途 | 无索引或索引名无意义 |
| Lombok `@Getter` + `@Setter` | `@Data`（生成 equals/toString/构造/变更行为过宽） |
| 覆写 `idPrefix()`，短小稳定 | 用临时模块名派生前缀，或重构时改前缀 |
| 表名 `nx_` + 单数 snake_case，列 mirror Java 术语 | 复数表名、驼峰列名 |

禁止重复声明继承来的字段：`tenantId` `workspaceId` `createdAt` `updatedAt` `createdBy` `updatedBy`。

---

## 4. 写 DAO

| 必须 | 禁止 |
|------|------|
| `dao` 包、`*Dao` 后缀、`extends BaseMapper<Entity>` | 自定义基础 CRUD |
| 自定义操作写成 `default` 方法 + `Wrappers.lambdaQuery()` / `lambdaUpdate()` | 手写大段条件拼装 |
| lambda 方法引用 `RoleEntity::getRoleCode` | 字符串列名 |
| **每个方法只访问一张表** | 任何形式的 SQL join |
| 跨表读：查主表 → 收集 ID → 分批查各表 → 内存组装 | N+1 逐行查关联表 |
| 跨表写/级联/稳定键传播交给事务 service | 在 DAO 里编排多表事务 |
| 业务配置用 `resources/**/*.yaml` + Java `@Configuration` | 新建 `*.properties`、`beans.xml`、MyBatis `mapper.xml` |

禁止创建 Mapper XML 文件与 XML 语句定义；禁止用 XML 装配 Bean 或 SQL。
自定义查询/更新/插入优先 `Wrappers.lambdaQuery()` / `lambdaUpdate()` 与 Dao `default` 方法；
单 Dao 过大时拆多表多 Dao 或把动态条件上提 operator，勿用 XML 堆砌。
细则见 [persistence-config.md](persistence-config.md)；权威条文见 `standards/code-style.md`「配置与资源文件」。
DAO 方法可用 `select/insert/update/delete` 对齐 `BaseMapper`；面向应用的 operator/service
必须用 `find/list/page/count` 词汇。

---

## 5. 写端点（Endpoint）

| 必须 | 禁止 |
|------|------|
| `endpoint` 包、`*Endpoint` 后缀、默认具体类 | 仅为 mock 抽接口 |
| 只用 `jakarta.ws.rs` 注解 | `@RestController`/`@RequestMapping`/`@GetMapping`/`@PostMapping` 等 Spring MVC 注解 |
| 类级 `@Path` + `@Produces` + `@Consumes`，方法级 HTTP 注解 | 把资源路径散落到各方法 |
| 显式 `@PathParam`/`@QueryParam`/`@HeaderParam`/`@BeanParam` | 依赖运行时隐式绑定 |
| 返回 `R<XxxVo>` / `R<PageResult<XxxVo>>` / `R<Void>` | 返回裸实体、返回 `Map`、返回 null |
| 推迟实现：`TODO` + `NexusException.build(StatusCode)` | `UnsupportedOperationException`；返回伪造成功数据 |
| 端点只做传输，委托 service/operator | 端点直接依赖 DAO、端点内编排事务与持久化 |

service / operator **不得**构造 `R<T>`，只返回领域值或 `PageResult<T>`。
约 7 个内聚方法即触发端点边界复审。

---

## 6. 写 Request / VO

| 必须 | 禁止 |
|------|------|
| record 类型 | class + Lombok |
| `XxxCreateRequest` / `XxxUpdateRequest` / `XxxStatusUpdateRequest` / `XxxPageRequest` / `XxxTreeRequest` / `XxxOrderRequest` / `XxxAddRequest` / `XxxReplaceRequest` | 一个笼统 `XxxRequest` 承担不同修改权的操作 |
| `XxxVo` 主视图，`XxxOptionVo` 特定投影 | 大写 `VO`、`Dto`、`Response`、`Result` |
| 紧凑构造器里 `List.copyOf`/`Set.copyOf`/`Map.copyOf` 防御拷贝 | 暴露调用方可变集合 |
| 更新请求排除不可变稳定键与受保护系统字段 | 更新请求携带 `roleCode` 之类的稳定键 |
| 集合组件用单数命名 | `RoleListVo` |

禁止把持久化实体直接当端点请求或响应。

---

## 7. 抛异常与定义状态码

| 必须 | 禁止 |
|------|------|
| `NexusException.build(StatusCode)` / `build(StatusCode, cause)` / `build(statusCode, displayOverride, cause)` | 为每个业务错误新建异常子类 |
| 业务失败一律 `NexusException` + 类型化 `StatusCode`；边界捕获后翻译 | `IllegalArgumentException` / `RuntimeException` / `Exception` / `UnsupportedOperationException` |
| 全码 `MODULE(3) + CATEGORY(2) + LOCAL(4)` = 9 字符 | 缩短、加分隔符、小写、把 HTTP 状态编进本地段 |
| `bisCode()` 与 `fullCode()` 返回同一值 | 两者不一致 |
| 非成功状态提供有意义的中英 message + advice | 只填英文、两 locale 复制粘贴 |
| 翻译底层失败时保留原始 cause | 新建异常替换掉有用诊断 |
| 捕获 `InterruptedException` 后 `Thread.currentThread().interrupt()` | 吞掉中断 |
| 状态码按语义选类别 | 因 HTTP 映射方便而选类别 |
| 新增状态码前先搜索现有目录 | 重复造同义码 |

字符串重载 `build(String, String, ...)` **仅限**互操作边界：需全码解析器 + 显式
白名单 + 结构化日志保留来源 + 尽量翻译为类型化状态。仓库内普通调用不得传
`status.fullCode()` 或复制字面量。

响应与状态文本中禁止出现：密码、令牌、凭据、密钥、授权头、含密钥的完整 SQL、
堆栈、请求 ID、记录 ID、文件路径、供应商原文、用户输入。

`kernel` 与 `platform` 不得互引对方的状态枚举、事件类型或业务包。

---

## 8. 分层与依赖方向

```text
endpoint → service → operator → dao
```

允许简化：`endpoint → operator → dao`、`service → dao`。

上述分层在**同一领域内**成立；包树仍须**领域优先**（`role/endpoint` → `role/service` →
`role/operator` → `role/dao`），不得把多个领域挂在 `endpoint/`、`dao/` 顶层下。

| 必须 | 禁止 |
|------|------|
| operator 简单数据操作，可跨多 DAO 但须简单内聚 | operator 依赖 service 或另一个 operator |
| service 管工作流、跨 operator 协调、跨领域、事务 | 端点直接编排 DAO |
| 构造器注入 + `final` 字段 + `@RequiredArgsConstructor` | 字段注入、依赖 setter |
| 契约按能力命名（`ResourceStore`、`PasswordDecryptor`） | 给每个具体类机械配接口 |
| `DefaultXxx` 仅在存在其他合法实现时使用 | 单实现也硬套 `Default` 前缀 |

模块依赖方向：`base → core → plugin → console → {kernel, platform}`。
`kernel` 与 `platform` 平行且互不依赖。反向依赖一律禁止。

---

## 9. 事务与并发

| 必须 | 禁止 |
|------|------|
| `jakarta.transaction.Transactional` | `org.springframework.transaction.annotation.Transactional` |
| 方法级注解，落在最小写操作上 | 默认类级注解 |
| 多 DAO 写入或跨表协调必须声明事务 | 简单单表读默认不加事务 |
| 可变 public 类型声明线程安全策略 | 无说明的可变共享状态 |
| 一种不变量一种同步机制 | 混用 synchronized/原子类/并发集合而不解释各自保护什么 |
| 持锁前先拷出注册项再回调 | 持锁时调用未知插件/事件处理器/回调代码 |
| 返回不可变快照 | 返回注册表、路由表、配置、指标的实时可变视图 |

---

## 10. 领域事件

| 必须 | 禁止 |
|------|------|
| 事件是不可变 record，放发布域的 `domain.event`，实现 `DomainEvent` | 在事件里塞 DAO、service、可变实体 |
| 状态变更**成功之后**才发布 | 写入/事务仍可能失败就先发成功事件 |
| `EventBus.publish(event)` 用于无返回值通知 | 用同步事件重建模块间直接调用 |
| `publishSync` 仅在调用方确实需要立即结果时使用 | 默认同步 |
| 订阅方在自身 `handler` 包定义 `XxxEventHandler` 实现 `EventHandler<XxxEvent>` | 发布方依赖消费方的 handler |
| 注册方负责 `unsubscribe` 清理 | 注册后不清理 |

事件类型串与配置键用稳定小写点分名（如 `role.created`），视为公共标识符而非展示文本。

---

## 11. 命名与注释

| 必须 | 禁止 |
|------|------|
| 一概念一英文词，`roleId`/`tenantId`/`pluginId` | `data`/`info`/`item`/`object` 占位 |
| 数值带单位 `timeoutMillis` | 裸 `timeout`/`delay`/`interval` |
| 时间点 `createdAt`，计数 `xxxCount` | 无语义后缀 |
| 布尔字段无 `is` 前缀（`enabled`），getter 用 `isEnabled()` | `isEnabled` 字段 |
| 查询动词 `find/list/page/count`，禁用 `getAll`、`queryXxx` | `getAllUsers()` |
| 常量与枚举常量全大写下划线 | 全大写类型名 |
| 缩写当单词：`pluginId`、`HttpClientBuilder` | `pluginID` / `pluginId` 混用 |
| 每个 public 类型有 Javadoc：`@author`、`@date`（`yyyy/MM/dd`）；有关联必填 `@see` | 类型缺登记信息；方法/行内写修改历史 |
| 每个 public/protected 方法有 Javadoc，说明契约与约束 | 复述代码的注释 |
| 行内注释解释 why | 解释 what |
| TODO 说明缺什么 + 未来归属，并配 `NexusException` | 裸 `TODO`/`FIXME`；`UnsupportedOperationException` |
| 测试方法 lowerCamelCase 行为短语 | `test` 前缀、`testCreate1`、`worksCorrectly` |

禁止创建 `impl`/`common`/`misc`/`util` 子包掩盖归属不清；禁止 `CommonUtils`/`BaseHelper`。

---

## 12. 日志

| 必须 | 禁止 |
|------|------|
| Lombok `@Slf4j` | 手写 logger 字段、`@Sl4j` 拼写 |
| 参数化日志 `log.info("Started plugin {}", pluginId)` | 字符串拼接 |
| 记录稳定标识符与生命周期转换 | 记录完整领域对象 |
| 在增加有用上下文处或最终处理处记录 | 逐层重复记录同一失败 |

绝不记录：密码、密钥、令牌、解密后的载荷、敏感配置值。

---

## 13. 兼容面（改动前先确认）

以下属于公共兼容面，机械重命名或内部重构都**不得**顺手改动：

- public 类型名、包名、方法签名、泛型边界与声明语义
- REST 路径、参数名、请求/响应字段、枚举值、状态码
- 表名列名、稳定业务键、实体 ID 前缀、索引支撑的唯一性假设
- 事件类型串、配置键、插件 ID、能力键、标签名、序列化字段名

废弃必须 `@Deprecated` + Javadoc `@deprecated` 同时使用，指明替代方案，并保留
明确约定的兼容期。

---

## 14. 遗留工程与技能文档

| 必须 | 禁止 |
|------|------|
| 遗留工程只用于理解业务术语、操作流程、生命周期、边界情况 | 复制/移动遗留源文件、机械复刻包结构、沿用遗留 POM |
| 每个字段/类型/端点/依赖都对当前模块边界与开发者意图给出理由 | 因为遗留实体有所以保留 |
| 模块 API 索引 `README.md` 与 `references/` 仅在开发者显式请求整体扫描时更新 | 写功能、修 Bug、重构、构建失败时顺手同步参考文档 |
