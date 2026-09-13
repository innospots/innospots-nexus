# 持久化与配置契约（设计）

设计阶段为 DAO、查询形态与运行时配置划定边界，供 `java:develop` 实现。
实现细则见 `java:develop` → [persistence-mybatis.md](../../java-develop/references/persistence-mybatis.md)。

---

## 硬约束（设计即禁止）

| 禁止 | 必须 |
|------|------|
| MyBatis `mapper.xml`、SQL 片段 XML | 单表访问：`BaseMapper` + Java `default` 方法 + LambdaWrapper |
| Spring `beans.xml`、MyBatis `mybatis-config.xml` 等 XML 装配 | `@Configuration` / `@Bean`、MyBatis-Plus Java 配置 |
| `src/main/resources` 下 `*.properties` 业务/应用配置 | `*.yaml` / `*.yml`（如 `application.yaml`、`config/<domain>.yaml`） |
| 多表 join（含设计里的「一条 SQL 搞定」） | 分批单表查询 + 内存组装（service/operator） |
| 一个 DAO 承担多实体、多表编排 | **一表一 `*Dao`**；跨表编排归 service |

MapStruct 的 `@Mapper(config = BaseMapperConfig.class)` 是 **对象映射**，与 MyBatis Mapper XML **无关**。

---

## 设计四步法中的持久化

### 定归属

- 每张业务表对应一个 `*Dao`、一个 `*Entity`；关联表有独立实体时独立 DAO。
- 复杂读模型是否需要**额外表**或**视图实体**（仍单表、无 join）？需要则在设计文档写明。

### 建词汇

- DAO 方法动词：`select` / `insert` / `update` / `delete`（对齐 `BaseMapper`）。
- Operator/Service：`find` / `list` / `page` / `count`。

### 划边界

- 哪些谓词是**可复用**的（设计为 Dao `default` 方法）？
- 哪些是一次性动态条件（设计为 Operator 内 `Wrappers.lambdaQuery()`，不膨胀 Dao）？
- 单 Dao 预计自定义 `default` 方法数量：**建议 ≤ 7**；接近 15 个方法或单文件难以维护时，设计拆表、拆 DAO 或上提 Operator。

### 定契约

在设计文档 §6.4 / 状态码表旁补充：

| 表 | Dao | 自定义访问（default / 继承 CRUD） | 禁止 |
|----|-----|-----------------------------------|------|
| `nx_role` | `RoleDao` | `selectByRoleCode`、按状态 `list` | join、XML |

**注解 SQL（`@Select` 等）**：仅当设计评审记录「LambdaWrapper 无法清晰表达」的单表语句；默认不规划。

---

## 配置契约（设计）

| 类型 | 形态 | 示例 |
|------|------|------|
| 业务/应用开关、阈值、URL | `src/main/resources/**/*.yaml` | `application.yaml`、`config/plugin-host.yaml` |
| 类型安全绑定 | Java `@ConfigurationProperties` + `@Configuration` | 宿主读取 yaml 填入运行时 |
| 禁止 | `application.properties`、`*.properties` 新业务配置 | — |

插件/宿主「由应用读文件再注入 Core」的模式不变：设计写明 **yaml 键路径** 与 **Java 配置类** 归属模块，不写 XML 装配。

---

## 设计评审门禁（持久化）

- [ ] 每表一对一 `*Dao`，无 join 方案？
- [ ] 无 mapper.xml / beans.xml 依赖？
- [ ] 配置项是否全部落在 yaml + Java config？
- [ ] 大查询是否已拆为分批单表 + 组装步骤？
- [ ] Dao 自定义方法清单是否精简（非把一切 SQL 都塞进 Dao）？
