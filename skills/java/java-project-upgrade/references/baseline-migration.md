# 基线迁移手册

按升级类型组织。每类给出：断点、执行顺序、验证要点、常见坑。

---

## 一、JDK 大版本升级

### 执行顺序

```text
1. 确认目标 JDK 已安装，且 Maven/IDE 指向它
2. 更新 parent 的 maven.compiler.release
3. 更新 enforcer 的 requireJavaVersion
4. mvn clean compile，处理编译错误
5. 处理依赖库对目标 JDK 的兼容性（不兼容的先升级）
6. mvn test 全量
7. 检查运行期行为：反射、模块系统、废弃 API、GC 与启动参数
```

### 高频断点

| 断点 | 说明 |
|------|------|
| 移除的 API | 被移除的 JDK API、内部 API（如 `sun.misc.*`）不可用 |
| 模块系统 | 强封装导致反射访问失败；surefire/failsafe 已配置 `useModulePath=false` |
| 字节码与工具链 | 旧版 Lombok / MapStruct / Mockito 可能不支持新 JDK，**先升注解处理器与测试库** |
| 语言特性 | 新版本引入的关键字或语法可能导致既有标识符冲突 |
| 序列化 | 跨版本序列化兼容性需验证 |
| JVM 参数 | 废弃/移除的 GC 与调优参数需清理 |
| 第三方 native 库 | 需与目标 JDK 匹配 |

### 本仓库要点

- 当前 `maven.compiler.release=25`，enforcer `requireJavaVersion=[25,)`
- 升级后同步检查：Lombok 与 MapStruct 在 `annotationProcessorPaths` 中的版本是否仍支持目标 JDK
- **本地 JDK 旧于配置时报告环境不匹配，不得下调 release**
- 目标 JDK 与 Spring 版本的 Java 支持范围需交叉核对（见 `java:spring` 的版本表）

### 验证要点

```bash
mvn clean compile
mvn validate                 # enforcer 生效
mvn test
mvn -q help:effective-pom    # 确认 release 值
```

特别关注：反射型契约测试是否仍能读到注解（依赖 `maven.compiler.parameters=true`）。

---

## 二、Java EE → Jakarta EE

### 核心变化

`javax.*` → `jakarta.*` 全量换包。涉及：

| 原包 | 新包 |
|------|------|
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.transaction.*` | `jakarta.transaction.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.annotation.*` | `jakarta.annotation.*` |
| `javax.ws.rs.*` | `jakarta.ws.rs.*` |

### 执行顺序

```text
1. 全量替换源码中的 javax.* → jakarta.*（限定上述命名空间）
2. 替换第三方库为 Jakarta 兼容版本
3. 检查注解处理器与框架（MapStruct、Hibernate、Lombok）
4. 检查配置文件（web.xml、persistence.xml、beans.xml 的 schema 与命名空间）
5. 检查 SPI 文件与 ServiceLoader 注册
6. mvn clean compile → mvn test
```

### 常见坑

| 坑 | 说明 |
|----|------|
| 只换源码不换依赖 | 运行时 `NoClassDefFoundError` |
| 传递依赖仍拉 `javax.*` | 用 `mvn dependency:tree` 排查并排除或升级 |
| 配置文件命名空间未换 | 容器启动失败 |
| 混合存在 `javax` 与 `jakarta` | 类型不匹配，编译或注入失败 |
| 第三方库无 Jakarta 版本 | 需替换、升级或加适配层 |

### 本仓库要点

本仓库已是 Jakarta 基线（`jakarta.ws.rs` 4.0、`jakarta.persistence` 3.2、
`jakarta.transaction` 2.0.1）。若是**从遗留工程迁移**到本仓库，
参照 `java:spring` 的 `spring-boundary.md` 中「从 Spring 工程迁移」步骤。

---

## 三、Spring Boot / Spring Framework 大版本升级

主流程在本技能，Spring 版本差异知识见 `java:spring` 的
`spring-migration-notes.md`。

### 执行顺序（以 2.x → 3.x 为例）

```text
1. 升到当前大版本线的最后一个补丁版（2.x → 2.7 最新）
2. 升 JDK 到目标要求（3.x 需 17）
3. 用 spring-boot-properties-migrator 扫描配置属性变更
4. 升到目标大版本，处理 javax → jakarta
5. 逐模块调用 java:tool-upgrade 处理 Jackson / Hibernate 等组件
6. 替换不兼容的第三方库
7. 升到目标版本线的最新补丁版
8. 全量测试 + 回归
```

### 3.x → 4.x 的额外注意

- **必须先升到 3.5**，不允许跨过
- 4.x 的模块化重构移除了自动配置类的 public 成员：
  自定义 starter 与内部自动配置若依赖这些成员会**编译失败**
- JSpecify 空安全会暴露大量空值告警
- Jackson 3 迁移（Jackson 2 以废弃形式提供）→ 交给 `java:tool-upgrade`
- Hibernate 7、Tomcat 11 → 交给 `java:tool-upgrade`
- 若干配置属性重命名

### 本仓库要点

本仓库管理端不用 Spring MVC 做端点契约，因此 Spring Boot 升级的主要影响面在
**依赖与兼容库**，而非端点注解。升级时重点检查：

- BOM 中 Spring 相关依赖是否仍需要
- `core` 是否因升级被带入自动配置绑定（违反业务中立约束）
- `base` 是否因传递依赖被污染（违反零中间件约束）

---

## 四、Maven / Gradle 大版本升级

### Maven

```text
1. 确认目标 Maven 版本
2. 更新 parent 的 maven.version 属性（enforcer requireMavenVersion 引用它）
3. 更新各插件版本（旧插件可能不兼容新 Maven）
4. mvn validate 验证 enforcer
5. mvn clean compile → mvn test
6. mvn -q help:effective-pom 核验
```

| 坑 | 说明 |
|----|------|
| 插件不兼容 | 旧插件在新 Maven 下行为变化或失败，需同步升插件 |
| 严格校验 | 新版本可能对 POM 格式、依赖声明更严格 |
| 构建缓存与本地仓库 | 大版本升级后建议 `mvn clean`，必要时清理过期产物 |
| CI 环境 | 需同步升级 CI 的 Maven 版本，避免本地与 CI 不一致 |

### Maven 3 → 4 特别关注

- POM 模型版本与校验行为变化
- 插件 API 变化：自定义插件需适配
- 构建生命周期与默认行为可能调整
- 先在小范围模块验证，再推广到全 reactor

### Gradle 升级

- 按官方升级指南逐版本推进，不要跨多个大版本
- 关注废弃 API 与 `deprecation` 警告
- 构建脚本 DSL 变化、插件兼容性、依赖解析策略变化

---

## 五、JUnit 4 → JUnit 5

### 核心映射

| JUnit 4 | JUnit 5 |
|---------|---------|
| `org.junit.Test` | `org.junit.jupiter.api.Test` |
| `org.junit.Assert.*` | AssertJ（本仓库首选）或 `org.junit.jupiter.api.Assertions` |
| `@Before` / `@After` | `@BeforeEach` / `@AfterEach` |
| `@BeforeClass` / `@AfterClass` | `@BeforeAll` / `@AfterAll`（方法需 static 或用 `@TestInstance`） |
| `@Ignore` | `@Disabled` |
| `@RunWith` | `@ExtendWith` |
| `@Rule` / `@ClassRule` | `@RegisterExtension` / `@ExtendWith` |
| `ExpectedException` | `assertThatThrownBy(...)` |
| `Timeout` | `@Timeout` 或 AssertJ 的超时断言 |
| 测试类与方法必须 public | **包级私有即可**（本仓库约定） |

### 执行顺序

```text
1. 引入 junit-jupiter（本仓库 parent 已统一提供）
2. 逐个测试类替换 import 与注解
3. 替换断言为 AssertJ
4. 处理 Rule → Extension 迁移
5. 检查 surefire 是否仍能发现测试
6. mvn test 全量
```

| 坑 | 说明 |
|----|------|
| 混用 JUnit 4 与 5 | 需要 `junit-vintage-engine` 过渡，但应作为临时措施 |
| surefire 找不到测试 | 检查 `useModulePath=false` 与引擎依赖 |
| `@RunWith(MockitoJUnitRunner)` | 改为 `@ExtendWith(MockitoExtension.class)` |
| 测试方法与类不再需要 public | 与仓库约定一致，改为包级私有 |
| 断言风格 | 统一改 AssertJ，不要保留 `Assert.assertEquals` |

### 本仓库约定

- 测试类与方法**包级私有**，方法名 lowerCamelCase 行为短语，**无 `test` 前缀**
- 断言统一 AssertJ
- 详见 `java:test`

---

## 六、构建体系升级

切换构建体系（如 Maven ↔ Gradle）成本高、风险大，需单独论证：

| 论证项 | 内容 |
|--------|------|
| 必要性 | 现有体系是否真的无法满足需求 |
| 影响面 | 多模块、BOM、插件、CI、IDE、发布流程 |
| 并存期 | 是否需要双体系并存过渡 |
| 回退 | 失败如何回到原体系 |
| 收益 | 构建速度、可维护性、生态适配的具体收益 |

切换时**不要**同时做其他升级——变量太多无法定位问题。

---

## 七、项目模块结构调整

属于架构级升级，需先走 `java:design` 出方案。

```text
1. 明确目标结构与依赖方向
2. 确认无环形依赖
3. 小批量移动包，每批 mvn clean compile
4. 同步更新：POM、import、SPI 注册、配置文件、文档
5. 确认未破坏公共兼容面（模块移动会改变包名 = 兼容面变化）
6. 全量测试
```

| 坑 | 说明 |
|----|------|
| 包名变化 = 兼容面破坏 | 需迁移方案与兼容期 |
| 依赖环 | 移动后出现环形依赖，需先抽取公共契约 |
| 资源与 SPI 文件未同步 | `META-INF/services`、自动配置注册文件等 |
| 一次移动太多 | 无法定位问题，应小批量 |

---

## 八、语言特性迁移

如从匿名内部类迁向 lambda、从 `Optional` 误用迁向正确用法、引入 record 等。

| 迁移 | 要点 |
|------|------|
| 匿名类 → lambda | 只在函数式接口上适用；注意 `this` 语义与异常签名 |
| Stream 化 | 可读性优先，不要为用而用；注意并行流的线程安全与是否有序 |
| 引入 record | 本仓库 `domain.request` / `domain.vo` 必须是 record；不可变数据优先 |
| `Optional` 使用 | 仅用于应用侧单值结果；**禁止**用于参数、字段、record 组件、集合 |
| 模式匹配、密封类型等新特性 | 确认目标 JDK 支持，且团队约定一致 |

---

## 升级前/后对比清单

| 项 | 升级前 | 升级后 | 是否一致/可接受 |
|----|-------|-------|---------------|
| `mvn clean compile` | | | |
| `mvn validate` | | | |
| `mvn test`（通过数/失败数） | | | |
| `mvn -q help:effective-pom` | | | |
| `mvn dependency:tree` | | | |
| 关键路径行为 | | | |
| 配置项 | | | |
| 性能基线（若可测） | | | |

任何一项不一致，都要能给出解释，而不是「应该是升级引起的，先这样」。
