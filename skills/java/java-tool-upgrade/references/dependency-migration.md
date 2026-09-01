# 依赖与组件迁移手册

包含：影响分析模板、本仓库已登记组件的迁移要点、常见组件的高频坑。

---

## 一、影响分析模板

每个待升级组件产出一份，作为升级方案的附件。

```markdown
## 组件：<groupId>:<artifactId>

- 当前生效版本：（来自 dependency:tree，不是声明版本）
- 目标版本：
- 升级驱动：安全补丁 / 功能需求 / 传递依赖要求 / 支持周期
- 官方迁移指南：<链接>

### 版本差异

| 类别 | 旧 | 新 | 影响 |
|------|----|----|------|
| 移除 | | | |
| 改名 | | | |
| 签名变化 | | | |
| 行为变化 | | | |
| 隐式契约 | | | |

### 使用位置

| 模块 | 文件:行 | 用法 | 级别（高/中/低） | 处理方式 |
|------|--------|------|----------------|---------|
| | | | | |

### 兼容面影响

- [ ] 是否出现在 public API 签名
- [ ] 是否影响持久化格式或序列化契约
- [ ] 是否影响事件载荷
- [ ] 是否影响配置格式
- [ ] 是否影响注解处理器输出

### 分阶段计划

| 阶段 | 内容 | 验证 |
|------|------|------|
| 1 | | |
| 2 | | |

### 专项测试（针对行为变化）

| 行为 | 测试 |
|------|------|
| | |
```

---

## 二、本仓库已登记组件一览

版本摘自 `innospots-nexus-bom`，升级时同步维护该文件的属性与条目。

| 组件 | 属性 | 迁移关注 |
|------|------|---------|
| `jackson-bom` | `jackson-bom.version` | 2→3 是大版本：包名与序列化行为变化。`Jsons`、`@MaskValue`、`ValueConverter` 等 base 能力受影响面大 |
| `mapstruct` / `mapstruct-processor` | `mapstruct.version` | 两处版本必须同步；`annotationProcessorPaths` 中 Lombok 必须在前；生成实现的命名与行为需复核 |
| `lombok` | `lombok.version` | 与目标 JDK 兼容性优先；`@Data` 在本仓库被禁，`@Getter`/`@Setter`/`@Slf4j`/`@RequiredArgsConstructor` 是主要用法 |
| `slf4j-api` | `slf4j-api.version` | 2.x 用 `ServiceLoader` 取代静态绑定；需确认日志实现（Logback）版本匹配 |
| `jakarta.persistence-api` | `jakarta-persistence.version` | 注解与 `@Table(indexes=...)` 行为；实体契约测试依赖它 |
| `jakarta.transaction-api` | `jakarta-transaction.version` | 本仓库唯一允许的事务注解来源 |
| `jakarta.ws.rs-api` | `jakarta-ws-rs.version` | 端点契约全部依赖它；升级会影响所有端点契约测试 |
| `mybatis-plus-core` / `-extension` | `mybatis-plus.version` | `BaseMapper`、`Wrappers.lambdaQuery`、`IdType.ASSIGN_UUID` 行为；DAO 契约测试依赖它 |
| `hutool-all` | `hutool.version` | 重量级工具库；升级需确认是否有被业务代码当作隐式契约的行为 |
| `caffeine` | `caffeine.version` | 缓存驱逐与统计行为 |
| `commons-io` / `commons-codec` / `commons-collections4` | 各自属性 | 多用于编码与 IO；注意行为边界（null、编码、空输入） |
| `httpclient5` | `httpclient5.version` | 超时、连接池、重试行为；这些属于运行时契约，需专项验证 |
| `junit-bom` / `assertj-core` / `mockito-core` | 各自属性 | 影响全部测试；JUnit 5 与 Mockito 对目标 JDK 的兼容性 |
| `HikariCP` | `hikari.version` | 连接池参数行为 |
| `postgresql` / `mysql-connector-j` | 各自属性 | 驱动与数据库版本兼容性；时区、类型映射行为 |
| `lettuce-core` | `lettuce.version` | Redis 客户端行为 |
| `amqp-client` / `kafka-clients` | 各自属性 | 消息客户端行为与配置键 |
| `micrometer-core` | `micrometer.version` | 指标命名与维度行为 |
| `quartz` | `quartz.version` | 调度持久化表结构与行为 |
| `ulid-creator` | `ulid-creator.version` | `IdGenerator.ulid(prefix)` 生成的 ID 格式属于**持久化契约**，升级需验证格式不变 |
| `aviator` | `aviator.version` | 表达式引擎语法与求值行为 |

---

## 三、高频坑分类

### 序列化与 JSON（Jackson 等）

| 坑 | 说明 |
|----|------|
| 默认行为变化 | 命名策略、null 包含策略、日期格式、未知字段处理 |
| 多态类型处理 | 类型标识字段与反序列化白名单 |
| 字段名与顺序 | 影响持久化数据与跨服务契约 |
| 大版本包名变化 | Jackson 3 与 2 的包结构不同 |
| 自定义模块/序列化器 | 升级后需重新注册与验证 |
| 泛型解析 | 复杂泛型的反序列化行为可能变化 |

**必须做的验证**：对真实载荷做「旧写新读」「新写旧读」的往返测试。

### 注解处理器（Lombok / MapStruct）

| 坑 | 说明 |
|----|------|
| 与目标 JDK 不兼容 | 升级 JDK 后 **必须先升注解处理器** |
| Lombok 与 MapStruct 顺序 | `annotationProcessorPaths` 中 Lombok 必须在前 |
| 生成实现命名变化 | 依赖生成类名的代码会失效 |
| 生成行为变化 | 如集合转换的 null 处理、嵌套映射策略 |
| `mapstruct` 与 `mapstruct-processor` 版本不一致 | 两处必须同步 |
| 反射型契约测试 | 参数名与注解保留依赖 `maven.compiler.parameters=true` |

### 日志（SLF4J / Logback）

| 坑 | 说明 |
|----|------|
| 绑定机制变化 | SLF4J 2.x 改用 `ServiceLoader`，静态绑定器不再适用 |
| 实现版本不匹配 | API 与实现版本需配套 |
| 配置文件格式 | Logback 配置语法与 schema 变化 |
| 输出格式变化 | 影响依赖日志格式的告警与采集规则 |
| 敏感信息 | 升级后检查脱敏规则是否仍生效 |

### HTTP 客户端

| 坑 | 说明 |
|----|------|
| 超时语义 | 连接超时 / 请求超时 / 响应超时的定义与默认值可能变化 |
| 连接池 | 默认值与驱逐策略变化 |
| 重试与幂等 | 默认重试策略变化可能放大非幂等请求 |
| 协议与 TLS | 默认 TLS 版本与密码套件变化 |
| 认证与重定向 | 默认行为变化 |

这些属于**运行时契约**，编译期发现不了，必须有专项验证。

### 工具库（commons-*、Guava、Hutool）

| 坑 | 说明 |
|----|------|
| null 容忍度变化 | 旧版吞 null，新版抛异常（或反之） |
| 空集合与空字符串处理 | 返回值从 null 变空集合（或反之） |
| 编码与 locale | 默认编码、locale 敏感行为 |
| 不可变性 | 返回的集合从可变变不可变（或反之） |
| 边界行为 | 除零、溢出、舍入、截断 |

工具库的**行为变化**是最容易被忽略的——逐个在使用点验证，不要假设等价。

### 内部 SDK / starter

| 坑 | 说明 |
|----|------|
| API 不兼容 | 内部 SDK 常缺少版本兼容保证 |
| 隐式全局行为 | starter 会改变配置默认值或注册全局组件 |
| 传递依赖冲突 | 内部 SDK 带来的第三方版本与 BOM 冲突 |
| 状态码与异常体系 | 内部公共异常组件升级会改变 `NexusException` 语义 |
| 文档缺失 | 内部组件往往只有代码没有迁移说明，需读源码对比 |

内部组件升级时，**优先读源码 diff**，不要只看版本号。

---

## 四、旧 Util 替换

把散落的旧 `Util` 类替换为统一组件（如统一 HTTP Client、统一 JSON 组件）时：

```text
1. 盘点      扫描所有使用点，包括测试
2. 对齐语义  逐个方法确认新组件是否有等价能力（含边界行为）
3. 建 mapping 表  旧方法 → 新方法，标注语义差异
4. 分模块替换  每模块一次提交
5. 保留期    旧类标记 @Deprecated + @deprecated，指明替代与移除计划
6. 清理      确认无引用后删除，不要无限期保留
```

| 注意 | 说明 |
|------|------|
| 不要一次性删旧类 | 先替换，确认无引用再删 |
| 废弃需指明替代 | `@Deprecated` + `@deprecated` 同时使用 |
| 语义不等价要显式说明 | 尤其是 null 处理、异常行为、边界值 |
| 命名规范 | 新工具类用内聚名（`Checks`、`Jsons`、`StringUtils`），**禁止** `CommonUtils`、`BaseHelper` |

---

## 五、依赖冲突处理优先级

```text
1. 升级到兼容版本      ← 首选
2. 统一到 BOM 管理     ← 版本不一致时
3. 排除传递依赖        ← 明确知道不需要且确认安全
4. 加适配层            ← 两个版本都必须存在时
5. 替换组件            ← 无兼容版本时
```

**不要**一遇到冲突就 `exclude`——那只是隐藏问题，运行时可能
`NoClassDefFoundError` 或行为异常。

排查命令：

```bash
mvn dependency:tree -Dverbose
mvn dependency:tree -Dincludes=<groupId>:<artifactId>
mvn -q help:effective-pom
```

---

## 六、升级前后对比清单

| 项 | 升级前 | 升级后 | 说明 |
|----|-------|-------|------|
| `dependency:tree` 中该组件版本 | | | 必须为目标版本 |
| 是否仍有旧版本传递依赖 | | | 应无 |
| `mvn clean compile` | | | |
| `mvn test` | | | |
| `mvn dependency:analyze` | | | |
| 序列化往返（如适用） | | | 旧写新读 / 新写旧读 |
| 关键运行时行为（如适用） | | | 超时、缓存、连接等 |
| 依赖树全文 diff | | | 与升级前快照对比 |
