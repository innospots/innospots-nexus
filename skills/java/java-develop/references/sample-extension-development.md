# 实施：innospots-nexus-sample 平台扩展

在 [domain-initialization.md](domain-initialization.md) 六阶段之上，扩展库按 **交付面** 追加文件时的推荐顺序。

**包树与契约权威：** [sample-extension-layout.md](../../java-reference/references/sample-extension-layout.md)。

---

## 推荐实施顺序（单领域）

| 序 | 位置 | 产出 | 测试（先红后绿） |
|----|------|------|------------------|
| 1 | `core.<domain>.domain` | enums、entity、request、vo | `*EntityContractsTest`、record 契约 |
| 2 | `core.<domain>.dao` | `*Dao extends BaseMapper` | `*DaoContractsTest` |
| 3 | `core.<domain>.operator` | `*Operator` | 拒绝路径 + 正常路径单测 |
| 4 | `core.<domain>.service` | `*Service` | 编排单测（mock operator/dao） |
| 5 | `console.<domain>.service` / `inbound.<domain>.service` | 薄 service | 委托 core 的单测 |
| 6 | `console.<domain>.endpoint` / `inbound.<domain>.endpoint` | JAX-RS **interface** | `*EndpointContractsTest` |
| 7 | 可选 `core.<domain>.loader` | `*Loader` 或 `NexusStartupTask` | 单测 mock service；集成在运行模块 |
| 8 | 端点 **实现类**（若需要） | `*EndpointImpl` | 运行模块或扩展库 + 集成测试 |

**编译门禁（每批）：**

```bash
mvn -pl innospots-nexus-sample/innospots-nexus-sample-platform -am clean compile
```

---

## 文件放置硬规则

| 规则 | 说明 |
|------|------|
| 无 Spring | `sample-platform` 内不得出现 `org.springframework.*` |
| 无 portal | 不得依赖或引用 `com.innospots.nexus.portal` |
| 端点位置 | REST interface 仅在 `console.*.endpoint` 或 `inbound.*.endpoint` |
| 持久化 | entity/dao/operator 仅在 `core.*` |
| marker | `SamplePlatformModule` 不注册 Bean、不调用 loader |

---

## 运行模块装配（非 sample-platform 代码）

在 `innospots-nexus-sample-spring-platform`（或 Quarkus 对称模块）：

1. POM 增加对 `innospots-nexus-sample-platform` 的依赖。
2. 注册 Endpoint 实现、Dao 扫描、`NexusStartupTask`（模式对照 `innospots-nexus-spring-console` 的 catalog 启动任务）。
3. schema SQL 放入该运行模块 `resources`。

不得把 Spring `@Configuration` 放进 `sample-platform` JAR。

---

## 推荐测试类

| 类名模式 | 模块 |
|----------|------|
| `SamplePlatformModuleTest` | sample-platform |
| `SamplePlatformModuleBoundaryTest` | sample-platform |
| `{Domain}EndpointContractsTest` | sample-platform（console 与 inbound 各一份若路径不同） |

OpenAPI / Filter 类测试保留在 `sample-spring-platform`（已有 `openapi`、`jaxrs.web` 测试包）。

---

## 出口

与 [develop-deliverables.md](develop-deliverables.md) 相同：`mvn test` → `java:check`。
