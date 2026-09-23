package com.innospots.nexus.core.quartz.enums;

/**
 * Quartz Cron 转换使用的常见调度周期单位。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum TimePeriod {

    /** 按分钟间隔重复，例如每 5 分钟。 */
    MINUTE,

    /** 按小时间隔或一天内选定小时重复。 */
    HOUR,

    /** 按天间隔重复，通常在固定时刻触发。 */
    DAY,

    /** 在选定星期几重复，通常在固定时刻触发。 */
    WEEK,

    /** 在选定月中日期重复，通常在固定时刻触发。 */
    MONTH
}
