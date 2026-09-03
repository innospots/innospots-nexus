# UiSpec 页面规格说明

UiSpec 是管理平台前端渲染消费的**页面规格契约**。规格以 YAML 描述，由后端 `JacksonUiSpecParser` 解析为 `com.innospots.nexus.base.ui.spec.UiSpec` 对象，再序列化为 JSON 下发给前端。

本目录提供三份配套文件：

| 文件 | 用途 |
|------|------|
| [ui-spec.md](./ui-spec.md) | 规格说明（本文档） |
| [ui-spec.template.yaml](./ui-spec.template.yaml) | 标准 YAML 模板，覆盖全部顶层与常用嵌套结构 |
| [ui-spec-schema.json](./ui-spec-schema.json) | JSON Schema，用于编辑器校验与自动化生成 |

## 设计原则

- **框架中立**：规格不绑定具体前端框架，只描述页面结构、数据源、动作与展示元数据。
- **声明式**：页面由组件、布局、数据源与动作组合而成，运行时通过变量与表达式控制可见性。
- **严格解析**：默认开启 `failOnUnknownProperties`，未知字段会导致解析失败。
- **跨引用校验**：`actionDefinitions`、`components` 的 Map 键必须与内部 `actionId` / `componentId` 一致；`datasourceKey` 与组件 `datasource` 必须引用已声明的数据源。

## 文件位置与命名

默认 classpath 路径（`UiSpecConfig.defaults()`）：

```
ui-spec/{moduleKey}/{pageKey}.yaml
```

- `moduleKey`：模块标识，如 `sales`
- `pageKey`：页面标识，须与 `pageInfo.pageId` 一致
- 后缀支持 `.yaml` 或 `.yml`

示例：`ui-spec/sales/order-list.yaml` 对应 `pageInfo.pageId: order-list`。

## 顶层结构

`UiSpec` 根对象字段如下：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageInfo` | `PageInfo` | 是 | 页面身份与展示元数据 |
| `pageType` | `string` | 否 | 页面类型，默认 `general`；常见值如 `table`、`dashboard` |
| `meta` | `object` | 否 | 页面级扩展元数据 |
| `variables` | `map<string, Variable>` | 否 | 页面变量，供条件表达式与请求模板使用 |
| `datasources` | `map<string, UiDatasource>` | 否 | 命名数据源集合 |
| `components` | `map<string, UiComponentSpec>` | 否 | 命名组件集合，键须等于 `componentId` |
| `layout` | `UiLayout` | 否 | 页面根布局 |
| `actionDefinitions` | `map<string, UiAction>` | 否 | 可复用动作定义，键须等于 `actionId` |
| `aiActions` | `map<string, UiAction>` | 否 | AI 动作定义，结构同 `actionDefinitions` |
| `optionSources` | `map<string, SelectOptions>` | 否 | 可复用下拉选项源 |

## pageInfo

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageId` | `string` | 是 | 页面唯一标识，须与资源 `pageKey` 一致 |
| `title` | `I18nObject` | 否 | 页面标题 |
| `name` | `I18nObject` | 否 | 页面名称，默认同 `title` |
| `description` | `I18nObject` | 否 | 页面描述 |

## I18nObject

国际化字符串，YAML 中为 locale → value 映射：

```yaml
title:
  en: Orders
  zh: 订单列表
```

解析时按当前语言回退：精确 locale → 语言代码 → 同语系变体 → 首个可用值。

## variables

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `string` | 变量名 |
| `type` | `string` | 类型声明，如 `Boolean`、`String` |
| `defaultValue` | `any` | 默认值 |
| `required` | `boolean` | 是否必填 |

运行时可通过 `UiSpec.addVariableValues()` 注入值；`filterActionDefinitions()` 会依据 `visibleIf.expression` 过滤动作。

## datasources

命名 HTTP 数据源，**不包含鉴权配置**（鉴权由平台运行时处理）。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `method` | `string` | 是 | `GET` / `POST` / `PUT` / `PATCH` / `DELETE` |
| `url` | `string` | 是 | 请求 URL，可含 `${variable}` 模板 |
| `params` | `object` | 否 | 查询参数模板 |
| `headers` | `object` | 否 | 请求头模板 |
| `body` | `object` | 否 | 请求体模板 |

```yaml
datasources:
  orders:
    method: GET
    url: /api/orders
    params:
      groupIds: ${groupIds}
```

## components

每个组件由 `componentId` 唯一标识，Map 键必须与其一致。

| 字段 | 类型 | 说明 |
|------|------|------|
| `componentId` | `string` | 组件 ID |
| `type` | `string` | 组件类型，见下表 |
| `title` | `I18nObject` | 标题 |
| `description` | `I18nObject` | 描述 |
| `datasource` | `string` | 引用 `datasources` 中的键 |
| `style` | `object` | 样式键值对 |
| `height` | `any` | 高度 |
| `width` | `string` | 宽度 |
| `visibleIf` | `VisibleCondition` | 可见条件 |
| `icon` | `string` | 图标 |
| `table` | `UiTable` | 表格配置（`type: table` 时使用） |
| `formFields` | `FormField[]` | 表单字段（`type: form` 时使用） |
| `layout` | `UiLayout` | 嵌套布局（`type: layout` 时使用） |
| `actions` | `UiAction[]` | 组件内联动作 |
| `properties` | `object` | 扩展属性 |

### 组件类型（ComponentType）

| 值 | 说明 |
|----|------|
| `alert-card` | 告警卡片 |
| `banner-card` | 横幅卡片 |
| `chart` | 图表 |
| `form` | 表单 |
| `gauge-card` | 仪表盘卡片 |
| `info-card` | 信息卡片 |
| `layout` | 布局容器 |
| `metric-card` | 指标卡片 |
| `page` | 页面容器 |
| `plain-text` | 纯文本 |
| `table` | 表格 |
| `text-card` | 文本卡片 |

## layout

页面或组件内的布局描述。

| 字段 | 类型 | 说明 |
|------|------|------|
| `layoutId` | `string` | 布局 ID |
| `type` | `string` | 布局类型，见下表 |
| `gap` | `integer` | 间距 |
| `components` | `ComponentRef[]` | 子组件引用 |
| `properties` | `object` | 扩展属性 |

### 布局类型（LayoutType）

| 值 | 说明 |
|----|------|
| `grid` | 栅格布局 |
| `asider` | 侧边栏布局 |
| `three-column` | 三栏布局 |
| `tab` | 标签页布局 |

### ComponentRef

| 字段 | 类型 | 说明 |
|------|------|------|
| `componentId` | `string` | 引用 `components` 中的键 |
| `span` | `integer` | 栅格占位（如 12 列制中的跨度） |
| `area` | `string` | 布局区域标识（如 `left` / `right`） |
| `properties` | `object` | 扩展属性 |

## table（UiTable）

| 字段 | 类型 | 说明 |
|------|------|------|
| `pagination` | `boolean` | 是否分页 |
| `pageSize` | `integer` | 每页条数 |
| `displayMode` | `string` | 展示模式 |
| `columns` | `TableColumn[]` | 列定义 |
| `rowAction` | `UiAction` | 行级动作（如行内下拉菜单） |

### TableColumn

| 字段 | 类型 | 说明 |
|------|------|------|
| `title` | `string` | 列标题（简写） |
| `label` | `I18nObject` | 列标题（国际化） |
| `field` | `string` | 数据字段名 |
| `valueType` | `string` | 值类型，如 `String`、`Number`、`Date` |
| `sortable` | `boolean` | 是否可排序 |
| `copyable` | `boolean` | 是否可复制 |
| `width` | `integer` | 列宽 |
| `action` | `UiAction` | 列内动作 |
| `properties` | `object` | 扩展属性 |

## formFields（FormField）

| 字段 | 类型 | 说明 |
|------|------|------|
| `field` | `string` | 字段键 |
| `name` | `string` | 字段名 |
| `label` | `I18nObject` | 标签 |
| `type` | `string` | 控件类型，如 `input`、`select`、`date` |
| `required` | `boolean` | 是否必填 |
| `placeholder` | `I18nObject` | 占位符 |
| `options` | `OptionItem[]` | 静态选项 |
| `datasource` | `string` | 动态选项数据源 |
| `valueField` | `string` | 选项值字段 |
| `labelField` | `string` | 选项标签字段 |
| `validation` | `FormFieldValidation` | 校验规则 |
| `maxLength` | `integer` | 最大长度 |
| `span` | `integer` | 表单栅格占位 |
| `defaultValue` | `any` | 默认值 |
| `hidden` | `boolean` | 是否隐藏 |
| `visibleIf` | `VisibleCondition` | 可见条件 |
| `multiple` | `boolean` | 是否多选 |
| `mode` | `string` | 控件模式 |
| `optionSource` | `string` | 引用 `optionSources` 中的键 |
| `selectionType` | `string` | 选择类型 |
| `min` / `max` | `integer` | 数值范围 |
| `format` | `string` | 格式 |
| `layout` | `string` | 字段布局 |
| `readonly` | `boolean` | 只读 |
| `copyable` | `boolean` | 可复制 |
| `style` | `object` | 样式 |

### FormFieldValidation

| 字段 | 类型 | 说明 |
|------|------|------|
| `required` | `boolean` | 必填 |
| `minLength` / `maxLength` | `integer` | 长度范围 |
| `pattern` | `string` | 正则 |
| `message` | `I18nObject` | 校验失败提示 |

### OptionItem

| 字段 | 类型 | 说明 |
|------|------|------|
| `value` | `any` | 选项值 |
| `label` | `I18nObject` | 选项标签 |
| `icon` | `string` | 图标 |
| `disabled` | `boolean` | 是否禁用 |
| `metadata` | `object` | 扩展元数据 |

## optionSources（SelectOptions）

可复用的下拉选项定义，供 `FormField.optionSource` 引用。

| 字段 | 类型 | 说明 |
|------|------|------|
| `items` | `OptionItem[]` | 静态选项列表 |
| `datasource` | `ApiRequest` | 动态加载请求 |
| `valueField` | `string` | 值字段 |
| `labelField` | `string` | 标签字段 |

## actionDefinitions / aiActions

动作定义 Map，键须等于 `actionId`。`aiActions` 专用于 AI 类交互，结构与 `actionDefinitions` 相同。

| 字段 | 类型 | 说明 |
|------|------|------|
| `actionId` | `string` | 动作 ID |
| `actionType` | `string` | 动作类型，见下表 |
| `label` | `I18nObject` | 按钮/菜单文案 |
| `icon` | `string` | 图标 |
| `scope` | `string` | 作用域，如 `page`、`row`、`batch` |
| `target` | `string` | 目标组件或页面 |
| `tooltip` | `I18nObject` | 提示文案 |
| `visibleIf` | `VisibleCondition` | 可见条件 |
| `style` | `ActionStyle` | 按钮样式 |
| `datasourceKey` | `string` | 引用 `datasources` 中的键 |
| `request` | `ApiRequest` | 独立 API 请求（与 `datasourceKey` 二选一或组合使用） |
| `confirm` | `ActionConfirm` | 确认对话框 |
| `feedback` | `ActionFeedback` | 执行结果反馈 |
| `children` | `UiAction[]` | 子动作（用于 `dropdown` 等） |
| `properties` | `object` | 扩展属性 |

### 动作类型（ActionType）

| 值 | 说明 |
|----|------|
| `refresh` | 刷新数据源 |
| `api` | 调用 API |
| `download` | 下载 |
| `link` | 跳转链接 |
| `component` | 组件交互 |
| `modal` | 打开弹窗 |
| `form` | 表单提交 |
| `dropdown` | 下拉菜单 |
| `toggle` | 切换状态 |
| `status` | 状态变更 |
| `page` | 页面导航 |
| `pop` | 弹出层 |
| `closeModal` | 关闭弹窗 |
| `ai` | AI 动作 |
| `import` | 导入 |

### ActionStyle

| 字段 | 类型 | 说明 |
|------|------|------|
| `variant` | `string` | 变体，如 `primary`、`ghost` |
| `color` | `string` | 颜色 |
| `size` | `string` | 尺寸 |
| `properties` | `object` | 扩展样式 |

### ActionConfirm

| 字段 | 类型 |
|------|------|
| `title` | `I18nObject` |
| `message` | `I18nObject` |

### ActionFeedback / FeedbackItem

```yaml
feedback:
  items:
    - status: success
      message:
        en: Saved successfully
        zh: 保存成功
```

## ApiRequest

动作用独立请求时使用（字段名与 `UiDatasource` 略有不同：`uri` 而非 `url`）。

| 字段 | 类型 | 说明 |
|------|------|------|
| `uri` | `string` | 请求 URI |
| `method` | `string` | HTTP 方法 |
| `params` | `object` | 查询参数 |
| `headers` | `object` | 请求头 |
| `body` | `object` | 请求体 |

## VisibleCondition

| 字段 | 类型 | 说明 |
|------|------|------|
| `expression` | `string` | 条件表达式，支持 `${variable}` 占位 |
| `context` | `object` | 附加上下文 |

表达式在运行时会将 `${name}` 规范化为 `name` 再求值。示例：

```yaml
visibleIf:
  expression: ${canDelete}
```

## 模板变量

在 `url`、`params`、`headers`、`body` 与 `visibleIf.expression` 中可使用 `${variableName}` 引用 `variables` 或运行时注入值。

## 解析与校验

```java
UiSpecConfig config = UiSpecConfig.defaults();
JacksonUiSpecParser parser = new JacksonUiSpecParser(config);
UiSpec spec = parser.parse(yamlContent);
```

`UiSpecValidator` 在解析后校验：

1. `pageInfo.pageId` 非空
2. 每个 datasource 的 `method`、`url` 合法
3. `actionDefinitions` / `aiActions` 的键与 `actionId` 一致，`datasourceKey` 可解析
4. `components` 的键与 `componentId` 一致，`datasource` 可解析
5. `ApiRequest` 的 `method`、`uri` 合法

从 classpath 加载：

```java
ClasspathUiSpecLoader loader = new ClasspathUiSpecLoader(config, parser, classLoader);
UiSpec spec = loader.load("sales", "order-list");
```

## 最小示例

```yaml
pageInfo:
  pageId: order-list
  title:
    en: Orders
pageType: table
datasources:
  orders:
    method: GET
    url: /api/orders
actionDefinitions:
  refresh:
    actionId: refresh
    actionType: refresh
    datasourceKey: orders
```

## 相关源码

| 类 | 路径 |
|----|------|
| `UiSpec` | `com.innospots.nexus.base.ui.spec.UiSpec` |
| `JacksonUiSpecParser` | `com.innospots.nexus.base.ui.spec.parser.JacksonUiSpecParser` |
| `UiSpecValidator` | `com.innospots.nexus.base.ui.spec.validation.UiSpecValidator` |
| `ClasspathUiSpecLoader` | `com.innospots.nexus.base.ui.spec.loader.ClasspathUiSpecLoader` |
| 测试样例 | `src/test/resources/ui-spec/sales/order-list.yaml` |
