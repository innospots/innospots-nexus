package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明本操作应用<strong>命名超时策略</strong>。
 *
 * <p><strong>用途</strong>：为单次 {@code InvocationEngine} 调用安装操作级超时，并与请求
 * {@link com.innospots.nexus.service.contract.time.Deadline} 取最小值。到期时通过
 * {@link com.innospots.nexus.service.contract.cancellation.CancellationRegistration} 请求协作取消，
 * 映射 {@link com.innospots.nexus.service.contract.status.ServiceStatusCode#DEADLINE_EXCEEDED}（HTTP 504）。</p>
 *
 * <p><strong>配置</strong>：{@link #value()} 是 {@code GovernanceConfig.timeouts} 的键，
 * <strong>不是</strong> {@link java.time.Duration} 字面量。解析后写入
 * {@link com.innospots.nexus.service.contract.policy.OperationPolicy#timeoutPolicyKey()}，
 * 由 {@code TimeoutInterceptor} 查找时长。</p>
 *
 * <p><strong>使用约束</strong>：</p>
 * <ul>
 *   <li>长循环或阻塞 I/O 应检查 {@code ServiceContext.cancellation()}，不能假设线程中断一定生效。</li>
 *   <li>可与 {@link ServiceOperation} 组合：二者键可相同，也可仅用 operationId 查超时表。</li>
 *   <li>容器读 body 超时与操作超时分离，各自映射不同 HTTP 状态。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.time.Deadline
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy#timeoutPolicyKey()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TimeoutProtected {

    /**
     * 超时策略键，映射 {@code GovernanceConfig.timeouts}。
     *
     * @return 非空配置键名（非时长字面量）
     */
    String value();
}
