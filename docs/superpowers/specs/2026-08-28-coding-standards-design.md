# 编码规范改进设计

**日期：** 2026-08-28

**范围：** `standards/` 下的日常开发规范，以 naming 为重点。
`module-skills.md` 保持不变，因为 module skill 文档由开发者触发的 scan 策略单独治理。

## 目标

使编码规范成为从当前 Nexus 代码库推导出的连贯决策体系。开发者应能选择名称、
组织 Java 源码、文档化契约、设计 API 并初始化 domain，
而无需在文件间解决矛盾或机械复制邻近代码。

## 事实来源

更新从当前 `base`、`core`、`console`、
`portal`、`platform` 源码树推导约定。代表性既有模式包括：

- domain 角色如 `RoleEntity`、`RoleDao`、`RoleCreateRequest`、
  `RoleOptionVo`、`TenantCreatedEvent`；
- platform infrastructure 角色如 `PluginManager`、`CapabilityRegistry`、
  `CapabilityRouter`、`CapabilityProvider`、`ClasspathPluginDiscovery`；
- 行为导向方法如 `findByUserId`、`listActiveTenantIds`、
  `publishSync`、`hasAvailableThread`、`validatePassword`；
- 作用域概念如 `PlatformUser`、`TenantMember`、`WorkspaceBaseEntity`、
  `CurrentAuthorizationEndpoint`。

既有名称是项目词汇的证据，不自动等于好示例。规范将优先清晰完整词汇与一致语义，
而非保留每个历史缩写或不规则命名。

## 文件与职责

### `standards/naming.md`

保留现有 Java、package、file naming 章节并新增：

1. 命名决策顺序：business concept、scope、responsibility，然后
   technical form。
2. 词汇规则：精确名词、一词一概念、上下文限定、避免模糊名。
3. domain object 与 technical role 的类型角色规则，包括
   `Manager`、`Registry`、`Router`、`Provider`、`Factory`、`Resolver`、
   `Discovery`、`Loader`、`Parser`、`Validator`、`Builder`、`Repository`、
   `Facade`、`Holder`。
4. read、collection、creation、mutation、lifecycle、
   validation、conversion、boolean predicate 的方法动词规则。
5. identifier、collection、boolean、time、
   count、unit、paired value 的字段与参数规则。
6. 缩写与 acronym 大小写规则，包括 `Id`、`Url`、`Http`、`Json`、`Rsa`、
   `Dao`、`Vo`、`Api`、`Ui` 等既有术语。
7. request/VO 命名规则：business resource 放在 operation 或
   view purpose 之前。
8. Java field、table/column name、index、
   identifier prefix 的 persistence 命名规则。
9. test class 与行为导向 test method 的测试命名规则。
10. 正反示例与最终 naming checklist。

### `standards/code-style.md`

澄清当前代码中反复出现的源码布局与实现约定：

- declaration 与 member 顺序；
- annotation、record、fluent-call、lambda、multiline 格式化；
- 对 mutable entity、configuration binder、internal model 的合适 Lombok 用法，
  而非将所有 domain class 一视同仁；
- dependency field、constructor injection、logging、collection 处理、
  避免 hidden mutable state；
- 将 formatting 规则与 API、architectural 规则分离，
  在有用处以聚焦交叉引用替代重复章节。

### `standards/code-comments.md`

扩展 package 文档、record 与 record component、
enum、override 方法、contract 与 implementation Javadoc、TODO、
以及解释 lifecycle、concurrency、security、compatibility
constraint 的注释规则。示例将强化 intent-focused 注释，
而不要求在自解释的 private implementation detail 上制造噪音。

### `standards/api-design.md`

围绕以下方面解决并扩展 API 指导：

- interface/implementation boundary 与 dependency injection；
- nullability 与 `Optional`，包括 framework-facing DAO exception；
- command/query method 语义、collection 返回类型、boolean
  predicate；
- validation ownership 与 normalization boundary；
- lifecycle、resource ownership、idempotency、thread safety、public API
  compatibility（当前 plugin、event、resource、session 代码有真实需求处）；
- 对 naming 与 source-style 规则的显式交叉引用，而非重复。

### `standards/domain-module-initialization.md`

保留现有分阶段工作流。仅做 targeted consistency 更新：

- 创建类型前要求 terminology 与 naming 决策；
- 使 domain model、converter、service、operator、endpoint gate 与
  澄清后的 naming、API 规则对齐；
- 使文档与 verification checkpoint 引用权威规范，
  而非 restating divergent variant。

### 排除文件

`standards/module-skills.md` 不改动。本任务不是请求的
module 或 project skill scan，普通代码文档工作不得
刷新 skill 文档。

## 命名决策模型

名称从左到右构造，仅使用消除歧义所需的 qualifier：

```text
[scope or variant] + business concept + [operation or view purpose] + responsibility
```

例如 `PlatformUserPasswordEntity` 标识 platform scope、user
concept、password 子概念与 persistence 角色。`RoleStatusUpdateRequest`
标识 role concept、 narrowly allowed mutation 与 transport
input 角色。Qualifier 必须描述真实区别；将 discourage
`RoleBusinessService`、`RoleDataDao`、`CommonUtils` 等冗余术语。

## 兼容性与范围

- 不重命名任何 Java type、member、package、database object 或 API contract。
- 不引入新的 build-time naming checker。
- 保留既有项目约定如 `Dao`、`Vo`、`Endpoint`、
  `domain.enums` 与 approved interface name。
- 与改进指导冲突的历史名称可保留至单独 refactor；不作为示例推广。
- 保留既有要求，除非与当前 module boundary 或其他 standard 冲突。
  任何修正的冲突将显式说明，而非静默删除规则。
- 规范描述 intended baseline。当前代码可能含较旧偏差；
  本任务不授权源码清理。
- Module skills 文档保持不变。

## Review 与验证

规范集将审查：

- 与 `AGENTS.md` 中 module、DDD boundary 的一致性；
- 与 `code-style.md`、`api-design.md`、
  `domain-module-initialization.md` 的一致性；
- 存在于或 closely match 当前源码树的示例；
- 无矛盾的 suffix、package、acronym、method-verb、Lombok、
  nullability、layering 规则；
- required convention 与 contextual preference 的清晰区分；
- 无 placeholder、推测架构或 implied source rename。

因实现仅改 Markdown，仓库 Java-source compile gate 不要求 Maven 编译。
验证使用 focused diff、cross-file search、Markdown/content check，
以及最终确认未改 Java 或 module skill 文档。

## 完成标准

- 读者能解释为何选择某 proposed name，而非仅判断 casing 是否合法。
- 常见 domain 与 infrastructure responsibility 映射到 stable suffix。
- Method、boolean、collection、identifier、time、unit name 遵循
  可预测语义。
- 缩写使用单一 documented casing 策略。
- Source layout、Lombok、comment、nullability、validation、lifecycle、
  compatibility 规则有单一无歧义权威表述。
- 交叉引用在 two standards 描述同一 concern 处替代 material duplication。
- Domain initialization gate 指向澄清后的规则，
  不引入 competing convention。
- 示例强化当前 Nexus terminology，而不将 legacy
  inconsistency 当作规则。
- 所有变更限于 approved standards 与 design/plan 文档；
  Java 源码与 module skill 文档保持不变。
