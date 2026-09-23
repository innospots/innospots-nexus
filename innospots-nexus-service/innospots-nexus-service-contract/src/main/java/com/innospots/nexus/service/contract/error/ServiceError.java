package com.innospots.nexus.service.contract.error;

import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * 用于传输映射的框架中性错误描述。
 *
 * @param code       九位状态码
 * @param httpStatus 传输状态
 * @param message    稳定双语摘要
 * @param retryable  是否适合重试
 * @param details    额外安全字段
 * @author Smars
 * @date 2026/09/13
 * @see ServiceErrorCatalog
 */
public record ServiceError(
        String code,
        int httpStatus,
        String message,
        boolean retryable,
        Map<String, Object> details
) {

    public ServiceError {
        Checks.notBlank(code, "code");
        Checks.notBlank(message, "message");
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
