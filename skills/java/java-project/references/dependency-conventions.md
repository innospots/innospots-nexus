# Maven 依赖引用约定

本文档定义**新建工程**或**调整已有工程**时的依赖与 parent 使用规范。
它不是当前仓库依赖关系的快照说明；具体传递链以 `mvn dependency:tree` 为准。

## 规范与存量 POM

| 对象 | 说明 |
|------|------|
| **本技能（规范目标）** | 新建或调整模块时应遵循的依赖写法 |
| **仓库现有 `pom.xml`** | 历史快照，可能与规范存在冗余声明 |

**规则：**

- **新模块、新依赖**：一律按本文最小依赖原则编写。
- **存量冗余**（例如组装模块多写了已由传递链覆盖的 `console`）：允许暂时保留；
  收敛须单独 PR，并用 `mvn dependency:tree` / `dependency:analyze` 证明无功能回归。
- **禁止**以存量 POM 为理由，在新代码中复制冗余依赖模式。

## 核心原则

| 原则 | 要求 |
|------|------|
| **版本统一由 BOM 管理** | 所有 JAR（含第三方与内部模块）的版本只在 `innospots-nexus-bom` 中定义 |
| **禁止单独引用 JAR 版本** | 模块 POM 只写 `groupId` + `artifactId`，**不得**写 `<version>` |
| **parent 统一** | 业务与库模块默认继承 `innospots-nexus-parent` |
| **最小依赖** | 只声明**直接使用的最上层模块**；能由传递依赖带来的下层模块不要重复声明 |
| **禁止绕过 BOM** | 不得在模块内引入 BOM 未登记的第三方坐标；新增依赖须先改 BOM |

---

## 三层 POM 分工

```text
innospots-nexus-bom      ← 全部依赖版本（dependencyManagement）
innospots-nexus-parent   ← 构建 parent（插件、编译基线、公共 test 依赖、import BOM）
各业务/库模块 POM         ← 只声明需要的 artifactId，不写 version
```

### parent 引用方式

所有 **innospots-nexus 体系内的 Java 模块**（`base`、`core`、`console`、`kernel` 等）默认：

```xml
<parent>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-parent</artifactId>
    <version>${revision}</version>
    <relativePath>../innospots-nexus-parent/pom.xml</relativePath>
</parent>
```

`<relativePath>` 按模块在仓库中的实际位置调整。

**运行时组装聚合器**（`innospots-nexus-spring`、`innospots-nexus-quarkus`）同样以
`innospots-nexus-parent` 为 parent；其子模块（如 `innospots-nexus-spring-console`）
的 parent 可指向对应的 spring/quarkus 聚合 POM，仍通过继承链获得 BOM 版本管理。

可根据实际情况调整 parent 层级（例如外部客户工程只 import BOM、不继承 parent），
但**版本来源仍必须是 BOM**，不得自行写版本号。

### BOM 生效方式

`innospots-nexus-parent` 已通过 `dependencyManagement` **import** BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.innospots</groupId>
            <artifactId>innospots-nexus-bom</artifactId>
            <version>${revision}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

继承 `innospots-nexus-parent` 的模块自动获得 BOM 中的版本约束。

**外部独立工程**（不在本 monorepo 内）若不能继承 parent，应在自己的 parent 或
`dependencyManagement` 中 **import** `innospots-nexus-bom`（同样不写各依赖 version）。

---

## 内部模块选型（按场景引用）

按**业务场景**选择**最上层**所需模块，不要为「保险」堆叠下层模块。

| 场景 | 应引用的模块 | 说明 |
|------|-------------|------|
| 仅需管理台契约、catalog、权限运行时等**控制台地基** | `innospots-nexus-console` | 传递带来 `plugin`、`core`、`base` |
| **租户侧管理业务**（用户、角色、权限、菜单、字典、认证实现等） | `innospots-nexus-kernel` | 业务类管理端；传递带来 `console` 及更下层 |
| **运营侧平台**（租户生命周期、企业主体、平台 IAM、`/platform/**`） | `innospots-nexus-platform` | 系统运营类平台；传递带来 `console`；**不得**依赖 `kernel` |
| 仅需业务中立平台基础设施（持久化基类、Quartz、watcher 等） | `innospots-nexus-core` | 不含控制台与管理业务 |
| 仅需纯 Java 基础契约与工具 | `innospots-nexus-base` | 零中间件 |
| 插件运行时与 Page DSL（不经管理台） | `innospots-nexus-plugin` | 见下文「直接引用 plugin」 |
| **Spring Boot 运行时组装** | `innospots-nexus-spring` 子模块 | 见下文「运行时组装」 |
| **Quarkus 运行时组装** | `innospots-nexus-quarkus` 子模块 | 见下文「运行时组装」 |

### 直接引用 `plugin`（不经 `console`）

在以下场景**直接**依赖 `innospots-nexus-plugin`，**不要**为了插件能力再引 `console`：

| 场景 | 引用 | 不引 |
|------|------|------|
| 独立 classpath 插件 JAR、贡献解码/安装宿主 | `plugin`（传递 `core`/`base`） | `console`、`kernel` |
| 仅需 Page DSL / contribution 运行时，无管理台 REST | `plugin` | `console` |
| 管理台 UI + catalog + 权限 + REST 契约 | `console`（已传递 `plugin`） | 单独再写 `plugin` |

插件扩展模块若只实现 `Plugin` 贡献、不暴露管理台 API，通常 **`plugin` 即可**；
需要挂到控制台目录与权限体系时再引 **`console`**。

### 管理控制台 vs 业务管理端

```text
innospots-nexus-console   → 管理台 API 契约与扩展地基（域无关）
innospots-nexus-kernel    → 租户域管理业务实现（业务类管理端）
innospots-nexus-platform  → 运营域平台能力（系统运营类）
```

- 做**管理台特性、扩展、REST 契约**时引用 **`console`**。
- 做**租户侧管理功能实现**时引用 **`kernel`**（通常已足够，无需再写 `console`）。
- 做**运营/平台侧能力**时引用 **`platform`**（与 `kernel` 平行，互不依赖）。

---

## 最小依赖原则

### 规则

1. **只声明你直接 import/调用的最上层 innospots 模块。**
2. **若 A 依赖 B，且 B 已传递引入 C，则不要在 A 中再写 C**（除非 A 的源码直接引用 C 的 API，且该传递在当前版本不可靠——此时应优先修 BOM/模块边界，而非长期重复声明）。
3. **第三方库**：只声明实际使用的 artifact；版本一律来自 BOM。
4. **禁止**为「对齐版本」在模块 POM 里写 `<version>`；版本问题在 BOM 解决。

### 正例与反例

**反例 — 堆叠传递依赖：**

```xml
<!-- 仅需租户管理业务，却重复声明整条链 -->
<dependency>
    <artifactId>innospots-nexus-kernel</artifactId>
</dependency>
<dependency>
    <artifactId>innospots-nexus-console</artifactId>
</dependency>
<dependency>
    <artifactId>innospots-nexus-core</artifactId>
</dependency>
<dependency>
    <artifactId>innospots-nexus-base</artifactId>
</dependency>
```

**正例 — 最小引用：**

```xml
<!-- 租户管理业务：kernel 已传递 console / plugin / core / base -->
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-kernel</artifactId>
</dependency>
```

**反例 — 运营平台误引 kernel：**

```xml
<dependency>
    <artifactId>innospots-nexus-kernel</artifactId>
</dependency>
<dependency>
    <artifactId>innospots-nexus-platform</artifactId>
</dependency>
```

**正例 — 运营平台独立：**

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-platform</artifactId>
</dependency>
```

**反例 — 第三方带版本：**

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-core</artifactId>
    <version>3.5.16</version>
</dependency>
```

**正例 — 版本由 BOM 管理：**

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-core</artifactId>
</dependency>
```

### 何时允许「看起来重复」的依赖

仅当模块源码**直接**使用下层 API，且团队约定该直接依赖必须显式表达可读性时，
才同时声明多个 innospots 模块。即便如此，仍**不得**写 `base` 等更底层重复项，
也**不得**为已由上层传递覆盖的模块再写一份（例如已引 `console` 仍写 `plugin`）。

**当前仓库已认可的显式依赖（存量约定，新模块可参考）：**

| 模块 | 显式声明 | 原因 |
|------|---------|------|
| `innospots-nexus-kernel` | `console` + `core` | 契约层与持久化基类 API 同时使用；`core` 虽可由 `console` 传递，显式声明表达直接依赖 |

新模块默认先选**一个最上层**依赖；若需上表模式，在 PR 说明中写明直接 import 的下层 API。

新增模块时用 `mvn dependency:tree` 验证是否已覆盖所需 API。

---

## 运行时组装（Spring / Quarkus）

框架绑定放在 **`innospots-nexus-spring`** 或 **`innospots-nexus-quarkus`** 聚合下，
不要在中立库模块（`base`/`core`/`console`/`kernel`/`platform`）中引入 Spring/Quarkus starter。

### 可运行应用组装决策表

库模块 `kernel` 与 `platform` **互不依赖**。可运行应用按部署域选型：

| 部署目标 | 库依赖（选一侧） | 运行时模块（Spring 示例） | 说明 |
|----------|-----------------|---------------------------|------|
| 租户管理端 | `innospots-nexus-kernel` | `innospots-nexus-spring-app` | 用户/角色/权限/菜单等 |
| 运营平台 | `innospots-nexus-platform` | `innospots-nexus-spring-app` | 租户生命周期、企业主体、`/platform/**` |
| 仅控制台契约/扩展（无 kernel 业务） | `innospots-nexus-console` | `innospots-nexus-spring-app` | 少见；通常仍有 `*-app` 提供 JDBC 等 |
| **同一进程同时要 kernel + platform** | **新建 `application` 模块** 同时依赖两者 | `innospots-nexus-spring-app` | 见下节；**禁止**让 `kernel` 与 `platform` 库模块互依 |

Quarkus 将上表 `spring-app` 替换为 `innospots-nexus-quarkus-app`，原则相同。

### 同一进程包含 kernel 与 platform

`kernel` 与 `platform` 是平级库模块，**不得**在二者之间加 Maven 依赖。
若产品要求**一个可执行 JAR** 同时暴露租户管理与运营能力：

1. 新建 **`application` / `adapter` 组装模块**（可放在 `innospots-nexus-spring` 或
   `innospots-nexus-quarkus` 聚合下，例如 `innospots-nexus-spring-unified`）。
2. 在该模块 POM 中**同时**声明 `kernel` 与 `platform`（以及 `*-app`）。
3. 运行时绑定、路由、安全配置在组装模块完成；**不**修改 `kernel`/`platform` 的依赖方向。

```xml
<!-- 示例：统一可运行应用（新建模块，非现有 artifact） -->
<dependencies>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-spring-app</artifactId>
    </dependency>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-kernel</artifactId>
    </dependency>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-platform</artifactId>
    </dependency>
</dependencies>
```

更常见做法是**两个独立可执行服务**（租户控制台 vs 运营平台），各自只引一侧库模块。

### Spring Boot

| 模块 | 用途 |
|------|------|
| `innospots-nexus-spring` | Spring 运行时聚合 parent（含 `spring-boot-maven-plugin` 管理） |
| `innospots-nexus-spring-app` | Spring 基础设施组装（Web、JDBC、MyBatis-Plus 等） |
| `innospots-nexus-spring-console` | **可运行的管理端** Spring Boot 应用（组装 `kernel` + 运行时） |

Spring 版本与 starter 约束见 `java:spring` → `spring-dependencies.md`：

- **版本跟随 BOM**（`spring-boot.version` 只在 `innospots-nexus-bom`）；spring 子模块禁止内联 `<version>`
- **禁止 Spring Data、Spring Security**；持久化用 MyBatis-Plus，鉴权用 kernel/console

典型可运行管理端依赖（遵循最小原则）：

```xml
<dependencies>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-spring-app</artifactId>
    </dependency>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-kernel</artifactId>
    </dependency>
</dependencies>
```

`kernel` 传递引入 `console`；**无需**再写 `innospots-nexus-console`，除非该组装模块
不引 `kernel` 却需要控制台契约（少见）。

**租户管理端（规范写法）：**

```xml
<dependencies>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-spring-app</artifactId>
    </dependency>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-kernel</artifactId>
    </dependency>
</dependencies>
```

**运营平台（规范写法）：**

```xml
<dependencies>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-spring-app</artifactId>
    </dependency>
    <dependency>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-platform</artifactId>
    </dependency>
</dependencies>
```

`kernel` / `platform` 传递引入 `console`；组装模块**无需**再写 `innospots-nexus-console`。

> 存量 `innospots-nexus-spring-console` 可能仍显式声明 `console`，属历史冗余；
> 新组装模块按上表即可。

### Quarkus

| 模块 | 用途 |
|------|------|
| `innospots-nexus-quarkus` | Quarkus 运行时聚合 parent |
| `innospots-nexus-quarkus-app` | Quarkus 基础设施组装 |
| `innospots-nexus-quarkus-console` | **可运行的管理端** Quarkus 应用 |

依赖原则与 Spring 对称：

| 部署 | 依赖 |
|------|------|
| 租户管理端 | `innospots-nexus-quarkus-app` + `innospots-nexus-kernel` |
| 运营平台 | `innospots-nexus-quarkus-app` + `innospots-nexus-platform` |
| 统一进程（kernel + platform） | `quarkus-app` + `kernel` + `platform`（新建 application 模块） |

---

## 新增第三方依赖流程

1. 在 **`innospots-nexus-bom`** 的 `<properties>` 增加 `<xxx.version>`（如需）。
2. 在 BOM 的 `dependencyManagement` 登记 `groupId:artifactId:version`。
3. 在业务模块 `<dependencies>` 中只写 `groupId` + `artifactId`。
4. 运行 `mvn -q help:effective-pom` 与 `mvn dependency:tree` 验证。

**禁止**在未更新 BOM 的情况下，在单个模块 POM 中临时写版本「先跑起来」。

---

## 验证命令

```bash
# 检查是否仍有手写 version（应对业务依赖为空输出）
mvn -q help:effective-pom | rg '<version>' 

mvn dependency:tree -pl <module>          # 查看传递链，核对最小依赖
mvn dependency:analyze -pl <module>       # 未使用/未声明依赖
mvn validate                                # enforcer + 构建基线
```

本地 JDK 低于 25 时报告环境不匹配，**不得**为通过构建而下调 BOM/parent 中的 Java 基线。
