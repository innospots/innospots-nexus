package com.innospots.nexus.service.contract.cancellation;

import java.util.Optional;
import java.util.function.Consumer;

import com.innospots.nexus.base.util.Checks;

/**
 * 当前调用的只读取消信号。
 *
 * @author Smars
 * @date 2026/09/13
 * @see CancellationReason
 * @see CancellationRegistration
 */
public interface CancellationToken {

    /**
     * 返回是否已请求取消。
     *
     * @return 已取消时为 {@code true}
     */
    boolean isCancelled();

    /**
     * 已取消时返回取消原因。
     *
     * @return 原因，未取消时为空
     */
    Optional<CancellationReason> reason();

    /**
     * 注册取消时最多调用一次的监听器。若已取消，则在调用线程立即执行。
     *
     * @param listener 取消回调
     * @return 可关闭以取消订阅的注册句柄
     */
    CancellationRegistration onCancel(Consumer<CancellationReason> listener);

    /**
     * 返回永不被取消的令牌。
     *
     * @return 永不取消的令牌
     */
    static CancellationToken none() {
        return NeverCancelledToken.INSTANCE;
    }
}

final class NeverCancelledToken implements CancellationToken {

    static final NeverCancelledToken INSTANCE = new NeverCancelledToken();

    private NeverCancelledToken() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public Optional<CancellationReason> reason() {
        return Optional.empty();
    }

    @Override
    public CancellationRegistration onCancel(Consumer<CancellationReason> listener) {
        Checks.notNull(listener, "listener");
        return () -> {
        };
    }
}
