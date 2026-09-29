# smallrye-open-api-maven-plugin — 对外 REST 模块配置

权威 OpenAPI 注解条文：[`standards/openapi.md`](../standards/openapi.md)。  
契约落地与检查清单：[`openapi-contract.md`](openapi-contract.md)。

本文件说明 **哪些 Maven 模块需要插件**、**版本从哪里来**、**完整 POM 模板**、
**console / portal / platform 差异**、以及 **新建对外 API 模块时的配置步骤**。

---

## 适用范围

| 模块类型 | 是否配置插件 | 说明 |
|----------|--------------|------|
| `innospots-nexus-console` | 是 | 管理控制台 Jakarta REST |
| `innospots-nexus-portal` | 是 | 租户域 REST（含 copy 到 JAR） |
| `innospots-nexus-platform` | 是 | 运维域 REST |
| `base` / `core` / `plugin` | 否 | 无对外 REST 面；仅被 `scanPackages` 引用契约类型 |
| 仓库外产品 `*-console` / `*-service` 等 | 按需 | 暴露 JAX-RS 且需独立 OpenAPI 产物时，复制本模板并改包名 |

**产物约定**：每个配置插件的模块在 `package` 后 JAR 内须包含：

```text
META-INF/nexus-openapi/<artifactId>.yaml
```

例如 `innospots-nexus-console.yaml`。运行时由 console 的 `OpenApiBundledSpecs` /
`GET /openapi/specs` 聚合 classpath 上所有此类文件，**不在运行时重新扫描 JAX-RS**。

---

## 版本与父 POM 支持

子模块 **不要写插件 `<version>`**，继承 `innospots-nexus-parent` 的 `pluginManagement`：

| 位置 | 属性 / 坐标 |
|------|----------------|
| `innospots-nexus-parent/pom.xml` | `smallrye-open-api-maven-plugin.version`（当前与 BOM 对齐，如 `4.3.5`） |
| `pluginManagement` | `io.smallrye:smallrye-open-api-maven-plugin` |
| `innospots-nexus-bom/pom.xml` | 同名插件版本，供 BOM 消费者对齐 |

模块 `pom.xml` 仅需：

```xml
<plugin>
    <groupId>io.smallrye</groupId>
    <artifactId>smallrye-open-api-maven-plugin</artifactId>
    ...
</plugin>
```

升级插件版本时改 **parent 属性**（及 BOM），不要在各 REST 模块分散写版本号。

---

## 执行绑定（所有对外模块一致）

| 项 | 值 |
|----|-----|
| `<execution><id>` | `generate-openapi` |
| `<phase>` | `process-classes` |
| `<goal>` | `generate-schema` |

在 `compile` 完成之后、`package` 之前生成 schema，并写入配置的 `outputDirectory`。

---

## 配置项说明

| 配置元素 | 必填 | 说明 |
|----------|------|------|
| `outputDirectory` | 是 | YAML 输出目录（见下文两种打包模式） |
| `schemaFilename` | 是 | 无扩展名；实际文件为 `${schemaFilename}.yaml`，**须等于 `${project.artifactId}`** |
| `outputFileTypeFilter` | 是 | `YAML` |
| `encoding` | 是 | `UTF-8` |
| `scanners` / `scanner` | 是 | 仅 `JAX-RS`（`jakarta.ws.rs` 注解） |
| `scanPackages` | 是 | 逗号分隔包列表；见「scanPackages 规则」 |
| `scanDependenciesDisable` | 是 | `false` — 允许从依赖 JAR 解析被扫描包中的类型 |
| `openApiVersion` | 是 | `3.1.0` |
| `operationIdStrategy` | 是 | `CLASS_METHOD` — 稳定、可预测的 operationId |
| `infoTitle` | 是 | 与模块 `*OpenApiDefinition` 的 `@Info.title` 一致 |
| `infoVersion` | 是 | 与 `@Info.version` 一致（产品 API 版本，非 `${revision}`） |
| `infoDescription` | 是 | 与 `@Info.description` 一致 |
| `infoContactName` | 是 | 与 `@Contact.name` 一致 |

`info*` 与 Java 里 `@OpenAPIDefinition` **必须同步修改**，避免 YAML 与源码注解两套文案。

---

## scanPackages 规则

每个对外 REST 模块至少包含：

1. **本模块 REST 根包**（端点、模块级 `*OpenApiDefinition`、本模块 request/vo）。
2. **共享响应与枚举**（与现有三模块保持一致）：
   - `com.innospots.nexus.base.domain.response`（`R`、`PageResult` 等）
   - `com.innospots.nexus.base.domain.enums`
   - `com.innospots.nexus.base.i18n`（若响应或错误文案类型出现在 schema 中）

**不要**扫描整个 `com.innospots.nexus.base` 或 `core` / `console` 全包，避免无关类型进入 schema。

| 模块 | 本模块根包（示例） |
|------|-------------------|
| console | `com.innospots.nexus.console` |
| portal | `com.innospots.nexus.portal` |
| platform | `com.innospots.nexus.platform` |

**Console 额外扫描（插件契约，非 plugin 模块 YAML）：** 插件 REST 在 console，但
`PluginManagementVo` 等引用 `innospots-nexus-plugin` 内带 `@Schema` 的枚举，须在 console
`scanPackages` 中加入
`com.innospots.nexus.core.plugin.installation.domain.enums`（权威常量见
`PluginOpenApiSupport.INSTALLATION_DOMAIN_ENUMS`）。`innospots-nexus-plugin` 本身**不**配置
SmallRye 插件。

若某 request 类型放在子包（如 `portal.auth.endpoint`），只要仍在 `com.innospots.nexus.portal` 树下即可，**无需**为每个子包单独列一行。

---

## 打包模式 A — 直接写入 `META-INF`（console、platform）

`outputDirectory` 指向编译输出下的 catalog 目录，**无需**额外 `maven-resources-plugin`：

```xml
<outputDirectory>${project.build.outputDirectory}/META-INF/nexus-openapi</outputDirectory>
```

参考：`innospots-nexus-console/pom.xml`、`innospots-nexus-platform/pom.xml`。

---

## 打包模式 B — 先生成再 copy（portal）

Portal 将生成目录与最终 JAR 路径分离，避免与其它 `process-classes` 步骤争用同一目录：

1. **SmallRye** 写到 `${project.build.directory}/generated/openapi`。
2. **`maven-resources-plugin`** 在 `prepare-package` 将  
   `${project.artifactId}.yaml` copy 到  
   `${project.build.outputDirectory}/META-INF/nexus-openapi`。

参考：`innospots-nexus-portal/pom.xml` 中 `package-openapi` execution。

新建模块任选 A 或 B；**与 portal 保持一致时选 B**，否则优先 **A（更简单）**。

---

## 完整模板 — 模式 A（推荐默认）

将 `YOUR_ARTIFACT`、`YOUR_ROOT_PACKAGE`、`YOUR_*` 替换为模块实参：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.smallrye</groupId>
            <artifactId>smallrye-open-api-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>generate-openapi</id>
                    <phase>process-classes</phase>
                    <goals>
                        <goal>generate-schema</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${project.build.outputDirectory}/META-INF/nexus-openapi</outputDirectory>
                        <schemaFilename>${project.artifactId}</schemaFilename>
                        <outputFileTypeFilter>YAML</outputFileTypeFilter>
                        <encoding>UTF-8</encoding>
                        <scanners>
                            <scanner>JAX-RS</scanner>
                        </scanners>
                        <scanPackages>
                            YOUR_ROOT_PACKAGE,
                            com.innospots.nexus.base.domain.response,
                            com.innospots.nexus.base.i18n,
                            com.innospots.nexus.base.domain.enums
                        </scanPackages>
                        <scanDependenciesDisable>false</scanDependenciesDisable>
                        <openApiVersion>3.1.0</openApiVersion>
                        <operationIdStrategy>CLASS_METHOD</operationIdStrategy>
                        <infoTitle>YOUR_INFO_TITLE</infoTitle>
                        <infoVersion>1.0.0</infoVersion>
                        <infoDescription>YOUR_INFO_DESCRIPTION</infoDescription>
                        <infoContactName>Innospots Nexus</infoContactName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 完整模板 — 模式 B（portal 同款）

在模式 A 的 SmallRye 块中改为：

```xml
<outputDirectory>${project.build.directory}/generated/openapi</outputDirectory>
```

并追加：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>package-openapi</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.outputDirectory}/META-INF/nexus-openapi</outputDirectory>
                <resources>
                    <resource>
                        <directory>${project.build.directory}/generated/openapi</directory>
                        <includes>
                            <include>${project.artifactId}.yaml</include>
                        </includes>
                        <filtering>false</filtering>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

`maven-resources-plugin` 版本由 parent 管理，子模块不写 version。

---

## 新建对外 REST 模块 checklist

1. 模块依赖：至少能解析 JAX-RS API 与本模块端点（平台库通常为 `console` 或 `core` + `jakarta.ws.rs-api`）。
2. 添加 `*OpenApiDefinition`（`@OpenAPIDefinition` + `bearerAuth` `@SecurityScheme`）。
3. 按上表配置 SmallRye 插件（A 或 B），`schemaFilename` = `artifactId`。
4. `scanPackages` 含本模块根包 + 三个 base 子包。
5. `info*` 与 `*OpenApiDefinition` 的 `@Info` 对齐。
6. 端点类补 `@Tag` / `@Operation` / `@Schema`（见 [`openapi-contract.md`](openapi-contract.md)）。
7. 运行验证（见下节）；若有契约测试，纳入 CI。

仓库内新增 **reactor 模块** 还须走 `java:project` / `grill-me` 与 `AGENTS.md` 模块职责，本清单仅覆盖 OpenAPI 构建面。

---

## 验证命令

单模块生成并检查 JAR：

```bash
mvn -pl innospots-nexus-console -am clean package -DskipTests
jar tf innospots-nexus-console/target/innospots-nexus-console-*.jar | grep META-INF/nexus-openapi
```

三模块一并：

```bash
mvn -pl innospots-nexus-console,innospots-nexus-portal,innospots-nexus-platform -am package -DskipTests
```

期望每个 JAR 内存在 `META-INF/nexus-openapi/innospots-nexus-<module>.yaml`。

仅重新生成 schema（不跑全量测试）时 `package` 或至少执行到 `process-classes` 之后的 lifecycle。

本地可查看生成 YAML：

```bash
unzip -p innospots-nexus-console/target/innospots-nexus-console-*.jar \
  META-INF/nexus-openapi/innospots-nexus-console.yaml | head
```

---

## 常见问题

| 现象 | 处理 |
|------|------|
| JAR 内无 YAML | 确认执行了 `package`；模式 B 是否配置了 `package-openapi` |
| schema 缺少 `R` / 枚举 | 检查 `scanPackages` 是否包含三个 base 子包 |
| 端点未出现在 paths | 类须在 `scanPackages` 树下，且带 JAX-RS `@Path`；scanner 为 `JAX-RS` |
| `info` 与 Scalar 标题不一致 | 同步改 POM `info*` 与 `*OpenApiDefinition` |
| 多模块同名 yaml | `schemaFilename` 必须唯一（使用 `artifactId`） |

---

## 源码对照

| 模块 | POM 路径 |
|------|----------|
| Console | `innospots-nexus-console/pom.xml` |
| Portal | `innospots-nexus-portal/pom.xml` |
| Platform | `innospots-nexus-platform/pom.xml` |
| Parent 插件版本 | `innospots-nexus-parent/pom.xml` → `pluginManagement` |
