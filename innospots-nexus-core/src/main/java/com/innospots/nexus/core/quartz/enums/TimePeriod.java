package com.innospots.nexus.core.quartz.enums;

/**
 * Common scheduling period units used by Quartz cron conversion.
 */
public enum TimePeriod {

    /** Repeat by minute interval, for example every 5 minutes. */
    MINUTE,

    /** Repeat by hour interval or selected hours in a day. */
    HOUR,

    /** Repeat by day interval, usually at a fixed time of day. */
    DAY,

    /** Repeat on selected days of week, usually at a fixed time of day. */
    WEEK,

    /** Repeat on selected days of month, usually at a fixed time of day. */
    MONTH
}
