# 规范章节级索引

`java:reference` 对 `standards/` 下 7 份规范的章节地图。用于快速定位
「哪条规则在哪份文件的哪一节」。内容发生疑义时一律回源到 `../standards/` 原文。

跨技能专题（模块归属、**包结构**、作用域、建模、契约）见同目录下的专题参考文件，不在此重复。
包结构权威专题：[package-structure.md](package-structure.md)。

---

## code-style.md — 代码风格

| 章节 | 关键规则 |
|------|---------|
| 花括号 | 所有 `if/else/for/while` 必须用花括号，禁止单行裸语句 |
| 缩进 | 4 空格，禁止 Tab |
| 行宽 | 上限 120 字符，超长按参数换行，续行缩进 8 空格 |
| 导入顺序 | `java.*`/`javax.*` → 第三方（含 Lombok） → `com.innospots.*`，组间空行，禁止通配符 |
| 源文件组织 | 常量 → 静态可变状态 → 实例字段 → 构造器/静态工厂 → public → protected → private → 嵌套类型；一条语句一个变量；注解独占一行 |
| 多行格式 | 流式链点号置于续行行首；record 多行头一个组件一行；禁止嵌套三元；可用菱形推断 |
| Lombok | 构造器注入用 `@RequiredArgsConstructor` + `final`；日志用 `@Slf4j`（禁止 `@Sl4j` 拼写）；可变实体/配置绑定用 `@Getter`+`@Setter`；`domain.request`/`domain.vo` 是 record 不适用；**禁止 `@Data`**；敏感值禁止 Lombok `toString` |
| 领域类型 | `domain.request`/`domain.vo` 必须是 record；领域类型可封装自身不变量与行为 |
| REST 端点 | `endpoint` 包 + `*Endpoint`；默认具体类；只用 `jakarta.ws.rs`；类级 `@Path`/`@Produces`/`@Consumes`，方法级 HTTP 注解；显式参数注解；推迟实现用 `TODO` + `NexusException`；返回 `R<T>` |
| MapStruct 转换器 | 非平凡结构转换必须用 MapStruct；`converter` 包 + `*Converter`；`@Mapper(config = BaseMapperConfig.class)`；模型↔实体继承 `BaseBeanConverter` |
| MyBatis-Plus DAO | `dao` 包 + `*Dao` + `BaseMapper<Entity>`；自定义操作用 `default` 方法 + lambda wrapper；**每方法仅单表、禁止 join、禁止 XML**；N+1 禁止 |
| 依赖字段与构造 | 构造器注入 + `final`；禁止字段注入 |
| 集合与状态 | 优先不可变空集合；边界处 `List.copyOf`/`Set.copyOf`/`Map.copyOf`；不暴露内部可变集合 |
| 日志与诊断 | `@Slf4j`；禁用 `System.out/err`、`printStackTrace`；参数化日志；不记录密钥/令牌；不逐层重复记录 |
| 字面量与表达式 | 领域字面量提为常量；枚举用 `==`；可空 String 用 `"LIT".equals(x)` |

---

## naming.md — 命名

| 章节 | 关键规则 |
|------|---------|
| 命名决策顺序 | 业务概念 → 范围/变体 → 操作/视图目的 → 技术职责；`[范围]+概念+[用途]+职责` |
| 词汇规则 | 一概念一英文词；禁止 `Common/General/Base/Default/Simple/Generic` 当万能词；不编码调用方不需要的实现细节；`code/key/id/name/status/type` 需有文档化含义 |
| Java 命名 | 完整对照表：Class/Interface/`Endpoint`/`Dao`/`Entity`/`Request`/`Vo`/`Config`/`Converter`/`StatusCode`/`Event`/`Enum`/Record/Annotation/Method/常量/包 |
| 类型职责命名 | `Service` `Operator` `Manager` `Registry` `Catalog` `Router` `Provider` `Factory` `Resolver` `Discovery` `Loader` `Parser` `Validator` `Builder` `Repository` `Store` `Facade` `Holder` `Handler` `Interceptor` `Listener` 的适用条件 |
| 领域类型命名 | 实体加 `Entity`；内部模型不用 `Dto/Pojo/Bean/Data/Model` 后缀；`Base` 仅用于抽象基类 |
| 请求 | `XxxCreateRequest` / `XxxUpdateRequest` / `XxxStatusUpdateRequest` / `XxxPageRequest` / `XxxTreeRequest` / `XxxOrderRequest` / `XxxAddRequest` / `XxxReplaceRequest` |
| 视图 | 主视图 `XxxVo`，特定投影 `XxxOptionVo`；禁用大写 `VO`/`Response`/`Result`/`Dto` |
| 事件与生命周期类型 | 事件用过去时事实；`State` 运行时状态机、`Status` 业务可用性、`Mode` 运行模式、`Type` 封闭分类 |
| 查询动词 | `get` `find` `list` `page` `count` `load` `discover` `resolve` `snapshot`；禁用 `getAll`、`queryXxx`；DAO 层例外可用 `select/insert/update/delete` |
| 命令与生命周期动词 | `create` vs `of/from/named`；`add/remove` 成员、`register/unregister` 注册、`subscribe/unsubscribe` 订阅；`update`/`replace`/`save` 语义区分 |
| 布尔谓词 | `is/has/can/supports/contains/matches` 开头；布尔字段无 `is` 前缀；优先肯定式；谓词名不得隐藏修改或 I/O |
| 字段、参数与局部变量 | 限定概念 ID（`roleId`）；集合复数；时间点 `At`；数值带单位；对称命名；禁用 `temp/tmp/foo/bar/obj/data/flag` |
| 缩写与首字母缩略词 | 缩写当单词：`HttpClientBuilder`、`pluginId`；项目既定形式 `Id Url Uri Http Json Rsa Oauth Dao Vo Api Ui Db Io`；禁止 `ID/Id` 混用 |
| 包命名 | **领域优先**（`role/endpoint`，禁止 `endpoint/role`）；**功能子模块**（`permission.authorization`，禁止 service 堆积）；**单包 ≤15** `.java`；专题见 [package-structure.md](package-structure.md)；`domain.{entity,request,vo,model,enums,event}`；职责包 `endpoint`/`api`/`operator`/`service`/`dao`/…；禁止 `impl`/`common`/`misc`/`util` |
| 持久化命名 | 表/列小写 snake_case；Nexus 表 `nx_` 前缀 + 单数；`TABLE_NAME` 常量；唯一索引 `uk_`、普通索引 `idx_`；ID 前缀短小稳定 |
| 文件与测试命名 | 一文件一顶层 public 类型；单测 `{Type}Test.java`，契约族 `{Concept}ContractsTest.java`；测试方法 lowerCamelCase 行为短语、**无 `test` 前缀** |
| 应避免的反例 | 反例对照表 |
| 命名检查清单 | 10 条自检 |

---

## api-design.md — API 设计

| 章节 | 关键规则 |
|------|---------|
| 方法签名 | 优先静态工厂 `of/create/from/named`；返回不可变集合；`Optional<T>` 仅用于应用侧单值结果，**禁用于参数/字段/集合**；简单数据载体用 record |
| 契约与实现边界 | 四类引入接口的条件；**不得仅为 mock 建接口**；`DefaultXxx` 仅在存在其他合法实现时使用 |
| 不可变性 | 字段尽量 `final`；访问器返回不可变副本；构造器对可变参数防御性拷贝 |
| 空值处理 | 必填空值抛 `NexusException` + `StatusCode`；禁止 `Objects.requireNonNull`/`IllegalArgumentException` 做业务校验；可选参数 `null→default`/`null→skip`；集合结果不返回 `null`；禁止 `Optional<List<T>>` |
| 校验与归一化 | record 紧凑构造器管不变量；请求 `validate()` 管字段组合；operator 管数据前置条件与映射缺失；service 管工作流/授权/跨记录；endpoint 只管传输层 |
| 异常处理 | 见 `exception-status-code.md` |
| 持久化实体 | 默认 `WorkspaceBaseEntity`；可选 `TenantBaseEntity`/`BaseEntity`；`ProjectBaseEntity` 仅设计评审批准后；禁止裸 `projectId` 列；主键 `String` + `@TableId(ASSIGN_UUID)` + `@Id` + `@Column(length=32)`；字符串长度为 2 的幂；`TABLE_NAME` 常量双注解共用；Lombok `@Getter`+`@Setter`；显式 `@Table(indexes=...)` |
| 领域模型 | `domain` 下按 `entity/request/vo/model/enums` 划分；配置类放模块级 `config` 包，不放 `domain` |
| REST 端点契约 | `*Endpoint` + `endpoint` 包；`jakarta.ws.rs`；每个方法返回 `R<T>`/`R<PageResult<T>>`/`R<Void>`；**service/operator 不得返回 `R`** |
| DAO 契约 | 同 `code-style.md`；跨表读用分批查询+内存组装；跨表写交由事务 service |
| Service 与 Operator 边界 | operator 简单数据操作，不得依赖 service 或另一 operator；service 复杂工作流；分页返回 `PageResult<T>` |
| 查询与命令语义 | 查询不改状态；`create` 遇重复稳定键失败；`update` 不接受不可变稳定键；`replace` 需定义省略是否删除；`delete` 需定义缺失是成功还是未找到；生命周期操作需定义重复调用行为 |
| 领域事件与 EventBus | 发布域拥有事件契约，放 `domain.event`，实现 `DomainEvent`；`kernel` 与 `platform` **不得互相引用事件类型**；事件是不可变 record；状态变更成功后才发布；`publish` 异步通知、`publishSync` 仅在真正需要立即结果时用；订阅者负责清理 |
| 领域转换 | 同 `code-style.md` 的 MapStruct 规则 |
| 密码注册请求 | 请求携带前端加密载荷，不含 hash/salt/algorithm；通过公共模块接口解密 |
| 分页请求 | 分页请求对象放 `domain.request`；可组合 `SimpleQueryRequest`；方法收请求对象而非散参数 |
| 事务边界 | 只用 `jakarta.transaction.Transactional`；优先方法级而非类级；简单单表读不加事务 |
| 流式 API | setter 式方法返回 `this`；`@SuppressWarnings("unchecked")` 需证明安全并注释 |
| 生命周期与资源归属 | `initialize`→`start`→`stop`→`destroy/close`；创建者负责清理；逆序释放；清理失败不得吞掉主失败 |
| 线程安全与并发 | 可变 public 类型需声明线程安全策略；一种不变量一种同步机制；**持锁时不得回调未知代码**；返回不可变快照；不得吞掉中断 |
| 公共契约兼容性 | 兼容面清单：类型名/签名、REST 路径与字段、表名列名/稳定键/ID 前缀、事件类型串/配置键/插件 ID/序列化字段名；废弃用 `@Deprecated` + `@deprecated` 并指明替代 |
| API 评审检查清单 | 8 条自检 |

---

## code-comments.md — 注释

| 章节 | 关键规则 |
|------|---------|
| 包文档 | `package-info.java` 用于公共契约/架构边界/非显然约束；说明职责、归属、依赖方向、生命周期与线程安全假设；禁止空洞文档 |
| 类型注释 | 每个 public 类型必须有 Javadoc：职责与约束 + **必填** `@author`、`@date`（`yyyy/MM/dd`）；有关联契约/端点/模块时**必填** `@see`；泛型用 `@param <T>`；实现类讲策略不复制接口文本；record/枚举/注解按专节 |
| 方法注释 | public/protected 必须有 Javadoc：`@param`/`@return`/`@throws`；补充可空性、所有权、变更、幂等、线程安全、安全、生命周期；record 访问器、平凡 getter/setter、枚举 `values/valueOf`、完整继承的覆写无需重复注释；方法级不要求 `@author`/`@date` |
| 行内注释 | 解释原因（why）而非表象（what）；算法理由、边界情况、非显然副作用、多步流程、生命周期清理、并发不变式、安全取舍、兼容性 |
| TODO 与推迟行为 | 仅用于有意推迟且有明确未来边界；**禁止裸 `TODO`/`FIXME`**；不得用 TODO 为伪造成功数据/吞异常开脱；实现时同变更移除 |
| 禁止的注释 | 禁止注释掉的代码；禁止在方法/行内写作者或修改历史；禁止工单/变更日志；禁止复述代码、承诺未实现行为、泄露密钥；不得用注释弥补坏命名（应改名） |
| 注释评审检查清单 | 8 条自检 |

---

## exception-status-code.md — 异常与状态码

| 章节 | 关键规则 |
|------|---------|
| 1. 原则 | 一次失败 = 一个归属边界 + 一个稳定状态码 + 一条有用 cause 链；状态码表应用语义，HTTP 只表传输结果 |
| 2. 异常分类 | 应用失败→**仅** `NexusException`；基础设施失败→边界翻译并保留 cause；**禁止**业务路径抛 JDK 通用异常；中断/取消→保留语义；致命 JVM 错误→不得捕获 |
| 3. 构造 `NexusException` | 默认 `build(StatusCode, ...)` 类型化重载；字符串重载**仅限**互操作边界，需全码解析器 + 显式白名单 + 结构化日志记录来源 + 尽量翻译为类型化状态；`build(StatusCode, String)` 的运行时消息同样是响应面，禁止密钥/ID/用户输入/SQL/路径/堆栈 |
| 4. 抛出、捕获与翻译 | 在能选出正确业务语义的边界抛出；重抛已有 `NexusException` 不改写（除非有更准确状态）；捕获最窄异常；**禁止为返回伪造成功而 catch**；捕获 `InterruptedException` 需恢复中断标志；禁止 `catch (Throwable)`；端点基础设施集中映射 `R.fail(...)`，`R<T>` 不含 HTTP status 字段 |
| 5. 状态码结构 | `MODULE(3 大写字母) + CATEGORY(2 位数字) + LOCAL(4 位数字)` = 9 字符；`bisCode()` 必须等于 `fullCode()`；类别按语义族选择而非 HTTP 便利；HTTP 映射表（400/401/403/404/409/429/500/502/503） |
| 6. 状态码命名与归属 | `XxxStatusCode` 实现 `StatusCode`；平台级失败→base `NexusStatusCode`；领域失败→`<domain>.domain.enums`；技术状态→技术边界旁（如 `core.plugin.status.PluginStatusCode`）；`kernel` 与 `platform` 不得互引状态枚举 |
| 7. 扩展状态码目录 | 九步流程：先搜索→定归属→保留模块段→选类别→分配本地码→定义稳定元数据（双语 message/advice）→类型化构造→**先加契约测试**→审查兼容性 |
| 8. 契约测试要求 | 13 项必须被测试证明的性质（形状、类别、唯一性、命名、双语文本、HTTP 映射、cause 保留、原始码拒绝、端点映射不泄露堆栈等） |
| 9. 评审检查清单 | 异常 6 条 / 状态码 6 条 / 扩展 5 条 |

---

## domain-module-initialization.md — 领域初始化

| 章节 | 内容 |
|------|------|
| 1. 范围与原则 | 1.1 确认归属；1.2 建立领域词汇表；1.3 遗留工程只能当**行为参考**不得当模板；1.4 初始面保持最小 |
| 2. 阶段一：实体 | 概念清单 → 持久化范围选择 → 主键与稳定键 → 逐字段评审 → 按访问模式定索引 → **实体门禁**（9 条） |
| 3. 阶段二：DAO | DAO 规则 → 跨表读五步（先查主表→收集 ID→分批查各表→内存映射→返回组装视图）→ **DAO 门禁**（7 条） |
| 4. 阶段三：领域契约 | 枚举 → Request record → VO record → model/事件/状态码 → **领域门禁** |
| 5. 阶段四：转换 | MapStruct 转换器；一两个标量值不建转换器 |
| 6. 阶段五：端点 | 按内聚资源边界拆分 → 初始形式（具体类 + `TODO` + `NexusException`）→ 方法规则 → **端点规模复审**（约 7 个方法即触发边界复审）→ **端点门禁** |
| 7. 阶段六：Operator/Service | 仅在需要时创建；依赖方向 `endpoint → service → operator → dao`；允许简化 `endpoint → operator → dao`、`service → dao` |
| 8. 测试先行初始化 | 10 步顺序：先写契约测试→确认红→实现 |
| 9. 强制编译门禁 | `mvn clean compile` 立即执行，失败不得继续加功能 |
| 10. 完整验证门禁 | `mvn validate` / `mvn test` / `mvn -q help:effective-pom` / `git diff --check` + 六项人工巡检 |
| 11. 模块 API 文档不在范围内 | 领域初始化不得触碰模块 API 索引 `README.md` 与 `references/` |
| 12. 完成检查清单 | 边界 / 实体 / DAO 与领域 / 端点 / 验证 五组勾选 |

---

## module-skills.md — 模块 API 参考

| 章节 | 内容 |
|------|------|
| 生成策略 | 仅在开发者**显式请求扫描**并指定范围时生成；**不是**可安装技能；功能开发、修 Bug、重构、改包、构建失败均**不得**推断为文档请求 |
| README.md 格式 | 纯 Markdown 索引，**无 YAML front matter**；路径 `references/modules/<artifact-id>/README.md` |
| 必填章节 | 标题 + 非技能说明 → `## 模块概览` → `## 类参考` → `## 包参考` |
| references 目录 | 每包一个 `<package-name>.md`（点转连字符）；每类记录类型、描述、方法签名/描述/参数/返回 |
| 一致性规则 | 一致性只要求在同一次扫描结果内；不得手工打补丁跟随孤立代码变更，应重跑扫描 |
