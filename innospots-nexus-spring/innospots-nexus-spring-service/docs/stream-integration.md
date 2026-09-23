# 流式 Stream 接入手册（Spring）

面向**后端开发**：如何在 Spring 里接 SSE 流、在代码里**持续往流里写数据**、在调用**大模型 / Chat 等流式下游**时把片段写给前端，以及**正常结束 / 失败 / 用户断开**怎么处理。

---

## 1. 先记住三件事

1. **对外**：Controller 方法返回 `StreamSession<你的事件类型>`，并声明 `produces = text/event-stream`。不要自己往 `HttpServletResponse` 里拼 SSE。
2. **写流**：在 Service（或专门的生产逻辑）里对 `StreamSession` 反复调用 `emit`，写完调用 `complete()`；出错用 `fail()`；用户关掉页面要能停下来（看 `isCancelled()`）。
3. **对内**：别的 Service 不要为了推流再去调一遍本服务的 HTTP 地址；应**注入同一个写流 Service**，或把 `StreamSession` / `StreamSink` 当参数传进去。

启用方式：在应用主类加 `@EnableNexusServiceStream`（HTTP 须 `@EnableNexusServiceHttp`）。或 `@EnableNexusService` 一次启用全部。

---

## 2. 一条请求里发生了什么

```text
客户端 GET /chat/stream  (Accept: text/event-stream)
    → Filter 建立 requestId、用户上下文、CancellationToken（用户断开会取消）
    → Controller 返回 StreamSession
    → 你在后台线程里 session.emit(...) 写事件
    → Adapter 把事件编成 SSE 发给浏览器
    → 你调用 session.complete() 后，连接在排空队列后关闭
```

`StreamManager.open(事件Class)` 会创建会话并绑定**当前登录用户和租户**；只有 adapter 订阅后，浏览器才能收到数据。

---

## 3. 标准对接步骤（从零开始）

### 3.1 定义事件（每条 SSE 里 `data` 的 JSON 形状）

```java
public record ChatStreamEvent(String role, String content, boolean finished) {}
```

### 3.2 Controller：只负责开门并交给 Service

```java
@RestController
@RequestMapping("/api/chat")
public class ChatStreamResource {

    private final ChatStreamService chatStreamService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamSession<ChatStreamEvent> stream(@RequestBody ChatRequest request) {
        return chatStreamService.startStream(request);
    }
}
```

### 3.3 Service：open 会话 + 启动写流任务 + 把 session 还给 Spring

```java
@Service
public class ChatStreamService {

    private final StreamManager streamManager;
    private final LlmStreamingClient llmClient; // 你们的 Chat 流式客户端

    public StreamSession<ChatStreamEvent> startStream(ChatRequest request) {
        StreamSession<ChatStreamEvent> session =
                (StreamSession<ChatStreamEvent>) streamManager.open(ChatStreamEvent.class);

        // 在后台跑写流；生产环境请用线程池或 ContextExecutor，不要无界 new Thread
        Thread worker = new Thread(() -> pumpFromLlm(session, request), "chat-stream");
        worker.setDaemon(true);
        worker.start();

        return session; // 先返回，客户端才能开始收 SSE
    }

    private void pumpFromLlm(StreamSession<ChatStreamEvent> session, ChatRequest request) {
        try {
            // 约定：streamCompletion 在「最后一个 chunk 回调执行完」之后才返回（同步阻塞或内部 await 流结束）
            llmClient.streamCompletion(request, chunk -> {
                if (session.isCancelled()) {
                    return false;
                }
                session.emit("message.delta", new ChatStreamEvent("assistant", chunk.text(), false))
                        .toCompletableFuture()
                        .join(); // 生产侧建议等待入队结果，避免紧接着 complete 时与最后几条 emit 竞态
                return true;
            });
            if (session.isCancelled()) {
                return; // 用户已断开，不必 complete（会话已由取消链路收尾）
            }
            session.emit("message", new ChatStreamEvent("assistant", "", true))
                    .toCompletableFuture()
                    .join();
            session.complete().toCompletableFuture().join();
        } catch (NexusException ex) {
            session.fail(ex);
        } catch (Exception ex) {
            session.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
        }
    }
}
```

要点：

- **`emit` 可以很多次**，每次对应 SSE 里一个 `event`（上面用了 `message.delta` / `message`）。
- **先 `return session`，再在后台写**；否则浏览器会一直等到你有第一条数据才连上（还受 `subscribe-timeout` 限制）。
- 循环里务必 **`session.isCancelled()`**，用户刷新或关 tab 时会为 true。

---

## 4. 持续写流：你该调哪些 API

| 你想做的事 | 调用 | 说明 |
|------------|------|------|
| 推一条数据 | `session.emit(payload)` 或 `session.emit("事件名", payload)` | 默认事件名是 `message` |
| 推模型增量 token | `session.emit("message.delta", new ChatStreamEvent(...))` | 与前端约定 event 名即可 |
| 正常结束 | `session.complete()` | 会尽力发 `stream.completed`，再关连接 |
| 业务失败 | `session.fail(NexusException.build(...))` | 会发 `error` 事件，不要用裸 Exception |
| 主动取消（如用户点停止） | `session.cancel(CancellationReason.APPLICATION_CANCELLED)` | 与 complete 不同，表示未正常完成 |
| 用户是否已断开 | `session.isCancelled()` | 为 true 时不要再 emit，并停止下游 |

`emit` 返回 `CompletionStage`：队列满（默认 `REJECT`）时会失败，关键路径建议 `whenComplete` 里打日志或降级。

---

## 5. Chat / 流式下游：下游是 `Flux` 或 `Flow` 时怎么接

对外接口不变：Controller 仍返回 `StreamSession<ChatStreamEvent>`。差别只在 **Service 里如何把下游流里的元素搬进 session**。

```text
Controller  return session
                 ▲
ChatStreamService │ open + 订阅下游 Flux/Flow
                 │
LlmStreamingClient.stream(request)  →  Flux<LlmChunk> 或  Flow.Publisher<LlmChunk>
```

下面用同一套类型贯穿示例：

```java
public record LlmChunk(String delta) {}

public record ChatStreamEvent(String role, String content, boolean finished) {}
```

---

### 5.1 下游客户端：`WebClient` 返回 `Flux`（最常见）

```java
@Component
public class LlmStreamingClient {

    private final WebClient webClient;

    public LlmStreamingClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://llm.example").build();
    }

    /**
     * 流式 Chat 补全：每个 JSON 行或 SSE 段解析为一个 LlmChunk。
     */
    public Flux<LlmChunk> stream(ChatRequest request) {
        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(LlmChunk.class);
    }
}
```

---

### 5.2 对接方式一：`Flux` → `Flow` + `StreamPublisherRelay`（代码最少）

适合：**所有片段用同一个 SSE 事件名**（如 `message.delta`），是否结束用 `ChatStreamEvent.finished` 字段区分。

```java
@Service
public class ChatStreamService {

    private final StreamManager streamManager;
    private final LlmStreamingClient llmClient;

    public StreamSession<ChatStreamEvent> startStream(ChatRequest request) {
        StreamSession<ChatStreamEvent> session =
                (StreamSession<ChatStreamEvent>) streamManager.open(ChatStreamEvent.class);

        Flux<ChatStreamEvent> mapped = llmClient.stream(request)
                .takeWhile(chunk -> !session.isCancelled())
                .map(chunk -> new ChatStreamEvent("assistant", chunk.delta(), false))
                .concatWith(Mono.fromSupplier(() -> new ChatStreamEvent("assistant", "", true)));

        Flow.Publisher<ChatStreamEvent> flow = FlowAdapters.toPublisher(mapped);
        StreamPublisherRelay.relay(flow, session, "message.delta");

        return session;
    }
}
```

依赖：`org.reactivestreams:reactive-streams`（Spring WebFlux 已带）里的 `org.reactivestreams.FlowAdapters`。

`StreamPublisherRelay` 行为：

- 上游每个元素 → `session.emit("message.delta", event)`
- 上游 `onComplete` → `session.complete()`
- 上游 `onError` → `session.fail(...)`

**注意**：`relay` 会立刻 `subscribe` 上游；因此要先 `open(session)`，再 `relay`，最后 `return session`。不要在 `relay` 之后再对同一个 `Flux` 二次订阅。

---

### 5.3 对接方式二：直接订阅 `Flux`（要不同 event 名或要取消下游时用）

适合：增量用 `message.delta`，结束单独 `message`；或要在 `session.isCancelled()` 时 `dispose()` 掉 WebClient 请求。

```java
@Service
public class ChatStreamService {

    private final StreamManager streamManager;
    private final LlmStreamingClient llmClient;

    public StreamSession<ChatStreamEvent> startStream(ChatRequest request) {
        StreamSession<ChatStreamEvent> session =
                (StreamSession<ChatStreamEvent>) streamManager.open(ChatStreamEvent.class);

        AtomicReference<Disposable> upstream = new AtomicReference<>();

        Disposable disposable = llmClient.stream(request)
                .takeWhile(chunk -> !session.isCancelled())
                .concatMap(chunk -> Mono.fromCompletionStage(
                        session.emit(
                                "message.delta",
                                new ChatStreamEvent("assistant", chunk.delta(), false))))
                .then(Mono.defer(() -> {
                    if (session.isCancelled()) {
                        return Mono.empty();
                    }
                    return Mono.fromCompletionStage(
                            session.emit("message", new ChatStreamEvent("assistant", "", true)));
                }))
                .then(Mono.fromCompletionStage(session.complete()))
                .subscribe(
                        unused -> { },
                        error -> {
                            if (error instanceof NexusException nexus) {
                                session.fail(nexus);
                            } else {
                                session.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
                            }
                        });

        upstream.set(disposable);

        // 可选：在另一线程轮询或注册回调，用户断连时取消 WebClient
        // if (session.isCancelled()) upstream.get().dispose();

        return session;
    }
}
```

要点：

- 用 `concatMap` + `Mono.fromCompletionStage(session.emit(...))` 保证**按顺序入队**，避免 `emit` 与 `complete` 抢跑。
- `subscribe` 的错误路径走 `session.fail`；正常结束由最后的 `session.complete()` 收尾。
- 用户关页面后 `takeWhile` 会停掉上游；若需更快释放连接，在检测到 `session.isCancelled()` 时对 `Disposable.dispose()`。

**MVC 应用**里通常没有全局 `block()`；在后台线程里订阅 `Flux` 是正常做法（与 §3.3 的 worker 线程同类）。不要在 Servlet 请求线程上对 WebClient `block()`。

---

### 5.4 对接方式三：下游已是 `Flow.Publisher`（无 Reactor）

客户端只提供 `Flow.Publisher<LlmChunk>` 时，不必先转成 `Flux`；在 `open` 之后手写一层 `Subscriber`（与 `StreamPublisherRelay` 同类，只是多了 map）：

```java
public StreamSession<ChatStreamEvent> startStream(ChatRequest request) {
    StreamSession<ChatStreamEvent> session =
            (StreamSession<ChatStreamEvent>) streamManager.open(ChatStreamEvent.class);

    llmClient.stream(request).subscribe(new Flow.Subscriber<LlmChunk>() {
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(LlmChunk chunk) {
            if (session.isCancelled()) {
                subscription.cancel();
                return;
            }
            session.emit("message.delta", new ChatStreamEvent("assistant", chunk.delta(), false))
                    .whenComplete((ignored, error) -> {
                        if (error != null) {
                            subscription.cancel();
                            session.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
                            return;
                        }
                        subscription.request(1);
                    });
        }

        @Override
        public void onError(Throwable throwable) {
            if (throwable instanceof NexusException nexus) {
                session.fail(nexus);
            } else {
                session.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
            }
        }

        @Override
        public void onComplete() {
            if (session.isCancelled()) {
                return;
            }
            session.emit("message", new ChatStreamEvent("assistant", "", true))
                    .thenCompose(ignored -> session.complete());
        }
    });

    return session;
}
```

若事件类型单一、不需要结尾单独 `message`，可把 `onComplete` 里改成直接 `session.complete()`，并全程 `StreamPublisherRelay.relay(llmClient.stream(request), session, "message.delta")`（需先把 `LlmChunk` map 成 `ChatStreamEvent` 的包装 Publisher，或让 relay 的泛型与 chunk 类型一致）。

---

### 5.5 Controller（三种方式共用）

```java
@RestController
@RequestMapping("/api/chat")
public class ChatStreamResource {

    private final ChatStreamService chatStreamService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamSession<ChatStreamEvent> stream(@RequestBody ChatRequest request) {
        return chatStreamService.startStream(request);
    }
}
```

---

### 5.6 选型对照

| 方式 | 何时用 |
|------|--------|
| **5.2 Relay + FlowAdapters** | 下游是 `Flux`，事件名单一，想少写订阅代码 |
| **5.3 订阅 Flux** | 多种 SSE `event` 名、要严格顺序 `emit`、要 `dispose` 取消 WebClient |
| **5.4 Flow.Publisher** | 下游 API 只有 JDK Flow，且不想引入 Reactor |

共同约束：

- 一个下游 `Flux` / `Publisher` **只订阅一次**。
- 先 `open`，再订阅，再 `return session`。
- 结束用 `complete()` / 异常用 `fail()`，含义见 §6、§7。

---

### 5.7 在别的 Service 里写同一条流（不经过 HTTP）

两种等价做法，二选一即可：

**做法 A — 把 session 传进去（最简单）**

```java
// Controller
return chatStreamService.startStream(request);

// ChatStreamService
public StreamSession<ChatStreamEvent> startStream(ChatRequest request) {
    StreamSession<ChatStreamEvent> session = openSession();
    chatOrchestrator.run(request, session); // 编排层里持续 session.emit
    return session;
}
```

**做法 B — 只知道 sessionId，在别的 Bean 里补推**

创建 session 时记下 `session.sessionId()`，在**同一用户、同一租户**上下文里：

```java
streamManager.emit(sessionId, "message.delta", event);
```

适合「任务跑在队列 Worker，HTTP 连接仍挂在原请求」的场景；不要乱把 sessionId 给未授权调用方。

---

## 6. `complete()` 会不会「立刻关连接」？

**不会立刻掐断 TCP。** `complete()` 的含义是：**我不再生产新事件了**，由框架：

1. 把已在队列里的事件继续发给浏览器（排空缓冲）；
2. 尽力写出 `stream.completed` 一类终态帧；
3. 再由 adapter 关闭 SSE 连接。

因此要在**所有需要下发的 `emit` 都成功入队（建议 `emit(...).toCompletableFuture().join()`）之后**再调 `complete()`。  
若 `complete()` 之后还 `emit`，会失败（会话已进入结束流程）。

`complete()` 返回的 `CompletionStage` 表示**本地会话与通道收尾**完成，仍不等于用户浏览器一定已经读完（网络慢时可能还在传）。

## 7. 结束与异常：什么时候用哪个

```text
下游正常结束、内容已全部 emit
    → session.complete()

下游或本服务抛出业务错误（要告诉前端错误码）
    → session.fail(NexusException.build(你的 StatusCode))

用户关页面 / 网络断开
    → 不必 complete；isCancelled() 为 true，停止 emit 并关掉下游即可

业务上用户点「停止生成」
    → session.cancel(APPLICATION_CANCELLED)，并取消 Chat 客户端
```

不要在 `fail` 之后再 `complete`；不要用户已断开后还长时间跑大模型（浪费资源）。

---

## 8. 和「老写法」的关系

若项目里还有 **`SseEmitter`（MVC）** 或 **`Flux<ServerSentEvent>`（WebFlux）**，可以短期保留，但要：

- 用 `ServiceContextAccessor` 取 `cancellation()`，循环里判断是否断开；
- MVC 写失败时调 `ServiceServletFilter.cancelIfClientDisconnected(request)`；
- WebFlux 在 `doOnCancel` 里调 `ServiceWebFilter.cancelIfClientDisconnected(exchange)`。

测试夹具：`SampleStreamResource`、`SampleWebFluxStreamResource`。

**迁移目标**：写流逻辑改为对 `StreamSession.emit`，Controller 改返回 `StreamSession`，删掉手写 SSE 格式。

---

## 9. 配置与容量（按需）

默认由 `StreamConfig.defaults()` 提供；可在应用里自定义 `@Bean StreamConfig`（队列长度、心跳、idle 超时等）。`StreamManager` 在未自定义时会自动使用你的 `StreamConfig`。

| 常见问题 | 说明 |
|----------|------|
| 刷新页面 | 旧连接断开，必须重新请求并重新 `open`；不会自动续上次的 session |
| 发太快队列满 | 默认拒绝新 `emit`，需降速或调大 `bufferSize` |
| 长时间无业务数据 | 受 `idleTimeout` 限制；心跳不刷新 idle |

---

## 10. 需要限流 / 熔断时

在 **Service 方法**上标 `@RateLimited` 等，并通过 `ServiceInvocationBridge.invokeStream` 包一层返回的 `Flow.Publisher`（与同步 `invoke` 用法类似）。详见 [治理接入](governance-integration.md)。

---

## 11. 自测

```bash
mvn -pl innospots-nexus-spring/innospots-nexus-spring-service -am test
```

关注 `StreamSseScenario`、`StreamCancelScenario`。

---

## 12. 延伸阅读

- [service-stream README](../../../innospots-nexus-service/innospots-nexus-service-stream/README.md)（会话状态、时序图）
- [HTTP 接入](http-api-integration.md)（Filter 与上下文）
- [观测接入](observability-integration.md)（流式指标）
