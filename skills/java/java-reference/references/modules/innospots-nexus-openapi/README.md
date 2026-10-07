# innospots-nexus-openapi — OpenAPI 契约与构建索引

> **跨模块契约索引**（非 Maven artifact、非 Cursor 技能）。汇总 console / portal / platform
> 的构建期 OpenAPI 与 core 安全注解。

快照版本：0.1.0-SNAPSHOT

## 模块概览

Innospots 使用 **MicroProfile OpenAPI 注解** + **SmallRye Maven 插件**在 **package 阶段**
生成 **OpenAPI 3.1 YAML**，打入 `META-INF/nexus-openapi/`。运行时通过 console 的
**规范目录 API** 读取，不动态扫描 JAX-RS。

**能力一览：**

| 能力 | 归属 |
|------|------|
| Bearer 安全方案名、`@NexusAuthenticatedApi` | `innospots-nexus-core` |
| `*OpenApiDefinition`、REST 端点注解 | console / portal / platform |
| 打包 YAML、`OpenApiCatalogEndpoint` | console（目录）；各模块 JAR 自带 yaml |
| Scalar 文档 UI 辅助 | `OpenApiScalarDocumentation`（console） |

## 规范与技能

| 文档 | 内容 |
|------|------|
| [`standards/openapi.md`](../../standards/openapi.md) | **权威**：注解与交付要求 |
| [`references/openapi-contract.md`](../../references/openapi-contract.md) | 模板、检查清单 |
| [`references/openapi-maven-plugin.md`](../../references/openapi-maven-plugin.md) | **smallrye-open-api-maven-plugin** 配置与对外模块 POM |
| [`api-contract.md`](../../api-contract.md) | REST 分层 + OpenAPI 链接 |

## 构建产物

| Maven 模块 | YAML 文件（`schemaFilename`） |
|------------|-------------------------------|
| `innospots-nexus-console` | `innospots-nexus-console.yaml` |
| `innospots-nexus-portal` | `innospots-nexus-portal.yaml` |
| `innospots-nexus-platform` | `innospots-nexus-platform.yaml` |

路径：`META-INF/nexus-openapi/<文件名>`。

打包方式：console / platform 在 `process-classes` 直接写入上述路径；portal 经
`target/generated/openapi` 再由 `maven-resources-plugin`（`package-openapi`）复制入 JAR。
详见 [`openapi-maven-plugin.md`](../../references/openapi-maven-plugin.md)。

## 类参考（关键类型）

| 类 | 模块 | 说明 |
|------|------|------|
| `NexusAuthenticatedApi` | core | 类级 Bearer 要求 |
| `NexusOpenApiSecurityNames` | core | `BEARER_AUTH = bearerAuth` |
| `NexusConsoleOpenApiDefinition` | console | Console API 全局 tags + SecurityScheme |
| `NexusPortalOpenApiDefinition` | portal | 租户域 tags |
| `NexusPlatformOpenApiDefinition` | platform | 运维域 tags |
| `OpenApiCatalogEndpoint` | console | `/openapi/specs` |
| `OpenApiBundledSpecs` | console | 读取 classpath YAML |
| `OpenApiScalarDocumentation` | console | Scalar HTML 渲染 |

## 包参考

| 主题 | 参考 |
|------|------|
| Core OpenAPI | [`innospots-nexus-core/references/openapi.md`](../innospots-nexus-core/references/openapi.md) |
| Console OpenAPI 包 | [`innospots-nexus-console/references/openapi.md`](../innospots-nexus-console/references/openapi.md) 及 `openapi-*.md` |
| REST 方法表 | [`innospots-nexus-console/references/endpoint-contracts.md`](../innospots-nexus-console/references/endpoint-contracts.md) |

## 验证

```bash
mvn -pl innospots-nexus-console,innospots-nexus-portal,innospots-nexus-platform -am package
jar tf innospots-nexus-console/target/*.jar | grep META-INF/nexus-openapi
```
