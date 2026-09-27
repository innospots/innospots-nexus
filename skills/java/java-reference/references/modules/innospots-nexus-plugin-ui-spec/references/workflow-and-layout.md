# 文件布局与使用方式

## 资源路径约定

默认 `PageDslConfig.defaults()`：

| 项 | 值 |
|----|-----|
| 基础目录 | `ui-pages`（classpath 根相对） |
| 文件后缀 | `.yaml` |
| 路径模式 | `ui-pages/{domainKey}/{moduleKey}/{pageKey}.yaml` |

示例：

```text
src/main/resources/ui-pages/nexus/menu/menu-main.yaml
src/main/resources/ui-pages/sales/sales/order-list.yaml
```

| 段 | 说明 |
|----|------|
| `domainKey` | 项目领域键；与 `console@1` 贡献中 `ConsoleModuleDeclaration.domainKey` 一致。内置控制台 entry 默认为 `nexus`（`ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY`） |
| `moduleKey` | 模块键；与 `ConsoleModuleDeclaration.moduleKey` 一致 |
| `pageKey` | 与 YAML 内 `page.id` 一致（通常同为 kebab-case 文件名） |

### `pageKey` 格式与入口页

| 规则 | 要求 |
|------|------|
| 模式 | `[a-z][a-z0-9]*(?:-[a-z0-9]+)*`（`PageDslValidator` / `UiSpecPageDeclaration`） |
| 长度 | ≤ 128 |
| 一致性 | `console@1` 的 `pageKey` = YAML `page.id` = 文件名 `{pageKey}.yaml` |
| 内置入口页 | `entryPageKey = {moduleKey}-main`（`ConsoleModuleDescriptor.mainPageKey`） |
| 子页 | 在贡献中声明 `pageKey`，YAML 设置 `page.parentPageKey` 为父页 `page.id` |

内置控制台 entry 插件（`MenuEntryPlugin` 等）的完整说明见
[`innospots-nexus-console` → entry.md](../../innospots-nexus-console/references/entry.md#入口页与-pagekey-规范)。
多模块/多页/多菜单组装见 [`console-entry-and-pages.md`](../../console-entry-and-pages.md)。

### 组装 API（console 模块）

| 场景 | 调用 |
|------|------|
| 单模块（六个内置 entry） | `ConsoleModuleEntrySupport.definition(ConsoleModuleDescriptor)` |
| 单插件多模块 | `ConsoleModuleEntrySupport.definition(ConsoleEntryPluginDescriptor.of(pluginId, …, modules))` |
| 模块内多顶层菜单 | `ConsoleModuleDescriptor.builtin(..., additionalPageKeys, menuEntries)` |

前端路由 `UiSpecPageDeclaration.pagePath` 推荐与 classpath 对齐：

```text
/{domainKey}/{moduleKey}/{pageKey}
```

例如：`/nexus/menu/menu-main`。

## 模块页面清单 vs 页面父子关系

| 来源 | 职责 |
|------|------|
| **console@1 / entry 贡献** | 声明模块内包含哪些页面（`UiSpecPageDeclaration` / `ConsoleModuleDescriptor.allPageKeys()`） |
| **Page DSL `page.parentPageKey`** | 声明模块内页面父子关系；未设置表示一级页面（挂在菜单/模块下），设置后为父页面的子页面 |

目录同步（`ConsoleCatalogSyncService`）以 **PageMeta.parentPageKey** 写入 `nx_console_catalog_resource` 的 `parent_resource_id`；
一级 PAGE 的父资源为 MODULE，子 PAGE 的父资源为父 PAGE。

权限设置接口 `GET /api/nexus/catalog/tree` 返回 **MODULE → 一级 PAGE → 嵌套子 PAGE**（不含 MENU / ACTION / DATASOURCE 节点）。

## 在插件工程中新增页面

1. 在 `plugin.yaml`（或 Java `PluginDefinition`）的 `console@1` 贡献中声明 `domainKey`、`moduleKey` 与各页面的 `pageKey` / `pagePath`。
2. 在 `src/main/resources/ui-pages/<domainKey>/<moduleKey>/<pageKey>.yaml` 编写 Page DSL。
3. 子页面在 YAML 中设置 `page.parentPageKey` 为父页 `page.id`，且父页必须在同一模块贡献中声明。
4. 本地解析校验（见下）。
5. 安装/启用插件后，由 console `ConsoleCatalogSyncService` 同步目录索引。

内置控制台 entry 插件示例目录：

`innospots-nexus-console/src/main/resources/ui-pages/nexus/{menu,dictionary,role,...}/`

## 解析与校验（Java）

```java
PageDslConfig config = PageDslConfig.defaults();
PageDslParser parser = new JacksonPageDslParser(config);
String yaml = /* classpath 读取 */;
PageDsl document = parser.parse(yaml);
new PageDslValidator().validate(document);
```

流程：

1. Jackson YAML → `PageDsl`（未知字段按 config 失败）。
2. `PageDslValidator.validate(document)`（含 `page.parentPageKey` 格式与不得等于 `page.id`）。

加载接口：

```java
PageDslLoader loader = /* 宿主实现，如 ClasspathPageDslLoader */;
PageDsl page = loader.load(domainKey, moduleKey, pageKey);
```

`PageDslConfig.resourcePath(domainKey, moduleKey, pageKey)` 与上述路径模式一致。

## 渲染扩展（可选）

| 类型 | 说明 |
|------|------|
| `PageDslFilter` | 在渲染前变换 `PageDsl` |
| `PageDslFilterChain` | 有序过滤器链 |
| `PageDslRenderContext` | `moduleKey`、`pageKey`、参数 map |

用于租户级裁剪、特性开关或 A/B，**不**改变 YAML 文件本身。

## 测试资源

| 文件 | 说明 |
|------|------|
| `innospots-nexus-plugin/src/test/resources/ui-pages/demo/demo/customer-list.yaml` | 全功能示例 |
| `innospots-nexus-plugin/src/test/resources/ui-pages/sales/sales/order-list.yaml` | 最小示例 |

运行模块测试：

```bash
mvn -pl innospots-nexus-plugin -am test
```

## 常见错误

| 现象 | 原因 |
|------|------|
| `PageDsl dsl version must be 1.0` | `dsl` 缺失或版本不对 |
| `PageDsl resource not found` | classpath 路径未包含 `domainKey` 段或目录层级错误 |
| `parentPageKey is not declared in module` | 子页父键未在 console@1 页面列表中声明 |
| `Unknown data source referenced by action` | `reload` 等引用了未定义的 `dataSources` 键 |
| `Unknown component reference` | `component: foo` 但 `components` 无 `foo` |
| Jackson 未知属性 | 拼写错误字段；默认严格模式 |
