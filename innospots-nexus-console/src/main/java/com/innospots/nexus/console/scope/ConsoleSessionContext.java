package com.innospots.nexus.console.scope;

import com.innospots.nexus.console.scope.service.SessionScopeTlc;

/**
 * 当前线程的控制台会话归属层级（委托 {@link SessionScopeTlc}）。
 */
public final class ConsoleSessionContext {

    private ConsoleSessionContext() {
    }

    public static void bindScope(ConsoleSessionScope scope) {
        SessionScopeTlc.putSessionScope(scope);
    }

    /**
     * 返回显式绑定的会话层级，未绑定时按安全域与工作区快照推断，并与 securityRealm 保持一致。
     */
    public static ConsoleSessionScope scope() {
        return SessionScopeTlc.sessionScope();
    }

    public static ConsoleSessionScope inferFromSession() {
        return SessionScopeTlc.inferSessionScopeFromSnapshots();
    }
}
