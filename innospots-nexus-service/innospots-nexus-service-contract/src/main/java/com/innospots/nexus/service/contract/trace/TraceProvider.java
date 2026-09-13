package com.innospots.nexus.service.contract.trace;

import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * Starts child spans for an invocation. Adapters call this; business code uses {@code @Traced}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see TraceHandle
 * @see TraceSnapshot
 */
public interface TraceProvider {

    /**
     * Starts a span for {@code invocation} under {@code parent}.
     *
     * @param invocation current invocation
     * @param parent     parent snapshot, possibly empty
     * @return handle that must be finished
     */
    TraceHandle start(InvocationContext invocation, TraceSnapshot parent);
}
