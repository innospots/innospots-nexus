# java:project-upgrade — 工程版本升级

> 本文档供**开发者阅读**。AI 代理执行入口为 [SKILL.md](./SKILL.md)。

## 是什么

`java:project-upgrade` 只负责 **innospots-nexus 自身产物版本号**（Maven `${revision}`）的统一升版与发版对齐。

它不改变 JDK、Spring、第三方库等外部技术栈版本——那些属于 `java:dependency-upgrade`。

## 怎么用

1. 确定 semver（patch / minor / major）与 SNAPSHOT 或 release。
2. **只改根 POM 的 `revision`**（推荐 `mvn versions:set`）。
3. `mvn validate` → `mvn -q help:effective-pom` 确认所有 `innospots-nexus-*` 版本一致。
4. `mvn clean install`（或 `test`）验证 reactor。
5. 检查 flatten 产物无未解析占位符；发版时打 tag。
6. 交 `java:check`。

```bash
mvn versions:set -DnewVersion=0.2.0-SNAPSHOT
mvn versions:commit
mvn validate
mvn clean install
```

发版策略有歧义（major 是否进位、多版本线）时可 `grill-me`；日常 patch/minor 通常不需要。

## 输入

| 类型 | 示例 |
|------|------|
| 目标版本 | `0.2.0-SNAPSHOT`、`1.0.0`（去 SNAPSHOT） |
| 发版类型 | 内部迭代 / 对外正式发布 |
| 约束 | 是否与 git tag、变更说明同步 |

## 输出

| 类型 | 内容 |
|------|------|
| 根 POM | 更新后的 `<revision>` / `innospots.version` |
| 一致性验证 | effective-pom 中各模块版本一致 |
| 构建结果 | validate + install/test 通过 |
| 发版产物（可选） | tag、release note 要点 |

**不产出**：BOM 中第三方版本变更、业务代码修改。

## 适用场景

- `0.1.0-SNAPSHOT` → `0.2.0-SNAPSHOT` 迭代 bump
- SNAPSHOT 转正发布 `1.0.0`
- 避免在各子模块手工改 `<version>`
- 校验 flatten 与 reactor 版本一致

## 不适用 / 边界

| 不做的事 | 应转交 |
|---------|--------|
| 升 JDK、Spring Boot、Jackson 等 | `java:dependency-upgrade` |
| 在各子模块 POM 手写 version | 禁止；破坏单点治理 |
| 与依赖大升级混在同一提交 | 应分提交、分验证 |
| 功能开发与 version bump 捆绑 | 分开 PR |

可与 `java:dependency-upgrade` **连续执行**，但必须分提交；两者完成后都过 `java:check`。

## 与上下游技能

```text
java:project-upgrade（仅 revision）
    ↓
java:check

或与 java:dependency-upgrade 分步交替，最后 java:check
```

## 详细参考

- [project-version.md](references/project-version.md) — `${revision}`、flatten、SNAPSHOT/发布清单
