# innospots-nexus-service-http

## 模块简介

HTTP **协议中立**工具：统一错误映射（Problem Details / Legacy）、标准头名与 requestId 策略。  
不包含 Controller 或 Filter；adapter 在边界调用这些类型。

## 何时使用

| 场景 | 用法 |
|---|---|
| 统一 HTTP 错误 JSON | adapter 注入 `HttpErrorMapper` |
| 透传/生成 requestId | `RequestIdPolicy` + `StandardHeaders.REQUEST_ID` |
| 业务模块直接依赖 | 通常不需要 |

## 包结构

```text
com.innospots.nexus.service.http
├── error              # HttpErrorMapper、ProblemDetailVo、ErrorResponseProfile
└── header             # StandardHeaders、RequestIdPolicy、HeaderPolicy
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-http</artifactId>
</dependency>
```

## 快速接入

### 场景 1：Spring 中映射异常（已自动装配）

`ServiceExceptionAdvice` / `ServiceWebExceptionHandler` 使用 `HttpErrorMapper`：

```java
// 业务侧只需：
throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND);
// adapter 输出统一 JSON + 正确 HTTP 状态码
```

### 场景 2：切换错误响应风格

```yaml
# application.yml（Spring）
service:
  enabled: true
  response-profile: LEGACY   # 或 PROBLEM_DETAIL
```

对应 `ServiceProperties.responseProfile` → `HttpErrorMapper` 使用的 `ResponseProfile`。

### 场景 3：手动构造错误体（测试/非 Spring）

```java
HttpErrorMapper mapper = new HttpErrorMapper();
ProblemDetailVo body = mapper.toProblemDetail(
        NexusException.build(NexusStatusCode.NO_PERMISSION));
```

## 标准头

| 常量 | 用途 |
|---|---|
| `StandardHeaders.REQUEST_ID` | 请求追踪 ID（入站可带，出站回写） |
| `StandardHeaders.CLIENT_ID` | 可选客户端标识 |

## 注意事项

- 普通 JSON API 与文件下载/流式响应语义分离；下载见 `service-transfer`。
- 本模块不绑定 Servlet；adapter 负责把 `HttpErrorMapper` 接到具体栈。

## 相关文档

- [HTTP 契约](../../docs/service-contract-design.md)
- [Spring adapter README](../../innospots-nexus-spring/innospots-nexus-spring-service/README.md)
