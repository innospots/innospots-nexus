package com.innospots.nexus.core.quartz;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper around a Quartz {@link Scheduler} that manages jobs and triggers
 * within a single group, using in-memory (RAM) job storage.
 * <p>Supports four schedule modes: {@link ScheduleMode#ONCE},
 * {@link ScheduleMode#CRON}, {@link ScheduleMode#SCHEDULED}, and
 * {@link ScheduleMode#MANUAL} (rejected).</p>
 */
public class QuartzScheduleManager {

    public static final String DEFAULT_GROUP = "NEXUS_QUARTZ";

    private final String schedulerName;
    private final int threadCount;
    private final String groupName;
    private volatile Scheduler scheduler;
    private volatile LocalDateTime updateTime;

    /** Creates a manager with default name ({@code nexus-quartz-scheduler}), 8 threads, and default group. */
    public QuartzScheduleManager() {
        this("nexus-quartz-scheduler", 8, DEFAULT_GROUP);
    }

    /** Creates a manager with the given scheduler name and thread count, using the default group. */
    public QuartzScheduleManager(String schedulerName, int threadCount) {
        this(schedulerName, threadCount, DEFAULT_GROUP);
    }

    /**
     * @param schedulerName Quartz instance name (defaults to {@code nexus-quartz-scheduler} if blank)
     * @param threadCount   thread pool size (minimum 1)
     * @param groupName     job/trigger group (defaults to {@code NEXUS_QUARTZ} if blank)
     */
    public QuartzScheduleManager(String schedulerName, int threadCount, String groupName) {
        this.schedulerName = schedulerName == null || schedulerName.isBlank() ? "nexus-quartz-scheduler" : schedulerName;
        this.threadCount = Math.max(1, threadCount);
        this.groupName = groupName == null || groupName.isBlank() ? DEFAULT_GROUP : groupName;
    }

    /**
     * Lazily initialises and starts the Quartz scheduler (double-checked locking).
     * Idempotent — safe to call multiple times.
     */
    public void startup() {
        try {
            Scheduler current = scheduler;
            if (current == null) {
                synchronized (this) {
                    current = scheduler;
                    if (current == null) {
                        current = createScheduler();
                        scheduler = current;
                    }
                }
            }
            if (!current.isStarted()) {
                current.start();
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to start Quartz scheduler", e);
        }
    }

    /**
     * Schedules or updates a job. If the job already exists, it is
     * replaced and its trigger is rescheduled (idempotent).
     *
     * @return true if the job was accepted, false if the schedule mode
     *         is MANUAL or the cron expression is invalid
     */
    public boolean refreshJob(QuartzJobRequest request) {
        validateStarted();
        if (!isScheduleAllowed(request)) {
            return false;
        }
        try {
            JobKey jobKey = jobKey(request.jobName());
            TriggerKey triggerKey = triggerKey(request.jobName());
            JobDetail jobDetail = buildJobDetail(request, jobKey);
            Trigger trigger = buildTrigger(request, jobDetail, triggerKey);

            if (scheduler.checkExists(jobKey)) {
                scheduler.addJob(jobDetail, true, true);
                scheduler.rescheduleJob(triggerKey, trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            touch();
            return true;
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to refresh Quartz job: " + request.jobName(), e);
        }
    }

    /** Deletes a job and its associated trigger. Returns false if the job does not exist. */
    public boolean deleteJob(String jobName) {
        validateStarted();
        try {
            boolean deleted = scheduler.deleteJob(jobKey(jobName));
            if (deleted) {
                touch();
            }
            return deleted;
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to delete Quartz job: " + jobName, e);
        }
    }

    /** Pauses a job. Returns false if the job does not exist. */
    public boolean pauseJob(String jobName) {
        validateStarted();
        try {
            JobKey key = jobKey(jobName);
            if (!scheduler.checkExists(key)) {
                return false;
            }
            scheduler.pauseJob(key);
            touch();
            return true;
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to pause Quartz job: " + jobName, e);
        }
    }

    /** Resumes a paused job. Returns false if the job does not exist. */
    public boolean resumeJob(String jobName) {
        validateStarted();
        try {
            JobKey key = jobKey(jobName);
            if (!scheduler.checkExists(key)) {
                return false;
            }
            scheduler.resumeJob(key);
            touch();
            return true;
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to resume Quartz job: " + jobName, e);
        }
    }

    /** Checks if a job exists. */
    public boolean hasJob(String jobName) {
        validateStarted();
        try {
            return scheduler.checkExists(jobKey(jobName));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to check Quartz job: " + jobName, e);
        }
    }

    /** Returns the set of all job names in the configured group. */
    public Set<String> scheduleJobs() {
        validateStarted();
        try {
            return scheduler.getJobKeys(GroupMatcher.groupEquals(groupName)).stream()
                    .map(JobKey::getName)
                    .collect(Collectors.toCollection(java.util.TreeSet::new));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to list Quartz jobs", e);
        }
    }

    /** Returns detailed info for all jobs in the configured group. */
    public List<QuartzJobInfo> schedulerInfo() {
        validateStarted();
        try {
            List<QuartzJobInfo> infos = new ArrayList<>();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(groupName))) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<QuartzTriggerInfo> triggers = scheduler.getTriggersOfJob(jobKey).stream()
                        .map(this::toTriggerInfo)
                        .sorted(Comparator.comparing(QuartzTriggerInfo::triggerName))
                        .toList();
                infos.add(new QuartzJobInfo(
                        jobKey.getName(),
                        jobKey.getGroup(),
                        jobDetail.getJobClass().getName(),
                        new LinkedHashMap<>(jobDetail.getJobDataMap()),
                        triggers
                ));
            }
            infos.sort(Comparator.comparing(QuartzJobInfo::jobName));
            return infos;
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to read Quartz scheduler info", e);
        }
    }

    /** Returns the timestamp of the last job schedule change. */
    public LocalDateTime latestUpdateTime() {
        return updateTime;
    }

    /**
     * Stops the scheduler, clears all jobs, and releases resources.
     * Safe to call multiple times.
     */
    public void shutdown() {
        Scheduler current = scheduler;
        if (current == null) {
            return;
        }
        try {
            if (!current.isShutdown()) {
                current.clear();
                current.shutdown(true);
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to shutdown Quartz scheduler", e);
        } finally {
            scheduler = null;
        }
    }

    private Scheduler createScheduler() throws SchedulerException {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", schedulerName);
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        return new StdSchedulerFactory(properties).getScheduler();
    }

    private JobDetail buildJobDetail(QuartzJobRequest request, JobKey jobKey) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.putAll(request.dataMap());
        return JobBuilder.newJob(request.jobClass())
                .withIdentity(jobKey)
                .usingJobData(dataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(QuartzJobRequest request, JobDetail jobDetail, TriggerKey triggerKey) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(triggerKey);

        if (request.startTime() != null) {
            builder.startAt(request.startTime());
        }
        if (request.endTime() != null) {
            builder.endAt(request.endTime());
        }

        if (ScheduleMode.ONCE.equals(request.scheduleMode())) {
            Date startTime = request.startTime() == null ? new Date() : request.startTime();
            return builder.startAt(startTime)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                    .build();
        }
        return builder.withSchedule(CronScheduleBuilder.cronSchedule(request.cronExpression())).build();
    }

    private QuartzTriggerInfo toTriggerInfo(Trigger trigger) {
        return new QuartzTriggerInfo(
                trigger.getKey().getName(),
                trigger.getKey().getGroup(),
                trigger.getStartTime(),
                trigger.getNextFireTime(),
                trigger.getEndTime(),
                trigger.getFinalFireTime(),
                trigger.getCalendarName(),
                trigger.mayFireAgain()
        );
    }

    /** Returns false for MANUAL mode or invalid cron expressions. */
    private boolean isScheduleAllowed(QuartzJobRequest request) {
        if (ScheduleMode.MANUAL.equals(request.scheduleMode())) {
            return false;
        }
        if (ScheduleMode.ONCE.equals(request.scheduleMode())) {
            return true;
        }
        return request.cronExpression() != null && CronExpression.isValidExpression(request.cronExpression());
    }

    private void validateStarted() {
        try {
            if (scheduler == null || scheduler.isShutdown() || !scheduler.isStarted()) {
                throw new IllegalStateException("Quartz scheduler is not started");
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("Quartz scheduler is not started", e);
        }
    }

    private JobKey jobKey(String jobName) {
        return JobKey.jobKey(jobName, groupName);
    }

    private TriggerKey triggerKey(String jobName) {
        return TriggerKey.triggerKey(jobName, groupName);
    }

    private void touch() {
        updateTime = LocalDateTime.now();
    }
}
