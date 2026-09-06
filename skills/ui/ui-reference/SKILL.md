---
name: ui:reference
display_name: UI 规范与 Page DSL 参考
description: |
  Pactor Page DSL 1.0 规范总索引。供 ui:page、ui:check 依赖，以及查询 schema、
  节点结构、页面布局（Page/Grid/Flex/Layout）、页面模式（list/form/dashboard）时使用。
  触发词：Page DSL 规范、布局、layout、Grid、Flex、list-page、schema、ui:reference。
category: ui
version: 1.2.0
---

# UI 规范与 Page DSL 参考

## 定位

**只读规范库**，供 `ui:page` / `ui:check` 与用户查阅。
不写 YAML → `ui:page`；不跑验证 → `ui:check`。

## 规范版本

- DSL：`dsl: '1.0'`
- 实现：`innospots-nexus-base` → `com.innospots.nexus.base.ui.spec`
- Schema 权威：[references/pactor-page-dsl.schema.yaml](references/pactor-page-dsl.schema.yaml)（本目录内唯一源）
- Schema 说明：[schema.md](references/schema.md)
- 脚本校验：`ui:check/scripts/validate.sh`

## 参考目录

```text
references/
├── dsl-overview.md          总览与设计层次
├── naming.md                文件与命名
├── pactor-page-dsl.schema.yaml
├── schema.md                ★ JSON Schema 说明与校验范围
├── implementation-map.md
│
├── structure/               文档结构与 UI 树
│   ├── page.md              顶层字段、body/children
│   ├── node.md              节点形状
│   ├── component.md         components 复用
│   ├── component-catalog.md ★ 内置组件 props 目录
│   ├── layout.md            ★ 布局能力与六条规则
│   └── layout-components.md ★ Layout/Header/Sider/Content/Footer 专篇
│
├── behavior/                数据与行为
│   ├── state.md
│   ├── datasource.md
│   ├── action.md
│   ├── overlay.md           ★ modal / drawer 弹层
│   ├── navigation.md        ★ 页面跳转与菜单加载
│   ├── lifecycle.md
│   └── expression.md
│
├── security/
│   └── permission.md
│
└── patterns/                ★ 页面结构模式（组合布局）
    ├── list-page.md
    ├── detail-page.md
    ├── form-page.md
    ├── dashboard-page.md
    ├── master-detail.md
    └── workspace-page.md
```

## 两个概念分开读

| 问题 | 读哪里 |
|------|--------|
| 顶层有哪些字段？`body` 从哪开始？ | [structure/page.md](references/structure/page.md) |
| Page / Grid / Flex 怎么选？ | [structure/layout.md](references/structure/layout.md) |
| Layout / Sider / Content 是什么？ | [structure/layout-components.md](references/structure/layout-components.md) |
| Button / Table / Form 有哪些 props？ | [structure/component-catalog.md](references/structure/component-catalog.md) |
| 列表页怎么搭？ | [patterns/list-page.md](references/patterns/list-page.md) |

```text
structure/layout.md  → 布局能力与规则
patterns/*.md        → 常见页面如何组合
```

## 设计层次

```text
Application Shell（Bootstrap，不属于 PageDsl）
        ↓
Page（body 根）
  → 选 Page Pattern（patterns/）
  → 选 Layout Primitive（structure/layout.md）
  → 组件树（structure/node.md + component.md）
  → state / dataSources / actions（behavior/）
```

## 快速路由

| 场景 | 文档 |
|------|------|
| 布局、Grid/Flex、双侧边栏 | [layout.md](references/structure/layout.md) |
| Layout 族职责与组合 | [layout-components.md](references/structure/layout-components.md) |
| 组件 props / events | [component-catalog.md](references/structure/component-catalog.md) |
| 列表/表单/看板 | [patterns/](references/patterns/) |
| 数据源 | [datasource.md](references/behavior/datasource.md) |
| 弹层 drawer/modal | [overlay.md](references/behavior/overlay.md) |
| 页面跳转 / 菜单加载 | [navigation.md](references/behavior/navigation.md) |
| 文件路径 | [naming.md](references/naming.md) |

## 模板与示例

场景索引与 YAML 模板 → `ui:page` → [scenarios.md](../../ui-page/references/scenarios.md)。

覆盖度审查 → [COVERAGE.md](../../COVERAGE.md)。

## 技能路由

```text
写 YAML     → ui:page
验证        → ui:check
改 Java     → java:develop + java:test
```
