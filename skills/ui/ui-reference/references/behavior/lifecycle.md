# Lifecycle（生命周期）

## 钩子

| 钩子 | 典型用途 |
|------|----------|
| `onInit` | 初始化 state、`call` 预加载配置 |
| `onLoad` | 首屏 `reload` 关键 dataSource |
| `onReady` | 就绪后提示、埋点 |
| `onShow` / `onHide` | 页签/抽屉显隐 |
| `onDestroy` | 清理临时 state |

## 写法

与 `actions` 相同，支持单对象或数组（ActionOrList）：

```yaml
lifecycle:
  onInit:
    - action: setState
      params:
        ready: true
  onLoad:
    - action: reload
      params:
        dataSource: customers
  onReady: []
```

## 与布局

生命周期配置**行为**，不定义页面结构。首屏数据加载写在 `onLoad`；UI 骨架写在 `body`。

## 注意

- 若 dataSource 已 `autoLoad: true`，避免 `onLoad` 重复 reload
- 空数组 `onLoad: []` 合法
