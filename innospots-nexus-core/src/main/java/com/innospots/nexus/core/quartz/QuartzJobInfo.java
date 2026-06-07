package com.innospots.nexus.core.quartz;

import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of a scheduled Quartz job and its triggers.
 *
 * @param jobName   the job name
 * @param jobGroup  the job group
 * @param jobClass  fully qualified class name of the job implementation
 * @param dataMap   job data map (defensively copied)
 * @param triggers  associated trigger info (defensively copied)
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
