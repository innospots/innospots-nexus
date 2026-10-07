# 包 `contribution.console`

## ConsoleContributionCatalog

**类型：** class

Console Contribution 的活动资源目录，不保存安装事实或 PluginState。

### 方法

#### `activeContributions() → synchronized List<ActiveConsoleContribution>`
- **说明：** 按 ownerPluginId 索引；与 PluginAvailability 共同决定对外可见性。 private final Map active = new LinkedHashMap<>(); /** 返回当前已通过共享 availability 门控的活动贡献。 public synchronized List activeContributions()

#### `activeContribution(String pluginId) → synchronized java.util.Optional<ActiveConsoleContribution>`
- **说明：** 返回当前插件的活动贡献；不可用或未提交时返回空。 public synchronized java.util.Optional activeContribution(String pluginId)


## ConsoleI18n

**类型：** class

Console 静态声明共用的本地化文本校验与防御性复制工具。


## ConsoleModuleDeclaration

**类型：** record

一个 Console 管理模块及其页面树、菜单树。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `moduleKey` | `String` | — |
| `displayName` | `I18nObject` | — |
| `description` | `I18nObject` | — |
| `pages` | `List<UiSpecPageDeclaration>` | — |
| `menuTree` | `List<MenuDeclaration>` | — |

### 方法

#### `resourceKey() → String`
- **说明：** 校验模块身份并复制本地化文本和子声明。 public ConsoleModuleDeclaration { if (moduleKey == null || moduleKey.length() > 128 || !KEY_PATTERN.matcher(moduleKey).matches()) { invalid("invalid moduleKey: " + moduleKey); } displayName = ConsoleI18n.copy( displayName, true, "displayName", PluginStatusCode.RESOURCE_CONFLICT); description = ConsoleI18n.copy( description, false, "description", PluginStatusCode.RESOURCE_CONFLICT); pages = pages == null ? List.of() : List.copyOf(pages); if (pages.isEmpty()) { invalid("pages must contain at least one root page"); } menuTree = menuTree == null ? List.of() : List.copyOf(menuTree); } /** 返回模块稳定资源身份。 public String resourceKey()


## ConsolePluginContribution

**类型：** record

console@1 管理模块、页面和菜单的静态资源贡献。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `PluginContributionType<>("console"` | `List<ConsoleModuleDeclaration> modules)
        implements PluginContribution {

    /** Console Contribution 的稳定类型。 */
    public static final PluginContributionType<ConsolePluginContribution> TYPE =
            new` | — |
| `(modules.isEmpty()` | `1);

    /** 防御性复制模块列表。 */
    public ConsolePluginContribution {
        modules = modules == null ? List.of() : List.copyOf(modules);
        if` | — |

### 方法

#### `type() → PluginContributionType<ConsolePluginContribution>`
- **说明：** Console Contribution 的稳定类型。 public static final PluginContributionType TYPE = new PluginContributionType<>("console", 1); /** 防御性复制模块列表。 public ConsolePluginContribution { modules = modules == null ? List.of() : List.copyOf(modules); if (modules.isEmpty()) { throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, "console contribution must declare at least one module"); } } /** 返回 console@1 类型。 public PluginContributionType type()


## ConsolePluginContributionDecoder

**类型：** class

将 YAML 的通用 map 严格解码为不可变 console@1 声明。

### 方法

#### `type() → com.innospots.nexus.core.plugin.contribution.PluginContributionType<ConsolePluginContribution>`
- **说明：** 返回 console@1 类型。 public com.innospots.nexus.core.plugin.contribution.PluginContributionType type()

#### `decode(Map<String, Object> declaration) → ConsolePluginContribution`
- **说明：** 解码模块、页面与菜单树。
- **参数：**
  - `declaration` — 已完成结构校验的 YAML 字段映射
- **返回：** 不可变 console@1 贡献实例


## ConsolePluginContributionHandler

**类型：** class

Console Contribution 的全局资源校验器和活动目录事务适配器。

### 方法

#### `type() → PluginContributionType<ConsolePluginContribution>`
- **说明：** 创建 Console Handler。
- **参数：**
  - `catalog` — 活动 Console 资源目录
  - `reservedResources` — 平台保留资源表，用于拒绝插件抢占系统身份

#### `validate(PluginCatalog pluginCatalog,
            List<PluginContributionEntry<ConsolePluginContribution>> entries) → void`
- **说明：** 对模块、页面、菜单、路径和历史归属执行无副作用全局校验。
- **参数：**
  - `pluginCatalog` — 当前有效插件目录；单插件 prepare 时可传 null
  - `entries` — 待校验的 console@1 贡献条目

#### `prepare(PluginContributionContext context,
            ConsolePluginContribution contribution) → PreparedPluginContribution`
- **说明：** 准备一个插件的 Console 资源，提交前不进入活动目录。
- **参数：**
  - `context` — 当前插件启动上下文
  - `contribution` — 待准备的贡献声明
- **返回：** 可在提交或回滚时关闭的预备资源

#### `catalog() → ConsoleContributionCatalog`
- **说明：** 返回供权限同步使用的活动资源目录。 public ConsoleContributionCatalog catalog()


## ConsolePluginContributionSnapshotter

**类型：** class

仅保存 Console 模块、页面、菜单稳定身份的安全快照器。

### 方法

#### `type() → com.innospots.nexus.core.plugin.contribution.PluginContributionType<ConsolePluginContribution>`
- **说明：** 返回 console@1 类型。 public com.innospots.nexus.core.plugin.contribution.PluginContributionType type()

#### `snapshot(ConsolePluginContribution contribution) → Map<String, Object>`
- **说明：** 生成不含 UiSpec 正文、Class、Handler 和 Secret 的稳定资源摘要。 public Map snapshot(ConsolePluginContribution contribution)


## MenuDeclaration

**类型：** record

Console 菜单树节点；目录和页面入口互斥。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `menuKey` | `String` | — |
| `title` | `I18nObject` | — |
| `icon` | `String` | — |
| `orderIndex` | `int` | — |
| `pageKey` | `String` | — |
| `children` | `List<MenuDeclaration>` | — |

### 方法

#### `directory(String menuKey, I18nObject title, String icon, int orderIndex, List<MenuDeclaration> children) → MenuDeclaration`
- **说明：** 创建目录菜单节点。 public static MenuDeclaration directory( String menuKey, I18nObject title, String icon, int orderIndex, List children )

#### `page(String menuKey, I18nObject title, String icon, int orderIndex, String pageKey) → MenuDeclaration`
- **说明：** 创建页面入口菜单节点。 public static MenuDeclaration page( String menuKey, I18nObject title, String icon, int orderIndex, String pageKey )

#### `resourceKey(String moduleKey) → String`
- **说明：** 校验菜单节点形态并复制嵌套菜单。 public MenuDeclaration { if (menuKey == null || menuKey.length() > 128 || !KEY_PATTERN.matcher(menuKey).matches()) { invalid("invalid menuKey: " + menuKey); } if (title == null || title.isEmpty()) { invalid("title is required"); } title = ConsoleI18n.copy(title, true, "title", PluginStatusCode.RESOURCE_CONFLICT); pageKey = pageKey == null || pageKey.isBlank() ? null : pageKey.trim(); if (pageKey != null && !KEY_PATTERN.matcher(pageKey).matches()) { invalid("invalid menu pageKey: " + pageKey); } if (icon != null && (icon.isBlank() || icon.length() > 128)) { invalid("icon must be 1-128 characters when present"); } children = children == null ? List.of() : List.copyOf(children); if ((pageKey == null) == children.isEmpty()) { invalid("menu node must have either pageKey or children"); } } /** 返回模块内稳定菜单资源身份。 public String resourceKey(String moduleKey)


## ReservedPluginResourceCatalog

**类型：** class

由 Core 安全快照提供的历史插件资源身份保留目录。

### 方法

#### `isReserved(String ownerPluginId, String resourceType, String resourceKey) → boolean`
- **说明：** 创建不可变的历史资源保留目录。 public ReservedPluginResourceCatalog(Collection resources) { this.resources = resources == null ? Set.of() : Set.copyOf(resources); } /** 返回某插件是否仍拥有指定的历史资源身份。 public boolean isReserved(String ownerPluginId, String resourceType, String resourceKey)

#### `ownerOf(String resourceType, String resourceKey) → Optional<String>`
- **说明：** 返回指定资源身份的历史归属插件。 public Optional ownerOf(String resourceType, String resourceKey)

#### `resources() → List<ReservedResource>`
- **说明：** 返回不可变保留资源快照。 public List resources()

#### `ReservedResource(String ownerPluginId, String resourceType, String resourceKey) → record`
- **说明：** 一项不含实现类和运行时对象的历史资源身份。 public record ReservedResource(String ownerPluginId, String resourceType, String resourceKey)


## UiSpecPageDeclaration

**类型：** record

由 PageDsl page.id 唯一对应的页面身份声明。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `pageKey` | `String` | — |
| `pagePath` | `String` | — |
| `children` | `List<UiSpecPageDeclaration>` | — |

### 方法

#### `resourceKey(String moduleKey) → String`
- **说明：** 校验页面身份、路径模板并复制子页面列表。 public UiSpecPageDeclaration { if (pageKey == null || pageKey.length() > 128 || !KEY_PATTERN.matcher(pageKey).matches()) { invalid("invalid pageKey: " + pageKey); } requireText(pagePath, "pagePath"); if (pagePath.length() > 2048 || !pagePath.startsWith("/") || pagePath.contains("?") || pagePath.contains("#")) { invalid("pagePath must be an absolute path without query or fragment"); } pagePath = normalize(pagePath); validatePath(pagePath); children = children == null ? List.of() : List.copyOf(children); } /** 返回模块内稳定页面资源身份。 public String resourceKey(String moduleKey)

#### `hasRequiredPathVariables() → boolean`
- **说明：** 返回页面是否含有不能由静态菜单提供值的路径变量。 public boolean hasRequiredPathVariables()
