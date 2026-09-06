# Java 实现与测试索引

## 包结构

```text
com.innospots.nexus.base.ui.spec
├── PageDsl, PageMeta, LifecycleConfig, RequiresConfig, HttpRequest, PaginationConfig
├── action/          ActionConfig, ActionOrList
├── datasource/      DataSourceConfig 及五类实现
├── node/            ComponentNode, ComponentReferenceNode, DslSourceRef, Children, ...
├── permission/      PermissionConfig, PermissionDenied
├── jackson/         自定义反序列化器
├── parser/          JacksonPageDslParser
├── loader/          ClasspathPageDslLoader
├── validation/      PageDslValidator
├── filter/          PageDslFilterChain, StateBindingPageDslFilter
└── endpoint/        PageDslEndpoint
```

## 反序列化器一览

| 类 | 用途 |
|----|------|
| `ActionOrListDeserializer` | 单 action / 数组 |
| `ActionOrListMapDeserializer` | `actions` map |
| `ChildrenDeserializer` | children 数组 / source / 单节点 |
| `DslNodeDeserializer` | `body`、placeholder |
| `DslRenderableDeserializer` | 单个 renderable |
| `DslRenderableMapDeserializer` | `components` map |
| `EventMapDeserializer` | 组件 `events` |
| `ExpressionOrBooleanDeserializer` | condition / when |
| `PermissionConfigDeserializer` | permission union |

## 测试类（按包）

| 包 | 测试 |
|----|------|
| `ui` | `PageDslContractsTest`, `PageDslYamlScenariosTest` |
| `ui.spec` | `PageDslSpecificationContractsTest` |
| `ui.spec.jackson` | 每个 Deserializer 独立 `*Test` |
| `ui.spec.action` | `ActionConfigDeserializationTest`, `ActionOrListDeserializationTest` |
| `ui.spec.datasource` | 五类 `*DataSourceDeserializationTest` |
| `ui.spec.node` | 各节点 `*DeserializationTest` |
| `ui.spec.permission` | `PermissionConfigDeserializerTest` |
| `ui.spec.validation` | `PageDslValidatorTest` |
| `ui.spec.filter` | `PageDslFilterContractsTest` |

## 验证命令

```bash
mvn test -pl innospots-nexus-base -Dtest='**/ui/spec/**/*Test,**/ui/PageDsl*Test'
```

## Schema

权威定义（`ui:reference` 内唯一源）：

```text
skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml
```

说明：[schema.md](../../../skills/ui/ui-reference/references/schema.md)。校验：`ui-check/scripts/validate.sh`。

## 下游集成

| 模块 | 类 |
|------|-----|
| `innospots-nexus-core` | `ConsoleCatalogSyncService` |
| `innospots-nexus-spring` | `ConsoleCatalogConfiguration` |
| `innospots-nexus-quarkus` | `ConsoleCatalogBeans` |
