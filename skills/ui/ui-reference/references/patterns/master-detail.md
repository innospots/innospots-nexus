# 主从页模式（Master-Detail）

左右分栏：列表 + 详情/编辑。

## 结构模型

```text
Page
└── Grid 或 Flex
    ├── Left（列表）
    └── Right（详情）
```

## Grid 方案（等宽双栏）

适用：左右**等宽**。

```yaml
body:
  type: Page
  props:
    title: 客户
  children:
    - type: Grid
      props:
        columns: 2
        gap: 16
      children:
        - type: Card
          props:
            title: 客户列表
          children:
            - component: customerList
        - type: Card
          props:
            title: 客户详情
          children:
            - component: customerDetail
```

## Flex 方案（非均分，1.0 推荐）

当前 `Grid.columns` 为**均分列**，不支持 `320px | 1fr`。主从不等宽时用 Flex：

```yaml
- type: Flex
  props:
    gap: 16
  children:
    - type: Card
      props:
        title: 客户列表
        style:
          width: 320
      children:
        - component: customerList
    - type: Card
      props:
        title: 客户详情
        style:
          flex: 1
      children:
        - component: customerDetail
```

> `style` 此处为 **1.0 逃生口**；未来栅格扩展后优先用语义 Props。见 [structure/layout.md](../structure/layout.md)。

## 状态

- `state.selectedId`：右侧详情数据源依赖
- 列表行点击 → `setState` + reload 详情 dataSource

## 与 Application Shell

主从是**页面内**布局，不要在 admin Shell 外再套 `Layout + Sider`。
