package com.innospots.nexus.spring.service.http.invocation;

/**
 * 防止 {@link GovernedInvocationAspect} 与 {@link ServiceInvocationBridge} 嵌套重复进入引擎。
 */
public final class GovernedInvocationGuard {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private GovernedInvocationGuard() {
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }

    public static void enter() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void leave() {
        int next = DEPTH.get() - 1;
        if (next <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(next);
        }
    }
}
