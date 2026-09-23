package com.innospots.nexus.service.contract.invocation;

import java.util.concurrent.CompletionStage;

/**
 * 观察流式或异步调用的传输层完成。
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationOutcome
 */
@FunctionalInterface
public interface TransportCompletion {

    /**
     * 返回传输完成时结束的阶段。
     *
     * @return 完成结果
     */
    CompletionStage<InvocationOutcome> completion();
}
