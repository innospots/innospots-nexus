# 工程结构与模块职责

本文说明当前 Nexus 平台库、Spring 装配层和示例工程的结构。模块边界以根
[`AGENTS.md`](../../../AGENTS.md) 为准。

## 1. 总体依赖方向

```text
innospots-nexus-base
  -> innospots-nexus-core
      -> innospots-nexus-plugin
          -> innospots-nexus-console
              ├── innospots-nexus-portal
              └── innospots-nexus-platform

innospots-nexus-service
  -> 独立的技术能力聚合（HTTP、WebSocket、Stream、Transfer、Observability、Governance）

innospots-nexus-spring-* / innospots-nexus-quarkus-*
  -> 各业务库的框架装配层
```

硬性约束：

- `portal` 与 `platform` 是并列关系，不得相互依赖。
- `base` 必须保持 middleware-free，不得依赖 Spring、数据库或业务域。
- `core` 可以包含业务中立的持久化、调度、资源存储和授权基础设施，但不得绑定 Spring Boot。
- `console` 只拥有管理台 API 契约、目录索引和权限运行时，不实现具体业务域。
- 框架装配层只注册 Bean、扫描 DAO、注册 Filter 和 Endpoint，不新增业务语义。
- 依赖只能指向更底层，不能反向指回高层模块。

## 2. 平台模块职责

| 模块 | 核心职责 | 主要产物 | 不应包含 |
|------|----------|----------|----------|
| `innospots-nexus-base` | 异常、状态码、统一响应、事件契约、快照、JSON、ID、密码学、HTTP 工具、condition DSL、execution SPI | 纯 Java foundation | Spring、JDBC、ORM、缓存实现、调度实现、业务实体 |
| `innospots-nexus-service` | 框架中立的 HTTP、WebSocket、Stream、Transfer、可观测性和治理能力 | service 技术库 | servlet/JAX-RS 实现绑定、portal/platform 业务 |
| `innospots-nexus-core` | 持久化基类、审计填充、ID、Quartz、启动 SPI、文件元数据、`EndpointGrant` 基础、OpenAPI 目录读取 | 中立平台基础设施 | console VO/REST、用户/角色/菜单业务、Spring 自动装配 |
| `innospots-nexus-plugin` | Java SPI 与 `plugin.yaml` 发现、安装、生命周期、配置 schema、`console@2` 贡献解码与快照 | 插件运行时 | 持久化 console catalog、具体业务 Endpoint |
| `innospots-nexus-console` | Jakarta REST 管理契约、请求/响应 VO、catalog 索引、权限目录、导航组装、认证与 Web 横切、插件管理契约 | 控制台 API surface | portal/platform 的具体业务实现 |
| `innospots-nexus-portal` | 租户域业务：认证、注册、用户、角色、权限、菜单、字典、审计、workspace/project 作用域 | 租户管理业务 | 运营平台租户生命周期、platform IAM |
| `innospots-nexus-platform` | 运营平台：租户生命周期、企业法定档案、平台用户、邀请、注册审批、平台 IAM、平台导航 | `/api/d/platform/**` 业务 | 租户 workspace/project 业务；不得依赖 portal |

### 2.1 `core` 与 `console` 的边界

`core` 可以定义和执行通用的 Endpoint Grant 机制，但管理台契约、VO、目录同步和导航投影属于 `console`。

### 2.2 `console` 与 `platform` 的边界

当 `platform` 实现 `console` 中的接口时：

- `@EndpointModule` 与 `@EndpointGrant` 只保留在 console 契约接口。
- `platform` 实现类必须重复 `@Path`、HTTP 动词、参数绑定、`@Operation` 等传输和 OpenAPI 注解。
- 授权元数据只能有一个事实源，否则接口和实现容易漂移。

## 3. 框架装配层

| 模块 | 职责 |
|------|------|
| `innospots-nexus-spring-core` | 宿主启动装配、持久化与事务、JAX-RS/Jersey、国际化、插件宿主、系统设置、OpenAPI 目录和 Scalar |
| `innospots-nexus-spring-console` | 认证、凭据、注册、角色、权限、字典、审计、catalog、导航、Web Filter 和 Endpoint Grant 扫描 |
| `innospots-nexus-spring-service` | service 技术库的 Spring 装配，包括 HTTP、WebSocket、Stream、Transfer 和 Governance |
| `innospots-nexus-spring-app` | app 基础装配，只组合通用宿主能力，不包含 portal/platform 业务 |
| `innospots-nexus-spring-portal` | 租户域 Service、DAO、Endpoint Bean，并提供 `@EnableNexusPortal` |
| `innospots-nexus-spring-platform` | 平台域 Service、DAO、Endpoint Bean，并提供组合入口 `@EnableNexusPlatform` |
| `innospots-nexus-quarkus-*` | 与 Spring 对称的 Quarkus 装配；不得与 Spring 混在同一运行 JAR |

运营平台示例使用：

```java
@SpringBootApplication
@EnableNexusPlatform
public class SamplePlatformConsoleServer {
    public static void main(String[] args) {
        SpringApplication.run(SamplePlatformConsoleServer.class, args);
    }
}
```

`@EnableNexusPlatform` 组合 `@EnableNexusConsole` 与 platform 域配置。只增加依赖而不使用对应 `@Enable*`，相关 Bean 不会自动出现。

## 4. 示例工程结构

当前 sample reactor 包含：

```text
innospots-nexus-sample/
├── innospots-nexus-sample-platform         无框架扩展样板
├── innospots-nexus-sample-spring-app       Spring 可运行示例
├── innospots-nexus-sample-spring-portal    Spring 可运行示例
├── innospots-nexus-sample-spring-platform  Spring 运营平台可运行示例
├── innospots-nexus-sample-quarkus-app      Quarkus 可运行示例
├── innospots-nexus-sample-quarkus-portal   Quarkus 可运行示例
└── innospots-nexus-sample-quarkus-platform Quarkus 可运行示例
```

本指南主要对应 `sample-spring-platform` 和 `sample-platform`。

### 4.1 `sample-spring-platform`

职责：

- 提供 Spring Boot 主类。
- 提供 `application*.yaml`、`nexus/console-*.yaml`、`console/meta-sitemap.yaml`。
- 提供 H2 schema 和日志配置。
- 提供平台集成测试与发行包描述。

当前状态：

- 直接依赖 `innospots-nexus-spring-platform`。
- 不依赖 `innospots-nexus-sample-platform`。
- 没有装配 sample 扩展库中的公告、工单 Endpoint。

### 4.2 `sample-platform`

职责：

- 演示在 platform 之上增加业务，不引入 Spring/Quarkus。
- 第一级按交付面分：`core`、`console`、`inbound`。
- 第二级按业务域分：`announcement`、`support`。
- 第三级按技术职责分：`endpoint`、`service`、`dao`、`domain`、`operator`、`loader`。

调用方向：

```text
console.<domain>.endpoint
  -> console.<domain>.service
  -> core.<domain>.service
  -> core.<domain>.operator
  -> core.<domain>.dao

inbound.<domain>.endpoint
  -> inbound.<domain>.service
  -> core.<domain>.service
```

规则：

- `core` 是领域真源，不放 Jakarta REST Endpoint。
- `inbound` 不得依赖 `console`，共享逻辑通过 `core`。
- `console`/`inbound` 的 Service 应保持薄，不复制核心业务规则。
- 单包不超过 15 个 `.java` 文件；类型较多时在技术模块内增加功能子包。

## 5. 把扩展库接入运行工程

当前 `sample-platform` 只是样板。正式接入时至少完成以下步骤：

1. 在运行模块 POM 增加扩展库依赖。
2. 为 Endpoint 接口提供实现 Bean；接口本身不会因为类路径存在而自动变成 REST 资源。
3. 在运行模块显式注册 Service、Operator、DAO，或通过专门的 Spring 适配配置类注册。
4. 为扩展 DAO 增加 `@MapperScan`，不要假设 `@SpringBootApplication` 会扫描同级兄弟包。
5. 把扩展 SQL 加入 `spring.sql.init.schema-locations`，生产环境改用正式迁移流程。
6. 如果扩展提供 console Entry，注册 Plugin SPI，并把插件 ID 加入 `nexus.plugins.required`（仅当启动必须依赖它）。
7. 如果扩展提供 OpenAPI，增加自己的构建期规范产物，或把扩展契约包加入相应 REST 模块的 `scanPackages` 后重新生成。
8. 增加契约、Filter、导航、OpenAPI 和数据库集成测试。

推荐把 Spring 注册代码放在**运行模块或独立 adapter 模块**，不要放进无框架扩展库。

## 6. 新增业务域的推荐目录

```text
com.example.product.platform
├── core.<domain>
│   ├── dao
│   ├── domain/{entity,vo,model,request,enums}
│   ├── operator
│   ├── service
│   └── loader
├── console.<domain>
│   ├── endpoint
│   └── service
└── inbound.<domain>
    ├── endpoint
    └── service
```

按产品形态裁剪：

- 只有管理台：保留 `core` + `console`。
- 只有对外 API：保留 `core` + `inbound`。
- 两者都有：三个面都保留，但共享逻辑必须位于 `core`。

## 7. 接入检查表

- [ ] 新能力归属模块明确，没有把业务代码塞进 `base`、`core` 或 Spring 适配层。
- [ ] `portal` 与 `platform` 没有互相依赖。
- [ ] 运行模块已显式依赖并注册扩展能力。
- [ ] DAO 扫描、schema 和事务边界可用。
- [ ] 管理 Endpoint 已声明权限元数据。
- [ ] console Entry、catalog、导航和前端页面分别验证。
- [ ] 构建后 OpenAPI 产物包含新增 operation。
- [ ] 文档中的资源路径和配置键与实际类路径一致。

