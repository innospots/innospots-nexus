---
name: java:dependency-upgrade
display_name: 依赖升级
description: |
  依赖、JDK 与开发框架升级。负责第三方依赖、JDK、Maven/Gradle 构建插件、
  Spring / Jakarta、JUnit、注解处理器、commons/Jackson/MapStruct/Lombok 等
  一切「工程所使用的外部技术栈」的版本升级，并分析对业务代码的影响范围。
  触发词：依赖升级、JDK 升级、Spring 升级、Jakarta 迁移、组件升级、
  BOM 升级、框架升级、库升级、影响范围分析。
category: java
version: 2.1.0
---

# 依赖、JDK 与开发框架升级

## 定位与边界

面向**改变工程所使用的外部技术栈**的升级：JDK、框架、构建插件、BOM 中的第三方
库与内部 SDK。**不是** innospots-nexus 自身产物版本号（`0.1.0-SNAPSHOT` → `0.2.0`）——
那属于 `java:project-upgrade`。

```text
改变依赖 / JDK / 开发框架  → java:dependency-upgrade
改变当前工程 revision 产物版本号  → java:project-upgrade
```

| 属于本技能 | 属于 `java:project-upgrade` |
|-----------|---------------------------|
| JDK 17 → 21 / 25 | 根 POM `${revision}` / `innospots.version` 升版 |
| Spring Boot / Spring Framework 大版本 | 各模块手工改 `<version>`（禁止；应只改 revision） |
| Java EE → Jakarta EE | SNAPSHOT → release 发版节奏 |
| Maven / Gradle / 构建插件大版本 | flatten 与 reactor 版本一致性 |
| JUnit 4 → JUnit 5、测试栈 | git tag 与工程版本对齐 |
| BOM 中第三方库：Jackson、Hibernate、commons-*、Guava 等 | |
| MapStruct、Lombok、SLF4J / Logback | |
| HTTP Client、JSON、缓存、加密等库 | |
| 内部 common SDK、starter、基础框架 | |
| 语言特性迁移、框架兼容性迁移、模块结构调整（因框架所致） | |

## 核心关注点

> **一个依赖或框架升级以后，对业务代码的影响范围。**

- 哪些模块/文件用了它？API 是否移除或行为变化？
- 传递依赖与 BOM 实际生效版本是否一致？
- 编译通过是否仍行为等价（序列化、反射、注解处理器）？

## grill-me

JDK 大版本、Spring 跨代、Jakarta 迁移、多种替换路径并存时，在批量改代码前调用
`grill-me`（见 `java:reference` → `grill-me.md`）。BOM patch 对齐通常无需 grill。

## 升级流程

```text
1. 建立升级前基线（java:check：compile / test / dependency:tree）
2. 识别当前生效版本（effective-pom + dependency:tree，非仅声明）
3. 确定目标版本（支持周期、CVE、与其他栈兼容）
4. 分析 API / 行为 / 配置差异
5. 全仓扫描使用位置（源码、测试、配置、SPI）
6. 评估影响并按模块分级
7. 先在 BOM / parent 改版本属性，再逐模块改代码
8. 小批量 compile + test；行为敏感点补专项测试
9. 交 java:check（依赖树对比、全量测试）
```

## 版本修改位置（本仓库）

| 改什么 | 改哪里 |
|--------|--------|
| 第三方库版本 | `innospots-nexus-bom` 的 `dependencyManagement` 与 `<properties>` 版本属性 |
| 编译 JDK、enforcer | `innospots-nexus-parent` |
| 注解处理器路径 | `innospots-nexus-parent` `annotationProcessorPaths` |
| 模块内依赖 | **不写 `<version>`**；只声明 artifact，版本由 BOM 管理 |

**不要**在根 `${revision}` 上冒充依赖升级；**不要**在各子模块内联第三方版本。

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 只改版本号不改代码 | 编译通过 ≠ 行为等价 |
| `versions:use-latest-versions` 无分析全量升 | 须逐项评估 |
| 与功能开发或工程 version bump 混提交 | 回归不可追溯 |
| 为通过测试削弱断言 | 见 `java:develop` → `test-conventions.md` |
| 把中间件依赖塞进 `base` | 见 `AGENTS.md` |

## 详细参考

- [dependency-migration.md](references/dependency-migration.md) — 组件迁移模板与高频坑
- [stack-migration.md](references/stack-migration.md) — JDK / Spring / Jakarta / 构建体系分类型手册
