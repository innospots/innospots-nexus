# 国际化开发实践

本文说明在 Nexus Spring 应用中如何为 **API 响应、错误信息、字典与插件元数据** 做国际化。核心能力来自 `innospots-nexus-base`（`I18nObject`、`@I18n`、`I18nConverter`、`Jsons`），Spring 侧由 **`innospots-nexus-spring-core`** 把字典文件与请求语言环境接到同一套引擎上。

读完本文你可以：

- 按场景选用三种常见写法（代码内 `I18nObject`、`@I18n`、ResourceBundle 字典）；
- 完成从依赖、注解启用到 `messages` 配置的端到端搭建；
- 理解「请求语言 → 序列化输出」的完整链路，避免常见踩坑。

---

## 1. 整体是怎么工作的？

可以把它想成两条线，最后在 **JSON 写出** 时汇合：

```text
┌─────────────────────────────────────────────────────────────────┐
│  HTTP 请求（Accept-Language / LocaleResolver）                   │
│       ↓                                                          │
│  Spring LocaleContextHolder                                      │
│       ↓  NexusI18nLocaleFilter（sync-web-locale=true）           │
│  I18nConverter.setLocale(locale)   ← 当前线程语言环境            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  业务对象里的文案来源（三选一或组合）                             │
│    A. I18nObject 多语言 Map                                      │
│    B. @I18n 字段 + 字段值（I18nObject / "${key}" 字符串等）       │
│    C. 字典键 → MessageSource（messages_xx.properties）           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Jsons.toJson / Controller 返回体序列化                          │
│    · I18nObject → 当前 locale 对应的一条字符串                   │
│    · @I18n → 按注解规则翻译后写出                                │
│    · "${app.title}" → I18nMessageResolver → MessageSource 查键   │
└─────────────────────────────────────────────────────────────────┘
```

**默认对外 JSON 行为**：`I18nObject` 和带 `@I18n` 的字段在响应里通常变成 **单个本地化字符串**（例如 `"title":"智汇"`），而不是把整个 `{ "en": "...", "zh-CN": "..." }` 都返回给前端。若需要保留完整多语言结构（管理端落库、配置导出等），在写出前调用 `I18nConverter.ignoreI18n()`（见文末说明）。

---

## 2. 前置：依赖与启用

### 2.1 Maven

在 **Web 入口 / assembly 模块** 引入 Spring Core（版本由 `innospots-nexus-bom` 管理）：

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-spring-core</artifactId>
</dependency>
```

`innospots-nexus-base` 会随传递依赖引入，一般无需在业务模块重复声明。

### 2.2 启动类启用桥接

在 `@SpringBootApplication` 或 `@Configuration` 上增加：

```java
import com.innospots.nexus.spring.core.i18n.EnableNexusI18n;

@SpringBootApplication
@EnableNexusI18n
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

`@EnableNexusI18n` 会完成：

| 能力 | 说明 |
|------|------|
| `MessageSource` → `I18nConverter` | 把 Spring 字典注册为 `I18nMessageResolver`，供 `${key}` 解析 |
| 可选 `MessageSource` Bean | 若容器里还没有 `MessageSource`，按 `nexus.i18n.basenames` 创建 |
| Web Locale 同步 | Servlet 下注册 Filter，把 `LocaleContextHolder` 同步到 `I18nConverter` |

非 Web 任务（定时任务、消息消费）没有 Filter 时，需在入口自行 `I18nConverter.setLocale(...)`（见第 7 节）。

---

## 3. 方案一：在代码里直接使用 `I18nObject`

**适用**：文案写在代码或数据库里，字段本身就是「多语言表」；不依赖 `messages.properties`；字典项、插件展示名、状态码双语等。

### 3.1 创建方式

```java
import com.innospots.nexus.base.i18n.I18nObject;

// 单语言默认值（内部默认 locale 键为 en）
I18nObject single = I18nObject.of("Hello");

// 多语言
I18nObject label = I18nObject.of(
        "zh-CN", "订单列表",
        "en", "Orders"
);

// 从 Map 构造
I18nObject fromMap = I18nObject.of(Map.of("zh-CN", "销售", "en", "Sales"));
```

### 3.2 在 VO / Record 中使用

```java
import com.innospots.nexus.base.i18n.I18nObject;

public record MenuItemVo(
        String menuKey,
        I18nObject title,
        int orderIndex
) {
}
```

业务代码赋值：

```java
return new MenuItemVo(
        "orders",
        I18nObject.of("zh-CN", "订单", "en", "Orders"),
        10
);
```

### 3.3 序列化结果（无需再加 `@I18n`）

`I18nObject` 类型已绑定 Jackson 序列化器。在 **当前请求 locale = 简体中文** 时：

```json
{
  "menuKey": "orders",
  "title": "订单",
  "orderIndex": 10
}
```

在 **locale = 英文** 时，`title` 为 `"Orders"`。查找顺序为：精确 locale → 语言代码 → 中英文变体回退 → 第一个非空值（详见 `I18nObject#value`）。

### 3.4 何时优先选这种方案？

| 优点 | 注意 |
|------|------|
| 不依赖外部 properties，单测简单 | 改文案要发版或走数据迁移 |
| 适合结构稳定的领域对象 | 大量重复键名时不如字典集中维护 |

---

## 4. 方案二：使用 `@I18n` 注解

**适用**：响应 DTO 在 **写出 JSON 时** 自动翻译；可与 `I18nObject`、字典键字符串组合使用。

注解定义在 `com.innospots.nexus.base.i18n.I18n`，可标在 **字段** 或 **Record 组件** 上。翻译逻辑在序列化阶段执行（`Jsons` / 应用内 `ObjectMapper` 需带 `I18nModule`，`Jsons.mapper()` 已默认注册）。

### 4.1 子方案 B1：注解上写字典键（忽略字段当前值）

字段值可以不关心，**只按注解里的键** 去 `MessageSource` 查文案。

```java
import com.innospots.nexus.base.i18n.I18n;

public record DashboardHeaderVo(
        @I18n("app.title") String title,
        @I18n("${app.subtitle}") String subtitle
) {
}
```

- `@I18n("app.title")`：裸键，内部会规范成 `${app.title}` 再解析。
- `@I18n("${app.subtitle}")`：与裸键等价，显式写出占位形式。

**要求**：`messages` 里必须有对应键（见第 5 节）。字段里放 `"unused"` 或 `null` 也不影响输出。

### 4.2 子方案 B2：在字段值里放字典键字符串

不加注解 `value()`，在成员变量里存 **`${key}`** 形式：

```java
public record StatusVo(
        @I18n String label
) {
}

// 构造时
new StatusVo("${status.enabled}");
```

若字典没有该键，可使用 **内联默认值**（中英文用 `|#|` 分隔）：

```java
new StatusVo("${status.unknown:已启用|#|Enabled}");
```

当前 locale 为中文时取前半段，英文时取后半段。

### 4.3 子方案 B3：`@I18n` + `I18nObject` 字段值

字段类型为 `I18nObject` 时，序列化 **直接按 `I18nObject` 类型** 处理（按当前 locale 输出一条字符串），无需再把 Map 猜成 i18n：

```java
public record ProductNameVo(
        @I18n I18nObject displayName
) {
}

new ProductNameVo(I18nObject.of("zh-CN", "笔记本", "en", "Notebook"));
```

### 4.4 子方案 B4：嵌套结构

`@I18n` 标在 `List<String>`、`Map` 等容器上时，会对元素 **递归** 调用 `I18nConverter.translate`（列表里的 `${key}`、内嵌 `I18nObject` 都会处理）。

```java
public record BundleVo(
        @I18n List<String> tags
) {
}

new BundleVo(List.of("${tag.hot}", "${tag.new}"));
```

### 4.5 `@I18n` 与方案一的对比

| 维度 | 仅 `I18nObject` 字段 | `@I18n` |
|------|----------------------|---------|
| 文案来源 | 对象内的 locale Map | 键查字典 + 字段值规则 |
| 典型字段类型 | `I18nObject` | `String`、`I18nObject`、集合等 |
| 注解 `value()` | 不需要 | 可选，用于固定字典键 |

---

## 5. 方案三：Spring 字典文件（ResourceBundle / MessageSource）

**适用**：文案集中在 properties、希望产品/运维可批量改键值、与 Spring 生态一致。

### 5.1 文件放置与命名

在 `src/main/resources` 下建立字典，例如：

```text
src/main/resources/i18n/messages.properties          # 默认（通常英文）
src/main/resources/i18n/messages_zh_CN.properties  # 简体中文
```

示例内容：

```properties
# messages.properties
app.title=Nexus
app.subtitle=Operations Console
status.enabled=Enabled
```

```properties
# messages_zh_CN.properties
app.title=智汇
app.subtitle=运维控制台
status.enabled=已启用
```

键名与代码中 **`${app.title}`** 或 **`@I18n("app.title")`** 一致（不要带 `${}` 写在 properties 里）。

### 5.2 application.yml 配置

**推荐**：与 Spring Boot 标准配置对齐（项目里已有 `MessageSource` 时，Nexus 会直接桥接该 Bean）：

```yaml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
    fallback-to-system-locale: false

nexus:
  i18n:
    enabled: true
    # Web 请求内把 LocaleContextHolder 同步到 I18nConverter（JSON 序列化用）
    sync-web-locale: true
    # 仅当容器中没有 MessageSource Bean 时，用此处 basenames 创建
    basenames:
      - i18n/messages
    encoding: UTF-8
```

说明：

- 若已配置 `spring.messages.basename`，且 Boot 已注册 `MessageSource`，**以 Spring 的 basename 为准**即可；`nexus.i18n.basenames` 主要兜底「没有 MessageSource」的场景。
- `nexus.i18n.enabled=false` 会关闭桥接注册（一般不关）。
- `sync-web-locale=false` 时，HTTP 请求不会自动设置 `I18nConverter` 的 locale，需自行设置（见第 7 节）。

### 5.3 在代码里使用字典键

与方案二配合，任选其一：

```java
// 注解固定键
@I18n("app.title") String title;

// 字段值
@I18n String title = "${app.title}";

// 非 DTO、手动翻译
import com.innospots.nexus.base.i18n.I18nConverter;

String text = (String) I18nConverter.translate("${app.title}");
```

启用 `@EnableNexusI18n` 后，无需再手动 `I18nConverter.setMessageResolver(...)`。

---

## 6. 完整示例：从配置到接口响应

### 6.1 目录结构

```text
my-app/
  src/main/java/.../MyApplication.java
  src/main/java/.../web/DemoController.java
  src/main/java/.../web/vo/DemoVo.java
  src/main/resources/application.yml
  src/main/resources/i18n/messages.properties
  src/main/resources/i18n/messages_zh_CN.properties
```

### 6.2 `DemoVo.java`（三种写法同框）

```java
package com.example.demo.web.vo;

import com.innospots.nexus.base.i18n.I18n;
import com.innospots.nexus.base.i18n.I18nObject;

public record DemoVo(
        @I18n("app.title") String fixedKeyTitle,
        @I18n String dynamicKeyTitle,
        I18nObject embeddedLabel,
        @I18n I18nObject annotatedLabel
) {
    public static DemoVo sample() {
        return new DemoVo(
                "ignored",
                "${app.subtitle}",
                I18nObject.of("zh-CN", "内嵌中文", "en", "Embedded EN"),
                I18nObject.of("zh-CN", "注解中文", "en", "Annotated EN")
        );
    }
}
```

### 6.3 `DemoController.java`

```java
package com.example.demo.web;

import com.example.demo.web.vo.DemoVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/api/demo/i18n")
    public DemoVo demo() {
        return DemoVo.sample();
    }
}
```

### 6.4 请求与预期响应

请求头：`Accept-Language: zh-CN`

```json
{
  "fixedKeyTitle": "智汇",
  "dynamicKeyTitle": "运维控制台",
  "embeddedLabel": "内嵌中文",
  "annotatedLabel": "注解中文"
}
```

请求头：`Accept-Language: en`

```json
{
  "fixedKeyTitle": "Nexus",
  "dynamicKeyTitle": "Operations Console",
  "embeddedLabel": "Embedded EN",
  "annotatedLabel": "Annotated EN"
}
```

（需保证 Spring MVC 的 `LocaleResolver` 已根据 `Accept-Language` 解析 locale；Nexus Filter 再同步到 `I18nConverter`。）

---

## 7. 非 Web 与测试里如何指定语言？

```java
import com.innospots.nexus.base.i18n.I18nConverter;

import java.util.Locale;

I18nConverter.setLocale(Locale.SIMPLIFIED_CHINESE);
// ... 执行业务、Jsons.toJson(dto) ...
I18nConverter.clear(); // 或 setLocale(null)，避免污染线程池
```

单元测试可参考 `innospots-nexus-spring-core` 中 `MessageSourceI18nMessageResolverTest`：构造 `ResourceBundleMessageSource` 并 `I18nConverter.setMessageResolver(...)`。

---

## 8. 写出完整多语言结构（管理端场景）

对外 API 默认「一条字符串」。若接口要返回完整 locale Map（例如给配置编辑器）：

```java
I18nConverter.ignoreI18n();
try {
    return Jsons.toJson(dto);
} finally {
    I18nConverter.clear(); // ignore 与 locale 均为 ThreadLocal，用完务必清理
}
```

`ignoreI18n()` 下 `I18nObject` 会序列化为 `{"en":"...","zh-CN":"..."}` 对象。

---

## 9. 方案选型速查

| 场景 | 推荐 |
|------|------|
| 插件/字典项名称随数据库存储 | **I18nObject** |
| 固定 UI 文案、错误提示、全局标题 | **messages + `@I18n` 或 `${key}`** |
| 同一 DTO 有的来自 DB、有的来自字典 | **组合**：DB 字段用 `I18nObject`，静态用 `@I18n("key")` |
| 仅服务端内部使用、不序列化 | 直接 `I18nObject#value(locale)` 或 `I18nConverter.translate` |
| 批量改文案、少发版 | **messages 方案** |

---

## 10. 常见问题

**Q：`${app.title}` 原样出现在 JSON 里？**  
A：检查是否 `@EnableNexusI18n`、`nexus.i18n.enabled=true`，以及 `messages` 中是否有 `app.title`；键是否与 basename 路径一致。

**Q：换了 `Accept-Language` 但 JSON 不变？**  
A：确认 `nexus.i18n.sync-web-locale=true`；确认没有在更早的 Filter 里把 locale 写死；非 Web 场景需手动 `setLocale`。

**Q：`@I18n` 不生效？**  
A：确认返回体走的是 Jackson 且使用带 `I18nModule` 的映射器（`Jsons.mapper()` 或 Spring 默认 ObjectMapper 若已注册该模块）；DTO 字段需能被 Jackson 扫描到（Record 组件或 getter 字段）。

**Q：反序列化请求体里的 `I18nObject`？**  
A：`Jsons.fromJson` 支持 `"label":"单语"` 或 `"label":{"en":"x","zh-CN":"y"}`；详见 `innospots-nexus-base` 中 `I18nObjectDeserializer` 测试。

---

## 11. 相关实现类（便于跳转源码）

| 类 | 模块 | 作用 |
|----|------|------|
| `I18nObject` / `@I18n` / `I18nConverter` | `innospots-nexus-base` | 核心模型与翻译 |
| `Jsons` / `I18nModule` | `innospots-nexus-base` | JSON 读写 |
| `@EnableNexusI18n` | `innospots-nexus-spring-core` | Spring 装配入口 |
| `MessageSourceI18nMessageResolver` | `innospots-nexus-spring-core` | 字典桥接 |
| `NexusI18nLocaleFilter` | `innospots-nexus-spring-core` | Web locale 同步 |

本地验证 Spring 桥接：

```bash
mvn -pl innospots-nexus-spring/innospots-nexus-spring-core test \
  -Dtest=MessageSourceI18nMessageResolverTest
```
