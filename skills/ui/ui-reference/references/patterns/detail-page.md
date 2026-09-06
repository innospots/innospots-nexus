# 详情页模式（Detail Page）

`page.type: detail` · 模板：[page-template-detail.yaml](../../ui-page/references/page-template-detail.yaml)

## 结构模型

```text
Page
├── Flex              顶栏（标题 + 返回/编辑）
└── Card
    └── Grid / Statistic   字段展示
```

## 骨架

```yaml
page:
  id: customer-detail

state:
  customerId: null

dataSources:
  detail:
    type: service
    service: customer.detail
    params:
      id: ${state.customerId}

lifecycle:
  onInit:
    - action: setState
      params:
        customerId: ${route.params.id}
    - action: reload
      params:
        dataSource: detail

body:
  type: Page
  props:
    title: 客户详情
  children:
    - type: Flex
      props:
        justify: space-between
        align: center
      children:
        - type: Text
          props:
            text: ${data.detail.name}
            strong: true
        - type: Flex
          props:
            gap: middle
          children:
            - type: Button
              props:
                text: 返回
              events:
                onClick:
                  - action: navigate
                    params:
                      to: /customers/list
            - type: Button
              props:
                text: 编辑
                variant: primary
              events:
                onClick:
                  - action: navigate
                    params:
                      to: /customers/${state.customerId}/edit

    - type: Card
      props:
        title: 基本信息
      children:
        - type: Grid
          props:
            columns: 2
            gap: 16
          children:
            - type: Statistic
              props:
                title: 名称
                value: ${data.detail.name}
            - type: Statistic
              props:
                title: 状态
                value: ${data.detail.status}

    - type: Alert
      props:
        message: 加载中…
        variant: info
      when: ${dataStatus.detail.loading}
```

## 配套

| 域 | 内容 |
|----|------|
| 路由参数 | `lifecycle.onInit` + `${route.params.id}` |
| 数据源 | `service` 单条查询 |
| 加载态 | `${dataStatus.detail.loading}` |
| 返回 | `navigate` 到列表 path |
| 编辑 | `navigate` 到编辑 path 或 `overlay.open` drawer |

## 与 Master-Detail 区别

| 模式 | 布局 | 路由 |
|------|------|------|
| 详情页 | 单栏全宽 | 独立 URL `/items/:id/detail` |
| 主从页 | 页内左右分栏 | 通常单 URL，无路由切换 |

主从见 [master-detail.md](master-detail.md)。

## 与弹层详情

只读预览可用 `overlay.open` + `modal` + 内联 `content`；完整详情页优先独立 YAML + `navigate`。
