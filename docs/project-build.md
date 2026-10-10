# 工程构建与制品发布

本文说明 **innospots-nexus** 在本地如何构建，以及如何发布到 **开发私服** 与 **Maven Central**。
Maven 细节（parent/BOM、flatten、enforcer）见
[`skills/java/java-project/references/build-config.md`](../skills/java/java-project/references/build-config.md)。

## 环境要求

| 项 | 要求 |
|----|------|
| JDK | **25+**（与 `innospots-nexus-parent` 的 `maven.compiler.release` 一致） |
| Maven | **3.9.0+**（`requireMavenVersion`） |
| 版本号 | 根 `pom.xml` 属性 `${revision}`（当前多为 `*-SNAPSHOT`） |

本地 JDK 低于 25 时：**报告环境不匹配**，不要下调项目基线。

```bash
java -version
mvn -version
```

## 日常构建（不发布）

在仓库根目录执行：

```bash
mvn clean compile    # 改 Java 后的编译门禁
mvn validate         # Maven/JDK/插件版本 enforcer
mvn test             # 全量测试
mvn clean install    # 安装到本地 ~/.m2，不推远程
```

单模块（含上游）：

```bash
mvn -pl innospots-nexus-base -am clean install
```

## 发布架构概览

部署目标**只在根** `pom.xml` 的 profile 中声明；子模块（含 `innospots-nexus-bom`、`innospots-nexus-parent`、各 jar）**继承**同一套
`distributionManagement`，**无需**在各模块 POM 重复配置。

```text
settings.xml          凭据与「拉依赖」仓库（解析）
        │
        ▼
根 pom.xml -Pdev      distributionManagement → 阿里云开发私服 (dev-repo)
根 pom.xml -Pcentral  distributionManagement → Sonatype Central (central-repo)
根 pom.xml -Pcentral-publish  GPG 签名 + central-publishing-maven-plugin
```

| Profile | 作用 | 是否默认激活 |
|---------|------|----------------|
| `dev` | 开发环境 **deploy** 地址（`serverId`: `dev-repo`） | 否，命令行显式 `-Pdev` |
| `central` | Central **deploy** 地址（`serverId`: `central-repo`） | 否，与 `central-publish` 联用 |
| `central-publish` | 签名与 Central 发布插件（`publishingServerId`: `central-repo`） | 否 |

未激活 `dev` / `central` 时执行 `deploy` 通常因缺少 `distributionManagement` 而失败，用于**防止误发**。

根 `pom.xml` 中可维护的 URL 属性：

| 属性 | 用途 |
|------|------|
| `innospots.maven.dev.repository.url` | 开发私服 |
| `innospots.maven.central.release.repository.url` | Central Release staging |
| `innospots.maven.central.snapshot.repository.url` | Central Snapshots |

## Maven `settings.xml` 约定

发布与解析分工：

- **发布（deploy）**：由 POM profile + `<servers>` 中 **id 与 `distributionManagement` 一致** 的账号完成。
- **解析（compile/test）**：由 settings 中的 `repositories` profile 提供（如 `public-aliyun`、`innospots-dev`）。

建议在用户级 `settings.xml` 中配置（示例结构，**勿将密码提交到 Git**）：

```xml
<servers>
    <server>
        <id>dev-repo</id>
        <!-- 阿里云 RDC / Packages 用户名、密码或令牌 -->
    </server>
    <server>
        <id>central-repo</id>
        <!-- Sonatype Central 发布令牌 -->
    </server>
</servers>

<mirrors>
    <mirror>
        <id>mirror</id>
        <mirrorOf>central,jcenter,!dev-repo</mirrorOf>
        <url>https://maven.aliyun.com/nexus/content/groups/public</url>
    </mirror>
</mirrors>

<activeProfiles>
    <activeProfile>public-aliyun</activeProfile>
    <activeProfile>innospots-dev</activeProfile>
</activeProfiles>
```

`innospots-dev` profile 仅用于从开发私服 **拉取** `com.innospots` 的 SNAPSHOT；**不要**在 settings 里用
`altSnapshotDeploymentRepository` 覆盖部署地址，开发发布统一使用 **`mvn -Pdev deploy`**。

私服解析仓库的 `<repository><id>` 必须为 **`dev-repo`**，与 `<server><id>dev-repo</id>` 对应，否则鉴权可能对不上。

## 发布到开发环境（阿里云私服）

### 前置条件

1. `settings.xml` 已配置 `dev-repo` 凭据。
2. 当前 `${revision}` 一般为 `x.y.z-SNAPSHOT`（开发迭代常见）。
3. 建议在发布前本地通过：`mvn clean test`（或至少 `mvn clean compile`）。

### 全量发布（推荐）

在仓库根目录：

```bash
mvn -Pdev clean deploy
```

Reactor 会发布所有参与 deploy 的模块，包括：

- `innospots-nexus-bom`、`innospots-nexus-parent`（`packaging=pom`）
- `innospots-nexus-base`、`innospots-nexus-core` 等各 jar 模块及 Spring/Quarkus 组装模块

**不会**发布 `innospots-nexus-sample`（该聚合器设置了 `maven.deploy.skip=true`）。

`flatten-maven-plugin` 会在 deploy 前把 POM 中的 `${revision}` 解析为具体版本，下游消费者不会拿到未解析的 CI 友好属性。

### 只发布部分模块

```bash
# 仅 BOM + 构建所需上游
mvn -pl innospots-nexus-bom -am -Pdev clean deploy

# 单个 jar 模块
mvn -pl innospots-nexus-base -am -Pdev clean deploy
```

### 下游消费（开发 SNAPSHOT）

在业务工程 `pom.xml` 中 import BOM（版本与发布的 `${revision}` 一致）：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.innospots</groupId>
            <artifactId>innospots-nexus-bom</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

业务工程需能解析开发私服（公司 settings 或自建 `innospots-dev` 类 profile）。

## 发布到 Maven Central

Central 用于**开源对外**分发，与开发私服相互独立。通常需要：

1. `settings.xml` 中 `central-repo` 的 Sonatype 令牌。
2. 本机可用的 **GPG** 密钥（`central-publish` 在 `verify` 阶段签名）。
3. 根 POM 已具备的许可证、SCM、developer 等元数据（见根 `pom.xml`）。

### SNAPSHOT → Central Snapshots

版本号带 `-SNAPSHOT` 时，deploy 走 `snapshotRepository` URL：

```bash
mvn -Pcentral,central-publish clean deploy
```

### Release → Central（staging）

1. 将 `${revision}` 改为**不带** `-SNAPSHOT` 的发布号（升版流程见 `java:project-upgrade` / `versions:set`）。
2. 全量校验后发布：

```bash
mvn -Pcentral,central-publish clean deploy
```

`central-publishing-maven-plugin` 配置为 `autoPublish=false`，一般在 [Sonatype Central](https://central.sonatype.com/) 控制台核对 staging 后再关闭或提升发布。

Release 制品还需满足 Central 对 **javadoc**、**sources**、签名等要求；`innospots-nexus-parent` 的 `central-publish` profile 会为 jar 模块附加 source/javadoc 并执行 GPG（继承 parent 的 Java 模块）。

### 与开发私服的区别

| 维度 | 开发 (`-Pdev`) | Central (`-Pcentral,central-publish`) |
|------|----------------|----------------------------------------|
| serverId | `dev-repo` | `central-repo` |
| 典型受众 | 公司内部 / 联调 | 公开 Maven 生态 |
| 签名 | 无（由私服策略决定） | GPG（profile 启用） |
| 示例模块 | 同 reactor，sample 跳过 | 同 reactor，sample 跳过 |

## 常用检查命令

```bash
# 当前激活的 settings profile
mvn help:active-profiles

# 激活 dev 后的有效 distributionManagement
mvn -Pdev help:effective-pom -pl innospots-nexus-bom | grep -A6 distributionManagement

# 发布前有效 POM（单模块）
mvn -q help:effective-pom -pl innospots-nexus-base -Pdev
```

## 常见问题

| 现象 | 排查 |
|------|------|
| `deploy` 报 401 / 403 | `distributionManagement` 的 `<id>` 是否与 `settings.xml` 里 `<server><id>` 一致（`dev-repo` / `central-repo`） |
| 开发私服拉不到 SNAPSHOT | settings 是否启用 `innospots-dev`；mirror 是否包含 `!dev-repo` |
| deploy 提示无 distributionManagement | 是否忘记 `-Pdev` 或 `-Pcentral` |
| Central 签名失败 | `gpg --list-secret-keys`；`maven-gpg-plugin` 的 pinentry / 环境变量 |
| 下游 POM 仍含 `${revision}` | flatten 是否执行；是否 deploy 了 flatten 后的产物 |
| sample 未出现在私服 | 预期行为：`innospots-nexus-sample` 故意 `maven.deploy.skip=true` |

## 相关文档

- [IDE 设置](./ide-setup.md)
- [Maven 构建配置参考](../skills/java/java-project/references/build-config.md)
- [模块布局](../skills/java/java-project/references/module-layout.md)
