package com.innospots.nexus.service.http.error;

import java.util.Map;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.policy.ResponseProfile;

/**
 * HTTP 错误响应描述。已提交响应不可改写。
 *
 * @author Smars
 * @date 2026/09/15
 * @see HttpErrorMapper
 */
public final class ErrorResponseProfile {

    private final ResponseProfile profile;
    private final int httpStatus;
    private final R<Void> legacyBody;
    private final ProblemDetailVo problemBody;
    private final Map<String, String> headers;
    private final boolean committed;

    private ErrorResponseProfile(
            ResponseProfile profile,
            int httpStatus,
            R<Void> legacyBody,
            ProblemDetailVo problemBody,
            Map<String, String> headers,
            boolean committed) {
        this.profile = profile;
        this.httpStatus = httpStatus;
        this.legacyBody = legacyBody;
        this.problemBody = problemBody;
        this.headers = Map.copyOf(headers);
        this.committed = committed;
    }

    /**
     * 返回已提交、不可改写的占位描述。
     *
     * @return 已提交描述
     */
    public static ErrorResponseProfile committed() {
        return new ErrorResponseProfile(ResponseProfile.LEGACY, 0, null, null, Map.of(), true);
    }

    /**
     * 创建 legacy {@link R} 错误描述。
     *
     * @param httpStatus HTTP 状态
     * @param body       legacy 响应体
     * @param headers    附加响应头
     * @return 错误描述
     */
    public static ErrorResponseProfile legacy(int httpStatus, R<Void> body, Map<String, String> headers) {
        Checks.notNull(body, "body");
        return new ErrorResponseProfile(ResponseProfile.LEGACY, httpStatus, body, null, headers, false);
    }

    /**
     * 创建 RFC 9457 problem 错误描述。
     *
     * @param httpStatus HTTP 状态
     * @param body       problem 响应体
     * @param headers    附加响应头
     * @return 错误描述
     */
    public static ErrorResponseProfile problem(int httpStatus, ProblemDetailVo body, Map<String, String> headers) {
        Checks.notNull(body, "body");
        return new ErrorResponseProfile(ResponseProfile.PROBLEM, httpStatus, null, body, headers, false);
    }

    /**
     * 返回配置的响应 profile。
     *
     * @return profile
     */
    public ResponseProfile profile() {
        return profile;
    }

    /**
     * 返回 HTTP 状态码。
     *
     * @return 状态码
     */
    public int httpStatus() {
        return httpStatus;
    }

    /**
     * 返回 legacy 响应体。
     *
     * @return legacy 体，非 legacy 时为空
     */
    public R<Void> legacyBody() {
        return legacyBody;
    }

    /**
     * 返回 problem 响应体。
     *
     * @return problem 体，非 problem 时为空
     */
    public ProblemDetailVo problemBody() {
        return problemBody;
    }

    /**
     * 返回附加响应头。
     *
     * @return 不可变头映射
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * 响应是否已提交且不可改写。
     *
     * @return 已提交时为 {@code true}
     */
    public boolean isCommitted() {
        return committed;
    }

    /**
     * 是否允许写入错误响应。
     *
     * @return 可写入时为 {@code true}
     */
    public boolean writable() {
        return !committed;
    }
}
