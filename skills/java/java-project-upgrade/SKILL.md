---
name: java:project-upgrade
display_name: 工程基线升级
description: |
  Java 工程整体版本与架构升级。面向「改变项目运行或构建基线」的升级：JDK 大版本
  升级、Spring Boot / Spring Framework 大版本升级、Java EE 转 Jakarta EE、
  Maven / Gradle 大版本升级、JUnit 4 转 JUnit 5、构建体系升级、项目模块结构调整、
  语言特性迁移、框架兼容性迁移。负责兼容性分析、Breaking Changes 梳理、分阶段
  迁移方案与验证。
  触发词：JDK 升级、Java 版本升级、Spring Boot 升级、Jakarta EE 迁移、
  Maven 升级、Gradle 升级、JUnit 5 迁移、项目迁移、基线升级、架构升级。
category: java
version: 1.0.0
---

# 工程整体版本与架构升级

## 定位与边界

面向**改变项目运行/构建基线**的升级。

```text
改变项目运行/构建基线  → java:project-upgrade
改变项目使用的基础组件 → java:tool-upgrade
```

| 属于本技能 | 属于 `java:tool-upgrade` |
|-----------|------------------------|
| JDK 17 → 21 / 25 | Jackson 2 → 3 |
| Spring Boot 2.x → 3.x / 4.x | MapStruct、Lombok、SLF4J/Logback 升级 |
| Spring Framework 大版本 | commons-lang3 / commons-io / Guava |
| Java EE → Jakarta EE | HTTP Client、JSON 工具、缓存工具、加密工具 |
| Maven 3 → 4、Gradle 大版本 | 内部 common SDK、内部 starter、内部基础框架 |
| JUnit 4 → JUnit 5 | `hojo-common 1.2 → 2.0`、旧 Util 替换 |
| 构建体系升级 | 公共异常组件 / 日志组件 / 统一 HTTP Client 升级 |
| 项目模块结构调整 | |
| 语言特性迁移、框架兼容性迁移 | |

**一次升级同时涉及两者时**（如 Spring Boot 2→3 连带 Jackson / Hibernate / Jakarta）：
主流程由本技能负责，过程中**调用 `java:tool-upgrade`** 处理具体基础组件迁移。

## 升级流程

```text
1. 建立升级前基线    跑 java:check，留存编译与测试结果
2. 识别当前基线      JDK / 框架 / 构建 / 测试 / 模块结构
3. 确定目标基线      依据支持周期与业务窗口，不追最新
4. 兼容性分析        逐项扫描受影响面
5. 梳理 Breaking Changes  分类：编译期 / 运行时 / 行为 / 配置 / 数据
6. 制定迁移方案      分阶段、可回退、每阶段可验证
7. 分阶段实施        每阶段编译 + 测试
8. 测试验证          聚焦测试 + 全量回归
9. 交 java:check     与升级前基线对比
```

**任何一个阶段失败，停下并回到方案，不要继续往下一个阶段推进。**

## 第 1 步：建立升级前基线（不可跳过）

```bash
mvn clean compile
mvn validate
mvn test
mvn -q help:effective-pom
mvn dependency:tree > /tmp/dependency-before.txt
git rev-parse HEAD
```

留存这些结果。没有基线就无法判断升级后的失败是**新引入的**还是**既有的**。

同时确认工作区干净（`git status --short`），保证可回退。

## 第 2–3 步：识别基线与确定目标

| 基线维度 | 当前（本仓库） | 查什么 |
|---------|--------------|-------|
| JDK | `maven.compiler.release=25`，enforcer `>= 25` | `mvn -version`、`java -version` |
| 构建工具 | Maven `>= 3.9.0` | enforcer `requireMavenVersion` |
| 框架 | Jakarta REST 4.0、Jakarta Persistence 3.2、Jakarta Transaction 2.0.1 | BOM 的 `jakarta-*.version` |
| 测试 | JUnit 5（`junit-bom` 6.1.0）、AssertJ、Mockito | BOM 与 parent |
| 模块结构 | 7 个 Maven 模块 | 根 `pom.xml` `<modules>` |

目标版本的选择依据（**不是越新越好**）：

- 官方是否仍提供 OSS 支持（已 EOL 的版本不再收安全补丁）
- 目标版本的最低 Java 要求是否与当前 JDK 匹配
- 关键第三方依赖是否已提供兼容版本
- 是否有明确的业务窗口与回退计划

## 第 4–5 步：兼容性分析与 Breaking Changes

按五类梳理，每类都要落到具体文件：

| 类别 | 典型表现 | 发现方式 |
|------|---------|---------|
| 编译期 | 包路径变更、API 移除、签名变化 | `mvn clean compile` |
| 运行时 | 类加载失败、`NoClassDefFoundError`、行为变化 | 启动 + 集成测试 |
| 行为 | 默认值变化、序列化差异、排序/精度变化 | 对比测试 |
| 配置 | 属性重命名、插件配置变更、profile 行为变化 | 官方迁移指南 + `help:effective-pom` |
| 数据 | 持久化格式、迁移脚本、索引/约束变化 | 数据库结构对比 + 数据校验 |

各类型升级的具体断点见 [baseline-migration.md](references/baseline-migration.md)。

## 第 6 步：制定迁移方案

方案必须包含：

| 要素 | 要求 |
|------|------|
| 阶段划分 | 每阶段结束后**可编译、可测试、可提交** |
| 顺序依赖 | 前置阶段（如先升 JDK 再升框架）必须显式标注 |
| 回退点 | 每个阶段一个可回退的提交 |
| 影响清单 | 受影响的模块、包、文件、配置项 |
| 兼容措施 | 适配器、兼容开关、双写/双读、数据迁移脚本 |
| 验证方式 | 每个阶段跑哪些命令、看哪些指标 |
| 风险登记 | 已知风险与应对 |

**禁止**一次性把整棵源码树改完再编译——那不是迁移，是重写。

## 第 7–8 步：分阶段实施与验证

每个阶段：

```bash
mvn clean compile          # 必须立即执行
mvn -pl <module> -am test  # 受影响模块
mvn test                   # 阶段结束时全量
git add -A && git commit   # 形成回退点
```

阶段内如涉及具体基础组件（Jackson、Hibernate、Jakarta 子模块等），
调用 `java:tool-upgrade` 处理该组件的识别 → 差异 → 扫描 → 替换 → 验证。

## 第 9 步：交 `java:check`

升级完成后与升级前基线对比：

| 对比项 | 说明 |
|--------|------|
| 编译 | 新增失败？新增告警？ |
| 测试 | 新增失败？失败的是升级引起的还是既有的？ |
| 依赖树 | 与 `/tmp/dependency-before.txt` 对比，是否引入意外传递依赖 |
| POM | `help:effective-pom` 是否正常，版本是否全部来自 BOM |
| 行为 | 关键路径是否等价 |

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 跳过升级前基线 | 无法区分新旧失败 |
| 不下调基线迁就环境 | 本地 JDK/Maven 不满足应**报告环境不匹配** |
| 一次性全量改完再编译 | 必须分阶段，每阶段可验证 |
| 只升版本号不改代码 | 编译通过不代表行为等价 |
| 为让测试通过而削弱断言 | 见 `java:test` |
| 顺手改公共兼容面 | 兼容面改动需单独方案 |
| 把升级与功能开发混在一起 | 无法判定回归来源 |
| 升级期间同步更新技能文档 | 仅开发者显式请求整体扫描时才更新 |

## 详细参考

- [baseline-migration.md](references/baseline-migration.md) — 各类型基线升级的断点与执行要点
