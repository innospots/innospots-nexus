package com.innospots.nexus.core.quartz.service;

import com.innospots.nexus.core.quartz.domain.enums.ScheduleMode;
import com.innospots.nexus.core.quartz.domain.model.QuartzJobInfo;
import com.innospots.nexus.core.quartz.domain.model.QuartzTriggerInfo;
import com.innospots.nexus.core.quartz.domain.request.QuartzJobRequest;
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
 * 围绕 Quartz {@link Scheduler} 的封装，在单一分组内管理作业与触发器，使用内存（RAM）作业存储。
 * <p>支持四种调度模式：{@link ScheduleMode#ONCE}、{@link ScheduleMode#CRON}、
 * {@link ScheduleMode#SCHEDULED} 与 {@link ScheduleMode#MANUAL}（拒绝调度）。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see ScheduleMode
 */
public class QuartzScheduleManager {

    public static final String DEFAULT_GROUP = "NEXUS_QUARTZ";

    private final String schedulerName;
    private final int threadCount;
    private final String groupName;
    private volatile Scheduler scheduler;
    private volatile LocalDateTime updateTime;

    /** 使用默认名称（{@code nexus-quartz-scheduler}）、8 线程与默认分组创建管理器。 */
    public QuartzScheduleManager() {
        this("nexus-quartz-scheduler", 8, DEFAULT_GROUP);
    }

    /** 使用给定调度器名称与线程数创建管理器，分组使用默认值。 */
    public QuartzScheduleManager(String schedulerName, int threadCount) {
        this(schedulerName, threadCount, DEFAULT_GROUP);
    }

    /**
     * @param schedulerName Quartz 实例名称（空白时默认为 {@code nexus-quartz-scheduler}）
     * @param threadCount   线程池大小（最小为 1）
     * @param groupName     作业/触发器分组（空白时默认为 {@code NEXUS_QUARTZ}）
     */
    public QuartzScheduleManager(String schedulerName, int threadCount, String groupName) {
        this.schedulerName = schedulerName == null || schedulerName.isBlank() ? "nexus-quartz-scheduler" : schedulerName;
        this.threadCount = Math.max(1, threadCount);
        this.groupName = groupName == null || groupName.isBlank() ? DEFAULT_GROUP : groupName;
    }

    /**
     * 延迟初始化并启动 Quartz 调度器（双重检查锁定）。
     * 幂等，可安全多次调用。
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
     * 调度或更新作业。作业已存在时替换并重新调度触发器（幂等）。
     *
     * @param request 作业请求
     * @return 接受作业时返回 {@code true}；模式为 MANUAL 或 Cron 无效时返回 {@code false}
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

    /**
     * 删除作业及其关联触发器。
     *
     * @param jobName 作业名称
     * @return 作业不存在时返回 {@code false}
     */
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

    /**
     * 暂停作业。
     *
     * @param jobName 作业名称
     * @return 作业不存在时返回 {@code false}
     */
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

    /**
     * 恢复已暂停的作业。
     *
     * @param jobName 作业名称
     * @return 作业不存在时返回 {@code false}
     */
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

    /**
     * 检查作业是否存在。
     *
     * @param jobName 作业名称
     * @return 存在时返回 {@code true}
     */
    public boolean hasJob(String jobName) {
        validateStarted();
        try {
            return scheduler.checkExists(jobKey(jobName));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to check Quartz job: " + jobName, e);
        }
    }

    /**
     * 返回配置分组内全部作业名称。
     *
     * @return 作业名称集合
     */
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

    /**
     * 返回配置分组内全部作业的详细信息。
     *
     * @return 作业信息列表
     */
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

    /**
     * 返回最后一次作业调度变更的时间戳。
     *
     * @return 最近更新时间
     */
    public LocalDateTime latestUpdateTime() {
        return updateTime;
    }

    /**
     * 停止调度器、清空全部作业并释放资源。
     * 可安全多次调用。
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

    /** MANUAL 模式或 Cron 无效时返回 {@code false}。 */
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
