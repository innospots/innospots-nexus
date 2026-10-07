package com.innospots.nexus.service.runtime.invocation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * 逻辑结果、工作终止与传输完成的 CAS 终态持有者。
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationEngine
 * @see InvocationOutcome
 */
public final class InvocationControl {

    private final AtomicReference<InvocationOutcome> logicalOutcome = new AtomicReference<>();
    private final AtomicBoolean workTerminated = new AtomicBoolean();
    private final AtomicBoolean transportCompleted = new AtomicBoolean();

    /**
     * 最多一次完成调用方可见结果。
     *
     * @param outcome 逻辑结果
     * @return 本次调用存储结果时为 {@code true}
     */
    public boolean completeLogical(InvocationOutcome outcome) {
        Checks.notNull(outcome, "outcome");
        return logicalOutcome.compareAndSet(null, outcome);
    }

    /**
     * 最多一次标记工作终止。
     *
     * @return 本次调用执行终止时为 {@code true}
     */
    public boolean terminateWork() {
        return workTerminated.compareAndSet(false, true);
    }

    /**
     * 最多一次标记传输完成。
     *
     * @return 本次调用记录传输完成时为 {@code true}
     */
    public boolean completeTransport() {
        return transportCompleted.compareAndSet(false, true);
    }

    /**
     * 完成时返回逻辑结果。
     *
     * @return 结果，未完成时为空
     */
    public Optional<InvocationOutcome> logicalOutcome() {
        return Optional.ofNullable(logicalOutcome.get());
    }
}
