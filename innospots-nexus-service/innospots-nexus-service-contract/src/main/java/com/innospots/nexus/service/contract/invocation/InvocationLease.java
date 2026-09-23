package com.innospots.nexus.service.contract.invocation;

import java.util.concurrent.CompletionStage;

/**
 * 自 {@link ServiceInterceptor#enter(InvocationContext)} 获取的租约。必须完成一次。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceInterceptor
 * @see InvocationOutcome
 */
@FunctionalInterface
public interface InvocationLease {

    /**
     * 以 {@code outcome} 完成租约。运行时忽略重复完成。
     *
     * @param outcome 终态结果
     * @return 完成阶段
     */
    CompletionStage<Void> finish(InvocationOutcome outcome);
}
