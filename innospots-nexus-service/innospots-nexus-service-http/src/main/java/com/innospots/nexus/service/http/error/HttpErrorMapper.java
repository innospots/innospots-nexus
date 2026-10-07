package com.innospots.nexus.service.http.error;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.error.ServiceError;
import com.innospots.nexus.service.contract.error.ServiceErrorCatalog;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 将 {@link NexusException} 映射为 HTTP 错误响应描述。
 *
 * @author Smars
 * @date 2026/09/15
 * @see ServiceErrorCatalog
 */
public final class HttpErrorMapper {

    private final ServiceErrorCatalog catalog;

    /**
     * 使用标准错误目录创建映射器。
     */
    public HttpErrorMapper() {
        this(ServiceErrorCatalog.standard());
    }

    /**
     * 创建映射器。
     *
     * @param catalog 错误目录
     */
    public HttpErrorMapper(ServiceErrorCatalog catalog) {
        this.catalog = Checks.notNull(catalog, "catalog");
    }

    /**
     * 映射异常。响应已提交时不改写。
     *
     * @param exception         平台异常
     * @param profile           响应 profile
     * @param requestId         请求标识
     * @param traceId           追踪标识，可为空字符串
     * @param retryAfter        可选重试间隔
     * @param responseCommitted 响应是否已提交
     * @return 错误描述
     */
    public ErrorResponseProfile map(
            NexusException exception,
            ResponseProfile profile,
            String requestId,
            String traceId,
            Duration retryAfter,
            boolean responseCommitted) {
        Checks.notNull(exception, "exception");
        Checks.notNull(profile, "profile");
        Checks.notBlank(requestId, "requestId");
        Checks.notNull(traceId, "traceId");
        if (responseCommitted) {
            return ErrorResponseProfile.committed();
        }
        ServiceError error = catalog.resolve(exception.code());
        Map<String, String> headers = headers(error, retryAfter);
        if (profile == ResponseProfile.PROBLEM) {
            return ErrorResponseProfile.problem(
                    error.httpStatus(),
                    toProblem(error, requestId, traceId),
                    headers);
        }
        return ErrorResponseProfile.legacy(
                error.httpStatus(),
                toLegacy(error, exception),
                headers);
    }

    private static R<Void> toLegacy(ServiceError error, NexusException exception) {
        return R.fail(error.code(), error.message(), exception.display());
    }

    private static ProblemDetailVo toProblem(ServiceError error, String requestId, String traceId) {
        return new ProblemDetailVo(
                URI.create("urn:innospots:problem:" + error.code()),
                error.message(),
                error.httpStatus(),
                error.message(),
                URI.create("urn:request:" + requestId),
                error.code(),
                requestId,
                traceId,
                error.retryable(),
                error.details());
    }

    private static Map<String, String> headers(ServiceError error, Duration retryAfter) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (error.httpStatus() == 429 && retryAfter != null && !retryAfter.isNegative() && !retryAfter.isZero()) {
            long seconds = Math.max(1L, (long) Math.ceil(retryAfter.toMillis() / 1000.0));
            headers.put(StandardHeaders.RETRY_AFTER, Long.toString(seconds));
        }
        return headers;
    }
}
