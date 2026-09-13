package com.innospots.nexus.service.contract.invocation;

/**
 * Default interceptor order constants. Lower values enter first and finish last.
 *
 * @author Smars
 * @date 2026/09/13
 * @see InterceptorIds
 * @see ServiceInterceptor
 */
public final class InterceptorOrders {

    public static final int CONTROL = 0;
    public static final int DEADLINE = 10;
    public static final int AUTHN = 20;
    public static final int AUTHZ = 30;
    public static final int AUDIT = 40;
    public static final int IDEMPOTENCY = 50;
    public static final int RATE_LIMIT = 60;
    public static final int BULKHEAD = 70;
    public static final int CIRCUIT = 80;

    private InterceptorOrders() {
    }
}
