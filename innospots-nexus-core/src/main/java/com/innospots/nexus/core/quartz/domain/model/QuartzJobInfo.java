package com.innospots.nexus.core.quartz.domain.model;

import java.util.List;
import java.util.Map;

/**
 * 已调度 Quartz 作业及其触发器的不可变快照。
 *
 * @author Smars
 * @date 2026/09/13
 * @param jobName  作业名称
 * @param jobGroup 作业分组
 * @param jobClass 作业实现类的全限定名
 * @param dataMap  作业数据映射（防御性复制）
 * @param triggers 关联触发器信息（防御性复制）
 */
public record QuartzJobInfo(
        String jobName,
        String jobGroup,
        String jobClass,
        Map<String, Object> dataMap,
        List<QuartzTriggerInfo> triggers
) {

    public QuartzJobInfo {
        dataMap = dataMap == null ? Map.of() : Map.copyOf(dataMap);
        triggers = triggers == null ? List.of() : List.copyOf(triggers);
    }
}
