package com.innospots.nexus.core.quartz;

/** Scheduling modes for Quartz jobs. */
public enum ScheduleMode {
    /** Fires once at a specified start time. */
    ONCE,
    /** Fires on a cron schedule until an end time. */
    SCHEDULED,
    /** Fires indefinitely on a cron schedule. */
    CRON,
    /** Manual trigger only — never scheduled automatically. */
    MANUAL
}
