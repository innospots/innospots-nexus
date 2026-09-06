# IDE 设置（IntelliJ IDEA / Cursor）

Maven 基线为 **Java 25**（`innospots-nexus-parent` → `maven.compiler.release=25`）。  
IDE 报 `source 1.5 不支持方法引用` 时，是本地工程模型问题，不是 POM 配置。

## 正确做法

1. **只通过 Maven 导入模块**  
   打开根目录 `pom.xml`，使用 Maven → Reload All Projects。

2. **不要保留 dumb `.iml` 模块**  
   以下文件若存在请删除（已在 `.gitignore`）：
   - `innospots-nexus.iml`
   - `innospots-nexus-spring/innospots-nexus-spring.iml`
   - `innospots-nexus-quarkus/innospots-nexus-quarkus.iml`

   它们带 `dumb="true"`，不继承 Maven 的 `release 25`，IDE 会回落到 source 1.5。

3. **Project SDK**  
   Settings → Project → SDK：**JDK 25**（或项目使用的 GraalVM 25）。  
   Language level：**25 - …** 或与 Maven 一致。

4. **Maven 导入后检查**  
   `.idea/compiler.xml` 中 `innospots-nexus-base` 等 Java 模块应为 **25**（或随 Maven）。  
   `packaging=pom` 的 BOM 子模块出现 `target="1.5"` 可忽略（无 Java 源码）。

5. **以 Maven 为准验证**

   ```bash
   mvn -pl innospots-nexus-base clean compile
   ```

## 禁止在子模块 POM 中单独写

```xml
<!-- 不要覆盖父 POM -->
<source>8</source>
<target>8</target>
```

统一使用父 POM 的 `<release>${maven.compiler.release}</release>`。

## 子模块不要重复声明 compiler 版本

所有 Java 模块继承 `innospots-nexus-parent` 即可；仅 Spring Boot 插件等运行插件在子模块 `build/plugins` 中声明。
