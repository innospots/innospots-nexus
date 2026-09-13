# MyBatis-Plus 持久化实现规范

设计侧契约见 `java:design` → [persistence-contract.md](../../java-design/references/persistence-contract.md)。
权威条文：`standards/code-style.md`、`standards/api-design.md`。

---

## 总原则

| 必须 | 禁止 |
|------|------|
| `*Dao extends BaseMapper<Entity>`，一表一 Dao | `mapper.xml`、MyBatis XML 语句、`<sql>` 片段文件 |
| Java 装配：`@Configuration`、`@MapperScan`（运行时模块） | `beans.xml`、`applicationContext.xml`、MyBatis XML 配置 |
| 查询/更新/插入条件优先 **LambdaWrapper** | 字符串列名、`QueryWrapper` 硬编码字段名（除非无 lambda 可用） |
| 可复用单表谓词：`interface` 上 **`default` 方法** | 在 Dao 内写多表事务、调其他 Dao |
| 业务配置：`resources` 下 **`*.yaml` / `*.yml`** | 新建 `*.properties` 业务/应用配置 |
| 跨表：operator/service 分批查询 + 内存组装 | SQL join、N+1 逐行查关联表 |

---

## 配置与装配

### 应用/业务配置（resources）

```text
src/main/resources/
  application.yaml              # 主配置（已有 spring 模块范例）
  application-{profile}.yaml    # 按 profile
  config/<domain>.yaml            # 域专属配置（按需）
```

- **禁止**新增 `application.properties`、`bootstrap.properties` 等 `.properties` 文件承载业务配置。
- 存量 `.properties` 仅作迁移参考；新功能只用 yaml。
- 类型绑定用 `@ConfigurationProperties` + `@Configuration`（见 spring 子模块 `*Properties` 类），不用 XML 注入。

### MyBatis-Plus 与 Bean

- Dao 接口：`org.apache.ibatis.annotations.Mapper`（或宿主统一 `@MapperScan` 扫描）。
- **不要**创建 `mybatis-config.xml` 或 `mapper/**/*.xml`。
- Spring 集成在 `innospots-nexus-spring-*`；**中立库模块**（kernel/console 等）不写 Spring 配置类。

---

## DAO 实现优先级

从高到低选用；上一层能满足则不用下一层。

| 优先级 | 方式 | 适用 |
|--------|------|------|
| 1 | 继承 `BaseMapper` 自带 `selectById` / `insert` / `updateById` / `deleteById` 等 | 主键与简单 CRUD |
| 2 | `default` 方法 + `Wrappers.lambdaQuery()` / `lambdaUpdate()` / `lambdaQuery()` 链 | **首选**自定义单表条件 |
| 3 | `default` 内调用 `selectList` / `update` / `delete` 传入 wrapper | 动态列表、批量条件 |
| 4 | 单表 `@Select` / `@Update` / `@Delete` 注解 SQL | 仅当 wrapper 冗长难读且**仍无 join**；设计文档已说明 |
| — | mapper.xml | **禁止** |

### LambdaWrapper 示例

```java
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

default RoleEntity selectByRoleCode(String roleCode) {
    return selectOne(Wrappers.<RoleEntity>lambdaQuery()
            .eq(RoleEntity::getRoleCode, roleCode));
}

default int updateStatusByRoleId(String roleId, String status) {
    return update(null, Wrappers.<RoleEntity>lambdaUpdate()
            .set(RoleEntity::getStatus, status)
            .eq(RoleEntity::getRoleId, roleId));
}

default List<RoleEntity> selectByStatuses(List<String> statuses) {
    if (statuses == null || statuses.isEmpty()) {
        return List.of();
    }
    return selectList(Wrappers.<RoleEntity>lambdaQuery()
            .in(RoleEntity::getStatus, statuses));
}
```

- 条件列一律 **方法引用** `Entity::getField`，不用 `"role_code"` 字符串。
- 更新用 `lambdaUpdate()`；插入用 `insert(entity)` 或 `BaseMapper` 批量 API，不用 XML `insert` 片段。
- 分页：service 层组 wrapper 后 `selectPage(Page, wrapper)`，或 Dao `default` 封装单表分页谓词。

### 注解 SQL（最后手段）

```java
@Select("SELECT role_id, role_code, role_name, status FROM nx_role WHERE owner_id = #{ownerId}")
List<RoleEntity> selectColumnsByOwnerId(@Param("ownerId") String ownerId);
```

- 必须单表、无 join；表名用实体 `TABLE_NAME` 常量为佳。
- PR 中说明为何不用 LambdaWrapper。

---

## 控制 Dao 体积（避免「巨型 Mapper」）

| 规则 | 说明 |
|------|------|
| **一表一 `*Dao`** | `RoleBinding` 用 `RoleBindingDao`，不要塞进 `RoleDao` |
| **`default` 方法宜精不宜多** | 可复用、稳定的单表谓词；建议单 Dao **≤ 7** 个自定义 default，超过则审视拆分 |
| **一次性动态条件** | 在 **operator** 内 `Wrappers.lambdaQuery()` + `roleDao.selectList(wrapper)`，不增加 Dao 方法 |
| **单 Dao 接口行数/方法数过大** | ① 是否多表混用 → 拆实体与 Dao；② 是否编排误入 → 移到 service；③ 是否重复 wrapper → 提取 **private static** 包内辅助（同模块 `dao` 包下 `RoleDaoPredicates` 纯 Java 工具类，**非**第二个 Mapper） |
| **禁止**同一实体注册两个 `BaseMapper` 接口 | 不建 `RoleDao` + `RoleQueryDao` 双 Mapper 指向同表 |
| **单包 ≤15 类** | 多个 `*Dao` 同属一领域时正常；勿把多领域 Dao 塞进同一扁平包 |

复杂度过高时回到 `java:design` 修订表结构或读模型拆分，不得在 XML 里「写全」。

---

## 与 operator / service 的分工

```text
Dao       单表 CRUD + 可复用单表谓词（default + wrapper）
Operator  直接数据操作、简单单表组合、动态 wrapper 调用 Dao
Service   事务、跨 Dao 编排、跨表组装、稳定键传播
```

- Dao **不得** `@Transactional`；事务只在 service（`jakarta.transaction.Transactional`）。
- Operator **不得**依赖其他 operator；跨表在 service 协调多个 Dao。

---

## 契约测试要点

- `*DaoContractsTest`：确认 `extends BaseMapper<CorrectEntity>`，无 XML 资源。
- 资源扫描测试（若有）：`src/main/resources` 下**不得**新增 `mapper/**/*.xml`。
- 自定义 `default` 方法：行为单测或集成测覆盖主要谓词（可选，design test-scope 定义）。

---

## 自检清单

- [ ] 无新建 `*.xml` mapper / spring beans 配置
- [ ] 无新建 `*.properties` 业务配置（仅 yaml）
- [ ] 自定义查询优先 `Wrappers.lambdaQuery()` / `lambdaUpdate()`
- [ ] 无 join、无 N+1
- [ ] 每个 Dao 只映射一张表
- [ ] 巨型 Dao 已拆表、拆职责或上提 operator
- [ ] 注解 SQL 有设计记录且单表

规范红线 → [quick-constraints.md](../../java-reference/references/quick-constraints.md) §4。
