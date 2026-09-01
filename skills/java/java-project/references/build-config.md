# Maven 构建配置参考

## 三层 POM 分工

| 层 | artifactId | 职责 | 是否含代码 |
|----|-----------|------|-----------|
| 根聚合器 | `innospots-nexus` | `<modules>` 注册、`<revision>` 版本属性、flatten 插件执行 | 否 |
| 构建 parent | `innospots-nexus-parent` | 编译属性、插件版本与配置、公共依赖、enforcer 规则、导入 BOM | 否 |
| 版本清单 | `innospots-nexus-bom` | 所有第三方与内部模块的 `dependencyManagement` | 否 |

业务模块的 `<parent>` 指向 **`innospots-nexus-parent`**。

---

## 根聚合器要点

```xml
<groupId>com.innospots</groupId>
<artifactId>innospots-nexus</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>

<properties>
    <revision>0.1.0-SNAPSHOT</revision>
    <innospots.version>${revision}</innospots.version>
    <flatten-maven-plugin.version>1.7.3</flatten-maven-plugin.version>
</properties>
```

`flatten-maven-plugin` 绑定两个执行：

- `flatten` → `process-resources` 阶段，`flattenMode=resolveCiFriendliesOnly`
- `flatten-clean` → `clean` 阶段

作用：把 `${revision}` 解析为实际版本号后再 install/deploy，避免下游消费者拿到
带未解析属性的 POM。

---

## parent 要点

### 编译属性

```xml
<maven.compiler.release>25</maven.compiler.release>
<maven.compiler.parameters>true</maven.compiler.parameters>
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
<maven.version>3.9.0</maven.version>
<lombok.version>1.18.46</lombok.version>
<mapstruct.version>1.6.3</mapstruct.version>
```

`maven.compiler.parameters=true` 保留方法参数名，Jakarta REST 的参数绑定与
反射型契约测试依赖它。

### 注解处理器

在 `maven-compiler-plugin` 的 `annotationProcessorPaths` 中声明，**不走依赖传递**：

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

> Lombok 与 MapStruct 同时使用时顺序敏感：Lombok 必须列在 MapStruct 之前。
> 使用 MapStruct 的模块还需在依赖中声明 `org.mapstruct:mapstruct`（不写版本）。

### 编译期告警

```xml
<showWarnings>true</showWarnings>
```

新增代码不应引入新的编译告警。

### enforcer 规则（绑定 `validate` 阶段）

```xml
<requireMavenVersion><version>[${maven.version},)</version></requireMavenVersion>
<requireJavaVersion><version>[25,)</version></requireJavaVersion>
<requirePluginVersions>
    <banLatest>true</banLatest>
    <banRelease>true</banRelease>
    <banSnapshots>true</banSnapshots>
</requirePluginVersions>
```

含义：Maven ≥ 3.9.0、JDK ≥ 25、插件不得使用 `LATEST`/`RELEASE`/快照版本。
**本地 JDK 不满足时报告环境不匹配，不得下调这些值。**

### 公共依赖（parent `<dependencies>`，所有模块继承）

| 依赖 | scope |
|------|-------|
| `org.slf4j:slf4j-api` | compile |
| `org.projectlombok:lombok` | provided |
| `org.junit.jupiter:junit-jupiter` | test |
| `org.assertj:assertj-core` | test |
| `org.mockito:mockito-core` | test |

模块**不需要**重复声明这些依赖。

### surefire / failsafe

两者都配置 `<useModulePath>false</useModulePath>`，禁用 JPMS 模块路径，
保证反射型契约测试与 Mockito 正常工作。

### versions-maven-plugin

配置 `<generateBackupPoms>false</generateBackupPoms>`，避免目录下堆积 `pom.xml.versionsBackup`。

---

## BOM 要点

BOM 用属性集中管理版本：

```xml
<properties>
    <jackson-bom.version>2.22.0</jackson-bom.version>
    <mybatis-plus.version>3.5.16</mybatis-plus.version>
    <!-- ... -->
</properties>
```

`dependencyManagement` 中登记三类条目：

1. **内部模块**：`innospots-nexus-{base,core,console,kernel,platform}`，版本 `${revision}`
2. **第三方 BOM（import）**：`junit-bom`、`jackson-bom`
3. **第三方普通依赖**：hutool、caffeine、commons-*、httpclient5、lombok、mapstruct、
   jakarta.*、HikariCP、mybatis-plus-*、数据库驱动、lettuce、amqp-client、
   kafka-clients、micrometer、quartz、ulid-creator 等

### 新增第三方依赖的标准动作

1. 在 BOM 的 `<properties>` 加 `<xxx.version>`（若需复用）
2. 在 BOM 的 `dependencyManagement` 加条目
3. 在具体模块 POM 的 `<dependencies>` 声明 `groupId:artifactId`（**不写 version**）
4. 若该库提供注解处理器且是项目级通用，评估是否加入 parent 的 `annotationProcessorPaths`

---

## 模块 POM 模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.innospots</groupId>
        <artifactId>innospots-nexus-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../innospots-nexus-parent/pom.xml</relativePath>
    </parent>

    <artifactId>innospots-nexus-<name></artifactId>

    <name>innospots-nexus-<name></name>
    <description><!-- 模块职责一句话 --></description>

    <dependencies>
        <dependency>
            <groupId>com.innospots</groupId>
            <artifactId>innospots-nexus-base</artifactId>
        </dependency>
        <!-- 使用 MapStruct 时需要 -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
    </dependencies>
</project>
```

`<relativePath>` 按模块相对位置调整。依赖均**不写 `<version>`**。

建完模块后别忘了在根 `pom.xml` 的 `<modules>` 中注册。

---

## 命令矩阵

| 目的 | 命令 |
|------|------|
| 编译门禁（改完 Java 立即执行） | `mvn clean compile` |
| 基线校验（Maven/JDK/插件版本） | `mvn validate` |
| 跑全部测试 | `mvn test` |
| 检查 POM 合并结果 | `mvn -q help:effective-pom` |
| 单模块 + 上游依赖构建 | `mvn -pl <module> -am clean install` |
| 跳过上游、只构建选定模块 | `mvn -pl <module> clean compile` |
| 依赖树 | `mvn dependency:tree` |
| 依赖升级候选 | `mvn versions:display-dependency-updates` |
| 插件升级候选 | `mvn versions:display-plugin-updates` |
| 属性升级候选 | `mvn versions:display-property-updates` |
| 检查未声明/未使用依赖 | `mvn dependency:analyze` |
| 安装到本地仓库 | `mvn clean install` |

`versions:*` 命令只列出候选，**是否升级必须由 `java:tool-upgrade` / `java:project-upgrade`
评估后决定**，不得直接套用。

---

## 常见构建问题定位

| 症状 | 优先排查 |
|------|---------|
| Lombok 生成方法找不到 | `annotationProcessorPaths` 是否含 lombok；IDE 是否启用注解处理 |
| MapStruct 生成实现缺失 | 是否声明了 `org.mapstruct:mapstruct` 依赖；处理器路径中 lombok 是否在 mapstruct 之前 |
| 参数名为 `arg0` | `maven.compiler.parameters` 是否为 `true` |
| 本地模块改动未生效 | 是否用 `-am` 重新构建上游；本地仓库是否有过期 SNAPSHOT |
| enforcer 报 JDK 版本不足 | **报告环境不匹配**，不得下调 `requireJavaVersion` 或 `maven.compiler.release` |
| 反射型契约测试读不到注解 | 注解是否为 `RUNTIME` 保留；surefire `useModulePath` 是否为 `false` |
| 依赖版本不是 BOM 里的值 | `mvn -q help:effective-pom` 看是否被其他路径覆盖 |
| 下游拿到带 `${revision}` 的 POM | flatten 插件是否正常执行 |
