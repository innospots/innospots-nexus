# Thread — Thread-Local Context & Executors

## TLC (Thread-Local Context)

**Type:** final class

Typed `ThreadLocal` map for propagating cross-cutting state (trace ID, tenant ID, user ID, project ID, etc.) across asynchronous boundaries.

### Key Constants
- `TRACE_ID = "traceId"`
- `TENANT_ID = "tenantId"`
- `SESSION_ID = "sessionId"`
- `CONVERSATION_ID = "conversationId"`
- `PROJECT_ID = "projectId"`
- `USER_ID = "userId"`
- `USER_NAME = "userName"`

### Compact Accessors
- **Signature:** `projectId(Long projectId) → void` — sets project ID (null removes entry)
- **Signature:** `projectId() → Long` — returns project ID or null
- **Signature:** `userId(Long userId) → void` — sets user ID (null removes entry)
- **Signature:** `userId() → Long` — returns user ID or null
- **Signature:** `userName(String userName) → void` — sets user name (null removes entry)
- **Signature:** `userName() → String` — returns user name or null

### Generic Methods
- **Signature:** `put(String key, Object value) → void`
- **Description:** Sets a context value. Null value removes the key.

- **Signature:** `putAll(Map<String, ?> values) → void`
- **Description:** Puts all entries from map into context. Null values remove keys.

- **Signature:** `get(String key) → Object`

- **Signature:** `getString(String key) → String`
- **Description:** Gets a context value as String, or null.

- **Signature:** `getLong(String key) → Long`
- **Description:** Gets a context value as Long. Supports Long passthrough, Number -> longValue(), String -> parseLong().

- **Signature:** `remove(String key) → void`

- **Signature:** `snapshot() → Map<String, Object>`
- **Description:** Captures a defensive copy of the current context.

- **Signature:** `restore(Map<String, ?> context) → void`
- **Description:** Replaces the entire context with the given map.

- **Signature:** `scope(Map<String, ?> values) → Scope`
- **Description:** Creates a scoped context merging values into current context. Returns a `Scope` that restores previous state on close. Use with try-with-resources.

- **Signature:** `context() → Map<String, Object>`
- **Description:** Returns the raw context map (shared reference).

- **Signature:** `clear() → void`
- **Description:** Removes all context values for the current thread.

### Inner Record: `TLC.Scope(Map<String, Object> previous)`
- **Type:** record implementing `AutoCloseable`
- **Method:** `close()` — restores previous context via `TLC.restore(previous)`

---

## NexusThreadFactory

**Type:** class implementing `ThreadFactory`

Named thread factory producing threads with a configurable prefix and sequence number.

### Constructors
- **Signature:** `NexusThreadFactory(String namePrefix)`
- **Description:** Creates factory with given prefix, daemon=false.

- **Signature:** `NexusThreadFactory(String namePrefix, boolean daemon)`
- **Description:** Creates factory with given prefix and daemon flag. Default prefix: `"nexus-worker"`.

### Methods
- **Signature:** `newThread(Runnable runnable) → Thread`
- **Description:** Creates a new thread with name `{prefix}-{sequence}` and configured daemon flag.

---

## NexusThreadPoolExecutor

**Type:** class extending `ThreadPoolExecutor`

Custom thread pool executor that captures and propagates TLC context from the submitting thread to the worker thread.

### Constructor
- **Signature:** `NexusThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)`

### Methods
- **Signature:** `poolName() → String`
- **Description:** Returns the human-readable pool name.

- **Signature:** `hasAvailableThread() → boolean`
- **Description:** Returns true if at least one thread is available.

- **Signature:** `availableThreadCount() → int`
- **Description:** Returns the number of threads not currently executing tasks.

- **Signature:** `execute(Runnable command) → void`
- **Description:** Executes command with TLC context propagation.

- **Signature:** `submit(Runnable task) → Future<?>`
- **Description:** Submits a Runnable task with TLC propagation.

- **Signature:** `submit(Callable<T> task) → Future<T>`
- **Description:** Submits a Callable task with TLC propagation.

---

## ThreadPoolBuilder

**Type:** final class

Fluent builder for `NexusThreadPoolExecutor` instances.

### Default Constants
- `DEFAULT_QUEUE_CAPACITY = 20_000`
- `DEFAULT_KEEP_ALIVE_SECONDS = 120`

### Static Methods
- **Signature:** `builder(String poolName) → ThreadPoolBuilder`
- **Signature:** `build(int coreSize, int maxSize, int queueCapacity, String poolName) → NexusThreadPoolExecutor`
- **Description:** Convenience method that builds a pool in one call.

### Fluent Builder Methods
- **Signature:** `coreSize(int coreSize) → ThreadPoolBuilder` — sets core pool size (min 1)
- **Signature:** `maxSize(int maxSize) → ThreadPoolBuilder` — sets max pool size (min 1)
- **Signature:** `queueCapacity(int queueCapacity) → ThreadPoolBuilder` — sets work queue capacity (0 or negative uses SynchronousQueue)
- **Signature:** `keepAliveSeconds(int keepAliveSeconds) → ThreadPoolBuilder` — sets keep-alive time in seconds
- **Signature:** `daemon(boolean daemon) → ThreadPoolBuilder` — sets daemon thread flag
- **Signature:** `rejectedExecutionHandler(RejectedExecutionHandler handler) → ThreadPoolBuilder` — sets rejected execution handler (default: CallerRunsPolicy)
- **Signature:** `build() → NexusThreadPoolExecutor` — builds the pool; max size normalized to at least core size

### Package-Private
- **Signature:** `createQueue(int queueCapacity) → BlockingQueue<Runnable>` (static)
- **Description:** Creates an ArrayBlockingQueue if capacity > 0, otherwise a SynchronousQueue.

---

## AsyncExecutors

**Type:** final class

Global async executor facade backed by a singleton `NexusThreadPoolExecutor`. Lazily initialized.

### Methods
- **Signature:** `initialize() → void` (static, synchronized)
- **Description:** Initializes with defaults: core = available processors, queue = 0, pool name = `"nexus-async"`.

- **Signature:** `initialize(int coreSize, int queueCapacity, String poolName) → void` (static, synchronized)
- **Description:** Initializes with explicit config. Closes existing executor if present.

- **Signature:** `submit(Runnable runnable) → Future<?>` (static)
- **Description:** Submits a Runnable to the global executor. Lazily initializes if needed.

- **Signature:** `submit(Callable<T> callable) → Future<T>` (static)
- **Description:** Submits a Callable to the global executor.

- **Signature:** `close() → void` (static, synchronized)
- **Description:** Shuts down and clears the global executor.