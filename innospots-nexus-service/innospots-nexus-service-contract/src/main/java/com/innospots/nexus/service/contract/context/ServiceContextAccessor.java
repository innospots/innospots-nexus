package com.innospots.nexus.service.contract.context;

import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 当前 {@link ServiceContext} 的宿主注入访问器。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceContext
 */
@FunctionalInterface
public interface ServiceContextAccessor {

    /**
     * 已绑定时返回当前上下文。
     *
     * @return 当前上下文，未绑定时为空
     */
    Optional<ServiceContext> current();

    /**
     * 返回当前上下文，未绑定时失败。
     *
     * @return 当前上下文
     * @throws NexusException 无可用上下文时
     */
    default ServiceContext requireCurrent() {
        return current().orElseThrow(() -> NexusException.build(ServiceStatusCode.CONTEXT_UNAVAILABLE));
    }
}
