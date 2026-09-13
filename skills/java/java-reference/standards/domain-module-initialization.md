# 新领域模块初始化

本文档定义在现有 Nexus Java 模块内初始化业务领域的必需工作流。适用于 `role`、`menu`、`permission`、`dictionary` 及其他管理能力等领域。

工作流分阶段进行。在进入下一阶段之前完成并评审每个阶段。不要先生成整个包树再事后辩解。

## 1. 范围与原则

### 1.1 确认归属

创建文件之前，确定：

- 哪个 Maven 模块拥有该业务能力；
- 该概念是业务特定还是可复用基础设施；
- 记录是平台级、租户作用域还是工作空间作用域；
- 哪些相邻领域与之交互；
- 哪些行为现在需要，哪些有意延后。

具体管理领域通常属于 `innospots-nexus-kernel`。业务中立的 console 契约属于 `innospots-nexus-console`。不含具体业务概念的数据库和中间件基础属于 `innospots-nexus-core`。

除非边界、依赖方向和独立可测试性清晰，不要创建新 Maven 模块。

### 1.2 建立领域词汇

命名包或类型之前，写下领域所需的小词汇表：

- 主要业务概念及任何关联概念；
- 归属范围（`platform`、tenant、workspace、realm 或其他明确批准的范围）；
- 技术 ID 与稳定业务键的区别；
- 生命周期术语及 `state`、`status`、`mode` 和 `type` 出现时的确切含义；
- 用于创建、更新、查询、排序、成员关系和运行时视图的名称；
- 属于其他领域、不得作为本地同义词复用的相邻概念。
- 哪些失败是平台级、领域特定或技术性的，以及各自的状态码归属；
- 所有应用可见失败必须使用 `NexusException` + 类型化 `StatusCode`；禁止以 JDK 通用异常作为业务失败契约（见 [`exception-status-code.md`](exception-status-code.md)）。

按 [`naming.md`](naming.md) 定义的顺序构造名称：业务概念、必要的范围/变体、操作或视图目的，然后技术职责。在创建源文件之前解决竞争术语。不要让 endpoint、entity、DAO 和数据库名称对同一概念引入不同词汇。状态码归属、异常翻译和扩展规则见 [`exception-status-code.md`](exception-status-code.md)；在创建枚举或 endpoint 契约之前决定这些边界。

### 1.3 将遗留项目作为行为参考

可检查遗留项目以理解：

- 业务术语；
- 常见管理操作；
- 生命周期规则；
- 树、成员关系、排序和状态行为；
- 更新间必须保持稳定的数据；
- 历史边界情况和受保护记录。

遗留项目不得作为源模板。切勿：

- 复制或移动遗留源文件；
- 机械复现其包结构；
- 保留过时的框架注解或依赖；
- 在无当前理由时沿用反规范化或重复字段；
- 合并现在属于不同领域的概念；
- 复现遗留 POM 结构。

每个字段、类型、endpoint 和依赖必须对照当前模块边界和开发者意图进行论证。

### 1.4 领域优先的包结构

业务源码包**必须先按领域、再按职责**组织：

```text
<domain>/endpoint、<domain>/dao、<domain>/domain/entity …
```

**禁止**技术层优先（`endpoint/<domain>`、`dao/<domain>`、`domain/<domain>/entity`）。
**禁止**在模块根或单个扁平 `service` 包内堆积全部业务；大领域须按功能子模块
（`authorization`、`grant`、`entry` …）划分，且**单包 ≤15** 个 `.java`。
理由、标准骨架与自检清单见 [`package-structure.md`](../references/package-structure.md)。

### 1.5 保持初始表面最小

仅创建具有即时职责的包。典型领域可能包含：

```text
<domain>
  ├── dao
  ├── domain
  │   ├── entity
  │   ├── enums
  │   ├── request
  │   └── vo
  └── endpoint
```

仅当当前任务需要其职责时，才添加 `model`、`converter`、`operator`、`service`、`event`、`handler` 或其他包。不要创建空的架构层或占位类型。

## 2. 阶段一：定义实体数据结构

实体设计是第一实现阶段，因为它确立领域身份、归属、持久化范围、唯一性、关系以及后续契约所需的数据。

### 2.1 清点领域概念

编写 Java 之前列出领域记录：

- 聚合或主记录；
- 关联记录；
- 父子关系；
- 稳定业务键；
- 生命周期和可见性字段；
- 排序字段；
- 受保护或内置标志；
- 从共享实体继承的审计和 tenant/workspace 归属。

不要仅因遗留项目将数据存储在一起就合并相邻领域。例如，当 permission 独立拥有时，菜单导航和 permission 资源必须保持分离。

### 2.2 选择持久化范围

工作空间拥有的业务记录默认使用 `WorkspaceBaseEntity`。记录为租户作用域但非工作空间作用域时使用 `TenantBaseEntity`。仅当记录明确为平台级或 realm 全局时使用 `BaseEntity`。

切勿重新声明继承字段，例如：

- `tenantId`；
- `workspaceId`；
- `createdAt`；
- `updatedAt`；
- `createdBy`；
- `updatedBy`。

### 2.3 定义身份与稳定键

每个具体持久化实体必须：

- 使用 `String` 主键；
- 使用 `@TableId(type = IdType.ASSIGN_UUID)`；
- 使用 Jakarta Persistence `@Id`；
- 使用 `@Column(length = 32, nullable = false)`；
- 声明 `public static final String TABLE_NAME`；
- 在 `@Table` 和 `@TableName` 中使用同一常量。

区分技术主键与稳定业务键。稳定键如 `roleCode` 或 `menuKey` 通常应：

- 在其归属范围内唯一；
- 创建后不可变；
- 变更会破坏引用时，从 update 请求中排除。

用简短稳定的领域前缀重写 `BaseEntity.idPrefix()`。主键由 `DbPrimaryGenerator` 通过 `IdGenerator.ulid(prefix)` 集中生成；operator 不得手动生成主键。

### 2.4 评审每个字段

对每个提议字段，记录或确定：

- 业务含义；
- 必需或可选状态；
- Java 类型；
- 持久化长度；
- 默认值责任；
- 是否可变；
- 是否参与索引；
- 是否属于本领域。

字符串长度使用 2 的幂。有意无界文本使用 `@Lob`。除非项目建立其他显式映射，枚举值存为 String。

不要添加：

- 重复的继承审计或 tenant/workspace 字段；
- 仅用于响应的瞬态字段到实体；
- 无已证明查询需求的缓存祖先路径；
- 无关导航实体的 HTTP 方法或 permission 操作；
- 框架特定传输对象；
- 仅因遗留实体包含而保留的字段。

### 2.5 从访问模式定义索引

为以下情况声明显式 Jakarta Persistence 索引：

- 稳定唯一查找；
- 外键或父查找；
- 常见状态和分页查询过滤；
- 关联唯一性；
- 树查询需要时的兄弟排序。

索引名必须显式且带表前缀。工作空间作用域唯一性通常包含 `workspace_id`。租户作用域唯一性通常包含 `tenant_id`。

### 2.6 实体关卡

在所有答案为是之前不要继续：

- 实体是否在正确的模块和领域中？
- 平台与 tenant/workspace 范围是否正确？
- 主键与稳定业务键是否清晰分离？
- 必需字段是否存在？
- 过时或相邻领域字段是否已移除？
- 可空性和 String 长度是否有意？
- 索引是否匹配预期查询路径？
- 实体是否使用 Lombok `@Getter` 和 `@Setter`？
- JPA 和 MyBatis-Plus 注解是否都完整？

实现实体之前添加实体契约测试。测试应验证表、继承、标识符注解、必需字段、长度、可空性和索引。运行测试并确认在添加生产代码之前因缺失契约而按预期失败。

实体类型、字段、表、列、索引和 ID 前缀名称必须遵循 [`naming.md`](naming.md)。Lombok、注解放置、成员顺序和集合规则遵循 [`code-style.md`](code-style.md)。实体 Javadoc 必须按 [`code-comments.md`](code-comments.md) 描述持久化范围和重要不变式。

## 3. 阶段二：定义 DAO 契约

实体契约稳定后创建 DAO。

### 3.1 DAO 规则

- 将 DAO 放在 `<domain>.dao` 下。
- 使用 `*Dao` 后缀。
- 继承 `BaseMapper<EntityType>`。
- 优先使用继承的 CRUD 操作。
- 不要创建 mapper XML 文件。
- 不要在 DAO 中放置业务工作流或事务编排。
- 每个 DAO 方法必须只访问一张表。
- 不要在 wrapper、注解 SQL、XML 或手写持久化语句中使用 SQL join。

仅当已知访问模式无法通过继承操作清晰表达时才添加自定义 DAO 方法。优先：

- 带 MyBatis-Plus lambda wrapper 的 `default` 方法，用于可复用谓词；
- 作为 SQL 更清晰的显式单表查询的注解 SQL。

初始化期间不要发明推测性查询。

面向 MyBatis 的 DAO 方法可使用 `select`、`insert`、`update` 和 `delete` 动词与 `BaseMapper` 对齐，如 `selectByRoleCode`。面向应用的 operator 和 service 使用 [`naming.md`](naming.md) 和 [`api-design.md`](api-design.md) 定义的 `find`、`list`、`page` 和 `count` 词汇。DAO 方法在 mapper 契约要求时可返回可空实体；在 operator 或 service 边界规范化缺失。

跨表数据必须在 DAO 外组装：

1. 查询所属或关联表；
2. 收集所有必需的标识符或稳定键；
3. 独立批量查询每个相关表；
4. 在内存中映射记录；
5. 返回组装后的领域视图。

禁止 N+1 查询模式。切勿对较早结果中的每一行查询一次相关表。

跨表写入、级联清理、关系完整性和稳定键重命名必须由带事务的 service 协调。例如，当关联存储 `tagName` 时，重命名 tag 定义必须在同一事务中通过独立 DAO 操作更新两张表。

### 3.2 DAO 关卡

验证：

- 每个 DAO 恰好映射一个持久化实体；
- 关联有自身实体时拥有独立 DAO；
- 无 DAO 依赖 service 或 operator；
- 自定义方法仅描述直接数据库操作；
- 每个方法可证明为单表；
- 结果组装使用批量查询而非 N+1 读取；
- 契约测试确认 `BaseMapper` 泛型实体。

## 4. 阶段三：定义领域契约

持久化形态已知后，定义 endpoint 和未来 service 所需的面向业务数据结构。

### 4.1 枚举

将领域特定枚举放在 `domain.enums` 下。

枚举应表示封闭业务概念，而非传输细节。当语义匹配时复用平台枚举如 `BasicStatus`。

不要合并由其他领域拥有的类别。例如，菜单类型可描述目录、页面和外部链接导航，而不同时定义按钮或 API permission。

### 4.2 请求 Record

将 endpoint 输入类型放在 `domain.request` 下，使用带 `Request` 后缀的 Java record。

当操作具有不同变更权限时创建独立请求：

- create 请求；
- update 请求；
- status 请求；
- 分页或树查询请求；
- 排序请求；
- 关联 add 或 replace 请求。

不要将实体复用为 endpoint 请求。

每个请求从资源后跟其确切操作或查询目的命名，如 `RoleCreateRequest`、`RoleStatusUpdateRequest` 或 `RolePageRequest`。当操作具有不同变更权限时，不要引入通用 `XxxRequest`。

创建请求可包含稳定业务键。update 请求应排除不可变键和受保护系统字段。

对于查询绑定请求：

- 使用显式 Jakarta REST 注解如 `@QueryParam`；
- 在适当时使用 `@DefaultValue`；
- bean 绑定需要时提供无参构造函数；
- 将无效分页值规范化为共享默认值。

对于集合组件：

- 当该含义有效时将 `null` 转为空不可变集合；
- 使用 `List.copyOf`、`Set.copyOf` 或 `Map.copyOf`；
- 切勿暴露调用方拥有的可变集合。

### 4.3 视图 Record

将响应类型放在 `domain.vo` 下，使用带 `Vo` 后缀的 Java record。

按用例分离视图：

- 详情或树视图；
- 紧凑选择器选项；
- 成员或关联视图；
- 运行时/导航视图。

不要直接暴露持久化实体。VO 可包含：

- 枚举类型而非持久化枚举 String；
- 派生计数；
- 树的子视图；
- 管理界面所需的审计时间戳；
- 为只读运行时边界定制的紧凑字段。

在紧凑构造函数中防御性复制子字段和集合字段。

主要视图使用 `XxxVo`，对不同投影在 `Vo` 前立即放置用例限定词，如 `RoleOptionVo` 或 `NavigationMenuVo`。不要在 `domain.vo` 下使用 `DTO`、大写 `VO` 或 `Response`。

### 4.4 模型、事件与状态码

仅当内部业务行为需要既非持久化实体也非传输 record 的模型时，才创建 `domain.model`。

仅对实际解耦交互创建领域事件。发布领域拥有事件契约。

仅当现有平台状态码无法准确表达业务失败时，才创建领域特定状态码枚举。遵循 [`exception-status-code.md`](exception-status-code.md) 的码归属、类别选择、双语元数据、HTTP 映射和扩展安全。

### 4.5 领域关卡

验证：

- 请求和 VO 是 record；
- 名称反映其用例；
- create 和 update 变更权限在必要时不同；
- 集合不可变；
- 在适当时将仅用于持久化的 String 暴露为领域枚举；
- 无 permission、user、role、menu 或其他相邻关注点跨越领域归属泄漏；
- 未创建推测性 model 或 event 包。

对每个应用可见失败，还要验证：

- 在提议新码之前搜索了已有状态码；
- 平台、领域或技术归属明确；
- module 段和四位 local 码唯一且稳定；
- category、HTTP 映射、英文 message 和中文 advice 有意；
- 异常构造使用类型化 `StatusCode`，翻译基础设施失败时保留 cause，且不创建每错误一个异常子类；
- 原始码互操作不存在或通过显式白名单校验。

## 5. 阶段四：定义转换边界

当 request、model、entity 和 VO 之间需要结构映射时，创建 MapStruct converter。

- 放在 `<domain>.converter` 下。
- 使用 `*Converter` 后缀。
- 使用 `@Mapper(config = BaseMapperConfig.class)`。
- 转换 model 和 entity 对时继承 `BaseBeanConverter<ModelType, EntityType>`。

不要仅为复制一两个标量值而创建 converter。不要在 endpoint、service 或 operator 中编写大块逐字段映射。

converter 名称和方法动词遵循 [`naming.md`](naming.md)。当泛型契约未使双方明显时，结构映射方法应声明两种表示，如 `requestToModel` 或 `modelToEntity`。

当方法有意未实现且尚未发生转换时，可延后 converter 包。

## 6. 阶段五：定义 Endpoint 边界

endpoint 定义传输边界。它们不拥有持久化或业务工作流。

### 6.1 按内聚资源边界拆分

从一个内聚资源生命周期的一个 endpoint 开始。当操作具有不同的以下特征时拆分 endpoint：

- 资源归属；
- 路径层次；
- 授权上下文；
- 读/写特征；
- 消费者；
- service 依赖；
- 预期速率或生命周期。

常见拆分示例包括：

- 角色生命周期与角色成员关系；
- 菜单管理与当前用户导航；
- permission 定义与角色 permission 分配；
- 用户资料与认证凭证。

不要创建一个累积与宽泛业务名词相关的所有操作的 endpoint。

### 6.2 初始 Endpoint 形式

领域初始化期间，除非开发者明确要求传输契约接口，否则将 Endpoint 类型创建为具体类。

当 endpoint 行为延后时：

- 声明完整的 Jakarta REST 注解和方法签名；
- 视情况返回 `R<T>`、`R<PageResult<T>>` 或 `R<Void>`；
- 添加描述未来 service 边界的聚焦 `TODO`；
- 抛出 `NexusException.build(合适的 StatusCode, "…尚未实现")`；
- 不要返回伪造的成功响应或空数据。

这使延后行为明确，并防止不完整的 endpoint 看起来可用。

### 6.3 Endpoint 方法规则

- 仅使用 Jakarta REST 注解。
- 资源路径在类级别，操作路径在方法级别。
- 显式注解所有 path、query、header 和 bean 绑定参数。
- 将请求校验、事务、持久化和编排保持在 endpoint 之外。
- 操作有结构化输入时使用一个请求 record。
- 一致使用名词和 HTTP 语义。
- 单资源操作将稳定标识符放在路径中。
- 优先使用专用排序请求而非重复标量参数。
- endpoint 方法使用与 service 边界相同的命令/查询词汇；不要使用模糊的 `process`、`handle` 或 `execute` 名称。
- 应用 [`api-design.md`](api-design.md) 中的可空性、校验、集合和兼容性规则。

### 6.4 Endpoint 规模评审

当出现以下一个或多个信号时拆分 endpoint：

- 同时管理生命周期和成员关系/分配；
- 混合管理写入与当前用户运行时读取；
- 无关方法将依赖不同的未来 service；
- 类级路径不再自然描述所有方法；
- 操作级授权将根本不同；
- 添加另一方法需要模糊名称以避免冲突。

仅方法数量不是决定因素，但超过约七个内聚操作应触发边界评审。

### 6.5 Endpoint 关卡

验证：

- endpoint 边界内聚；
- 在适当时分离管理和运行时关注点；
- 每个方法返回共享 `R` 包装；
- 请求和响应类型是领域 record；
- 所有参数显式注解；
- 延后方法明确失败；
- 无 endpoint 直接依赖 DAO；
- 未将 permission 资源操作插入另一领域。

为类形式、路径、HTTP 注解、方法签名、request/VO record 和显式延后行为添加基于反射的 endpoint 契约测试。

## 7. 阶段六：仅在需要时添加 Operator 和 Service

不要自动脚手架 operator 和 service。

实现主要对一个 DAO 的直接数据操作时创建 operator。仅当操作仍简单且内聚时，operator 可协调多个 DAO。

实现以下情况时创建 service：

- 非平凡工作流或校验；
- 跨多个写入的事务；
- 跨多个 operator 的协调；
- 跨领域行为；
- 树级联、受保护记录或生命周期编排；
- 授权感知的组装。

依赖方向必须保持：

```text
endpoint -> service -> operator -> dao
```

允许的简化取决于行为，例如：

```text
endpoint -> operator -> dao
service -> dao
```

operator 不得依赖另一个 operator 或 service。endpoint 不得直接编排 DAO。

在选择 `Operator`、`Service`、`Manager`、`Registry` 或其他技术后缀之前，使用 [`naming.md`](naming.md) 中的职责定义。构造函数注入、日志和依赖字段遵循 [`code-style.md`](code-style.md)。查询/命令语义、事务边界、幂等性、生命周期和资源归属遵循 [`api-design.md`](api-design.md)。

## 8. 测试优先初始化工作流

使用聚焦契约测试在生产类型存在之前确立预期形态。

推荐顺序：

1. 编写实体契约测试。
2. 运行并确认因缺失实体契约而失败。
3. 实现实体。
4. 编写 DAO 泛型绑定测试。
5. 实现 DAO。
6. 编写 request、VO、枚举、状态码和 endpoint 契约测试。领域添加失败时包含完整码形态、local 唯一性、元数据、HTTP 映射以及类型化异常/cause 翻译测试。
7. 运行并确认因缺失契约而失败。
8. 实现领域 record、状态枚举（如需要）和 endpoint。
9. 重新运行聚焦测试直至通过。
10. 运行完整项目验证。

不要仅为迁就意外实现而削弱有效测试。当测试失败揭示预先存在的无关问题时，将其与预期红状态区分，并在本地依赖工件过时时使用适当的 reactor 构建如 `-am`。

## 9. 强制编译关卡

每批 Java 源码变更之后，立即运行：

```bash
mvn clean compile
```

此命令是强制的。不要推迟到任务结束才编译。不要降低配置的 Java release 以匹配较旧的本地 JDK。

若编译失败：

1. 识别失败是否由新领域变更引起；
2. 修正 import、签名、注解、泛型或模块依赖；
3. 重新运行 `mvn clean compile`；
4. 源码树不可编译时不要继续添加功能。

## 10. 完整验证关卡

聚焦测试和编译通过后，运行：

```bash
mvn validate
mvn test
mvn -q help:effective-pom
git diff --check
```

还要检查：

- `git status --short` 是否有意外文件；
- 领域树是否有空或不必要的包；
- import 是否有禁止的框架耦合；
- 实体字段是否有缺失或无关数据；
- endpoint 路径和方法数量是否有边界漂移；
- 状态码归属、完整码唯一性、HTTP 映射和异常翻译是否对照 [`exception-status-code.md`](exception-status-code.md)；
- diff 是否有复制的遗留代码或机械包复现。

若本地 JDK 低于配置的 release，报告不匹配而非更改项目基线。

## 11. 技能文档不在范围内

普通领域初始化不得创建、编辑、同步或删除：

- `references/modules/` 下的模块 API 索引 `README.md` 文件；
- 模块 `references/` 包详情目录下的文件；
- 生成的技能索引。

技能文档仅在开发者明确请求完整扫描并标识模块或项目范围时更新。领域代码变更不得触发增量技能文档维护。

## 12. 完成清单

### 边界

- [ ] 所属 Maven 模块正确。
- [ ] 领域不重复 base、core 或 console 职责。
- [ ] 遗留代码仅用于理解行为。
- [ ] 相邻领域保持分离。

### 实体

- [ ] 持久化范围和超类正确。
- [ ] 主键注解和长度完整。
- [ ] 稳定业务键和变更规则明确。
- [ ] 已评审必需字段、可空性和长度。
- [ ] 索引匹配唯一性和查询路径。
- [ ] 未重复继承字段。

### DAO 与领域

- [ ] DAO 继承正确的 `BaseMapper` 类型。
- [ ] 每个 DAO 方法访问一张表且无 join。
- [ ] 跨表视图设计为独立批量查询和内存组装。
- [ ] 跨表写入和稳定键传播分配给事务性 service。
- [ ] 请求和 VO 是不可变 record。
- [ ] 集合组件防御性复制。
- [ ] 枚举仅表达本领域。
- [ ] 未创建推测性层或空包。

### Endpoint

- [ ] endpoint 边界内聚且刻意拆分。
- [ ] Jakarta REST 路径和参数注解显式。
- [ ] 每个方法返回共享响应包装。
- [ ] 延后方法使用 `TODO` 和 `NexusException`（禁止 `UnsupportedOperationException`）。
- [ ] endpoint 方法不含持久化或工作流实现。

### 验证

- [ ] 契约测试在实现前观察到失败。
- [ ] 聚焦测试通过。
- [ ] Java 变更后 `mvn clean compile` 通过。
- [ ] `mvn validate` 通过。
- [ ] `mvn test` 通过。
- [ ] `mvn -q help:effective-pom` 通过。
- [ ] `git diff --check` 通过。
- [ ] 未生成或修改技能文档。
