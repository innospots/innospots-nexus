package com.innospots.nexus.service.contract.cancellation;

/**
 * {@link CancellationToken#onCancel(java.util.function.Consumer)} 返回的注册句柄。
 * 关闭操作幂等。
 *
 * @author Smars
 * @date 2026/09/13
 * @see CancellationToken
 */
@FunctionalInterface
public interface CancellationRegistration extends AutoCloseable {

    /**
     * 注销监听器。重复关闭无效果。
     */
    @Override
    void close();
}
