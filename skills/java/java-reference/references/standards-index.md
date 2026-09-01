# 规范章节级索引

本文件是 `standards/` 下 7 份规范的章节地图。用于快速定位「哪条规则在哪份文件
的哪一节」。内容发生疑义时一律回源到 `standards/` 原文。

---

## code-style.md — 代码风格

| 章节 | 关键规则 |
|------|---------|
| Braces | 所有 `if/else/for/while` 必须用花括号，禁止单行裸语句 |
| Indentation | 4 空格，禁止 Tab |
| Line Width | 上限 120 字符，超长按参数换行，续行缩进 8 空格 |
| Import Order | `java.*`/`javax.*` → 第三方（含 Lombok） → `com.innospots.*`，组间空行，禁止通配符 |
| Source File Organization | 常量 → 静态可变状态 → 实例字段 → 构造器/静态工厂 → public → protected → private → 嵌套类型；一条语句一个变量；注解独占一行 |
| Multiline Formatting | 流式链点号置于续行行首；record 多行头一个组件一行；禁止嵌套三元；可用菱形推断 |
| Lombok | 构造器注入用 `@RequiredArgsConstructor` + `final`；日志用 `@Slf4j`（禁止 `@Sl4j` 拼写）；可变实体/配置绑定用 `@Getter`+`@Setter`；`domain.request`/`domain.vo` 是 record 不适用；**禁止 `@Data`**；敏感值禁止 Lombok `toString` |
| Domain Types | `domain.request`/`domain.vo` 必须是 record；领域类型可封装自身不变量与行为 |
| REST Endpoints | `endpoint` 包 + `*Endpoint`；默认具体类；只用 `jakarta.ws.rs`；类级 `@Path`/`@Produces`/`@Consumes`，方法级 HTTP 注解；显式参数注解；推迟实现用 `TODO` + `UnsupportedOperationException`；返回 `R<T>` |
| MapStruct Converters | 非平凡结构转换必须用 MapStruct；`converter` 包 + `*Converter`；`@Mapper(config = BaseMapperConfig.class)`；模型↔实体继承 `BaseBeanConverter` |
| MyBatis-Plus DAOs | `dao` 包 + `*Dao` + `BaseMapper<Entity>`；自定义操作用 `default` 方法 + lambda wrapper；**每方法仅单表、禁止 join、禁止 XML**；N+1 禁止 |
| Dependency Fields and Construction | 构造器注入 + `final`；禁止字段注入 |
| Collections and State | 优先不可变空集合；边界处 `List.copyOf`/`Set.copyOf`/`Map.copyOf`；不暴露内部可变集合 |
| Logging and Diagnostics | `@Slf4j`；禁用 `System.out/err`、`printStackTrace`；参数化日志；不记录密钥/令牌；不逐层重复记录 |
| Literals and Expressions | 领域字面量提为常量；枚举用 `==`；可空 String 用 `"LIT".equals(x)` |

---

## naming.md — 命名

| 章节 | 关键规则 |
|------|---------|
| Naming Decision Order | 业务概念 → 范围/变体 → 操作/视图目的 → 技术职责；`[范围]+概念+[用途]+职责` |
| Vocabulary Rules | 一概念一英文词；禁止 `Common/General/Base/Default/Simple/Generic` 当万能词；不编码调用方不需要的实现细节；`code/key/id/name/status/type` 需有文档化含义 |
| Java Naming | 完整对照表：Class/Interface/`Endpoint`/`Dao`/`Entity`/`Request`/`Vo`/`Config`/`Converter`/`StatusCode`/`Event`/`Enum`/Record/Annotation/Method/常量/包 |
| Type Responsibility Names | `Service` `Operator` `Manager` `Registry` `Catalog` `Router` `Provider` `Factory` `Resolver` `Discovery` `Loader` `Parser` `Validator` `Builder` `Repository` `Store` `Facade` `Holder` `Handler` `Interceptor` `Listener` 的适用条件 |
| Domain Type Names | 实体加 `Entity`；内部模型不用 `Dto/Pojo/Bean/Data/Model` 后缀；`Base` 仅用于抽象基类 |
| Requests | `XxxCreateRequest` / `XxxUpdateRequest` / `XxxStatusUpdateRequest` / `XxxPageRequest` / `XxxTreeRequest` / `XxxOrderRequest` / `XxxAddRequest` / `XxxReplaceRequest` |
| Views | 主视图 `XxxVo`，特定投影 `XxxOptionVo`；禁用大写 `VO`/`Response`/`Result`/`Dto` |
| Events and lifecycle types | 事件用过去时事实；`State` 运行时状态机、`Status` 业务可用性、`Mode` 运行模式、`Type` 封闭分类 |
| Query Verbs | `get` `find` `list` `page` `count` `load` `discover` `resolve` `snapshot`；禁用 `getAll`、`queryXxx`；DAO 层例外可用 `select/insert/update/delete` |
| Command and lifecycle verbs | `create` vs `of/from/named`；`add/remove` 成员、`register/unregister` 注册、`subscribe/unsubscribe` 订阅；`update`/`replace`/`save` 语义区分 |
| Boolean predicates | `is/has/can/supports/contains/matches` 开头；布尔字段无 `is` 前缀；优先肯定式；谓词名不得隐藏修改或 I/O |
| Field, Parameter, and Local Names | 限定概念 ID（`roleId`）；集合复数；时间点 `At`；数值带单位；对称命名；禁用 `temp/tmp/foo/bar/obj/data/flag` |
| Abbreviations and Acronyms | 缩写当单词：`HttpClientBuilder`、`pluginId`；项目既定形式 `Id Url Uri Http Json Rsa Oauth Dao Vo Api Ui Db Io`；禁止 `ID/Id` 混用 |
| Package Naming | 业务先按域再按职责；`domain.{entity,request,vo,model,enums,event}`；`endpoint`/`api`/`operator`/`service`/`dao`/`converter`/`interceptor`/`handler`/`listener`/`runtime`/`contract`/`declaration`/`discovery`；禁止 `impl`/`common`/`misc`/`util` 子包 |
| Persistence Names | 表/列小写 snake_case；Nexus 表 `nx_` 前缀 + 单数；`TABLE_NAME` 常量；唯一索引 `uk_`、普通索引 `idx_`；ID 前缀短小稳定 |
| File and Test Naming | 一文件一顶层 public 类型；单测 `{Type}Test.java`，契约族 `{Concept}ContractsTest.java`；测试方法 lowerCamelCase 行为短语、**无 `test` 前缀** |
| Examples to Avoid | 反例对照表 |
| Naming Checklist | 10 条自检 |

---

## api-design.md — API 设计

| 章节 | 关键规则 |
|------|---------|
| Method Signatures | 优先静态工厂 `of/create/from/named`；返回不可变集合；`Optional<T>` 仅用于应用侧单值结果，**禁用于参数/字段/集合**；简单数据载体用 record |
| Contract and Implementation Boundaries | 四类引入接口的条件；**不得仅为 mock 建接口**；`DefaultXxx` 仅在存在其他合法实现时使用 |
| Immutability | 字段尽量 `final`；访问器返回不可变副本；构造器对可变参数防御性拷贝 |
| Null Handling | 必填空值抛 `NexusException` + `StatusCode`；禁止 `Objects.requireNonNull`/`IllegalArgumentException` 做业务校验；可选参数 `null→default`/`null→skip`；集合结果不返回 `null`；禁止 `Optional<List<T>>` |
| Validation and Normalization | record 紧凑构造器管不变量；请求 `validate()` 管字段组合；operator 管数据前置条件与映射缺失；service 管工作流/授权/跨记录；endpoint 只管传输层 |
| Exception Handling | 见 `exception-status-code.md` |
| Persistence Entities | 继承 `BaseEntity`/`TenantBaseEntity`/`WorkspaceBaseEntity`；禁止 `ProjectBaseEntity`；主键 `String` + `@TableId(ASSIGN_UUID)` + `@Id` + `@Column(length=32)`；字符串长度为 2 的幂；`TABLE_NAME` 常量双注解共用；Lombok `@Getter`+`@Setter`；显式 `@Table(indexes=...)` |
| Domain Models | `domain` 下按 `entity/request/vo/model/enums` 划分；配置类放模块级 `config` 包，不放 `domain` |
| REST Endpoint Contracts | `*Endpoint` + `endpoint` 包；`jakarta.ws.rs`；每个方法返回 `R<T>`/`R<PageResult<T>>`/`R<Void>`；**service/operator 不得返回 `R`** |
| DAO Contracts | 同 `code-style.md`；跨表读用分批查询+内存组装；跨表写交由事务 service |
| Service and Operator Boundaries | operator 简单数据操作，不得依赖 service 或另一 operator；service 复杂工作流；分页返回 `PageResult<T>` |
| Query and Command Semantics | 查询不改状态；`create` 遇重复稳定键失败；`update` 不接受不可变稳定键；`replace` 需定义省略是否删除；`delete` 需定义缺失是成功还是 not-found；生命周期操作需定义重复调用行为 |
| Domain Events and EventBus | 发布域拥有事件契约，放 `domain.event`，实现 `DomainEvent`；`kernel` 与 `platform` **不得互相引用事件类型**；事件是不可变 record；状态变更成功后才发布；`publish` 异步通知、`publishSync` 仅在真正需要立即结果时用；订阅者负责清理 |
| Domain Conversion | 同 `code-style.md` 的 MapStruct 规则 |
| Password Registration Requests | 请求携带前端加密载荷，不含 hash/salt/algorithm；通过公共模块接口解密 |
| Page Requests | 分页请求对象放 `domain.request`；可组合 `SimpleQueryRequest`；方法收请求对象而非散参数 |
| Transaction Boundaries | 只用 `jakarta.transaction.Transactional`；优先方法级而非类级；简单单表读不加事务 |
| Fluent API | setter 式方法返回 `this`；`@SuppressWarnings("unchecked")` 需证明安全并注释 |
| Lifecycle and Resource Ownership | `initialize`→`start`→`stop`→`destroy/close`；创建者负责清理；逆序释放；清理失败不得吞掉主失败 |
| Thread Safety and Concurrency | 可变 public 类型需声明线程安全策略；一种不变量一种同步机制；**持锁时不得回调未知代码**；返回不可变快照；不得吞掉中断 |
| Public Contract Compatibility | 兼容面清单：类型名/签名、REST 路径与字段、表名列名/稳定键/ID 前缀、事件类型串/配置键/插件 ID/序列化字段名；废弃用 `@Deprecated` + `@deprecated` 并指明替代 |
| API Review Checklist | 8 条自检 |

---

## code-comments.md — 注释

| 章节 | 关键规则 |
|------|---------|
| Package Documentation | `package-info.java` 用于公共契约/架构边界/非显然约束；说明职责、归属、依赖方向、生命周期与线程安全假设；禁止空洞文档 |
| Type Comments | 每个 public 类型必须有 Javadoc；说明职责、约束、线程安全、`@see`、`@param <T>`；实现类讲策略不复制接口文本；record 用 `@param` 说明组件；枚举常量含义不清时加注释；注解说明使用场景与运行时效果 |
| Method Comments | public/protected 必须有 Javadoc：`@param`/`@return`/`@throws`；补充可空性、所有权、变更、幂等、线程安全、安全、生命周期；record 访问器、平凡 getter/setter、枚举 `values/valueOf`、完整继承的覆写无需重复注释 |
| Inline Comments | 解释 *why* 而非 *what*；算法理由、边界情况、非显然副作用、多步流程、生命周期清理、并发不变式、安全取舍、兼容性 |
| TODO and Deferred Behavior | 仅用于有意推迟且有明确未来边界；**禁止裸 `TODO`/`FIXME`**；不得用 TODO 为伪造成功数据/吞异常开脱；实现时同变更移除 |
| Prohibited Comments | 禁止注释掉的代码、作者/日期/工单历史、复述代码、承诺未实现行为、泄露密钥；不得用注释弥补坏命名（应改名） |
| Comment Review Checklist | 6 条自检 |

---

## exception-status-code.md — 异常与状态码

| 章节 | 关键规则 |
|------|---------|
| 1. Principles | 一次失败 = 一个归属边界 + 一个稳定状态码 + 一条有用 cause 链；状态码表应用语义，HTTP 只表传输结果 |
| 2. Exception taxonomy | 五类失败的表示法：预期应用失败→`NexusException`；基础设施失败→翻译并保留 cause；纯工具/程序员误用→可保留 JDK 异常但不得外泄；中断/取消→保留语义；致命 JVM 错误→不得捕获 |
| 3. Constructing `NexusException` | 默认 `build(StatusCode, ...)` 类型化重载；字符串重载**仅限** interop 边界，需全码解析器 + 显式 allowlist + 结构化日志记录来源 + 尽量翻译为类型化状态；`build(StatusCode, String)` 的运行时消息同样是响应面，禁止密钥/ID/用户输入/SQL/路径/堆栈 |
| 4. Throwing, catching, and translating | 在能选出正确业务语义的边界抛出；重抛已有 `NexusException` 不改写（除非有更准确状态）；捕获最窄异常；**禁止为返回伪造成功而 catch**；捕获 `InterruptedException` 需恢复中断标志；禁止 `catch (Throwable)`；端点基础设施集中映射 `R.fail(...)`，`R<T>` 不含 HTTP status 字段 |
| 5. Status-code structure | `MODULE(3 大写字母) + CATEGORY(2 位数字) + LOCAL(4 位数字)` = 9 字符；`bisCode()` 必须等于 `fullCode()`；类别按语义族选择而非 HTTP 便利；HTTP 映射表（400/401/403/404/409/429/500/502/503） |
| 6. Status-code naming and placement | `XxxStatusCode` 实现 `StatusCode`；平台级失败→base `NexusStatusCode`；领域失败→`<domain>.domain.enums`；技术状态→技术边界旁（如 `core.plugin.status.PluginStatusCode`）；`kernel` 与 `platform` 不得互引状态枚举 |
| 7. Extending the status-code catalog | 九步流程：先搜索→定归属→保留模块段→选类别→分配本地码→定义稳定元数据（双语 message/advice）→类型化构造→**先加契约测试**→审查兼容性 |
| 8. Contract-test requirements | 13 项必须被测试证明的性质（形状、类别、唯一性、命名、双语文本、HTTP 映射、cause 保留、raw-code 拒绝、端点映射不泄露堆栈等） |
| 9. Review checklists | 异常 6 条 / 状态码 6 条 / 扩展 5 条 |

---

## domain-module-initialization.md — 领域初始化

| 章节 | 内容 |
|------|------|
| 1. Scope and Principles | 1.1 确认归属；1.2 建立领域词汇表；1.3 遗留工程只能当**行为参考**不得当模板；1.4 初始面保持最小 |
| 2. Stage One: Entity | 概念清单 → 持久化范围选择 → 主键与稳定键 → 逐字段评审 → 按访问模式定索引 → **实体门禁**（9 条） |
| 3. Stage Two: DAO | DAO 规则 → 跨表读五步（先查主表→收集 ID→分批查各表→内存映射→返回组装视图）→ **DAO 门禁**（7 条） |
| 4. Stage Three: Domain Contract | 枚举 → Request record → VO record → model/事件/状态码 → **领域门禁** |
| 5. Stage Four: Conversion | MapStruct 转换器；一两个标量值不建转换器 |
| 6. Stage Five: Endpoint | 按内聚资源边界拆分 → 初始形式（具体类 + `TODO` + `UnsupportedOperationException`）→ 方法规则 → **端点规模复审**（约 7 个方法即触发边界复审）→ **端点门禁** |
| 7. Stage Six: Operator/Service | 仅在需要时创建；依赖方向 `endpoint → service → operator → dao`；允许简化 `endpoint → operator → dao`、`service → dao` |
| 8. Test-First Initialization | 10 步顺序：先写契约测试→确认红→实现 |
| 9. Mandatory Compile Gate | `mvn clean compile` 立即执行，失败不得继续加功能 |
| 10. Full Verification Gate | `mvn validate` / `mvn test` / `mvn -q help:effective-pom` / `git diff --check` + 六项人工巡检 |
| 11. Skills Documentation Is Out of Scope | 领域初始化不得触碰模块 `SKILL.md` 与 `references/` |
| 12. Completion Checklist | 边界 / 实体 / DAO 与领域 / 端点 / 验证 五组勾选 |

---

## module-skills.md — 模块技能文档

| 章节 | 内容 |
|------|------|
| Generation Policy | 仅在开发者**显式请求扫描**并指定范围时生成；功能开发、修 Bug、重构、改包、构建失败均**不得**推断为文档请求；显式请求时必须整体扫描并重生成整套 |
| SKILL.md Format | frontmatter：`name` = `<module-artifact-id>`、`description`（摘要 + 能力列表）、`version` |
| Required Sections | `# <Module Name>` → `## Module Overview`（能力表 `\| Capability \| Description \|`）→ `## Class Reference`（按包分表 `\| Class \| Type \| Description \|`）→ `## References`（到 `references/` 的映射表） |
| References Directory | 每包一个 `<package-name>.md`（点转连字符）；每类记录 Type、描述、方法签名/描述/参数/返回 |
| Consistency Rules | 一致性只要求在同一次扫描结果内；不得手工打补丁跟随孤立代码变更，应重跑扫描 |
