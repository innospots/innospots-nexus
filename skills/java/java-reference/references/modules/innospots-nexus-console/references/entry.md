# 包 `entry`

**innospots-nexus-console** 中与内置 entry **身份、版本、前端路由**相关的薄层。`console@2` 贡献树与 `PluginDefinition` 组装在 **plugin** 模块（见 [contribution-console-entry-assembly.md](../../innospots-nexus-plugin/references/contribution-console-entry-assembly.md)）。

**技能总索引：** [console-entry-and-pages.md](../../../console-entry-and-pages.md)

## 本包类型

| 类 | 说明 |
|----|------|
| `BuiltinConsoleEntryPlugins` | 本模块内置 entry 插件 ID、`REQUIRED_PLUGIN_IDS`、`pluginVersion()`；`tagsFor(pluginId, contribution)` 委托 `ConsoleEntryTags` |
| `ConsolePageRoutes` | `BUILTIN_DOMAIN_KEY`（`nexus`）、`PAGE_ROUTE_PREFIX`、`compositePageKey`、`pagePath`（委托 `PageDslPageRef`） |

内置 SPI 实现位于 `com.innospots.nexus.console.plugin.entry`：

| 类 | `pluginId` | `moduleKey` | 入口页 YAML |
|----|------------|-------------|-------------|
| `PluginManagementEntryPlugin` | `BuiltinConsoleEntryPlugins.PLUGIN_MANAGEMENT` | `plugin` | `ui-pages/nexus/plugin/nexus-plugin-main.yaml` |

`REQUIRED_PLUGIN_IDS` 当前仅包含插件管理一个 ID。其它 `ui-pages/nexus/**` 资源存在时，须由新的 `Plugin` 实现或 portal/platform 贡献注册后才会进入 catalog。

---

## 入口页与 `pageKey` 规范

### 术语

| 名称 | 含义 |
|------|------|
| **`pageKey`** | 复合键 `{domainKey}-{moduleKey}-{pageSuffix}`；与 YAML `page.id`、文件名一致 |
| **`entryPageKey`** | MENU 节点上的 `entryPageKey`；侧栏点击后打开的 Page DSL |
| **`domainKey`** | `ConsolePluginContribution.domainKey` 与 `ui-pages/{domainKey}/...` 第一段 |
| **`moduleKey`** | MODULE 节点键；与 `ui-pages/{domainKey}/{moduleKey}/` 目录名一致 |

### 命名与编码

- 编码：`PageDslPageRef.encode(domainKey, moduleKey, pageSuffix)` 或 `ConsolePageRoutes.compositePageKey(...)`
- domain / module 段禁止含 `-`；`pageSuffix` 可含连字符
- 解析：`PageDslPageRef.decode(pageKey)`（`split("-", 3)`）

示例：`PageDslPageRef.encode("nexus", "plugin", "main")` → `nexus-plugin-main` → `ui-pages/nexus/plugin/nexus-plugin-main.yaml`

### `pagePath`（前端路由）

```text
/page/{domainKey}/{moduleKey}/{pageKey}
```

实现：`ConsolePageRoutes.pagePath(domainKey, moduleKey, pageKey)`。导航组装见 `NavigationMenuAssembler`。

### 授权名

MENU contribute **无** `grantName` 字段；权限目录组装时访问项 `grantName` 与 `entryPageKey` 相同。

### 子页与 catalog

子页不在 MENU 上重复声明时，由 Page DSL `page.parentPageKey` 与 `ConsoleCatalogSyncService` 挂到父 PAGE 下。MODULE 的 `viewGroups` / `operationGroups` 默认含 `default` 组（`ConsolePermissionDefaults`）。

### 第三方 / 新产品 entry

1. 在 **plugin** 用 `ConsoleContributeNodes` + `ConsoleEntryPlugins.define` 构建贡献；或
2. 在 `plugin.yaml` 声明 `console@2`（由 `ConsolePluginContributionDecoder` 解码）。

标签键使用 `ConsoleEntryTags`；模块专属 `pluginId` 常量放在各自 Maven 模块的 `Builtin*EntryPlugins` 中，**不要**在 console 包中声明不存在的 SPI 插件 ID。

---

## BuiltinConsoleEntryPlugins

### 常量

| 名称 | 说明 |
|------|------|
| `PLUGIN_MANAGEMENT` | `com.innospots.nexus.console.plugin-management` |
| `REQUIRED_PLUGIN_IDS` | 宿主 `nexus.plugins.required` 应包含的列表（当前一项） |

### 方法

#### `pluginVersion() → String`

`innospots-nexus-console` JAR 的 `Implementation-Version`；缺失时 `0.0.0-dev`。

#### `tagsFor(String pluginId, ConsolePluginContribution contribution) → Tags`

委托 `ConsoleEntryTags.forContribution`。

---

## ConsolePageRoutes

| 常量 / 方法 | 说明 |
|-------------|------|
| `BUILTIN_DOMAIN_KEY` | `"nexus"` |
| `PAGE_ROUTE_PREFIX` | `"/page"` |
| `compositePageKey(domain, module, suffix)` | 委托 `PageDslPageRef.encode` |
| `pagePath(domain, module, pageKey)` | 前端路由路径 |

---

## 相关文档

| 主题 | 位置 |
|------|------|
| 组装 API | [contribution-console-entry-assembly.md](../../innospots-nexus-plugin/references/contribution-console-entry-assembly.md) |
| 插件管理 entry | [plugin-entry.md](plugin-entry.md) |
| Page DSL 布局 | [workflow-and-layout.md](../../innospots-nexus-plugin-ui-spec/references/workflow-and-layout.md) |
