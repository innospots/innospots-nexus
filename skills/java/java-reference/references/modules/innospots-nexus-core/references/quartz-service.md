# 包 `quartz.service`

## QuartzScheduleManager

**类型：** class

围绕 Quartz Scheduler 的封装，在单一分组内管理作业与触发器，使用内存（RAM）作业存储。 支持四种调度模式：ScheduleMode、ScheduleMode、 ScheduleMode 与 ScheduleMode（拒绝调度）。

### 方法

#### `startup() → void`
- **说明：** 使用默认名称（nexus-quartz-scheduler）、8 线程与默认分组创建管理器。 public QuartzScheduleManager() { this("nexus-quartz-scheduler", 8, DEFAULT_GROUP); } /** 使用给定调度器名称与线程数创建管理器，分组使用默认值。 public QuartzScheduleManager(String schedulerName, int threadCount) { this(schedulerName, threadCount, DEFAULT_GROUP); } /**
- **参数：**
  - `schedulerName` — Quartz 实例名称（空白时默认为 nexus-quartz-scheduler）
  - `threadCount` — 线程池大小（最小为 1）
  - `groupName` — 作业/触发器分组（空白时默认为 NEXUS_QUARTZ）

#### `refreshJob(QuartzJobRequest request) → boolean`
- **说明：** 调度或更新作业。作业已存在时替换并重新调度触发器（幂等）。
- **参数：**
  - `request` — 作业请求
- **返回：** 接受作业时返回 true；模式为 MANUAL 或 Cron 无效时返回 false

#### `deleteJob(String jobName) → boolean`
- **说明：** 删除作业及其关联触发器。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 作业不存在时返回 false

#### `pauseJob(String jobName) → boolean`
- **说明：** 暂停作业。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 作业不存在时返回 false

#### `resumeJob(String jobName) → boolean`
- **说明：** 恢复已暂停的作业。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 作业不存在时返回 false

#### `hasJob(String jobName) → boolean`
- **说明：** 检查作业是否存在。
- **参数：**
  - `jobName` — 作业名称
- **返回：** 存在时返回 true

#### `scheduleJobs() → Set<String>`
- **说明：** 返回配置分组内全部作业名称。
- **返回：** 作业名称集合

#### `schedulerInfo() → List<QuartzJobInfo>`
- **说明：** 返回配置分组内全部作业的详细信息。
- **返回：** 作业信息列表

#### `latestUpdateTime() → LocalDateTime`
- **说明：** 返回最后一次作业调度变更的时间戳。
- **返回：** 最近更新时间

#### `shutdown() → void`
- **说明：** 停止调度器、清空全部作业并释放资源。 可安全多次调用。 / public void shutdown()
