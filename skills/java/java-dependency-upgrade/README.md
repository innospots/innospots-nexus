# java:dependency-upgrade — 依赖升级

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:dependency-upgrade` 负责 **外部技术栈升级**：JDK、Spring / Jakarta、Maven 与构建插件、BOM 中的第三方库（Jackson、JUnit、Lombok、commons-* 等）及内部 SDK，并分析对业务代码的影响范围。

**不是** innospots-nexus 自身 `${revision}` 升版（那是 `java:project-upgrade`）。

## 怎么用

1. **建立基线** — `java:check`：`compile`、`test`、`dependency:tree`。
2. **定目标版本** — 支持周期、CVE、与当前栈兼容（Spring 细则见 `java:spring`）。
3. **改版本位置** — 主要在 `innospots-nexus-bom` 与 `innospots-nexus-parent`；模块 POM **不写** `<version>`。
4. **全仓扫描** — 源码、测试、配置、SPI 中的 API 使用点。
5. **分批改代码** — 小批量 `compile` + `test`；行为敏感处补测试。
6. **JDK / Spring 跨代** — 改代码前 `grill-me`。
7. 交 `java:check`，对比升级前后依赖树。

## 输入

| 类型 | 示例 |
|------|------|
| 升级目标 | JDK 25、Boot 3→4、Jackson 大版本 |
| 动机 | CVE、EOL、新语言特性 |
| 基线 | 升级前 tree、失败测试列表 |
| 范围 | 全仓或指定模块 |

## 输出

| 类型 | 内容 |
|------|------|
| BOM / parent 变更 | 版本属性、`dependencyManagement` |
| 代码迁移 | 适配 API 变更、javax→jakarta、配置键重命名 |
| 影响分析 | 受影响模块/文件列表、行为风险说明 |
| 验证结果 | compile + test + 依赖树对比 |

**不产出**：根 `${revision}` 变更（除非用户同时要求 project-upgrade）；无分析的 `use-latest-versions` 全量升级。

## 适用场景

- JDK 大版本（如 17 → 25）
- Spring Boot / Framework 跨代升级
- Java EE → Jakarta EE 迁移
- BOM 第三方库安全或功能升级
- 构建插件、JUnit 栈、注解处理器升级
- 因框架变更需要的模块结构调整

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 只升 `0.1` → `0.2` 工程坐标 | `java:project-upgrade` |
| 新功能开发与升级混提交 | 应拆分 |
| 为通过测试削弱断言 | 回到 `java:develop` / `java:design` |
| 把中间件依赖塞进 `base` | 违反 `AGENTS.md` |
| Spring 边界与禁止 Data/Security | 对照 `java:spring` |

核心原则：**编译通过 ≠ 行为等价**；须扫描使用点并补测试。

## 与上下游技能

```text
java:check（升级前基线）
    ↓
（大版本）grill-me
    ↓
java:dependency-upgrade
    ↓
java:spring（Spring 专项知识）
    ↓
java:develop（必要代码迁移）
    ↓
java:check（升级后对比）
```

可与 `java:project-upgrade` 分步执行，**不得**混在同一提交。

## 详细参考

- [dependency-migration.md](references/dependency-migration.md) — 组件迁移模板
- [stack-migration.md](references/stack-migration.md) — JDK / Spring / Jakarta 分类型手册
- [spring-migration-notes.md](../java-spring/references/spring-migration-notes.md) — Spring 断点
