/**
 * <strong>操作超时</strong>：拦截器与 adapter 可插拔的取消安装 SPI。
 *
 * <p>{@link com.innospots.nexus.service.governance.timeout.TimeoutInterceptor} id
 * {@code governance.timeout}，order 15（deadline 之后、认证之前）。
 * 有效超时为 {@code min(策略超时, deadline.remaining())}。
 * 具体如何调度取消由 {@link com.innospots.nexus.service.governance.timeout.OperationTimeoutArmer}
 * 在 Spring/Quarkus adapter 中实现。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.timeout.TimeoutInterceptor
 * @see com.innospots.nexus.service.governance.timeout.OperationTimeoutArmer
 */
package com.innospots.nexus.service.governance.timeout;
