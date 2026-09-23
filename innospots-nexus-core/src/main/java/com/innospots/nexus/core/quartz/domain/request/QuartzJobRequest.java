package com.innospots.nexus.core.quartz.domain.request;

import com.innospots.nexus.core.quartz.domain.enums.ScheduleMode;
import org.quartz.Job;

import java.util.Date;
import java.util.Map;

/**
 * 调度 Quartz 作业的请求；构造时校验必填字段。
 * <p>可使用静态工厂方法便捷创建常见调度类型：{@link #cron}、{@link #once}、{@link #scheduled}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @param jobName        唯一作业名称（必填、非空白）
 * @param jobClass       Quartz Job 实现类（必填）
 * @param scheduleMode   调度模式（必填）
 * @param cronExpression Cron 表达式（CRON/SCHEDULED 模式必填）
 * @param startTime      首次触发时间（可选；ONCE 模式默认为当前时间）
 * @param endTime        SCHEDULED 模式的结束时间
 * @param dataMap        作业数据映射（防御性复制）
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

    /**
     * 创建 Cron 触发作业请求。
     *
     * @param jobName        作业名称
     * @param jobClass       作业类
     * @param cronExpression Cron 表达式
     * @param dataMap        作业数据
     * @return 作业请求
     */
    public static QuartzJobRequest cron(
            String jobName,
            Class<? extends Job> jobClass,
            String cronExpression,
            Map<String, ?> dataMap
    ) {
        return new QuartzJobRequest(jobName, jobClass, ScheduleMode.CRON, cronExpression, null, null, dataMap);
    }

    /**
     * 创建在指定开始时间触发一次的作业请求。
     *
     * @param jobName   作业名称
     * @param jobClass  作业类
     * @param startTime 开始时间
     * @param dataMap   作业数据
     * @return 作业请求
     */
    public static QuartzJobRequest once(
            String jobName,
            Class<? extends Job> jobClass,
            Date startTime,
            Map<String, ?> dataMap
    ) {
        return new QuartzJobRequest(jobName, jobClass, ScheduleMode.ONCE, null, startTime, null, dataMap);
    }

    /**
     * 创建带 Cron 表达式与可选结束时间的调度作业请求。
     *
     * @param jobName        作业名称
     * @param jobClass       作业类
     * @param cronExpression Cron 表达式
     * @param endTime        结束时间
     * @param dataMap        作业数据
     * @return 作业请求
     */
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
