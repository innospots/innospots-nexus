package com.innospots.nexus.service.contract.error;

import java.util.LinkedHashMap;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 将已注册状态目录解析为 {@link ServiceError} 值。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceError
 * @see ServiceStatusCode
 */
public final class ServiceErrorCatalog {

    private final Map<String, StatusCode> codes;

    private ServiceErrorCatalog(Map<String, StatusCode> codes) {
        this.codes = Map.copyOf(codes);
    }

    /**
     * 从平台与服务状态枚举构建默认目录。
     *
     * @return 覆盖 AIO 与 SRV 码的目录
     */
    public static ServiceErrorCatalog standard() {
        return of(concat(NexusStatusCode.values(), ServiceStatusCode.values()));
    }

    /**
     * 从给定状态码构建目录。
     *
     * @param statuses 待注册的状态码
     * @return 目录
     */
    public static ServiceErrorCatalog of(StatusCode... statuses) {
        Checks.notNull(statuses, "statuses");
        Map<String, StatusCode> codes = new LinkedHashMap<>();
        for (StatusCode status : statuses) {
            String fullCode = status.fullCode();
            if (codes.containsKey(fullCode)) {
                throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
            }
            codes.put(fullCode, status);
        }
        return new ServiceErrorCatalog(codes);
    }

    /**
     * 将完整码解析为安全错误。未知码映射为 {@link NexusStatusCode#SYSTEM_ERROR}。
     *
     * @param fullCode 机器可读码
     * @return 错误描述
     */
    public ServiceError resolve(String fullCode) {
        StatusCode status = codes.get(fullCode);
        if (status == null) {
            status = NexusStatusCode.SYSTEM_ERROR;
        }
        return new ServiceError(
                status.fullCode(),
                status.httpStatusCode(),
                status.summary(),
                retryable(status),
                Map.of());
    }

    private static boolean retryable(StatusCode status) {
        if (status instanceof ServiceStatusCode serviceStatus) {
            return serviceStatus.retryable();
        }
        return status == NexusStatusCode.LIMIT_EXCEEDED;
    }

    private static StatusCode[] concat(StatusCode[] first, StatusCode[] second) {
        StatusCode[] merged = new StatusCode[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }
}
