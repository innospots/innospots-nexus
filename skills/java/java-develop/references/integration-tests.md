# 集成测试（`*IT`）

契约测试与行为单测覆盖**结构与单类行为**；集成测试覆盖**跨层或真实中间件**路径。
默认由 `java:develop` 在 design 的 test-scope 中判定是否需要。

---

## 何时写 `*IT`

| 写 IT | 不写 IT（契约/单测足够） |
|-------|-------------------------|
| 端点经真实 JAX-RS 运行时装配（Testcontainers / 嵌入式容器） | 端点契约测试（反射读注解 + 推迟行为） |
| 事务 + 数据库真实提交/回滚 | operator/service 用测试替身 DAO |
| 插件装载、贡献解析等跨子系统路径 | 纯 MapStruct 转换 |
| design 明确要求 `*IT` | 平凡 CRUD 且 DAO 已契约锁定 |

---

## 命名与执行

| 项 | 约定 |
|----|------|
| 类名 | `{Concept}IT.java`（如 `RoleEndpointIT`） |
| 位置 | `src/test/java`，包路径镜像被测入口 |
| 运行 | Maven Failsafe：`mvn -pl <module> -am verify` |
| 与单测分工 | `*Test` / `*ContractsTest` → Surefire；`*IT` → Failsafe |

---

## 编写原则

1. **少而精** — 每条 IT 覆盖一条端到端主路径或一条关键集成风险。
2. **独立** — 自备数据或使用可重复 fixture；不依赖单测执行顺序。
3. **不重复契约** — 路径、`R<T>` 包装已在 `*ContractsTest` 锁住的，IT 不断言重复细节。
4. **环境** — 优先 Testcontainers 或项目已有测试基类；不得要求开发者手工起外部服务且无文档。
5. **敏感信息** — 断言与日志不含密码、令牌、密钥。

---

## 与交付的关系

- 无 `*IT` 时：`mvn -pl <module> -am test` 即可交 `java:check`。
- 有 `*IT` 时：交付前须 `mvn -pl <module> -am verify`（或全量 `mvn verify`）。

详见 [test-conventions.md](test-conventions.md) 运行与过滤命令。
