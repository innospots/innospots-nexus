package com.innospots.nexus.core.quartz;

import org.quartz.Job;

import java.util.Date;
import java.util.Map;

/**
 * Request to schedule a Quartz job. Validates required fields at construction.
 * <p>Use the static factory methods for convenient creation of common
 * schedule types: {@link #cron}, {@link #once}, {@link #scheduled}.</p>
 *
 * @param jobName        unique job name (required, non-blank)
 * @param jobClass       Quartz Job implementation class (required)
 * @param scheduleMode   scheduling mode (required)
 * @param cronExpression cron expression (required for CRON/SCHEDULED modes)
 * @param startTime      first fire time (optional, defaults to now for ONCE)
 * @param endTime        end time for SCHEDULED mode
 * @param dataMap        job data map (defensively copied)
 */
public record QuartzJobRequest(
        String jobName,
        Class<? extends Job> jobClass,
        ScheduleMode scheduleMode,
        String cronExpression,
        Date startTime,
        Date endTime,
        Map<String, ?> dataMap
) {

    public QuartzJobRequest {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName must not be blank");
        }
        if (jobClass == null) {
            throw new IllegalArgumentException("jobClass must not be null");
        }
        if (scheduleMode == null) {
            throw new IllegalArgumentException("scheduleMode must not be null");
        }
        dataMap = dataMap == null ? Map.of() : Map.copyOf(dataMap);
    }

    /** Creates a cron-triggered job request. */
    public static QuartzJobRequest cron(
            String jobName,
            Class<? extends Job> jobClass,
            String cronExpression,
            Map<String, ?> dataMap
    ) {
        return new QuartzJobRequest(jobName, jobClass, ScheduleMode.CRON, cronExpression, null, null, dataMap);
    }

    /** Creates a one-shot job request that fires once at the given start time. */
    public static QuartzJobRequest once(
            String jobName,
            Class<? extends Job> jobClass,
            Date startTime,
            Map<String, ?> dataMap
    ) {
        return new QuartzJobRequest(jobName, jobClass, ScheduleMode.ONCE, null, startTime, null, dataMap);
    }

    /** Creates a scheduled job request with a cron expression and optional end time. */
    public static QuartzJobRequest scheduled(
            String jobName,
            Class<? extends Job> jobClass,
            String cronExpression,
            Date endTime,
            Map<String, ?> dataMap
    ) {
        return new QuartzJobRequest(jobName, jobClass, ScheduleMode.SCHEDULED, cronExpression, null, endTime, dataMap);
    }
}
