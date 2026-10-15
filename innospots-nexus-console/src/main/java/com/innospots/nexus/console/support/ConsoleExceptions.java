package com.innospots.nexus.console.support;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console 模块统一的 {@link NexusException} 构建入口，抛出前记录 error 日志（不含堆栈）。
 *
 * @author Smars
 * @date 2026/10/15
 */
public final class ConsoleExceptions {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleExceptions.class);

    private ConsoleExceptions() {
    }

    /**
     * @see NexusException#build(StatusCode)
     */
    public static NexusException build(StatusCode statusCode) {
        return logAndReturn(NexusException.build(statusCode));
    }

    /**
     * @see NexusException#build(StatusCode, I18nObject)
     */
    public static NexusException build(StatusCode statusCode, I18nObject display) {
        return logAndReturn(NexusException.build(statusCode, display));
    }

    /**
     * @see NexusException#build(StatusCode, I18nObject, Throwable)
     */
    public static NexusException build(StatusCode statusCode, I18nObject display, Throwable cause) {
        return logAndReturn(NexusException.build(statusCode, display, cause));
    }

    /**
     * @see NexusException#build(StatusCode, String)
     */
    public static NexusException build(StatusCode statusCode, String message) {
        return logAndReturn(NexusException.build(statusCode, message));
    }

    /**
     * @see NexusException#build(StatusCode, Throwable)
     */
    public static NexusException build(StatusCode statusCode, Throwable cause) {
        return logAndReturn(NexusException.build(statusCode, cause));
    }

    /**
     * @see NexusException#build(String, String)
     */
    public static NexusException build(String code, String message) {
        return logAndReturn(NexusException.build(code, message));
    }

    /**
     * @see NexusException#build(String, String, Throwable)
     */
    public static NexusException build(String code, String message, Throwable cause) {
        return logAndReturn(NexusException.build(code, message, cause));
    }

    private static NexusException logAndReturn(NexusException exception) {
        String location = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(stream -> stream
                        .skip(2)
                        .filter(frame -> !ConsoleExceptions.class.getName().equals(frame.getClassName()))
                        .findFirst()
                        .map(frame -> frame.getClassName() + "." + frame.getMethodName())
                        .orElse("unknown"));
        LOG.error("{}: [{}] {}", location, exception.code(), exception.getMessage());
        return exception;
    }
}
