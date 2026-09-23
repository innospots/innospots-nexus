package com.innospots.nexus.service.observability.logging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * 将 {@link ServiceContext} 字段桥接到 SLF4J MDC。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class MdcContextBridge {

    /**
     * 安装 MDC 字段并返回恢复句柄。
     *
     * @param context 服务上下文
     * @return 恢复句柄
     */
    public MdcSnapshot install(ServiceContext context) {
        Checks.notNull(context, "context");
        Map<String, String> previous = MDC.getCopyOfContextMap();
        MDC.put("requestId", context.requestId());
        MDC.put("traceId", context.trace().traceId());
        MDC.put("spanId", context.trace().spanId());
        MDC.put("principalId", context.security().id());
        MDC.put("operation", context.request().path());
        return new MdcSnapshot(previous);
    }

    /**
     * MDC 快照。
     *
     * @param previous 先前 MDC 映射
     */
    public record MdcSnapshot(Map<String, String> previous) {

        /**
         * 恢复先前 MDC 状态。
         */
        public void restore() {
            if (previous == null || previous.isEmpty()) {
                MDC.clear();
                return;
            }
            MDC.setContextMap(new HashMap<>(previous));
        }
    }
}
