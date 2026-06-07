package com.innospots.nexus.core.quartz;

import java.util.Date;

/**
 * Immutable snapshot of a Quartz trigger's current state.
 *
 * @param triggerName  trigger name
 * @param triggerGroup trigger group
 * @param startTime    first scheduled fire time
 * @param nextFireTime next expected fire time (null if not scheduled)
 * @param endTime      end time (null if no end)
 * @param finalFireTime last fire time (null if not yet fired)
 * @param calendarName calendar name (null if none)
 * @param mayFireAgain whether the trigger may fire again
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
