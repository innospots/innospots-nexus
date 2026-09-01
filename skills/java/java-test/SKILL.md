---
name: java:test
display_name: Java 测试
description: |
  Java 单元测试、契约测试、集成测试与回归测试。当用户要为新功能写测试、为业务
  领域写实体/DAO/端点/状态码契约测试、补回归测试、定位并修复失败测试、提升测试
  覆盖，或需要测试命名与结构约定时使用。遵循测试先行：先写测试确认红灯，再实现。
  触发词：单元测试、契约测试、集成测试、回归测试、测试用例、测试覆盖、
  JUnit、AssertJ、Mockito、测试失败、ContractsTest。
category: java
version: 1.0.0
---

# 单元测试、集成测试与回归测试

## 定位

负责**测试代码**。测试是领域初始化的第一步，不是实现之后的补票。

## 测试技术栈

| 能力 | 库 | 说明 |
|------|-----|------|
| 测试框架 | JUnit 5（`junit-jupiter`） | parent 统一提供，模块无需声明 |
| 断言 | AssertJ（`assertj-core`） | 唯一推荐的断言库 |
| Mock | Mockito（`mockito-core`） | |
| 运行 | surefire / failsafe | 均配置 `useModulePath=false` |

surefire 与 failsafe 都禁用了 JPMS 模块路径，保证反射型契约测试与 Mockito 正常工作。

## 测试分层

| 层次 | 命名 | 目的 | 依赖 |
|------|------|------|------|
| 契约测试 | `{Concept}ContractsTest.java` | 锁住结构契约：注解、路径、签名、状态码形状、索引 | 通常零外部依赖，可用反射 |
| 聚焦单元测试 | `{TypeName}Test.java` | 锁住单个类型的行为 | 必要时 Mockito |
| 集成测试 | `*IT.java`（failsafe） | 跨层或带真实中间件的行为 | 真实基础设施或测试容器 |
| 回归测试 | 随缺陷修复新增 | 锁住已修缺陷不再重现 | 视情况 |

### 命名规则

- 包级私有测试类，无 `public`
- 聚焦单测：`{TypeName}Test.java`，如 `PasswordValidatorTest.java`
- 契约族：`{Concept}ContractsTest.java`，如 `RoleEntityContractsTest.java`
- 测试源镜像生产包，位于 `src/test/java`
- 测试方法用 lowerCamelCase 行为短语，**无 `test` 前缀**

```java
// 好
void createRejectsMissingLegalName()
void roleEntitiesDeclareOwnerAwareIndexes()
void refreshIssuesNewPairFromRefreshToken()

// 差
void testCreate1()
void worksCorrectly()
void testRole()
```

只命名行为与结果；条件重要时才带上条件。避免编号式命名。

## 测试先行

领域初始化的推荐顺序：

```text
1.  写实体契约测试
2.  运行并确认因缺少契约而红灯
3.  实现实体
4.  写 DAO 泛型绑定测试
5.  实现 DAO
6.  写 Request / VO / 枚举 / 状态码 / 端点契约测试
7.  运行并确认红灯
8.  实现领域 record、状态枚举与端点
9.  重跑聚焦测试直到通过
10. 跑全量工程验证
```

**不得为了让偶然的实现通过而削弱有效测试。**
测试暴露出的既有无关问题应与预期红灯区分开，并用 `-am` 之类的 reactor 构建
处理本地依赖产物过期。

## 各类测试的必测项

### 实体契约测试

表名与常量、基类继承、主键注解与长度、必填字段、字符串长度、可空性、索引定义。

### DAO 契约测试

`BaseMapper` 泛型实体绑定、自定义 `default` 方法的单表约束、无 join。

### 端点契约测试（反射型）

类形态、类级与操作级路径、HTTP 注解、方法签名、请求/VO record 类型、
每个方法返回 `R<T>`、推迟方法抛出 `UnsupportedOperationException`。

### 状态码契约测试

13 项必测性质，见 [contract-tests.md](references/contract-tests.md)：
模块三字母、类别语义族、本地码四位、全码九字符且等于 `module + category + local`、
本地与全码唯一、枚举命名、双语 message/advice、类别 `label()` 与 `priority()`、
HTTP 映射、`NexusException.build(status)` 的 code 与 cause 保留、
raw-code 拒绝畸形或未登记的码、端点映射为 `R.fail(...)` 且不泄露 cause 或堆栈、
模块/包归属与同级模块依赖规则。

### 领域行为测试

校验拒绝、状态转换、幂等、集合不可变性与防御拷贝、查询/命令语义、
跨表组装不是 N+1。

## 运行方式

```bash
mvn test                                # 全量
mvn -pl <module> test                   # 单模块
mvn -pl <module> -am test               # 单模块及上游依赖
mvn -pl <module> test -Dtest=RoleEntityContractsTest          # 单个测试类
mvn -pl <module> test -Dtest=RoleEntityContractsTest#methodName  # 单个方法
mvn -pl <module> test -DfailIfNoSpecifiedTests=false -Dtest='Role*Test'
```

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 为让实现通过而削弱断言 | 测试价值所在，不可妥协 |
| 注释掉失败测试 | 等同于放弃验证 |
| 用 `Thread.sleep` 做同步 | 用明确的等待条件或 CountDownLatch |
| 测试之间共享可变状态 | 每个测试独立可重复 |
| 依赖测试执行顺序 | 顺序不确定 |
| 在测试里打印而非断言 | 测试必须有断言 |
| 用 `catch (Exception)` 吞掉后断言成功 | 掩盖失败 |
| 为便于 mock 而给生产代码加接口 | 违反接口引入原则（见 `java:design`） |

## 详细参考

- [contract-tests.md](references/contract-tests.md) — 各类契约测试的写法与必测项
- [test-conventions.md](references/test-conventions.md) — 命名、结构、断言风格与组织约定
