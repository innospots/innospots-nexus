package com.innospots.nexus.core.quartz.domain.enums;

/**
 * Quartz 作业的调度模式。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum ScheduleMode {
    /** 在指定开始时间触发一次。 */
    ONCE,
    /** 按 Cron 调度直至结束时间。 */
    SCHEDULED,
    /** 按 Cron 无限期调度。 */
    CRON,
    /** 仅手动触发，不自动调度。 */
    MANUAL
}
