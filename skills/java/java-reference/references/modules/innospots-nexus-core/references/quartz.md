# Quartz（`core.quartz`）

面向平台中立的定时任务的内存 Quartz 调度器包装器。

## QuartzScheduleManager

**类型：** class

- 默认组：`NEXUS_QUARTZ`
- RAM 作业存储（`StdSchedulerFactory`）
- 在单个调度器实例内管理作业/触发器

典型宿主用法：每个应用或每个有界子系统一个管理器。

## ScheduleMode

| 值 | 含义 |
|-------|---------|
| `ONCE` | 在 `startTime`（默认当前时间）触发一次 |
| `CRON` | 无限期 cron 调度 |
| `SCHEDULED` | 在 `startTime` 与 `endTime` 之间的 cron 调度 |
| `MANUAL` | 永不自动调度 |

## QuartzJobRequest

**类型：** record

静态工厂：`cron(...)`、`once(...)`、`scheduled(...)`。

| 字段 | 说明 |
|-------|-------|
| `jobName` | 组内唯一 |
| `jobClass` | `org.quartz.Job` 实现 |
| `scheduleMode` | 必填 |
| `cronExpression` | `CRON` / `SCHEDULED` 时必填 |
| `dataMap` | 防御性复制的作业数据 |

作业实现类位于**上层模块**（kernel/platform/plugin）；core 仅提供调度基础设施。

## QuartzJobInfo / QuartzTriggerInfo

**类型：** class

用于列出作业和触发器的读模型（名称、组、cron、下次触发时间、状态）。

## CronConverter / TimePeriod

**类型：** class / enum

从命名周期构建或解析 cron 表达式的辅助工具。
