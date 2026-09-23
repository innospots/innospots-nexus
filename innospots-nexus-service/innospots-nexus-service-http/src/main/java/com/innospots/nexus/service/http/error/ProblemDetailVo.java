package com.innospots.nexus.service.http.error;

import java.net.URI;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * RFC 9457 problem detail HTTP 表示。
 *
 * @param type       问题类型 URI
 * @param title      稳定标题
 * @param status     HTTP 状态
 * @param detail     安全详情
 * @param instance   请求实例 URI
 * @param code       九位状态码
 * @param requestId  请求标识
 * @param traceId    追踪标识
 * @param retryable  是否可重试
 * @param details    额外安全字段
 * @author Smars
 * @date 2026/09/15
 * @see HttpErrorMapper
 */
public record ProblemDetailVo(
        URI type,
        String title,
        int status,
        String detail,
        URI instance,
        String code,
        String requestId,
        String traceId,
        boolean retryable,
        Map<String, Object> details
) {

    public ProblemDetailVo {
        Checks.notNull(type, "type");
        Checks.notBlank(title, "title");
        Checks.notBlank(detail, "detail");
        Checks.notNull(instance, "instance");
        Checks.notBlank(code, "code");
        Checks.notBlank(requestId, "requestId");
        Checks.notNull(traceId, "traceId");
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
