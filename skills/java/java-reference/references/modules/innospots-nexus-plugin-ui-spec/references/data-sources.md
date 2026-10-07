# 数据源（`dataSources`）

`dataSources` 是 **命名 map**：键在动作 `reload` 等参数中引用；在表达式中以 `${data.<key>}`、
`${dataStatus.<key>.loading}` 等形式访问（由前端/宿主运行时解释）。

## 公共字段（`DataSourceConfig`）

| 字段 | 说明 |
|------|------|
| `type` | 鉴别器，见下表 |
| `autoLoad` | 是否在页面加载时自动拉取 |
| `pagination` | 可选分页绑定（`PaginationConfig`） |
| `valueField` / `labelField` / `disabledField` | 选项类数据规范化字段名 |

## `type` 种类

| type | Java 类型 | 必填内容 | 说明 |
|------|-----------|----------|------|
| `static` | `StaticDataSource` | `value` | 编译期静态 JSON/YAML 值 |
| `service` | `ServiceDataSource` | `service` | 服务注册表 ID；**业务页首选** |
| `http` | `HttpDataSource` | `request.url`（及 `method`） | 直接 HTTP；校验支持 GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS |
| `computed` | `ComputedDataSource` | （实现见 Java 模型） | 派生/计算数据 |
| `resource` | `ResourceDataSource` | （实现见 Java 模型） | 资源型数据 |

### `static`

```yaml
statusOptions:
  type: static
  value:
    - label: 全部
      value: ''
```

### `service`

```yaml
customers:
  type: service
  service: customer.list
  autoLoad: true
  params:
    keyword: ${state.keyword}
    page: ${state.page}
```

- `params` 值可为表达式字符串；由宿主在请求前求值。
- `service` 必须在宿主服务注册表中存在（**校验器不检查**）。

### `http`

```yaml
customerDetail:
  type: http
  request:
    method: GET
    url: /api/customers/${state.selectedId}
  autoLoad: false
```

`request` 形状对应 `HttpRequest`（`method`、`url`、headers/body 等见 Java 模型）。

## 校验规则（`PageDslValidator`）

- 每个条目必须有非空键与非空定义。
- `static` 必须有 `value`。
- `service` 必须有非空 `service`。
- `http` 必须有合法 `request`（url 非空，method 在允许集合内）。
- 动作 `params.dataSource` 引用的名称必须存在于 `dataSources` map。

## 设计建议

| 场景 | 推荐 |
|------|------|
| 控制台业务 API | `type: service`，避免在 YAML 中硬编码 REST 路径 |
| 集成外部只读 HTTP | `type: http` |
| 固定下拉选项 | `type: static` |
