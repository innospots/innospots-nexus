# Plugin 设计文档（v2）

本目录只保留 **Plugin Extensions v2** 与当前代码对齐的规范。Capability 路由、`console@1` 页面树、
`plugin-dsl-v1` 等 v1 材料已移除，不再作为实施依据。

## 规范索引

| 文档 | 责任 |
|------|------|
| [plugin-extensions-v2-design.md](plugin-extensions-v2-design.md) | **主规范**：console@2、catalog/权限资源模型、目标 `extensions[]` / function / subscribe、迁移与测试 |
| [plugin-runtime-design.md](plugin-runtime-design.md) | 发现、定义编译、Contribution 生命周期、运行时与并发（**无 Capability**） |
| [plugin-dsl-spec.md](plugin-dsl-spec.md) | YAML 交换格式：**当前** `nexus.plugin/v1` + `contributions`；**目标** `apiVersion: 2` + `extensions` |
| [plugin-installation-design.md](plugin-installation-design.md) | 安装表、对账、启停意图与 `PluginInstallationManager` |

## 关联（仓库外）

- Console Catalog/Navigation 总原则：[2026-09-04-console-catalog-and-navigation-design.md](../../../../docs/superpowers/specs/2026-09-04-console-catalog-and-navigation-design.md)（资源类型与 sync 输入以 v2 主规范为准）

## 实现状态（摘要）

| 能力 | 状态 |
|------|------|
| 删除 Capability / Dependency 运行时 | **已落地** |
| `console@2`（`ConsoleContributeNode`、`modules[]` + `children[]`） | **已落地** |
| Catalog 仅 MODULE/MENU、`permission-catalog` 组装 | **已落地**（console 模块） |
| Manifest `apiVersion: 2`、`extensions[]` | **未落地**（仍用 v1 文档头 + `spec.contributions`） |
| `provide` / `subscribe` / `FunctionRegistry` | **未落地** |
| `plugin-dsl-v2.schema.json` | **未落地** |

操作手册见 [`../manual/README.md`](../manual/README.md)，已与当前 `console@2` 和配置实现同步。
