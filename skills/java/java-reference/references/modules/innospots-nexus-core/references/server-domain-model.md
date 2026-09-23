# 包 `server.domain.model`

## ServiceInfo

**类型：** class

已注册服务节点的流式领域模型。 {@link #serverKey()} 派生为 {@code host:port}，保证集群内唯一。</p>

### 方法

#### `named(String serviceName, String instanceId) → ServiceInfo`

- **说明：** 按服务名与实例标识创建服务。
- **参数：**
  - `serviceName` — 逻辑服务类型名称
  - `instanceId` — 唯一实例标识（如 UUID 或主机名）
- **返回：** 新服务信息

#### `serverId() → Long`

- **说明：** 返回数据库分配的服务标识。

#### `serverId(Long serverId) → ServiceInfo`

- **说明：** 设置数据库分配的服务标识并返回自身以支持链式调用。

#### `serviceName() → String`

- **说明：** 返回逻辑服务类型名称。

#### `instanceId() → String`

- **说明：** 返回唯一实例标识。

#### `host() → String`

- **说明：** 返回主机地址。

#### `host(String host) → ServiceInfo`

- **说明：** 设置主机地址并返回自身以支持链式调用。

#### `port() → int`

- **说明：** 返回端口号。

#### `port(int port) → ServiceInfo`

- **说明：** 设置端口号并返回自身以支持链式调用。

#### `status() → ServiceStatus`

- **说明：** 返回服务状态。

#### `status(ServiceStatus status) → ServiceInfo`

- **说明：** 设置服务状态（{@code null} 时默认为 ONLINE）并返回自身以支持链式调用。

#### `role() → ServiceRole`

- **说明：** 返回服务角色。

#### `role(ServiceRole role) → ServiceInfo`

- **说明：** 设置服务角色（{@code null} 时默认为 FOLLOWER）并返回自身以支持链式调用。

#### `group() → String`

- **说明：** 返回分组名称。

#### `group(String group) → ServiceInfo`

- **说明：** 设置分组名称（{@code null} 时默认为 "default"）并返回自身以支持链式调用。

#### `updatedAt() → LocalDateTime`

- **说明：** 返回最后心跳更新时间。

#### `updatedAt(LocalDateTime updatedAt) → ServiceInfo`

- **说明：** 设置最后心跳更新时间并返回自身以支持链式调用。

#### `serverKey() → String`

- **说明：** 集群范围唯一键：{@code host:port}。

#### `tags(String tags) → ServiceInfo`

- **说明：** 从逗号分隔的 {@code key=value} 对解析并设置标签。 示例：{@code "env=prod,region=us-east-1"}。
- **参数：**
  - `tags` — 标签字符串
- **返回：** 自身以支持链式调用

#### `metrics(Map<String, String> metrics) → ServiceInfo`

- **说明：** 将给定映射合并到运行时指标中。

#### `elapsedSecondsSinceUpdate() → long`

- **说明：** 自最后心跳更新起的秒数，用于过期检测。

## ServiceLifecycle

**类型：** class

本地服务节点的可变生命周期状态。 跟踪节点是否运行、集群键及 Leader 状态。</p>

### 方法

#### `isRunning() → boolean`

- **说明：** 服务仍在运行（未关闭）时返回 {@code true}。

#### `isShutdown() → boolean`

- **说明：** 调用 {@link #shutdown()} 后返回 {@code true}。

#### `shutdown() → void`

- **说明：** 将服务标记为已停止。

#### `serverKey() → String`

- **说明：** 返回集群范围的服务键（{@code host:port}）。

#### `isLeader() → boolean`

- **说明：** 当前节点是否为 Leader 时返回 {@code true}。

#### `isRegistered() → boolean`

- **说明：** 已通过注册分配服务键时返回 {@code true}。
