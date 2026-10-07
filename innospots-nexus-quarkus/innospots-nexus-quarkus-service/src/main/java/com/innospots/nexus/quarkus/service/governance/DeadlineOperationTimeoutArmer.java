package com.innospots.nexus.quarkus.service.governance;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.quarkus.service.config.DeadlineSchedulerProducer;
import com.innospots.nexus.quarkus.service.rest.ServiceRequestLifecycleAccessor;
import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.governance.timeout.OperationTimeoutArmer;
import com.innospots.nexus.service.runtime.time.DeadlineScheduler;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * 通过请求级取消源安装操作超时。
 */
@ApplicationScoped
public final class DeadlineOperationTimeoutArmer implements OperationTimeoutArmer {

    private final DeadlineScheduler scheduler;
    private final ServiceRequestLifecycleAccessor lifecycleAccessor;

    public DeadlineOperationTimeoutArmer(
            DeadlineSchedulerProducer deadlineSchedulerProducer,
            ServiceRequestLifecycleAccessor lifecycleAccessor) {
        this.scheduler = Checks.notNull(deadlineSchedulerProducer.scheduler(), "scheduler");
        this.lifecycleAccessor = Checks.notNull(lifecycleAccessor, "lifecycleAccessor");
    }

    @Override
    public CancellationRegistration arm(InvocationContext invocation, Duration timeout) {
        Checks.notNull(invocation, "invocation");
        Checks.notNull(timeout, "timeout");
        var cancellationSource = lifecycleAccessor.requireCurrent().cancellationSource();
        Deadline deadline = Deadline.of(Ticker.system(), timeout);
        return scheduler.schedule(deadline, cancellationSource);
    }
}
