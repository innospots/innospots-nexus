# 六阶段领域初始化 — 执行清单

**权威流程**：[standards/domain-module-initialization.md](../standards/domain-module-initialization.md)

本文件是 `java:develop` 的执行 checklist。规则条文不在此重复，疑义回源
`java:reference` → `standards-index.md` 或 `quick-constraints.md`。

**前置硬门禁**：必须先完成 `java:design` 四步法（见 `module-ownership.md`）。
包树须**领域优先**（`role/dao`、`role/endpoint`），禁止 `dao/role`、`endpoint/role`；
大领域须划**功能子模块**，禁止 service 堆积；**单包 ≤15** 类（见
[package-structure.md](package-structure.md)）。

**测试先行**：由 `java:develop` 与实现同步完成；顺序见
[`standards/domain-module-initialization.md`](../standards/domain-module-initialization.md) 与
`java:develop` → [test-conventions.md](../../java-develop/references/test-conventions.md)。

---

## 阶段一：实体 → 实体门禁

- [ ] 概念清单已写下（聚合、关联、稳定键、生命周期、排序、受保护标志）
- [ ] 持久化范围与基类正确（默认 `WorkspaceBaseEntity`）
- [ ] 主键与稳定键规则已定义
- [ ] 逐字段评审完成；索引与访问模式匹配
- [ ] 实体契约测试已写且观察到预期红灯
- [ ] 实体实现完成；`@Getter`/`@Setter`；JPA + MyBatis-Plus 注解完整

## 阶段二：DAO → DAO 门禁

- [ ] `*Dao` extends `BaseMapper<Entity>`；无 Mapper XML
- [ ] 每个方法单表、无 join
- [ ] 跨表读：分批查询 + 内存组装（无 N+1）
- [ ] DAO 泛型绑定契约测试通过

## 阶段三：领域契约 → 领域门禁

- [ ] Request / VO 为 record；按修改权拆分
- [ ] 枚举只表达本域；集合已防御拷贝
- [ ] 事件/状态码（若有）归属与扩展流程正确
- [ ] 契约测试已写且观察到预期红灯

## 阶段四：转换器（按需）

- [ ] 仅在非平凡映射时需要 MapStruct converter
- [ ] `@Mapper(config = BaseMapperConfig.class)`

## 阶段五：端点 → 端点门禁

- [ ] 按内聚资源边界拆分；只用 `jakarta.ws.rs`
- [ ] 返回 `R<T>`；推迟实现用 `TODO` + `NexusException`；业务失败禁止 JDK 通用异常
- [ ] 端点契约测试（反射型）通过

## 阶段六：Operator / Service（按需）

- [ ] 未自动脚手架；仅在规则触发时创建
- [ ] operator 不依赖 service 或其他 operator
- [ ] 多 operator 协调与事务在 service

---

## 编译与验证门禁

每批 Java 改动后：

```bash
mvn clean compile
```

全部阶段完成后交 `java:check`（`mvn validate`、`mvn test`、`mvn -q help:effective-pom`）。

---

## 完成检查（摘要）

- [ ] 归属模块与相邻域边界正确
- [ ] 遗留代码只用于理解行为，未复制
- [ ] 聚焦测试转绿；未削弱断言
- [ ] 未顺手更新模块 API 索引 README / references 文档
