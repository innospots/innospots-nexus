---
name: java:tool-upgrade
display_name: 基础组件升级
description: |
  基础组件、基础工具与公共依赖升级。负责内部基础工具、公共组件、公共依赖的
  版本升级：commons-lang3、commons-io、Jackson、Guava、MapStruct、Lombok、
  SLF4J、Logback、HTTP Client、JSON 工具、缓存工具、加密工具，以及内部 common
  SDK、内部 starter、内部基础框架、旧 Util 替换。核心关注一个基础组件升级后
  对业务代码的影响范围。
  触发词：依赖升级、组件升级、工具库升级、Jackson 升级、MapStruct 升级、
  Lombok 升级、SDK 升级、Util 替换、影响范围分析、版本升级。
category: java
version: 1.0.0
---

# 基础组件、基础工具与公共依赖升级

## 定位与边界

面向**改变项目使用的基础组件**的升级。

```text
改变项目运行/构建基线  → java:project-upgrade
改变项目使用的基础组件 → java:tool-upgrade
```

| 属于本技能 | 属于 `java:project-upgrade` |
|-----------|---------------------------|
| `commons-lang3`、`commons-io`、`commons-collections4`、`commons-codec` | JDK 17 → 21 / 25 |
| Jackson 2 → 3 | Spring Boot 2.x → 3.x / 4.x |
| Guava、Caffeine | Spring Framework 大版本 |
| MapStruct、Lombok | Java EE → Jakarta EE |
| SLF4J / Logback | Maven 3 → 4、Gradle 大版本 |
| HTTP Client（httpclient5、JDK HttpClient） | JUnit 4 → JUnit 5 |
| JSON 工具、缓存工具、加密工具 | 构建体系升级、模块结构调整 |
| 内部 common SDK、内部 starter、内部基础框架 | 语言特性迁移、框架兼容性迁移 |
| `hojo-common 1.2 → 2.0`、旧 Util 替换 | |
| 公共异常组件、日志组件、统一 HTTP Client、统一 JSON 组件 | |

**被主流程调用**：当一次升级同时涉及两者（如 Spring Boot 2→3 连带
Jackson / Hibernate / Jakarta），主流程由 `java:project-upgrade` 负责，
本技能负责其中**具体基础组件的迁移**。

## 核心关注点

> **一个基础组件升级以后，对业务代码的影响范围。**

不是「把版本号改了能编译就行」，而是回答：

- 哪些业务代码用了它？
- 用到的 API 在新版本里还在不在？行为变没变？
- 变了的地方，业务语义还等价吗？
- 有没有隐式依赖（序列化格式、反射、SPI、注解处理器）？

## 升级流程

```text
1. 识别当前工具版本    实际生效版本，而非声明版本
2. 确定目标版本        支持周期 + 兼容性 + 传递影响
3. 分析 API 差异       移除/改名/签名/行为/默认值
4. 扫描使用位置        全仓扫描，含测试与配置
5. 评估影响范围        按模块与使用模式分级
6. 制定替换方案        mapping 表 + 分阶段 + 回退点
7. 逐步修改            小批量，每批编译 + 测试
8. 测试                聚焦测试 + 全量回归
9. 交 java:check       版本核验 + 依赖树对比
```

## 第 1 步：识别当前版本

**实际生效版本 ≠ 声明版本**。传递依赖与 BOM 导入都可能改变最终结果。

```bash
mvn -q help:effective-pom                       # 看合并后的 dependencyManagement
mvn dependency:tree                             # 看实际解析版本
mvn dependency:tree -Dincludes=<groupId>:<artifactId>
mvn dependency:tree -Dverbose                   # 冲突与省略细节
```

同时识别：

| 项 | 说明 |
|----|------|
| 声明位置 | BOM 的属性 + `dependencyManagement` 条目 |
| 使用模块 | 哪些模块声明了该依赖 |
| 传递来源 | 是否有其他依赖把它带进来 |
| 相关配置 | 注解处理器路径、SPI 注册、配置文件 |

## 第 2 步：确定目标版本

| 依据 | 说明 |
|------|------|
| 支持状态 | 官方是否仍在维护、是否有已知 CVE |
| 兼容性 | 与当前 JDK、其他依赖的组合是否兼容 |
| 迁移成本 | 是否有 breaking changes、是否有官方迁移指南 |
| 传递影响 | 升级它是否会连带改变其他依赖的解析结果 |
| 必要性 | 安全补丁优先；无驱动的升级应暂缓 |

`mvn versions:display-dependency-updates` 只提供候选，**不是升级依据**。

## 第 3 步：分析 API 差异

按五类梳理：

| 类别 | 表现 | 影响 |
|------|------|------|
| 移除 | 类/方法被删除 | 编译失败 |
| 改名 | 类/方法/包重命名 | 编译失败 |
| 签名变化 | 参数或返回类型变化 | 编译失败，或重载解析变化 |
| 行为变化 | 默认值、边界处理、排序、精度变化 | **编译通过但行为不等价** |
| 隐式契约 | 序列化格式、注解处理器输出、SPI 接口 | 运行时才暴露 |

**行为变化与隐式契约是最危险的**——编译通过不代表正确。

## 第 4 步：扫描使用位置

```bash
# 按 import 扫描
grep -rn "import com\.fasterxml\.jackson" --include=*.java .

# 按类名扫描（含全限定名用法）
grep -rn "ObjectMapper" --include=*.java .

# 含测试与资源
grep -rn "<groupId>com.fasterxml</groupId>" --include=pom.xml .
grep -rn "jackson" --include=*.yml --include=*.yaml --include=*.properties .

# 注解处理器与 SPI
grep -rn "META-INF/services" --include=* .
```

覆盖范围必须包括：**主源码、测试源码、POM、配置文件、资源文件、SPI 注册、
构建脚本、CI 配置**。

## 第 5 步：评估影响范围

按使用模式分级，而不是按文件数：

| 级别 | 使用模式 | 处理 |
|------|---------|------|
| 高 | 出现在 public API 签名、持久化格式、序列化契约、事件载荷 | 必须出兼容方案与迁移验证 |
| 高 | 被多处业务代码直接依赖的核心工具 | 集中替换或加适配层 |
| 中 | 内部实现使用，未外泄 | 按 mapping 表逐个替换 |
| 低 | 仅测试使用 | 随测试一起改 |

产出影响清单：模块 / 文件 / 行号 / 用法 / 新旧 API mapping / 是否涉及兼容面。

## 第 6 步：制定替换方案

| 要素 | 要求 |
|------|------|
| API mapping 表 | 旧 API → 新 API，含语义等价性说明 |
| 行为差异清单 | 哪些地方编译能过但行为变了 |
| 分阶段 | 每阶段可编译、可测试、可提交 |
| 适配策略 | 直接替换 / 适配层 / 兼容开关 |
| 回退点 | 每阶段一个提交 |
| 验证方式 | 针对行为差异的专项测试 |

**先在 BOM 改版本**，再逐模块改代码——这样能先暴露编译期影响面。

## 第 7–8 步：逐步修改与测试

```bash
# 1. BOM 改版本
# 2. 每批改动后立即
mvn clean compile
mvn -pl <module> -am test
# 3. 阶段结束
mvn test
```

针对**行为变化**补专项测试（编译通过但语义可能变的地方）：

- 序列化/反序列化：字段顺序、命名策略、null 处理、日期格式、多态类型
- 集合与排序：稳定性、null 容忍度
- 数值与精度：舍入、溢出、除零
- 字符串处理：空白、编码、locale
- 时间处理：时区、精度、闰秒/闰年边界

## 第 9 步：交 `java:check`

| 核验项 | 命令 |
|--------|------|
| 实际生效版本是目标版本 | `mvn dependency:tree` |
| 无残留旧版本传递依赖 | `mvn dependency:tree -Dverbose` |
| 无未声明依赖 | `mvn dependency:analyze` |
| POM 正常 | `mvn -q help:effective-pom` |
| 全量测试 | `mvn test` |
| 依赖树与升级前对比 | 与 `/tmp/dependency-before.txt` diff |

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 只改版本号不改代码 | 编译通过不代表行为等价 |
| 用 `versions:use-latest-versions` 一键全量升 | 无影响分析的升级等于赌博 |
| 忽略传递依赖带进来的旧版本 | 实际生效版本可能没变 |
| 为消除冲突而全局排除依赖 | 可能破坏功能，应升级而非盲目 exclude |
| 把依赖升级与功能开发混在一次提交 | 无法判定回归来源 |
| 因测试失败就放宽断言 | 见 `java:test` |
| 顺手改公共兼容面 | 需单独方案 |

## 本仓库的特殊约束

| 约束 | 说明 |
|------|------|
| 版本只在 BOM | 模块 POM 不得内联 `<version>` |
| `base` 零中间件 | 升级不得把中间件依赖带进 `base` |
| `core` 不绑自动配置 | 升级不得引入自动配置绑定 |
| 注解处理器 | Lombok 与 MapStruct 在 parent 的 `annotationProcessorPaths` 中，**同时**要升 `mapstruct-processor` 与 `mapstruct` |
| Lombok 顺序 | `annotationProcessorPaths` 中 Lombok 必须在 MapStruct 之前 |
| 契约测试 | 反射型契约测试依赖注解与参数名，升级注解处理器后需确认仍生效 |

## 详细参考

- [dependency-migration.md](references/dependency-migration.md) — 常见组件的迁移要点与影响分析模板
