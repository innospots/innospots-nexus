package com.innospots.nexus.base.exception;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCode;

import java.util.Objects;

/**
 * 平台基础运行时异常。携带机器可读的错误码（见 {@link StatusCode}）、
 * 人类可读的消息、可选的国际化展示信息，以及（使用 {@link StatusCode}
 * 构建时）原始状态码，供 HTTP 层按状态码声明的
 * {@link StatusCode#httpStatusCode()} 解析响应状态。
 * <p>请使用静态工厂方法（{@link #build(StatusCode)} 和
 * {@link #build(String, String)}）而非直接调用构造函数。使用原始错误码
 * 构建时异常不携带状态码，HTTP 层回退到平台码解析。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see StatusCode
 * @see com.innospots.nexus.base.status.NexusStatusCode
 */
public class NexusException extends RuntimeException {

    private final String code;
    private final I18nObject display;
    private final StatusCode statusCode;

    private NexusException(String code, String message, I18nObject display,
                           StatusCode statusCode, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.display = display;
        this.statusCode = statusCode;
    }

    /**
     * 基于 {@link StatusCode} 构建异常，使用其完整码与摘要消息。
     *
     * @param statusCode 状态码
     * @return 异常实例
     */
    public static NexusException build(StatusCode statusCode) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary(), null, code, null);
    }

    /**
     * 基于 {@link StatusCode} 构建异常，附带国际化展示消息。
     *
     * @param statusCode 状态码
     * @param display    国际化展示信息
     * @return 异常实例
     */
    public static NexusException build(StatusCode statusCode, I18nObject display) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary(), display, code, null);
    }

    /**
     * 基于 {@link StatusCode} 构建异常，附带展示消息与原始原因。
     *
     * @param statusCode 状态码
     * @param display    国际化展示信息
     * @param cause      原始异常
     * @return 异常实例
     */
    public static NexusException build(StatusCode statusCode, I18nObject display, Throwable cause) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary(), display, code, cause);
    }

    /**
     * 基于 {@link StatusCode} 构建异常，使用覆盖消息。
     *
     * @param statusCode 状态码，提供机器可读错误码
     * @param message    人类可读消息；空白时回退到状态码摘要
     * @return 携带状态码完整码的异常
     */
    public static NexusException build(StatusCode statusCode, String message) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        String resolved = message == null || message.isBlank() ? code.summary() : message;
        return new NexusException(code.fullCode(), resolved, null, code, null);
    }

    /**
     * 基于 {@link StatusCode} 构建异常，附带原始原因。
     *
     * @param statusCode 状态码，提供机器可读错误码与摘要
     * @param cause      原始失败原因
     * @return 携带状态码完整码的异常
     */
    public static NexusException build(StatusCode statusCode, Throwable cause) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary(), null, code, cause);
    }

    /**
     * 使用机器可读错误码与人类可读消息构建异常。
     *
     * @param code    错误码
     * @param message 消息
     * @return 异常实例
     */
    public static NexusException build(String code, String message) {
        return new NexusException(code, message, null, null, null);
    }

    /**
     * 使用错误码、消息与原始原因构建异常。
     *
     * @param code    错误码
     * @param message 消息
     * @param cause   原始异常
     * @return 异常实例
     */
    public static NexusException build(String code, String message, Throwable cause) {
        return new NexusException(code, message, null, null, cause);
    }

    /**
     * 返回机器可读错误码。
     *
     * @return 错误码
     */
    public String code() {
        return code;
    }

    /**
     * 返回国际化展示消息，可能为 {@code null}。
     *
     * @return 展示信息
     */
    public I18nObject display() {
        return display;
    }

    /**
     * 返回构建异常时携带的状态码；使用原始错误码构建时为 {@code null}。
     *
     * <p>HTTP 层据此解析响应状态，使产品域状态码（非 {@code AIO*}
     * 平台码）也能返回其声明的 {@link StatusCode#httpStatusCode()}。</p>
     *
     * @return 状态码；未携带时返回 {@code null}
     */
    public StatusCode statusCode() {
        return statusCode;
    }
}
