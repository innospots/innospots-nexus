package com.innospots.nexus.core.quartz.domain.model;

import java.util.Date;

/**
 * Quartz 触发器当前状态的不可变快照。
 *
 * @author Smars
 * @date 2026/09/13
 * @param triggerName  触发器名称
 * @param triggerGroup 触发器分组
 * @param startTime    首次计划触发时间
 * @param nextFireTime 下次预期触发时间（未调度时为 {@code null}）
 * @param endTime      结束时间（无结束时为 {@code null}）
 * @param finalFireTime 最后一次触发时间（尚未触发时为 {@code null}）
 * @param calendarName 日历名称（无日历时为 {@code null}）
 * @param mayFireAgain 触发器是否可能再次触发
 */
public record QuartzTriggerInfo(
        String triggerName,
        String triggerGroup,
        Date startTime,
        Date nextFireTime,
        Date endTime,
        Date finalFireTime,
        String calendarName,
        boolean mayFireAgain
) {
}
