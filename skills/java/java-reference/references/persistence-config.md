# 持久化与配置（专题）

`java:reference` 对 DAO、资源文件与运行时配置的**操作细则**。硬禁令在
[`standards/code-style.md`](../standards/code-style.md)（MyBatis-Plus DAO、配置与资源文件）
与 [`standards/api-design.md`](../standards/api-design.md)（DAO 契约）；疑义以 standards 为准。

设计侧契约见 `java:design` → [persistence-contract.md](../../java-design/references/persistence-contract.md)。
实现侧见 `java:develop` → [persistence-mybatis.md](../../java-develop/references/persistence-mybatis.md)。

---

## 硬约束摘要

| 禁止 | 必须 |
|------|------|
| MyBatis `mapper.xml`、SQL 片段 XML | `BaseMapper` + Dao `default` 方法 + `Wrappers.lambdaQuery()` / `lambdaUpdate()` |
| Spring `beans.xml`、`mybatis-config.xml` 等 XML 装配 | `@Configuration` / `@Bean`、MyBatis-Plus Java 配置 |
| `src/main/resources` 下业务/应用 `*.properties` | `*.yaml` / `*.yml`（如 `application.yaml`、`config/<domain>.yaml`） |
| 多表 join（含注解 SQL） | 分批单表查询 + 内存组装（operator/service） |
| 一个 Dao 承担多实体、多表编排 | **一表一 `*Dao`**；跨表编排归 service |

MapStruct 的 `@Mapper(config = BaseMapperConfig.class)` 是**对象映射**，与 MyBatis Mapper XML **无关**。

---

## Dao 组织

| 信号 | 动作 |
|------|------|
| 可复用、稳定的单表谓词 | Dao `default` 方法 + lambda 方法引用 |
| 一次性动态条件、调用方专属组合 | Operator 内 `Wrappers.lambdaQuery()`，不膨胀 Dao |
| 自定义 `default` 方法约 **≥ 7** 或单 Dao 难以维护 | 设计阶段拆表/拆 Dao 或上提 Operator |
| LambdaWrapper 无法清晰表达的单表语句 | `@Select` 等注解 SQL；**须设计评审记录**（默认不规划） |

DAO 层动词：`select` / `insert` / `update` / `delete`（对齐 `BaseMapper`）。
Operator/Service 层：`find` / `list` / `page` / `count`。

---

## 配置绑定

- 业务与模块配置键写在 **yaml**；类型安全的绑定类放在模块级 `com.innospots.nexus.<module>.config`。
- 不要把领域专属配置塞进 `domain` 包；不要把配置类放进 `src/main/resources` 以外的非 Java 路径。
- 新增配置键属于**公共兼容面**；废弃须文档化并保留兼容期。

---

## 评审门禁

- [ ] 每张业务表有且仅有一个 `*Dao`、一个 `*Entity`
- [ ] 资源侧无新增 XML/properties
- [ ] 跨表读已规划为分批 + 内存组装
- [ ] yaml 键路径与 `@ConfigurationProperties` / 配置类归属已写明（若有）
- [ ] MapStruct `@Mapper` 未与 MyBatis XML 混淆
