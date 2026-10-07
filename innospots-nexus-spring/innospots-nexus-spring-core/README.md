# innospots-nexus-spring-core

Spring Boot 侧 **公共装配** 模块，包结构对齐 `com.innospots.nexus.spring.core.*`：

| 包 | 内容 |
|----|------|
| `core.i18n` | MessageSource / I18nConverter 桥接 |
| `core.bootstrap` | `@EnableNexusHostBootstrap`、MyBatis 公共行为、启动编排 |
| `core.plugin` | `@EnableNexusPluginHost`、插件安装 DAO 扫描 |

**不含**演示 H2 URL 或本地 DDL；数据源与各 runnable 的 `application.yaml` / `resources` 负责 JDBC 与演示 schema。

## 文档

| 文档 | 说明 |
|------|------|
| [国际化开发实践](docs/i18n-development-practice.md) | `I18nObject`、`@I18n`、字典文件配置与完整示例 |

## 快速启用

```java
@SpringBootApplication
@EnableNexusI18n
public class MyApplication {
}
```

```yaml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8

nexus:
  i18n:
    enabled: true
    sync-web-locale: true
```

详见 [国际化开发实践](docs/i18n-development-practice.md)。

## 依赖边界

- 依赖 `innospots-nexus-base`；**不**引入 portal、console 等业务模块。
- 在 **application / assembly** 模块引用本 artifact，版本由 `innospots-nexus-bom` 管理。
