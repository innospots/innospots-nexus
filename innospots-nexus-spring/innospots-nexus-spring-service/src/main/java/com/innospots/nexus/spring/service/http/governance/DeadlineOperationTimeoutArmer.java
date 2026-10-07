package com.innospots.nexus.spring.service.http.governance;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.governance.timeout.OperationTimeoutArmer;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;
import com.innospots.nexus.service.runtime.time.DeadlineScheduler;
import com.innospots.nexus.spring.service.http.mvc.ServiceRequestLifecycleAccessor;

/**
 * 通过请求级 {@link CancellationSource} 安装操作超时。
 */
public final class DeadlineOperationTimeoutArmer implements OperationTimeoutArmer {

    private final DeadlineScheduler scheduler;
    private final ServiceRequestLifecycleAccessor lifecycleAccessor;

    /**
     * 创建安装器。
     *
     * @param scheduler           截止时间调度器
     * @param lifecycleAccessor   请求生命周期访问器
     */
    public DeadlineOperationTimeoutArmer(DeadlineScheduler scheduler, ServiceRequestLifecycleAccessor lifecycleAccessor) {
        this.scheduler = Checks.notNull(scheduler, "scheduler");
        this.lifecycleAccessor = Checks.notNull(lifecycleAccessor, "lifecycleAccessor");
    }

    @Override
    public CancellationRegistration arm(InvocationContext invocation, Duration timeout) {
        Checks.notNull(invocation, "invocation");
        Checks.notNull(timeout, "timeout");
        CancellationSource cancellationSource = lifecycleAccessor.requireCurrent().cancellationSource();
        Deadline deadline = Deadline.of(Ticker.system(), timeout);
        return scheduler.schedule(deadline, cancellationSource);
    }
}
