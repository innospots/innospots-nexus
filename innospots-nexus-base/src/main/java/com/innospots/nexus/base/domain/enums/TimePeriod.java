package com.innospots.nexus.base.domain.enums;

/**
 * Common scheduling period units used by platform time configuration.
 * <p>
 * The enum is intentionally infrastructure-neutral so base domain models can
 * describe a period without depending on Quartz or any scheduler implementation.
 * </p>
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
