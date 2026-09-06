# 看板页模式（Dashboard Page）

`page.type: dashboard`

## 结构模型

```text
Page
└── Grid（指标区）
    ├── Card → Statistic
    ├── Card → Statistic
    └── ...
└── Grid / Card（图表区，可选第二段）
```

## 骨架

```yaml
body:
  type: Page
  props:
    title: 经营看板
  children:
    - type: Grid
      props:
        columns: 4
        gap: 16
      children:
        - type: Card
          children:
            - type: Statistic
              props:
                title: 客户数
                value: ${data.summary.customers}
        - type: Card
          children:
            - type: Statistic
              props:
                title: 今日新增
                value: ${data.summary.today}
        - type: Card
          children:
            - type: Statistic
              props:
                title: 活跃客户
                value: ${data.summary.active}
        - type: Card
          children:
            - type: Statistic
              props:
                title: 转化率
                value: ${data.summary.rate}
                suffix: '%'
```

## 布局规则

| 场景 | 选用 |
|------|------|
| 统计卡片横排 | **Grid** + Card |
| 图表多列 | Grid（均分列） |
| 区块标题+操作 | Card 内 Flex |

**不要**用 Flex 手工算宽度排列指标卡；用 Grid `columns`。

## 数据

- `dataSources.summary`：聚合指标（service）
- 各 Statistic 绑定 `${data.summary.xxx}`
