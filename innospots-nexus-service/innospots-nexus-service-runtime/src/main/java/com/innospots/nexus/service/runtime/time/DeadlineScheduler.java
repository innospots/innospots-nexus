package com.innospots.nexus.service.runtime.time;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;

/**
 * Schedules cancellation when a finite deadline elapses.
 *
 * @author Smars
 * @date 2026/09/13
 * @see Deadline
 * @see CancellationSource
 */
public final class DeadlineScheduler {

    private final ScheduledExecutorService executor;

    /**
     * Creates a scheduler using {@code executor}.
     *
     * @param executor scheduler
     */
    public DeadlineScheduler(ScheduledExecutorService executor) {
        this.executor = Checks.notNull(executor, "executor");
    }

    /**
     * Cancels {@code source} when {@code deadline} expires. Unlimited deadlines are ignored.
     *
     * @param deadline deadline to watch
     * @param source   cancellation write-side
     * @return registration that cancels the scheduled task
     */
    public CancellationRegistration schedule(Deadline deadline, CancellationSource source) {
        Checks.notNull(deadline, "deadline");
        Checks.notNull(source, "source");
        if (deadline.isUnlimited() || deadline.isExpired()) {
            if (deadline.isExpired()) {
                source.cancel(CancellationReason.DEADLINE_EXCEEDED);
            }
            return () -> {
            };
        }
        Duration remaining = deadline.remaining();
        ScheduledFuture<?> future = executor.schedule(
                () -> source.cancel(CancellationReason.DEADLINE_EXCEEDED),
                remaining.toNanos(),
                TimeUnit.NANOSECONDS);
        return () -> future.cancel(false);
    }
}
