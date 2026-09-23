# 文件上传下载接入手册（Spring）

说明在 Spring 服务中使用 Nexus **传输标准类型** `DownloadResource`、`UploadResource` 完成 HTTP 文件下载与上传校验，由 adapter 写回响应体（MVC `ReturnValueHandler` / WebFlux `HandlerResultHandler`）。

---

## 1. 能力边界

| 模块职责 | 说明 |
|----------|------|
| `innospots-nexus-service-transfer` | 契约、`DownloadPlanner`、二进制 `BinarySource`、上传策略 |
| `innospots-nexus-spring-service` | `ServletDownloadWriter`、`ReactiveDownloadWriter`、`ServiceDownloadReturnValueHandler` |
| **不包含** | JDBC/S3 持久化；通过 `ResourceContentReader` SPI 由 assembly 桥接 |

业务禁止直接操作 `HttpServletResponse.getOutputStream()` 写下载流。

---

## 2. 下载接入步骤

### 步骤 1：依赖

与 HTTP API 相同：`innospots-nexus-spring-service` + `spring-boot-starter-web` 或 `webflux`。

自动装配：

- `ServiceTransferConfiguration` → `DownloadTransferSupport`
- `ServiceMvcTransferConfiguration` → `addFirst(downloadReturnValueHandler)`
- WebFlux：`ServiceWebFluxTransferConfiguration`

### 步骤 2：Controller 返回 `DownloadResource`

```java
@GetMapping("/files/{id}")
public DownloadResource download(@PathVariable String id) {
    byte[] bytes = loadBytes(id);
    ContentMetadata metadata = new ContentMetadata(
            id,
            bytes.length,
            "application/pdf",
            "\"v1\"",
            Instant.now(),
            Map.of(),
            true);
    return new DownloadResource(
            id,
            "report.pdf",
            metadata,
            () -> CompletableFuture.completedFuture(PublisherBinarySource.ofBytes(bytes)));
}
```

模块测试参考：`SampleHttpResource.download()` → `AdapterDownloadFixtures.memoryDownload()`。

### 步骤 3：条件请求与 Range（可选）

业务可预先使用中立规划器：

```java
DownloadPlanner planner = new DefaultDownloadPlanner();
DownloadPlan plan = planner.plan(downloadRequest, resource.metadata());
```

adapter 内 `DownloadTransferSupport.prepare(...)` 已组合 planner 与 `resource.content()`；Servlet 写回见 `ServletDownloadWriter`。

### 步骤 4：验证响应

- `Content-Type`、`Content-Disposition`、ETag、`Accept-Ranges` 由 planner + writer 设置。
- `ServiceDownloadReturnValueHandler` 必须排在 JSON 转换器 **之前**（本模块已 `addFirst`）。

---

## 3. 上传接入步骤

中立库提供 `UploadResource`、`UploadValidator`、`UploadPolicy`、`MalwareScanner` SPI。Spring adapter **当前未** 提供统一的 `@RequestPart` → `UploadResource` 参数解析器（设计中的 `ServletUploadAdapter` / `ReactiveUploadAdapter` 按版本核对）。

推荐分阶段接入：

### 阶段 A：在 Web 层组装 `UploadResource`（过渡）

```java
@PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public UploadResult upload(@RequestPart("file") MultipartFile file) throws IOException {
    UploadResource resource = UploadResource.builder()
            .filename(file.getOriginalFilename())
            .contentType(file.getContentType())
            .size(file.getSize())
            .source(() -> CompletableFuture.completedFuture(
                    PublisherBinarySource.ofBytes(file.getBytes())))
            .build();

    UploadValidator validator = UploadValidator.builder()
            .policy(UploadPolicy.defaults())
            .malwareScanner(NoOpMalwareScanner.INSTANCE)
            .build();
    validator.validate(resource);

    return storage.save(resource);
}
```

`MultipartFile` 仅允许出现在 **Web 入口模块**；domain 服务只接收已校验的 DTO 或 `UploadResource`。

### 阶段 B：存储 SPI

启用 `TransferConfig.startupValidationEnabled` 并提供 `ResourceContentReader` 时，启动期校验存储桥接是否就绪：

```java
@Bean
TransferConfig transferConfig() {
    return new TransferConfig(
            true,
            TransferConfig.defaults().smallObjectThresholdBytes(),
            UploadPolicy.defaults(),
            true);
}
```

---

## 4. 配置（`TransferConfig`）

```java
TransferConfig config = TransferConfig.defaults();
// startupValidationEnabled: false → 无 ResourceContentReader 时不报错（内存下载模式）
```

设计 YAML：`service.transfer.*`。

---

## 5. MVC 与 WebFlux

| 栈 | 下载写回类 |
|----|------------|
| MVC | `ServletDownloadWriter` + `ServiceDownloadReturnValueHandler` |
| WebFlux | `ReactiveDownloadWriter` + `ServiceDownloadResultHandler` |

上传在 adapter 完整落地前，两栈均在 Controller 将 multipart 转为 `UploadResource`。

---

## 6. 验证清单

- [ ] 返回类型为 `DownloadResource` 时响应为二进制而非 JSON 包装
- [ ] 大文件使用 `PublisherBinarySource` 等流式 `BinarySource`，避免无谓 `readAll`
- [ ] 运行 `AdapterScenarioMvcTest` 下载场景

---

## 7. 二次扩展开发

### 7.1 自定义 `DownloadPlanner`

实现 `DownloadPlanner` 接口，注册 Bean 并注入自定义 `DownloadTransferSupport`：

```java
@Bean
DownloadTransferSupport downloadTransferSupport() {
    return new DownloadTransferSupport(new MyDownloadPlanner());
}
```

同时需提供使用新 `DownloadTransferSupport` 的 `ServletDownloadWriter` / `ReactiveDownloadWriter` Bean，或复制 `ServiceTransferConfiguration` 装配。

### 7.2 替换 `MalwareScanner`

```java
public final class ClamAvMalwareScanner implements MalwareScanner {
    @Override
    public ScanResult scan(UploadResource resource) {
        // ...
    }
}
```

在 `UploadValidator.builder().malwareScanner(...)` 中使用。

### 7.3 扩展 `BinarySource`

实现 `BinarySource` 供 `DownloadResource` 的 `content()` 工厂返回；`BinarySources.writeTo` 由 writer 调用。

### 7.4 禁止事项

- 在业务 service 模块 import `jakarta.servlet` / `MultipartFile`。
- 绕过 `DownloadResource` 直接写 response。

---

## 8. 相关文档

- [service-transfer README](../../../innospots-nexus-service/innospots-nexus-service-transfer/README.md)
- [HTTP API 接入](http-api-integration.md)
