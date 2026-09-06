# 列表页模式（List Page）

`page.type: list` · 模板：[page-template-list.yaml](../../ui-page/references/page-template-list.yaml)（整页跳转）、[page-template-list-drawer.yaml](../../ui-page/references/page-template-list-drawer.yaml)（抽屉编辑）

## 结构模型

```text
Page
├── Card              搜索 / 筛选
├── Flex              工具栏（标题 + 操作）
│   ├── Title
│   └── Flex          按钮组
└── Table             主内容
```

## 标准 body 骨架

```yaml
body:
  type: Page
  props:
    title: 客户管理
  children:
    - type: Card
      children:
        - component: searchForm

    - type: Flex
      props:
        justify: space-between
        align: center
      children:
        - type: Text
          props:
            text: 客户列表
        - type: Flex
          props:
            gap: middle
          children:
            - type: Button
              props:
                text: 导出
            - type: Button
              props:
                text: 新建客户
                variant: primary

    - type: Table
      props:
        dataSource: ${data.customers}
        rowKey: id
```

## 配套配置

| 域 | 典型内容 |
|----|----------|
| `state` | `keyword`, `page`, `pageSize`, `selectedId` |
| `dataSources` | `static` 选项 + `service` 列表（`autoLoad`） |
| `actions` | `search`, `resetSearch`, 行操作 `deleteRow`, `openEditDrawer` |
| `components` | `searchForm` |
| `lifecycle` | `onLoad` → reload（若未 autoLoad） |

行内编辑常用 `overlay.open` + `drawer` → [overlay.md](../behavior/overlay.md) + [scenarios.md](../../ui-page/references/scenarios.md)。  
跳转详情页用 `navigate` → [navigation.md](../behavior/navigation.md)。

## 布局选用

| 区域 | 组件 |
|------|------|
| 根 | `Page` |
| 搜索区 | `Card` |
| 工具栏 | `Flex` |
| 表格 | `Table`（主内容，不包多余 Card 除非需标题） |

详见 [structure/layout.md](../structure/layout.md)。

## 设计顺序

```text
1. 定列表数据源（service）
2. 定搜索 state 字段
3. 设计 Page → Card(搜索) → Flex(工具栏) → Table
4. 补 actions / components
```
