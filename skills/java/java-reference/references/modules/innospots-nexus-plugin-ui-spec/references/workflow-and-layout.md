# 文件布局与使用方式

## 资源路径约定

默认 `PageDslConfig.defaults()`：

| 项 | 值 |
|----|-----|
| 基础目录 | `ui-pages`（classpath 根相对） |
| 文件后缀 | `.yaml` |
| 路径模式 | `ui-pages/{moduleKey}/{pageKey}.yaml` |

示例：

```text
src/main/resources/ui-pages/sales/order-list.yaml
```

`moduleKey` 与 `console@1` 贡献中 `ConsoleModuleDeclaration.moduleKey` 对齐。
`pageKey` 与 YAML 内 `page.id` 一致（通常同为 kebab-case 文件名）。

## 在插件工程中新增页面

1. 在 `plugin.yaml`（或 Java `PluginDefinition`）的 `console@1` 贡献中声明模块与 `pageKey` / `pagePath`。
2. 在 `src/main/resources/ui-pages/<moduleKey>/<pageKey>.yaml` 编写 Page DSL。
3. 本地解析校验（见下）。
4. 安装/启用插件后，由 console `ConsoleCatalogSyncService` 同步目录索引。

内置控制台 entry 插件示例目录：

`innospots-nexus-console/src/main/resources/ui-pages/{role,menu,dictionary,...}/`

## 解析与校验（Java）

```java
PageDslConfig config = PageDslConfig.defaults();
PageDslParser parser = new JacksonPageDslParser(config);
String yaml = /* classpath 读取 */;
PageDsl document = parser.parse(yaml);
```

流程：

1. Jackson YAML → `PageDsl`（未知字段按 config 失败）。
2. `PageDslValidator.validate(document)`。

加载接口：

```java
PageDslLoader loader = /* 宿主实现 */;
PageDsl page = loader.load(moduleKey, pageKey);
```

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
| `innospots-nexus-plugin/src/test/resources/ui-pages/demo/customer-list.yaml` | 全功能示例 |
| `innospots-nexus-plugin/src/test/resources/ui-pages/sales/order-list.yaml` | 最小示例 |

运行模块测试：

```bash
mvn -pl innospots-nexus-plugin -am test
```

## 常见错误

| 现象 | 原因 |
|------|------|
| `PageDsl dsl version must be 1.0` | `dsl` 缺失或版本不对 |
| `Unknown data source referenced by action` | `reload` 等引用了未定义的 `dataSources` 键 |
| `Unknown component reference` | `component: foo` 但 `components` 无 `foo` |
| Jackson 未知属性 | 拼写错误字段；默认严格模式 |
