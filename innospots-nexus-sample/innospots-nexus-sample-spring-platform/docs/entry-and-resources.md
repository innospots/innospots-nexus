# Entry 定义、引入方式与资源目录

## 1. Entry 是什么

本工程中的 Entry 是 `console@2` 插件贡献，用来声明管理台目录结构：

```text
PluginDefinition
  └── ConsolePluginContribution
        ├── domainKey
        └── modules[]
              └── MODULE / MENU
```

Entry 负责：

- 声明领域键、模块键、显示名、描述、图标和排序。
- 声明稳定的 `entryPageKey`。
- 形成 catalog、导航菜单和权限目录的模块/资源骨架。

Entry 不负责：

- 创建页面文件或 React 组件。
- 实现 REST Endpoint。
- 创建数据库表。
- 保存生产配置或密钥。
- 按 `entryPageKey` 提供页面运行时。

## 2. 核心字段

| 字段 | 说明 |
|------|------|
| `pluginId` | 稳定反向域名，例如 `com.example.reports.console` |
| `domainKey` | 业务领域键，小写 kebab-case，例如 `reports` |
| `moduleKey` | 模块键，同一插件内唯一，例如 `report-order` |
| `entryPageKey` | 稳定、不透明的页面入口键，推荐 `{domainKey}-{moduleKey}-{pageSuffix}` |
| `displayName` / `description` | `I18nObject`，用于目录和多语言展示 |
| `orderIndex` | 同级排序值，不参与资源身份 |
| `icon` | 前端图标标识，不代表后端资源 |
| `viewGroups` | 访问侧权限分组，必须包含 `default` |
| `operationGroups` | 操作侧权限分组，必须包含 `default` |
| `children` | 嵌套 MODULE 或 MODULE 下的 MENU |

节点规则：

- 叶子 MODULE：有 `moduleKey` 和 `entryPageKey`，没有 `children`。
- 分组 MODULE：有 `moduleKey` 和 `children`，没有 `entryPageKey`。
- MENU：没有 `moduleKey`，有 `entryPageKey`，且 `children` 为空。
- 同一 MODULE 下 `entryPageKey` 必须唯一。
- `entryPageKey` 发布后应保持稳定。catalog 路由、导航和前端页面映射都依赖它。
- `entryPageKey` **不是**当前的权限项名称，也不直接写入 `nx_permission_grant`。权限目录中的访问项和操作项都来自模块下的 `@EndpointGrant.name`，详见
  [permissions-and-openapi.md](permissions-and-openapi.md#3-授权键)。
- 不要通过拆分 `entryPageKey` 字符串推导 domain 或 module；运行时也不保证这种格式。

资源键：

```text
MODULE -> module:{moduleKey}
MENU   -> menu:{ownerModuleKey}.{entryPageKey}
```

目录菜单的内部路由由后端生成：

```text
/page/{domainKey}/{moduleKey}/{entryPageKey}
```

该路由只表示目录中的入口地址，不保证前端已经有对应页面。

## 3. 当前 platform Entry

`innospots-nexus-platform` 的 `entry` 包使用五个窄类型：

| 类型 | 职责 |
|------|------|
| `BuiltinPlatformEntryPlugins` | plugin ID、domainKey、realm 标签、内置必选插件列表 |
| `PlatformModuleDescriptors` | 集中声明 MODULE 常量、入口键和稳定顺序 |
| `PlatformEntryPluginDescriptor` | 绑定 plugin ID、显示文本和 MODULE 列表，拒绝空列表 |
| `PlatformModuleEntrySupport` | 调用 `ConsoleContributions.entryPlugin(...)` 组装定义 |
| `PlatformConsoleEntryPlugin` | 实现 `Plugin.definition()`，由 SPI 发现 |

内置模块：

| moduleKey | entryPageKey |
|-----------|--------------|
| `organization` | `platform-organization-main` |
| `menu` | `platform-menu-main` |
| `user` | `platform-user-main` |
| `dictionary` | `platform-dictionary-main` |
| `role` | `platform-role-main` |
| `permission` | `platform-permission-main` |
| `logger` | `platform-logger-main` |
| `settings` | `platform-settings-registration-mode` |

对应源码与 SPI 资源：

```text
innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/entry/
innospots-nexus-platform/src/main/resources/META-INF/services/
  com.innospots.nexus.core.plugin.contract.Plugin
```

SPI 文件当前注册：

```text
com.innospots.nexus.platform.entry.PlatformConsoleEntryPlugin
```

运行工程还需要在 `nexus/console-plugins.yaml` 中把
`com.innospots.nexus.platform.console` 放入 `nexus.plugins.required`，否则虽然类路径能发现插件，
却不会按“平台必需插件”的启动约束检查它。

新增 platform 内置模块时，主要修改：

```text
innospots-nexus-platform/src/main/java/com/innospots/nexus/platform/entry/PlatformModuleDescriptors.java
```

然后补充契约、Endpoint、权限和测试。只有新增独立插件实现时才需要修改 SPI 文件。

## 4. 引入一个 Java Entry 插件

### 4.1 实现 `Plugin`

```java
public final class ReportsConsoleEntryPlugin implements Plugin {

    @Override
    public PluginDefinition definition() {
        ConsoleContributeNode reports = ConsoleContributeNode.singlePageModule(
                "reports",
                10,
                I18nObject.of("zh-CN", "报表", "en", "Reports"),
                I18nObject.of("zh-CN", "报表管理", "en", "Report management"),
                "reports-reports-main",
                "chart");

        return ConsoleContributions.entryPlugin(
                "com.example.reports.console",
                "1.0.0",
                ConsoleContributions.discoveryTags(
                        "com.example.reports.console",
                        "reports",
                        List.of("reports")),
                I18nObject.of("zh-CN", "报表", "en", "Reports"),
                I18nObject.of("zh-CN", "报表管理", "en", "Report management"),
                "reports",
                List.of(reports));
    }
}
```

`definition()` 必须无副作用，不能读取数据库、环境变量或当前登录用户。

### 4.2 注册 Java SPI

在插件 JAR 或运行模块资源中创建：

```text
src/main/resources/META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin
```

内容为一行一个实现类：

```text
com.example.reports.console.ReportsConsoleEntryPlugin
```

`ClasspathPluginDiscovery` 通过 `ServiceLoader` 加载该实现。

### 4.3 配置插件启停

在宿主的 `nexus/console-plugins.yaml` 或等价配置中：

```yaml
nexus:
  plugin:
    auto-install: true

  plugins:
    required:
      - com.innospots.nexus.platform.console
      - com.example.reports.console
    disabled: []
```

语义：

- `auto-install: true`：classpath 首次发现时自动写入安装记录。
- `required`：启动后必须处于 `ACTIVE`；未发现或未启动会阻止应用成功启动。
- `disabled`：不允许自动启动。
- `required` 与 `disabled` 不能包含同一个插件 ID。

如果扩展只是可选能力，不要加入 `required`。否则扩展 JAR 未部署时整个宿主会启动失败。

### 4.4 插件运行配置

插件配置 schema 在 Java `ConfigDefinition` 或 YAML 的 `spec.config` 中声明。运行值使用：

```yaml
plugins:
  com.example.reports.console:
    endpoint: https://reports.example.com
    timeout: 30
```

对应环境变量：

```text
plugins.com.example.reports.console.endpoint
-> NEXUS_PLUGIN_COM_EXAMPLE_REPORTS_CONSOLE_ENDPOINT
```

插件配置优先级从低到高：

```text
schema 默认值
-> Spring/宿主汇总配置
-> 动态 ConfigSource
-> 环境变量
-> JVM 系统属性
-> runtimeVariables
```

插件启动后只持有配置快照；修改配置后需要停用并重新启用插件。

## 5. 使用 YAML Entry 的替代方式

简单静态 Entry 也可以使用：

```text
src/main/resources/META-INF/nexus/plugin.yaml
```

示意：

```yaml
apiVersion: nexus.plugin/v1
kind: Plugin
metadata:
  pluginId: com.example.reports.console
  version: "1.0.0"
spec:
  apiVersion: 1
  displayName:
    zh-CN: 报表
  contributions:
    - type: console
      majorVersion: 2
      domainKey: reports
      displayName:
        zh-CN: 报表
      modules:
        - moduleKey: reports
          displayName:
            zh-CN: 报表
          orderIndex: 10
          entryPageKey: reports-reports-main
          children: []
```

注意：

- YAML 路径固定为 `META-INF/nexus/plugin.yaml`，一个文件是一个插件。
- `spec.contributions` 和 `contributions[].modules` 都是数组；不要写成单个 `module:` 对象。
- 解码器严格校验字段白名单。根节点可写 `type`、`majorVersion`、`domainKey`、`displayName`、
  `description`、`orderIndex`、`entryPageKey`、`modules`；根 `entryPageKey` 必须为空。
- MODULE 可写 `moduleKey`、`displayName`、`description`、`orderIndex`、`entryPageKey`、`icon`、
  `viewGroups`、`operationGroups`、`children`；MENU 不允许写 `moduleKey`、`viewGroups` 或
  `operationGroups`。
- 未知字段、重复键、YAML alias、空 `modules` 都会使插件定义解析失败。
- Java SPI 和 YAML 最终都会编译成 `PluginDefinition`。
- 两种方式不要声明同一个 `pluginId`。
- YAML 适合纯声明；需要生命周期、动态配置读取或资源作用域时优先 Java 插件。

## 6. Entry 到目录和导航的时机

```text
classpath 发现
  -> 安装记录对账
  -> 创建插件运行时
  -> 启动 ACTIVE 插件
  -> ConsoleCatalogSyncStartupTask
  -> 写入/更新 nx_console_catalog_resource
  -> 权限目录与导航读取
```

同步会：

- 新增或更新当前活动贡献。
- 把不再存在的资源标记为 `DISABLED`。
- 保留历史 `nx_permission_grant`，不会因插件停用而删除授权记录。

手动触发同步：

```text
POST /api/d/nexus/catalog/sync
```

## 7. 资源目录

### 7.1 后端运行工程

| 目录/文件 | 所属 | 用途 | 是否手改 |
|-----------|------|------|----------|
| `src/main/resources/application.yaml` | 运行模块 | 应用名、profile、Jersey、配置导入、端口、Actuator | 是 |
| `src/main/resources/application-local.yaml` | 运行模块 | 本地 H2、schema 顺序、日志 | 是 |
| `src/main/resources/application-test.yaml` | 运行模块 | 测试内存库 | 是 |
| `src/main/resources/application-prod.yaml` | 运行模块 | 生产 MySQL 与环境变量 | 是 |
| `src/main/resources/nexus/console-auth.yaml` | 运行模块 | Token、验证码、锁定、RSA | 是 |
| `src/main/resources/nexus/console-registration.yaml` | 运行模块 | 注册模式、OTP、邮件、短信 | 是 |
| `src/main/resources/nexus/console-credential.yaml` | 运行模块 | TOTP 主密钥 | 是 |
| `src/main/resources/nexus/console-web.yaml` | 运行模块 | 安全、dev session、路径规则、CORS | 是 |
| `src/main/resources/nexus/console-openapi.yaml` | 运行模块 | Scalar 路由与页面选项 | 是 |
| `src/main/resources/nexus/console-plugins.yaml` | 运行模块 | 插件安装、required/disabled、插件配置 | 是 |
| `src/main/resources/console/meta-sitemap.yaml` | 运行模块 | 后端导航兜底菜单 | 是 |
| `src/main/resources/schema/*.sql` | 运行模块 | H2/测试库初始化 | 是 |
| `src/main/resources/log4j2.xml` | 运行模块 | 日志配置 | 是 |

### 7.2 插件 JAR

| 目录/文件 | 用途 |
|-----------|------|
| `META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin` | Java SPI 注册 |
| `META-INF/nexus/plugin.yaml` | YAML 插件声明 |
| `META-INF/nexus-openapi/<artifactId>.yaml` | 构建期 OpenAPI 产物，不应手改 |

### 7.3 前端工程

前端资源不在 `sample-spring-platform/src/main/resources` 中：

| 目录/文件 | 用途 |
|-----------|------|
| `innospots-nexus-ui-console/src/server/yaml/sitemap.yaml` | 前端页面、布局和菜单总表 |
| `innospots-nexus-ui-console/src/server/yaml/**/{pageId}.yaml` | 页面 DSL，文件名必须等于 `pages[].id` |
| `innospots-nexus-ui-console/app/api/sitemap/route.ts` | BFF 的 `/api/sitemap`，按会话过滤后返回前端 sitemap |
| `innospots-nexus-ui-console/app/api/public/pages/[id]/route.ts` | 懒加载页面 DSL |
| `innospots-nexus-ui-console/src/pactor/components/` | 自定义组件 |
| `innospots-nexus-ui-console/public/` | 静态资源 |

后端 `console/meta-sitemap.yaml` 与前端 `sitemap.yaml` 的 `menus[]` 是两套事实源，菜单节点外形相近，但可见性字段不同：后端使用 `roles`，前端 BFF 使用 `public` / `permission`。二者不是同一个文件，也不会互相读取。

## 8. 如何新增资源目录

### 8.1 新增后端配置目录

只有宿主读取约定路径时才需要新建目录。例如：

```text
src/main/resources/nexus/console-observability.yaml
```

还必须在 `application.yaml` 的 `spring.config.import` 中显式加入：

```yaml
spring:
  config:
    import:
      - optional:classpath:nexus/console-observability.yaml
```

仅仅创建文件不会自动加载。

### 8.2 新增插件资源

在插件库中创建：

```text
src/main/resources/META-INF/services/...
src/main/resources/META-INF/nexus/plugin.yaml
```

如果插件还输出 REST 规范，再由 Maven 构建生成：

```text
src/main/resources/META-INF/nexus-openapi/<artifactId>.yaml
```

`target/classes` 下的同名文件是构建输出，不要当作源码提交。

### 8.3 新增前端页面目录

1. 在 `src/server/yaml/` 下创建 `{pageId}.yaml`。
2. 在前端 `sitemap.yaml` 的 `pages[]` 添加页面记录。
3. 需要侧栏入口时，在前端 `sitemap.yaml` 的 `menus[]` 或后端 Entry/meta-sitemap 中添加菜单。
4. 若 `lazy: true`，确认 `/api/public/pages/{id}` 能按 `id` 找到同名页面 DSL。
5. 确认 `pageId`、`entryPageKey` 和前端路由策略一致；不一致时在 BFF 或页面 DSL 中做显式映射。

### 8.4 资源冲突规则

- `console/meta-sitemap.yaml` 使用 classpath 固定路径。多个 JAR 放同名文件会产生 classpath 选择不确定性。
- 配置和导航兜底文件建议只放在最终运行模块。
- OpenAPI 文件名必须等于对应 Maven `artifactId`，否则规范 ID 会与模块身份不一致。
- `entryPageKey`、`moduleKey` 和资源键发布后必须保持稳定。

## 9. 检查表

- [ ] Entry 只声明目录，不承担页面和 REST 实现。
- [ ] plugin ID、domainKey、moduleKey、entryPageKey 命名稳定。
- [ ] Java SPI 或 YAML 只选一种注册方式。
- [ ] 可选插件没有误加入 `required`。
- [ ] 插件配置 schema、运行值键和环境变量一致。
- [ ] catalog 已同步，资源状态为 `ENABLED`。
- [ ] 前端已有对应页面，或明确只提供外部跳转/占位入口。
- [ ] 资源放在运行模块、插件 JAR 或前端工程中的正确位置。

