# 测试约定与组织

---

## 文件与命名

| 项 | 约定 | 示例 |
|----|------|------|
| 位置 | 测试源镜像生产包，位于 `src/test/java` | `kernel/role/service/` → `src/test/java/.../role/service/` |
| 聚焦单测 | `{TypeName}Test.java` | `PasswordValidatorTest.java` |
| 契约族 | `{Concept}ContractsTest.java` | `RoleEntityContractsTest.java` |
| 集成测试 | `*IT.java`（failsafe 执行） | `RoleEndpointIT.java` |
| 类可见性 | 包级私有，不加 `public` | `class RoleServiceTest {` |
| 方法可见性 | 包级私有，不加 `public` | `void createRejectsDuplicateCode() {` |
| 方法命名 | lowerCamelCase 行为短语，**无 `test` 前缀** | `refreshIssuesNewPairFromRefreshToken()` |
| 一文件一顶层类型 | 与生产代码一致 | |

### 方法命名模式

```text
<动作><条件><预期结果>
```

| 好 | 差 | 原因 |
|----|----|------|
| `createRejectsMissingLegalName` | `testCreate1` | 编号无语义 |
| `roleEntitiesDeclareOwnerAwareIndexes` | `testIndexes` | 未说明断言什么 |
| `refreshIssuesNewPairFromRefreshToken` | `worksCorrectly` | 无信息量 |
| `pageRolesReturnsEmptyWhenNoMatch` | `testPageRoles` | 未说明结果 |

只命名行为与结果；条件重要时才带上条件。

---

## 断言风格

统一使用 AssertJ 静态导入，不用 JUnit 的 `org.junit.jupiter.api.Assertions`。

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

| 场景 | 写法 |
|------|------|
| 值相等 | `assertThat(role.getRoleCode()).isEqualTo("ADMIN");` |
| 集合 | `assertThat(roles).extracting(RoleEntity::getRoleCode).containsExactly("A", "B");` |
| 单元素集合 | `assertThat(models).singleElement().extracting(DemoModel::getName).isEqualTo("nexus");` |
| 空集合 | `assertThat(converter.modelsToEntities(null)).isEmpty();` |
| 异常 | `assertThatThrownBy(() -> service.createRole(request)).isInstanceOf(NexusException.class);` |
| 异常属性 | 链式 `.extracting(...)` 或 `.hasMessageContaining(...)` |
| 反射循环中 | 用 `.as("length of %s", field.getName())` 标注，失败时能定位 |

断言要**针对外部可见行为**，不要断言实现细节（如被调用了几次私有方法）。

---

## 测试结构

推荐 Arrange-Act-Assert 三段式，段间空行：

```java
@Test
void createRejectsDuplicateRoleCode() {
    // Arrange
    roleDao.insert(existingRole("ADMIN"));
    RoleCreateRequest request = new RoleCreateRequest("ADMIN", "Admin", null);

    // Act & Assert
    assertThatThrownBy(() -> roleService.createRole(request))
            .isInstanceOf(NexusException.class);
}
```

不加注释也可以，但要保持三段清晰。测试方法应短小、一个测试一个关注点。

---

## Mock 使用边界

| 该 mock | 不该 mock |
|--------|-----------|
| 外部服务、远程客户端 | 被测对象自身 |
| 难以构造的重型协作者 | 简单的值对象、record |
| 需要观测的调用计数（如验证 N+1） | 为了 mock 而给生产代码加接口 |

**不得**为了便于 mock 而给只有一个稳定实现的生产类型硬加接口——
接口引入必须满足 `java:design` 中列出的真实边界条件。

优先用真实协作者 + 测试替身 DAO，只有在真实构造代价过高时才用 Mockito。

---

## 独立性要求

| 要求 | 说明 |
|------|------|
| 测试之间不共享可变状态 | 每个测试自己准备数据 |
| 不依赖执行顺序 | JUnit 不保证顺序 |
| 可重复运行 | 连续两次运行结果一致 |
| 不依赖系统时间 | 用注入的时钟或固定值 |
| 不用 `Thread.sleep` 做同步 | 用明确等待条件或 `CountDownLatch` |
| 清理自己创建的资源 | 尤其是文件、线程、订阅 |

---

## 覆盖率取向

覆盖率的目的是**暴露未验证的分支**，不是为了数字好看。

优先覆盖：

| 优先级 | 内容 |
|--------|------|
| 高 | 校验与拒绝路径、状态码归属、事务边界、幂等、空值与缺失处理、集合不可变性 |
| 高 | 结构性契约（实体/DAO/端点/状态码） |
| 中 | 正常路径的组装与转换 |
| 中 | 状态机与生命周期转换 |
| 低 | 纯 getter/setter、平凡委托 |

不要为了覆盖率写「断言实现细节」或「只调用不断言」的测试。

---

## 失败测试的处理

```text
1. 判断失败来源
   ├── 本次改动引起        → 修实现或修测试（看哪个表达的是正确契约）
   ├── 既有无关问题        → 单独记录，不要混在本次变更里
   └── 本地依赖产物过期    → 用 mvn -am 重新构建上游
2. 不得削弱断言
3. 不得注释掉失败测试
4. 不得加 try-catch 吞掉后断言成功
```

判断「实现错了还是测试错了」的标准：**哪个更符合已确立的契约**。
若契约本身不明确，回到 `java:design` 澄清，而不是挑一个容易通过的写法。

---

## 运行与过滤

```bash
mvn test                                                    # 全量
mvn -pl <module> test                                       # 单模块
mvn -pl <module> -am test                                   # 单模块 + 上游依赖
mvn -pl <module> test -Dtest=RoleEntityContractsTest        # 单个类
mvn -pl <module> test -Dtest='Role*Test'                    # 通配符
mvn -pl <module> test -Dtest=RoleEntityContractsTest#declaresNexusTableWithConstantName
mvn -pl <module> test -DfailIfNoSpecifiedTests=false -Dtest='Role*Test'
```

`-am` 在本地依赖产物过期时尤其重要：不要因为构建产物陈旧就削弱测试。

---

## 与领域初始化的配合

契约测试在领域初始化的第 1、4、6 步写成，并在实现之前确认红灯：

```text
第 1 步  写实体契约测试
第 2 步  运行确认红灯
第 3 步  实现实体
第 4 步  写 DAO 泛型绑定测试
第 5 步  实现 DAO
第 6 步  写 Request/VO/枚举/状态码/端点契约测试
第 7 步  运行确认红灯
第 8 步  实现领域 record、状态枚举与端点
第 9 步  重跑聚焦测试直到通过
第 10 步 跑全量工程验证
```

完整验证交由 `java:check` 执行 `mvn validate`、`mvn test`、
`mvn -q help:effective-pom`、`git diff --check`。

---

## 评审检查清单

- [ ] 测试类与方法命名符合约定（无 `test` 前缀）
- [ ] 测试源镜像生产包
- [ ] 使用 AssertJ 断言
- [ ] 每个测试一个关注点，失败可定位
- [ ] 契约测试在实现前被观察到红灯
- [ ] 未为让实现通过而削弱断言
- [ ] 未为便于 mock 而给生产代码加接口
- [ ] 测试独立、可重复、不依赖顺序与时间
- [ ] 未用 `Thread.sleep` 做同步
- [ ] 状态码测试覆盖全码形状、唯一性、双语文本、HTTP 映射、cause 保留
- [ ] 端点测试覆盖路径、返回包装、显式推迟行为
- [ ] 敏感值未出现在测试夹具的断言文本中
