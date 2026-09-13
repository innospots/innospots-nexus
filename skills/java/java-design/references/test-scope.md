# 测试范围（设计）

设计阶段确定**测什么、不测什么**，供 `java:develop` 编写单元测试与契约测试。
测试写法与规约见 `java:develop` → [test-conventions.md](../../java-develop/references/test-conventions.md)、
[contract-tests.md](../../java-develop/references/contract-tests.md)。

---

## 设计交付物

在四步法「定契约」时同步产出：

| 项 | 说明 |
|----|------|
| 契约测试范围 | 实体/DAO/端点/状态码哪些结构必须锁住 |
| 行为单测范围 | 校验拒绝、状态转换、幂等、集合不可变、关键正常路径 |
| 不测范围 | 平凡 getter、纯委托、已由契约测试覆盖的形状 |
| 集成测试边界 | 是否需 `*IT`（跨层、真实中间件） |

---

## 按层的默认测试期望

| 层 | 设计阶段至少明确 |
|----|-----------------|
| Entity | 表名、基类、主键、索引、必填与长度 → 契约测试 |
| DAO | `BaseMapper` 绑定、单表约束、无 mapper.xml → 契约测试；可复用 `default` 方法是否行为单测（见 persistence-contract） |
| Request/VO/枚举 | record 形状、校验不变量 → 契约 + 拒绝路径单测 |
| StatusCode | 九字符、双语、类别、HTTP 映射 → 契约测试 |
| Endpoint | 路径、HTTP 注解、`R<T>`、推迟行为 → 契约测试 |
| Service/Operator | 正常路径 + 每种失败语义至少一条单测 |

---

## 设计评审门禁（测试）

- [ ] 每个应用可见失败是否已有对应测试断言（`NexusException` + 状态码）？
- [ ] 新公共类型/端点是否列入契约测试清单？
- [ ] 是否避免「实现后再补测试」而无测试先行计划？
- [ ] 修 Bug 类需求是否已要求复现测试？
- [ ] 持久化相关：资源侧无新增 XML/properties；Dao 清单与 [persistence-contract.md](persistence-contract.md) 一致？

实现与测试编写统一在 **`java:develop`** 完成；**`java:check`** 负责最终核验。
