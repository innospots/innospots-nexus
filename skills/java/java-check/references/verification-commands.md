# 验证命令矩阵

---

## L0 编译门禁

```bash
mvn clean compile
```

每批 Java 源文件改动后立即执行。**不得延后，不得降低 release 以迁就旧 JDK。**

| 失败类型 | 处理 |
|---------|------|
| 找不到 Lombok 生成的方法 | 检查 `annotationProcessorPaths` 含 lombok；IDE 是否启用注解处理 |
| MapStruct 实现未生成 | 是否声明 `org.mapstruct:mapstruct` 依赖；处理器路径中 lombok 是否在 mapstruct 之前 |
| 参数为 `arg0` | `maven.compiler.parameters` 应为 `true` |
| 模块间改动未生效 | 用 `-am` 重新构建上游 |
| JDK 版本不足 | **报告环境不匹配**，不得下调 `requireJavaVersion` / `maven.compiler.release` |

变体：

```bash
mvn -pl <module> clean compile              # 单模块
mvn -pl <module> -am clean compile          # 单模块 + 上游依赖
mvn clean install                            # 安装到本地仓库
```

---

## L1 基线校验

```bash
mvn validate
```

enforcer 在 `validate` 阶段执行，校验三项：

| 规则 | 要求 |
|------|------|
| `requireMavenVersion` | `>= 3.9.0` |
| `requireJavaVersion` | `>= 25` |
| `requirePluginVersions` | 禁用 `LATEST` / `RELEASE` / 快照版本插件 |

本地不满足时**报告环境不匹配**，不得修改 enforcer 配置。

查看当前环境：

```bash
mvn -version
java -version
```

---

## L2 测试

```bash
mvn test                                     # 全量
mvn -pl <module> test                        # 单模块
mvn -pl <module> -am test                    # 单模块 + 上游依赖（本地产物过期时用这个）
mvn -pl <module> test -Dtest=RoleEntityContractsTest
mvn -pl <module> test -Dtest='Role*Test'
mvn -pl <module> test -DfailIfNoSpecifiedTests=false -Dtest='Role*Test'
mvn -pl <module> test -Dtest=RoleEntityContractsTest#declaresNexusTableWithConstantName
mvn -o test                                  # 离线模式
```

### 结果判读

| 结果 | 处理 |
|------|------|
| 全部通过 | 进入 L3 |
| 本次改动引起的失败 | 判断是实现错还是测试错：哪个更符合已确立的契约 |
| 既有无关问题 | 单独记录，不要混在本次变更里 |
| 本地依赖产物过期 | `mvn -am` 重新构建上游，而不是削弱测试 |
| 编译通过但测试编译失败 | 检查测试依赖是否继承自 parent（junit/assertj/mockito 无需重复声明） |

**不得**为让测试通过而削弱断言、注释掉测试、或用 try-catch 吞掉后断言成功。

---

## L3 POM 核验

```bash
mvn -q help:effective-pom
mvn -q help:effective-pom -pl <module>
mvn -q help:evaluate -Dexpression=maven.compiler.release -DforceStdout
```

核验要点：

- 依赖版本是否全部来自 BOM（而非内联）
- 是否有意外的版本覆盖
- 插件版本是否来自 parent 的 `pluginManagement`
- `maven.compiler.release` 是否为 25
- flatten 是否正确解析 `${revision}`

依赖分析：

```bash
mvn dependency:tree
mvn dependency:tree -Dincludes=<groupId>:<artifactId>
mvn dependency:analyze
mvn dependency:analyze-duplicate
```

| 命令输出 | 含义 |
|---------|------|
| `Used undeclared dependencies found` | 隐式依赖传递依赖，应显式声明 |
| `Unused declared dependencies found` | 声明但未使用，应移除（注意 provided / 运行时依赖的误报） |
| 版本冲突树 | 检查是否被非 BOM 路径覆盖 |

---

## L4 工作区检查

```bash
git diff --check                             # 空白与格式错误
git status --short                           # 未跟踪 / 意外文件
git diff --stat                              # 改动概览
git diff --name-only                         # 改动文件清单
```

要点：

- 无行尾空白、无混合缩进
- 无意外生成的文件（`.flattened-pom.xml` 属正常产物，但不应被提交）
- 无 `pom.xml.versionsBackup`（parent 已配置 `generateBackupPoms=false`）
- 无遗留的临时文件或调试代码

---

## L5 静态巡检

### 规范巡检（可用 grep 辅助）

```bash
# 端点中是否混入 Spring MVC 注解
grep -rn "org.springframework.web.bind.annotation" --include=*.java */src/main/java

# 是否误用 Spring 事务注解
grep -rn "org.springframework.transaction.annotation" --include=*.java */src/main/java

# 是否使用 @Data
grep -rn "@Data" --include=*.java */src/main/java

# 是否使用 System.out / printStackTrace
grep -rn "System\.out\|printStackTrace" --include=*.java */src/main/java

# 是否手写 logger 或拼错 @Sl4j
grep -rn "@Sl4j\b\|LoggerFactory.getLogger" --include=*.java */src/main/java

# 是否存在裸 TODO / FIXME
grep -rn "TODO\|FIXME" --include=*.java */src/main/java

# 是否存在 Mapper XML
find . -name "*.xml" -path "*/mapper/*"

# 模块 POM 中是否内联版本
grep -rn "<version>" --include=pom.xml */pom.xml

# kernel 与 platform 是否互引
grep -rn "nexus.platform" --include=*.java innospots-nexus-kernel/src
grep -rn "nexus.kernel" --include=*.java innospots-nexus-platform/src

# base 是否引入中间件/框架依赖
grep -n "artifactId" innospots-nexus-base/pom.xml
```

### 结构与命名巡检

- 领域目录树是否有空的或不必要的包
- 端点方法数是否逼近 7 个（触发边界复审）
- 表名是否 `nx_` + 单数 snake_case
- 索引名是否 `uk_` / `idx_` + 表概念 + 用途
- 测试类与方法命名是否符合约定

---

## 组合执行

完整验证（领域初始化或大改动后）：

```bash
mvn clean compile && \
mvn validate && \
mvn test && \
mvn -q help:effective-pom && \
git diff --check
```

单模块完整验证：

```bash
mvn -pl <module> -am clean install -DskipTests && \
mvn -pl <module> test && \
git diff --check
```

---

## 升级场景的额外命令

`java:project-upgrade` 与 `java:tool-upgrade` 结束时除上述命令外还需：

```bash
mvn versions:display-dependency-updates      # 依赖升级候选
mvn versions:display-plugin-updates          # 插件升级候选
mvn versions:display-property-updates        # 属性升级候选
mvn dependency:tree -Dverbose                # 传递依赖冲突细节
mvn clean test                               # 干净全量测试，避免增量构建掩盖问题
```

> `versions:*` 的输出只是候选，是否升级必须由升级技能评估影响后决定。
