# 工程版本号升级

本仓库通过根 POM 的 **`${revision}`** 统一所有内部模块的 Maven 坐标版本，配合
`flatten-maven-plugin` 生成可发布的扁平 POM。**禁止**在各子模块 POM 中手工逐处
改 `<version>`。

`java:project-upgrade` 只处理 **innospots-nexus 自身产物版本**（`0.1.0-SNAPSHOT` →
`0.2.0-SNAPSHOT` / `1.0.0` 等）。JDK、第三方依赖、Spring 等依赖升级属于
`java:dependency-upgrade`。

---

## 版本定义位置（唯一入口）

| 属性 | 位置 | 作用 |
|------|------|------|
| `revision` | 根 `pom.xml` `<properties>` | 全 reactor 与内部模块坐标版本 |
| `innospots.version` | 根 `pom.xml`，通常 `${revision}` | 业务属性别名，与 `revision` 同步 |
| 子模块 `<version>` | **不写** | 继承 `innospots-nexus-parent` → 根 `${revision}` |
| BOM 内 `innospots-nexus-*` | `innospots-nexus-bom/pom.xml` | 使用 `${revision}`，随根属性联动 |

---

## 升级流程

```text
1. 确认 semver 策略（patch / minor / major）与发布类型（SNAPSHOT / release）
2. 仅在根 POM 修改 revision（或 versions:set 一次到位）
3. mvn validate && mvn -q help:effective-pom  确认各模块 effective version 一致
4. mvn clean install（或至少 compile + test）验证 reactor
5. 检查 flatten 产物（.flattened-pom.xml）无残留未解析占位符
6. 同步标签/变更说明（若发版）；交 java:check
```

### 推荐命令

```bash
# 方式 A：versions 插件（改根 POM 的 project.version / revision）
mvn versions:set -DnewVersion=0.2.0-SNAPSHOT
mvn versions:commit          # 确认后提交；放弃则 versions:revert

# 方式 B：只改根 pom.xml 中 <revision> 与（如有）<innospots.version>
# 然后：
mvn validate
mvn -q help:effective-pom | grep -A1 '<artifactId>innospots-nexus-base</artifactId>'
mvn clean install
```

---

## 禁止的手动方式

| 禁止 | 原因 |
|------|------|
| 在每个子模块 POM 单独改 `<version>` | 与 `${revision}` 单点治理冲突，易漏改 |
| 只改 BOM 不改根 `revision` | 内部模块坐标不一致 |
| 在模块 POM 写死版本号替代 `${revision}` | 破坏 flatten 与发布一致性 |
| 为迁就本地仓库手动降版本 | 应统一升/降根 `revision` |

---

## SNAPSHOT 与正式发布

| 场景 | `revision` 示例 | 注意 |
|------|-----------------|------|
| 日常开发 | `0.2.0-SNAPSHOT` | 本地 `install` 后下游 `-am` 构建 |
| 发布候选 | `0.2.0-RC1` | 团队约定前缀；仍走同一 `${revision}` 入口 |
| 正式发布 | `0.2.0` | 去掉 `-SNAPSHOT`；打 git tag 与 `revision` 对齐 |
| 下一开发线 | `0.3.0-SNAPSHOT` | 发布完成后在根 POM 升 revision |

---

## 验证清单

- [ ] 仅根 POM（或 `versions:set`）变更了工程版本？
- [ ] `mvn -q help:effective-pom` 中各 `innospots-nexus-*` 模块版本一致？
- [ ] BOM 中内部 artifact 的 `${revision}` 与根属性一致？
- [ ] `mvn validate` / `mvn test` 通过？
- [ ] flatten 后无未解析的 `${revision}` 泄漏到需发布 POM？
- [ ] 未把第三方依赖版本误当作 `revision` 修改（第三方走 `java:dependency-upgrade` → BOM 属性）？

---

## 与 `java:dependency-upgrade` 的边界

| 问题 | 使用技能 |
|------|---------|
| 「把整个工程从 0.1 升到 0.2」 | **`java:project-upgrade`**（本文件） |
| 「把 JDK 升到 25」「Spring Boot 升 3.x」「Jackson 升 3」 | **`java:dependency-upgrade`**（改 BOM / parent 中的**外部**版本属性） |

两者可连续执行，但**不得混在一次提交**里把「工程版本 bump」与「依赖大升级」缠在一起，以免无法判定回归来源。
