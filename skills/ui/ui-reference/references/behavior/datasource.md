# 数据源类型（dataSources）

所有数据源在 `dataSources` 下命名，表达式中通过 `${data.<name>}` 引用。

## 公共字段（static / service / http）

| 字段 | 说明 |
|------|------|
| `type` | 判别字段 |
| `autoLoad` | 是否自动加载 |
| `pagination` | `{ page, pageSize, totalField, dataField }` |
| `valueField` / `labelField` / `disabledField` | 选项类字段映射 |

## static

固定数据、枚举、演示数据。

```yaml
statusOptions:
  type: static
  value:
    - label: 正常
      value: active
```

- **必填**：`value`

## service（推荐）

通过服务注册表加载业务数据，DSL 不绑定 HTTP。

```yaml
customers:
  type: service
  service: customer.list
  autoLoad: true
  params:
    keyword: ${state.keyword}
    page: ${state.page}
```

- **必填**：`service`

## http

直连 HTTP 端点。

```yaml
customerDetail:
  type: http
  request:
    method: GET
    url: /api/customers/${state.selectedId}
    params: {}
    headers: {}
    timeout: 5000
```

- **必填**：`request.url`

## computed

派生数据（协议保留，运行时语义由实现定义）。

```yaml
filtered:
  type: computed
  expression: ${data.customers}
  dependsOn:
    - customers
```

## resource

平台资源注册表（协议保留）。

```yaml
roleOptions:
  type: resource
  resource: roles
  params:
    tenantId: ${state.tenantId}
```

## 选型建议

| 场景 | 推荐 type |
|------|-----------|
| 下拉固定选项 | `static` |
| 列表/详情/提交 | `service` |
| 遗留 API 直连 | `http` |
| 前端派生 | `computed` |
| 平台内置资源 | `resource` |
