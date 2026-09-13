# Spring 依赖与版本约定

`innospots-nexus-spring` 聚合下的运行时组装规则。与
[`dependency-conventions.md`](../../java-project/references/dependency-conventions.md) 一致：
**版本只由 BOM 管理，模块禁止自定义版本。**

---

## 版本来源（唯一）

| 项 | 位置 | 说明 |
|----|------|------|
| `spring-boot.version` | `innospots-nexus-bom` `<properties>` | Spring Boot 主线版本 |
| `spring-boot-dependencies` BOM import | `innospots-nexus-bom` `dependencyManagement` | 传递约束全部 `org.springframework.boot:*` |
| MyBatis-Plus Spring Boot starter | `innospots-nexus-bom` | 与 Boot 4 对齐的 `mybatis-plus-spring-boot4-starter` |
| `spring-boot-maven-plugin` | 应由 BOM / parent 插件管理继承版本 | **禁止**在 spring 子模块 POM 内联插件 `<version>` |

```text
innospots-nexus-bom（spring-boot.version + import spring-boot-dependencies）
    ↓ import
innospots-nexus-parent
    ↓
innospots-nexus-spring / *-spring-app / *-spring-console
```

### 模块 POM 写法

```xml
<!-- 正确：无 version -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 禁止 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>4.1.1</version>
</dependency>
```

升 Spring Boot 版本：**只改 BOM**（`java:dependency-upgrade`），然后全仓 `mvn dependency:tree`
验证，不得在 `innospots-nexus-spring/pom.xml` 单独维护第二套 `spring-boot.version`。

---

## 禁止的 Spring 组件

本仓库**不使用** Spring Data 与 Spring Security。认证、授权、持久化分别由
`innospots-nexus-kernel` / `console`（Jakarta REST + 领域服务）与 **MyBatis-Plus**
（`innospots-nexus-core`）承担。

| 禁止 | 包括但不限于 | 替代 |
|------|-------------|------|
| **Spring Data** | `spring-boot-starter-data-jpa`、`spring-boot-starter-data-redis`、`spring-boot-starter-data-mongodb`、`spring-data-*` | `mybatis-plus-spring-boot4-starter` + `*Dao` / `BaseMapper`；缓存/消息走明确 adapter（未来模块） |
| **Spring Security** | `spring-boot-starter-security`、`spring-security-*`、`SecurityFilterChain` 配置 | `kernel` / `console` 认证与 `permission` 域；HTTP 边界仍为 **Jakarta REST**，不用 Spring MVC 安全链 |

### 为何禁止

| 组件 | 原因 |
|------|------|
| Spring Data | 与「DAO 单表、禁 join、禁 XML」及 Jakarta Persistence 注解 + MyBatis-Plus 栈冲突；易引入 JPA 懒加载、Repository 语义与项目分层不一致 |
| Spring Security | 与现有 `NexusException` + 状态码 + console 鉴权模型重复；易与 Jakarta REST 端点、多租户 Session 模型产生两套安全边界 |

需要新能力时，先走 `java:design` 评估是否扩展现有 kernel/console 契约，**不得**默认引入上述 starter。

---

## 允许的运行时依赖（参考）

以 `innospots-nexus-spring-app` 为基准白名单；新增 starter 须设计评审 + BOM 登记。

| 依赖 | 用途 |
|------|------|
| `spring-boot-starter` | 核心容器 |
| `spring-boot-starter-web` | Servlet 栈（承载 Jakarta REST 运行时，**不用于 Spring MVC 端点**） |
| `spring-boot-starter-jdbc` | DataSource |
| `spring-boot-starter-log4j2` | 日志（排除默认 logging） |
| `spring-boot-configuration-processor` | 配置元数据（optional） |
| `mybatis-plus-spring-boot4-starter` | ORM 绑定 |
| `spring-boot-starter-test` | 测试（test scope） |

---

## 自检

```bash
# 模块 POM 是否内联 Spring 版本
grep -rn '<version>' --include=pom.xml innospots-nexus-spring/ | grep -i spring

# 是否引入禁止的 starter
grep -rn 'spring-boot-starter-data\|spring-data-\|spring-boot-starter-security\|spring-security-' \
  --include=pom.xml innospots-nexus-spring/

mvn -q help:effective-pom -pl innospots-nexus-spring/innospots-nexus-spring-app
mvn dependency:tree -pl innospots-nexus-spring/innospots-nexus-spring-app | grep -E 'spring-data|spring-security'
```

交付前交 `java:check` 全量验证。
