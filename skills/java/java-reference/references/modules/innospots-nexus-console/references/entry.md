# 包 `entry`

内置控制台 **entry 插件**：每个插件贡献一个 `console@1` 模块、该模块下的 Page DSL 页面清单、以及一条顶层菜单节点。页面正文在 classpath `ui-pages/`；本包负责元数据组装。

**相关文档：**

| 主题 | 位置 |
|------|------|
| Page DSL 路径、`parentPageKey` | [`innospots-nexus-plugin-ui-spec` → workflow-and-layout.md](../../innospots-nexus-plugin-ui-spec/references/workflow-and-layout.md) |
| `page.id` 字段 | [`yaml-document.md`](../../innospots-nexus-plugin-ui-spec/references/yaml-document.md) |
| `console@1` 类型 | [`innospots-nexus-plugin` → contribution-console.md](../../innospots-nexus-plugin/references/contribution-console.md) |
| `MenuEntryPlugin` 示例 | [`menu-entry.md`](menu-entry.md) |

---

## 入口页与 `pageKey` 规范

### 术语

| 名称 | 含义 |
|------|------|
| **`pageKey`** | 模块内页面稳定标识；与 `UiSpecPageDeclaration.pageKey`、`PageDsl.page.id`、YAML 文件名（无后缀）**必须一致** |
| **`entryPageKey`** | 模块**入口页**的 `pageKey`；侧栏菜单 `MenuDeclaration.page` 指向该键 |
| **`additionalPageKeys`** | 同模块内除入口页外、需在目录/权限中出现的其它页面键（通常含子页；父子关系由 YAML `parentPageKey` 定义） |
| **`domainKey`** | Page DSL classpath 第一段；内置模块固定为 `nexus`（`ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY`） |
| **`moduleKey`** | 模块键；与 `ui-pages/{domainKey}/{moduleKey}/` 目录名、`ConsoleModuleDeclaration.moduleKey` 一致 |

### 命名规则（与校验器一致）

`pageKey` / `page.id` 必须匹配：

```text
[a-z][a-z0-9]*(?:-[a-z0-9]+)*
```

| 规则 | 说明 |
|------|------|
| 长度 | ≤ 128 字符 |
| 字符集 | 小写字母、数字、连字符；**不以连字符开头/结尾** |
| 风格 | **kebab-case**（如 `menu-main`、`order-list`） |
| 唯一性 | 同一 `moduleKey` 下 `pageKey` 全局唯一（含入口页与子页） |

`PageDslValidator` 与 `UiSpecPageDeclaration` 使用同一 `KEY_PATTERN`；YAML 中 `page.parentPageKey` 也须满足该模式，且**不得等于** `page.id`。

### 内置模块默认入口页键

对 `ConsoleModuleDescriptor.builtin(...)`：

```text
entryPageKey = ConsoleModuleDescriptor.mainPageKey(moduleKey)
             = {moduleKey}-main
```

示例：`moduleKey=menu` → 入口页 `menu-main`；`moduleKey=plugin` → `plugin-main`。

对应资源：

```text
ui-pages/nexus/{moduleKey}/{entryPageKey}.yaml
```

示例：`ui-pages/nexus/menu/menu-main.yaml`，且 YAML 内 `page.id: menu-main`。

### `pagePath`（前端路由）

不手写进 `builtin()`；由 `ConsoleModuleEntrySupport` 按统一公式生成：

```text
pagePath = /{domainKey}/{moduleKey}/{pageKey}
```

实现：`ConsoleModuleDescriptor.pagePath(domainKey, moduleKey, pageKey)`。

内置示例：`/nexus/menu/menu-main`。

第三方插件在 `plugin.yaml` 的 `console@1` 中声明 `pageKey` + `pagePath` 时，**推荐**与上述公式一致，并与 classpath 布局对齐。

### 三层一致性（安装前自检）

```text
console@1 pages[].pageKey  ==  PageDsl page.id  ==  {pageKey}.yaml 文件名
```

目录同步（`ConsoleCatalogSyncService`）会：

1. 遍历贡献中的每个 `pageKey`，加载 `PageDslLoader.load(domainKey, moduleKey, pageKey)`；
2. 校验 `parentPageKey` 指向的父页已在**同一模块**贡献中声明；
3. 将一级 PAGE 挂在 MODULE 下，子 PAGE 挂在父 PAGE 下（见 ui-spec workflow）。

若贡献中声明了 `pageKey` 但 classpath 无对应 YAML，同步/测试加载会失败。

### 入口页 vs 子页

| 维度 | 入口页 | 子页（非入口） |
|------|--------|----------------|
| 在描述符中 | `entryPageKey`（或 `mainPageKey` 推导） | `additionalPageKeys` 列表项 |
| 在 `console@1` | `pages` 中第一项（`allPageKeys()` 顺序） | 同列表后续项 |
| 在菜单 | `menuTree` 中 `MenuDeclaration.page(entryPageKey)` | 一般不单独占顶层菜单项；由入口页内导航或 `parentPageKey` 树展示 |
| 在 YAML | 通常**无** `parentPageKey` | 设置 `page.parentPageKey` 为父页 `page.id` |
| 在权限树 API | MODULE 下的一级 PAGE | 嵌套在父 PAGE 下 |

内置六个模块当前均为**单入口页**（无 `additionalPageKeys`）；扩展子页时使用 `builtin(..., List.of("other-page"))`  overload。

### 与 `menuKey` 的关系（内置）

`ConsoleModuleDescriptor.builtin` 将 **`menuKey` 设为与 `entryPageKey` 相同**（均为 `{moduleKey}-main`）。`menuKey` 是 `console@1` 菜单树节点的稳定键，与租户库表 `nx_menu` **无关**。

### 第三方插件（非内置 entry）

不经过 `ConsoleModuleEntrySupport` 时，在 `plugin.yaml` / Java `PluginDefinition` 中自行声明：

- `ConsoleModuleDeclaration`：`domainKey`、`moduleKey`、`pages`、`menuTree`；
- 每个 `UiSpecPageDeclaration`：`pageKey`、`pagePath`（及可选嵌套 `children`）；
- 每个 `MenuDeclaration.page(...)`：菜单节点绑定的 `pageKey`（入口菜单通常指向模块主页面键）。

规范上与内置相同：**pageKey 与 YAML `page.id` 及文件名一致**。

---

## 内置 entry 插件一览

| `pluginId` 常量 | `*EntryPlugin` | `moduleKey` | `entryPageKey` | YAML 资源 |
|-----------------|----------------|-------------|----------------|-----------|
| `BuiltinConsoleEntryPlugins.MENU` | `MenuEntryPlugin` | `menu` | `menu-main` | `ui-pages/nexus/menu/menu-main.yaml` |
| `DICTIONARY` | `DictionaryEntryPlugin` | `dictionary` | `dictionary-main` | `.../dictionary/dictionary-main.yaml` |
| `LOGGER` | `LoggerEntryPlugin` | `logger` | `logger-main` | `.../logger/logger-main.yaml` |
| `PERMISSION` | `PermissionEntryPlugin` | `permission` | `permission-main` | `.../permission/permission-main.yaml` |
| `ROLE` | `RoleEntryPlugin` | `role` | `role-main` | `.../role/role-main.yaml` |
| `PLUGIN_MANAGEMENT` | `PluginManagementEntryPlugin` | `plugin` | `plugin-main` | `.../plugin/plugin-main.yaml` |

`BuiltinConsoleEntryPlugins.REQUIRED_PLUGIN_IDS` 列出上述六个 id，供宿主校验内置 entry 是否齐全。

---

## 组装流程（`ConsoleModuleEntrySupport`）

```text
ConsoleModuleDescriptor
        │
        ▼
PluginDefinition.builder(pluginId)
  .version(BuiltinConsoleEntryPlugins.pluginVersion())
  .tags(BuiltinConsoleEntryPlugins.tagsFor(descriptor))
  .contribute(ConsolePluginContribution)
        │
        ▼
ConsoleModuleDeclaration(
  domainKey, moduleKey, displayName, description,
  pages = allPageKeys → UiSpecPageDeclaration(pageKey, pagePath(...), []),
  menuTree = [ MenuDeclaration.page(menuKey, pageTitle, menuIcon, orderIndex, entryPageKey) ]
)
```

各 `*EntryPlugin` 仅持有静态 `ConsoleModuleDescriptor` 并调用 `ConsoleModuleEntrySupport.definition(DESCRIPTOR)`。

---

## BuiltinConsoleEntryPlugins

**类型：** class

内置控制台 entry 插件身份与组装元数据常量。

### 常量

| 名称 | 说明 |
|------|------|
| `MENU` … `PLUGIN_MANAGEMENT` | 六个反向域名 `pluginId` |
| `REQUIRED_PLUGIN_IDS` | 宿主应安装的内置 entry 列表 |
| `TAG_KIND` / `TAG_KIND_ENTRY` | 插件标签 `kind=entry` |
| `TAG_DOMAIN` / `TAG_MODULE` | 标签 `domain`、`module` 键名 |

### 方法

#### `pluginVersion() → String`

- **说明：** 当前 `innospots-nexus-console` JAR 的 `Implementation-Version`（Maven `${revision}`）；缺失时 `0.0.0-dev`。
- **用途：** 内置 entry 的 `PluginDefinition.version`。

#### `tagsFor(ConsoleModuleDescriptor descriptor) → Tags`

- **说明：** 默认标签：`kind=entry`、`domain={domainKey}`、`module={moduleKey}`。

---

## ConsoleModuleDescriptor

**类型：** record

单个内置控制台模块 entry 插件的不可变元数据。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pluginId` | `String` | 反向域名插件标识 |
| `domainKey` | `String` | Page DSL 领域目录（内置为 `nexus`） |
| `moduleKey` | `String` | 模块键与 `ui-pages/.../{moduleKey}/` 目录名 |
| `entryPageKey` | `String` | 模块入口页 PageDsl 页面键 |
| `additionalPageKeys` | `List<String>` | 模块内其余页面键（去重；不含重复 `entryPageKey`） |
| `menuKey` | `String` | `console@1` 顶层菜单节点键（内置等于 `entryPageKey`） |
| `menuIcon` | `String` | 菜单图标 |
| `orderIndex` | `int` | 同级菜单排序 |
| `displayName` | `I18nObject` | 模块显示名称 |
| `description` | `I18nObject` | 模块描述 |
| `pageTitle` | `I18nObject` | 入口菜单与入口页面标题 |

### 常量

#### `BUILTIN_DOMAIN_KEY`

- **值：** `"nexus"` — 内置控制台 Page DSL 默认 `domainKey`。

### 方法

#### `mainPageKey(String moduleKey) → String`

- **说明：** 默认入口页键 `{moduleKey}-main`。
- **示例：** `mainPageKey("menu")` → `menu-main`。

#### `pagePath(String domainKey, String moduleKey, String pageKey) → String`

- **说明：** `/{domainKey}/{moduleKey}/{pageKey}`。

#### `allPageKeys() → List<String>`

- **说明：** `entryPageKey` 在前，随后为 `additionalPageKeys`（跳过与入口重复的项）。

#### `builtin(...) → ConsoleModuleDescriptor`

- **说明：** 构建内置描述符；`domainKey=nexus`，`entryPageKey=mainPageKey(moduleKey)`，`menuKey=entryPageKey`。
- **重载：** 无 `additionalPageKeys` 或传入 `List<String> additionalPageKeys`。

---

## ConsoleModuleEntrySupport

**类型：** class

内置控制台模块 entry 插件的共享组装辅助工具。

### 方法

#### `definition(ConsoleModuleDescriptor descriptor) → PluginDefinition`

- **说明：** 为单个控制台模块构建仅贡献型 `PluginDefinition`（含 `console@1` 模块、全部 `UiSpecPageDeclaration`、单条 `MenuDeclaration.page`）。
- **参数：**
  - `descriptor` — 内置模块元数据
- **返回：** immutable 插件定义
