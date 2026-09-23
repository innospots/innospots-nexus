# 包 `quartz.domain.enums`

## ScheduleMode

**类型：** enum

Quartz 作业的调度模式。

### 枚举常量

| 常量 | 说明 |
|------|------|
| `ONCE` | 在指定开始时间触发一次。 |
| `SCHEDULED` | 按 Cron 调度直至结束时间。 |
| `CRON` | 按 Cron 无限期调度。 |
| `MANUAL` | 仅手动触发，不自动调度。 |
