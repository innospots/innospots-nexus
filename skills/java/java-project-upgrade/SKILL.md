---
name: java:project-upgrade
display_name: 工程版本升级
description: |
  当前工程产物版本号（revision）的统一升级。面向 innospots-nexus 自身 Maven
  坐标版本：在根 POM 通过 ${revision} 一次性升版，避免在各子模块手工改 version；
  配合 flatten 校验 reactor 一致性，支持 SNAPSHOT 与正式发布。不负责 JDK、
  第三方依赖或 Spring 等升级（见 java:dependency-upgrade）。
  触发词：工程版本升级、revision 升级、发版、升版本号、SNAPSHOT 转正、
  0.1 升 0.2、统一改版本、避免手动改各模块 version。
category: java
version: 2.0.0
---

# 工程产物版本升级

## 定位与边界

面向 **innospots-nexus 自己发布的版本号**（`${revision}`），不是外部依赖版本。

本仓库约定：

- 根 `pom.xml` 的 `<revision>` 是全 reactor 的**唯一版本入口**
- 子模块继承 parent，**不得**逐个手工改 `<version>`
- `flatten-maven-plugin` 负责发布用扁平 POM

```text
升工程 revision / 发版对齐     → java:project-upgrade
升 JDK / 依赖 / 开发框架       → java:dependency-upgrade
```

| 属于本技能 | 属于 `java:dependency-upgrade` |
|-----------|------------------------|
| `0.1.0-SNAPSHOT` → `0.2.0-SNAPSHOT` | JDK 大版本 |
| 去 SNAPSHOT 发布 `1.0.0` | Spring Boot / Framework 大版本 |
| `versions:set` 统一改工程版本 | BOM 中 Jackson、commons、Lombok 等 |
| 校验 flatten 与各模块 effective version 一致 | Jakarta 迁移、JUnit 栈、构建插件 |
| git tag 与 `revision` 对齐 | 第三方 SDK、工具库、影响范围分析 |

## 升级流程

```text
1. 确定 semver（patch / minor / major）与 SNAPSHOT / release
2. 仅修改根 POM 的 revision（推荐 mvn versions:set）
3. mvn validate
4. mvn -q help:effective-pom  确认所有 innospots-nexus-* 版本一致
5. mvn clean install（或 test）验证 reactor
6. 检查 flatten 产物无未解析占位符
7. （发版）打 tag、更新变更说明；交 java:check
```

```bash
mvn versions:set -DnewVersion=0.2.0-SNAPSHOT
mvn versions:commit
mvn validate
mvn clean install
```

## grill-me

仅在发版策略有歧义时（major 是否进位、多分支版本线、与外部消费方协调）调用
`grill-me`。日常 patch/minor bump 通常不需要。

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 在每个子模块 POM 手工改 version | 破坏 `${revision}` 单点治理 |
| 只改 BOM 内部条目版本而不改根 revision | 坐标不一致 |
| 把工程 version bump 与依赖大升级混在同一提交 | 无法判定回归来源 |
| 为本地过期 SNAPSHOT 在子模块写死旧版本 | 应 `install` 或升 revision |
| 跳过 `mvn validate` / effective-pom 检查 | 可能泄漏未解析 `${revision}` |

## 与 dependency-upgrade 协作

可先 `java:project-upgrade` 升 revision，再 `java:dependency-upgrade` 升 BOM 依赖，或反之；
**分提交、分验证**。两者都完成后交 `java:check`。

## 详细参考

- [project-version.md](references/project-version.md) — `${revision}`、flatten、SNAPSHOT/发布、验证清单
