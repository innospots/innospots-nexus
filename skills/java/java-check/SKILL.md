---
name: java:check
display_name: Java 质量检查
description: |
  Java 编译、测试、规范、质量、依赖、安全与性能风险检查。当用户要求验证改动、
  跑编译与测试、做代码评审、检查是否符合编码规范、排查依赖冲突或安全与性能风险、
  或在提交/合入前做最终核验时使用。是 java:develop / java:design / java:test /
  两个升级技能的统一出口。
  触发词：编译验证、跑测试、代码检查、代码评审、规范检查、质量检查、
  依赖检查、安全审查、性能风险、回归验证、提交前检查。
category: java
version: 1.0.0
---

# 编译、测试、规范、质量、依赖、安全与性能检查

## 定位

所有 Java 工作的**统一出口**：`java:develop`、`java:design`、`java:test`、
`java:project-upgrade`、`java:tool-upgrade` 完成后都要过这一关。

## 验证分层

按成本从低到高执行，**前一层失败就不要继续下一层**：

```text
L0  编译门禁      mvn clean compile                 ← 改完 Java 立即执行
L1  基线校验      mvn validate                      ← enforcer：Maven / JDK / 插件版本
L2  测试         mvn test                          ← 单元测试 + 契约测试
L3  POM 核验     mvn -q help:effective-pom          ← 依赖版本与合并结果
L4  工作区       git diff --check / git status --short
L5  静态巡检     规范 / 依赖 / 安全 / 性能（人工 + 工具）
```

| 层 | 命令 | 失败时 |
|----|------|-------|
| L0 | `mvn clean compile` | 修正导入、签名、注解、泛型或模块依赖；**不得**继续加功能，**不得**下调 release |
| L1 | `mvn validate` | 本地 Maven/JDK 不满足时**报告环境不匹配**，不得下调 enforcer 规则 |
| L2 | `mvn test` | 区分「本次改动引起」「既有无关问题」「本地依赖产物过期（用 `-am`）」 |
| L3 | `mvn -q help:effective-pom` | 检查依赖版本是否来自 BOM、是否被意外覆盖 |
| L4 | `git diff --check` | 修掉空白与格式问题 |
| L5 | 检查清单 | 逐项过清单，产出报告 |

## 五类检查

### 1. 编译与构建

- [ ] `mvn clean compile` 通过
- [ ] `mvn validate` 通过（Maven ≥ 3.9.0、JDK ≥ 25、插件无 LATEST/RELEASE/SNAPSHOT）
- [ ] `mvn -q help:effective-pom` 正常，依赖版本全部来自 BOM
- [ ] 无新增编译告警（parent 已开启 `showWarnings`）
- [ ] 未为迁就本地环境而下调 `maven.compiler.release`

### 2. 规范合规

按 `java:reference` 的硬性红线逐项核对，重点：

| 面 | 检查项 |
|----|-------|
| 命名 | 类型后缀与职责匹配；方法动词揭示结果形态；字段带单位/时间/计数后缀；缩写大小写一致 |
| 代码风格 | 花括号、4 空格、120 行宽、导入顺序、成员顺序、Lombok 用法 |
| 注释 | public 类型与方法 Javadoc；行内注释解释 why；无裸 TODO；无注释掉的代码 |
| API 设计 | 不可变性；空值与缺失处理；校验归属；查询/命令语义；兼容面未破坏 |
| 分层 | `endpoint → service → operator → dao`；无 operator→service、无 endpoint→dao |
| 持久化 | 单表无 join、无 Mapper XML、索引与访问模式匹配、未重复继承字段 |
| 异常 | `NexusException` + 类型化 `StatusCode`；cause 保留；响应无敏感值与堆栈 |
| 事务 | 只用 `jakarta.transaction.Transactional`；方法级最小写操作 |
| 事件 | 成功后发布；订阅有清理；kernel/platform 不互引 |

详见 [review-checklist.md](references/review-checklist.md)。

### 3. 依赖与构建卫生

```bash
mvn dependency:tree                          # 依赖树
mvn dependency:analyze                       # 未声明 / 未使用依赖
mvn versions:display-dependency-updates      # 依赖升级候选
mvn versions:display-plugin-updates          # 插件升级候选
mvn versions:display-property-updates        # 属性升级候选
```

- [ ] 模块 POM 中无内联 `<version>`（版本只在 BOM）
- [ ] 无未声明但被使用的依赖（隐式传递）
- [ ] 无声明但未使用的依赖
- [ ] 无版本冲突或意外的版本覆盖
- [ ] `base` 未引入任何中间件或运行时框架依赖
- [ ] `core` 未绑定 Spring Boot 自动配置
- [ ] `kernel` 与 `platform` 无互相依赖
- [ ] 新增第三方依赖已完成 `java:design` 的选型评估并登记到 BOM

`versions:*` 只列候选，**是否升级交给 `java:tool-upgrade` 评估**，不得直接套用。

### 4. 安全

| 检查项 | 说明 |
|--------|------|
| 敏感值不入日志 | 密码、令牌、密钥、解密载荷、敏感配置 |
| 敏感值不入状态文本 | `message()` / `advice()` / `display()` 中无密钥、ID、SQL、路径、堆栈 |
| 响应不泄露内部细节 | 端点异常映射不含 cause 链、类名、SQL、文件路径、供应商诊断 |
| Lombok `toString` | 凭据/密钥/令牌类字段不得进入生成的 `toString` |
| `@Data` | 领域与持久化类型禁止使用 |
| 集合与快照 | 不暴露内部可变集合与实时可变视图 |
| 校验完整性 | 不得只依赖数据库约束做应用层可表达的校验 |
| 中断与取消 | 未静默吞掉 `InterruptedException` |
| 未知失败兜底 | 外层记录关联上下文并映射为通用内部状态，不暴露实现细节 |

### 5. 性能与并发风险

| 风险 | 检查点 |
|------|-------|
| N+1 查询 | 跨表读是否分批查询 + 内存组装，而非逐行查关联表 |
| 事务范围过大 | 是否类级注解；简单单表读是否被包进事务 |
| 长事务 | 事务中是否包含远程调用、文件 I/O、批量循环 |
| 持锁回调 | 是否在持锁状态下调用插件/事件处理器/回调 |
| 同步事件滥用 | 是否用 `publishSync` 重建了模块间直接调用 |
| 无界集合 | 分页是否生效；是否一次性载入全表 |
| 资源泄漏 | 线程、执行器、订阅、客户端是否有明确生命周期与清理 |
| 重复计算 | 循环内是否重复查询同一稳定数据 |

## 输出报告格式

检查完成后按以下结构汇总，便于开发者快速定位：

```markdown
## 检查结论

通过 / 有条件通过 / 不通过

## 执行结果

| 层 | 命令 | 结果 |
|----|------|------|
| L0 | mvn clean compile | ✅ / ❌ |
| L1 | mvn validate | ✅ / ❌ |
| L2 | mvn test（N 个测试，M 个失败） | ✅ / ❌ |
| L3 | mvn -q help:effective-pom | ✅ / ❌ |
| L4 | git diff --check | ✅ / ❌ |

## 问题清单

| 级别 | 位置 | 问题 | 建议 |
|------|------|------|------|
| 阻塞 | `path:line` | 描述 | 修正方式 |
| 警告 | `path:line` | 描述 | 修正方式 |
| 提示 | `path:line` | 描述 | 可选改进 |

## 遗留项

需要开发者决策、本次未处理的问题。
```

级别定义：

- **阻塞**：违反硬性红线、编译或测试失败、安全泄露、依赖方向错误 → 必须修
- **警告**：偏离规范但不破坏契约、性能风险、可维护性问题 → 建议修
- **提示**：风格偏好、可选优化 → 可不修

## 严格禁止

| 禁止 | 说明 |
|------|------|
| 为让检查通过而下调 enforcer 或 release | 应报告环境不匹配 |
| 为让测试通过而削弱断言 | 见 `java:test` |
| 因构建产物陈旧就放宽验证 | 用 `-am` 重新构建上游 |
| 把阻塞项降级为警告只为"先过一版" | 阻塞就是阻塞 |
| 只报结论不给位置 | 每个问题必须带 `path:line` 与修正建议 |

## 详细参考

- [verification-commands.md](references/verification-commands.md) — 完整命令矩阵与结果判读
- [review-checklist.md](references/review-checklist.md) — 分组评审清单
