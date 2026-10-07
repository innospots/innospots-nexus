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
 * 有限截止时间到期时调度取消。
 *
 * @author Smars
 * @date 2026/09/13
 * @see Deadline
 * @see CancellationSource
 */
public final class DeadlineScheduler {

    private final ScheduledExecutorService executor;

    /**
     * 使用 {@code executor} 创建调度器。
     *
     * @param executor 调度执行器
     */
    public DeadlineScheduler(ScheduledExecutorService executor) {
        this.executor = Checks.notNull(executor, "executor");
    }

    /**
     * {@code deadline} 到期时取消 {@code source}。无限制截止时间被忽略。
     *
     * @param deadline 待监视截止时间
     * @param source   取消写侧
     * @return 可取消已调度任务的注册句柄
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
