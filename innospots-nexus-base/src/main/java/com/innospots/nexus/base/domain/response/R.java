package com.innospots.nexus.base.domain.response;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * 通用 API 响应包装器，包含成功/失败状态、结果码、消息、可选数据载荷，
 * 以及失败时供前端渲染的国际化展示消息。
 *
 * @author Smars
 * @date 2026/09/13
 * @param <T> 数据载荷类型
 * @see StatusCode
 * @see NexusException
 */
public record R<T>(
        boolean success,
        String code,
        String message,
        T data,
        I18nObject display
) {

    public static final String OK = "OK";

    /**
     * 返回数据为 null 的成功响应。
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 返回包装给定数据的成功响应。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(true, OK, OK, data, null);
    }

    /**
     * 返回带错误码与消息的失败响应。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String code, String message) {
        return new R<>(false, code, message, null, null);
    }

    /**
     * 返回带错误码、消息与数据载荷的失败响应。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    数据载荷
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String code, String message, T data) {
        return new R<>(false, code, message, data, null);
    }

    /**
     * 返回带错误码、消息与前端展示信息的失败响应。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param display 国际化展示信息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String code, String message, I18nObject display) {
        return new R<>(false, code, message, null, display);
    }

    /**
     * 返回带错误码、消息、数据与展示信息的失败响应。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    数据载荷
     * @param display 国际化展示信息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String code, String message, T data, I18nObject display) {
        return new R<>(false, code, message, data, display);
    }

    /**
     * 基于 {@link StatusCode} 返回失败响应。
     *
     * @param statusCode 提供码、摘要与展示信息的状态码
     * @param <T>        数据类型
     * @return 无数据的失败响应
     */
    public static <T> R<T> fail(StatusCode statusCode) {
        return fail(statusCode, null);
    }

    /**
     * 基于 {@link StatusCode} 返回带载荷的失败响应。
     *
     * @param statusCode 提供码、摘要与展示信息的状态码
     * @param data       可选数据载荷
     * @param <T>        数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(StatusCode statusCode, T data) {
        StatusCode code = Checks.notNull(statusCode, "statusCode");
        return new R<>(false, code.fullCode(), code.summary(), data, code.message());
    }

    /**
     * 将 {@link NexusException} 映射为失败响应。
     *
     * @param exception 平台异常
     * @param <T>       数据类型
     * @return 携带异常码、消息与展示信息的失败响应
     */
    public static <T> R<T> from(NexusException exception) {
        NexusException error = Checks.notNull(exception, "exception");
        return new R<>(false, error.code(), error.getMessage(), null, error.display());
    }
}
