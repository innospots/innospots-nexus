package com.innospots.nexus.service.contract.invocation;

/**
 * Default interceptor identifiers used by runtime and governance adapters.
 *
 * @author Smars
 * @date 2026/09/13
 * @see InterceptorOrders
 * @see ServiceInterceptor
 */
public final class InterceptorIds {

    public static final String CONTROL = "runtime.control";
    public static final String DEADLINE = "runtime.deadline";
    public static final String AUTHN = "runtime.authn";
    public static final String AUTHZ = "runtime.authz";
    public static final String AUDIT = "runtime.audit";
    public static final String IDEMPOTENCY = "runtime.idempotency";
    public static final String RATE_LIMIT = "governance.ratelimit";
    public static final String BULKHEAD = "governance.bulkhead";
    public static final String CIRCUIT = "governance.circuit";

    private InterceptorIds() {
    }
}
