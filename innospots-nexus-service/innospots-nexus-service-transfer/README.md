# innospots-nexus-service-transfer

## 模块简介

文件上传/下载**契约与内存流**支持：`UploadResource`、`DownloadResource`、范围/条件请求规划、二进制源抽象。  
**不涉及** JDBC/S3 等存储；持久化通过 `ResourceContentReader` SPI 由装配层桥接（当前 adapter 以内存源为主）。

## 何时使用

| 场景 | 类型 |
|---|---|
| 接口返回文件下载 | `DownloadResource` |
| 接收上传 | `UploadResource` |
| 内存/流式字节 | `BinarySource`、`PublisherBinarySource` |
| 绑定 core 文件元数据 | 实现 `ResourceContentReader`（adapter/core 边界） |

## 包结构

```text
com.innospots.nexus.service.transfer
├── upload             # UploadResource、UploadPolicy、UploadValidator、MalwareScanner
├── download           # DownloadResource、DownloadPlanner、DownloadTransferSupport
├── content            # BinarySource、ContentMetadata、ByteRange、BinarySources
└── config             # TransferConfig、TransferConfigSupport
```

## Maven 依赖

```xml
<dependency>
    <groupId>com.innospots</groupId>
    <artifactId>innospots-nexus-service-transfer</artifactId>
</dependency>
```

## 快速接入

### 场景 1：内存文件下载（无存储）

```java
@GetMapping("/files/{id}")
public DownloadResource download(@PathVariable String id) {
    byte[] bytes = loadFromMemory(id);
    ContentMetadata metadata = new ContentMetadata(
            id, bytes.length, "application/pdf", "\"v1\"",
            Instant.now(), Map.of(), true);
    return new DownloadResource(
            id,
            "report.pdf",
            metadata,
            () -> CompletableFuture.completedFuture(PublisherBinarySource.ofBytes(bytes)));
}
```

Spring/Quarkus adapter 自动写回响应体（见各 adapter README）。

### 场景 2：Range / If-None-Match（规划器）

```java
DownloadPlanner planner = new DefaultDownloadPlanner();
DownloadPlan plan = planner.plan(request, resource.metadata());
// plan.httpStatus()、plan.headers()、plan.bodyRequired()
```

`DownloadTransferSupport` 在 adapter 内组合 planner + `resource.content()` 打开 `BinarySource`。

### 场景 3：上传校验（中立）

```java
UploadValidator validator = UploadValidator.builder()
        .policy(UploadPolicy.defaults())
        .malwareScanner(NoOpMalwareScanner.INSTANCE)
        .build();
validator.validate(uploadResource);
```

## 配置

```java
TransferConfig config = TransferConfig.defaults();
// startupValidationEnabled: false 表示无 ResourceContentReader 时不报错（adapter 内存模式）
```

启用存储桥接时设为 `true` 并提供 `ResourceContentReader` bean。

## Adapter 写回

| 框架 | 机制 |
|---|---|
| Spring MVC | `ServiceDownloadReturnValueHandler` |
| Spring WebFlux | `HandlerResultHandler` for `DownloadResource` |
| Quarkus | `QuarkusDownloadBodyWriter` |

## 注意事项

- 业务禁止直接写 `HttpServletResponse.getOutputStream()`。
- `BinarySources.readAll` / `viewRange` 仅用于请求内内存视图，非持久化 API。
- 恶意扫描默认 `NoOpMalwareScanner`；生产可替换 SPI 实现。

## 相关文档

- [开发者体验 · 文件](../../docs/service-developer-experience-design.md#63-文件)
- [Transfer 契约测试](src/test/java/com/innospots/nexus/service/transfer/download/UploadDownloadContractsTest.java)
