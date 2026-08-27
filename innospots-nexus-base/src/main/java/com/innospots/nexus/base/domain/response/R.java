package com.innospots.nexus.base.domain.response;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * Generic API response wrapper with success/failure status, result code,
 * message, optional data payload, and internationalized display message
 * for frontend rendering on failures.
 */
public record R<T>(
        boolean success,
        String code,
        String message,
        T data,
        I18nObject display
) {

    public static final String OK = "OK";

    /** Returns a success response with null data. */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /** Returns a success response wrapping the given data. */
    public static <T> R<T> ok(T data) {
        return new R<>(true, OK, OK, data, null);
    }

    /** Returns a failure response with an error code and message. */
    public static <T> R<T> fail(String code, String message) {
        return new R<>(false, code, message, null, null);
    }

    /** Returns a failure response with an error code, message, and data payload. */
    public static <T> R<T> fail(String code, String message, T data) {
        return new R<>(false, code, message, data, null);
    }

    /** Returns a failure response with an error code, message, and display for frontend. */
    public static <T> R<T> fail(String code, String message, I18nObject display) {
        return new R<>(false, code, message, null, display);
    }

    /** Returns a failure response with an error code, message, data, and display. */
    public static <T> R<T> fail(String code, String message, T data, I18nObject display) {
        return new R<>(false, code, message, data, display);
    }

    /**
     * Returns a failure response derived from a {@link StatusCode}.
     *
     * @param statusCode status providing code, summary, and display
     * @param <T>        payload type
     * @return failure response with no data
     */
    public static <T> R<T> fail(StatusCode statusCode) {
        return fail(statusCode, null);
    }

    /**
     * Returns a failure response derived from a {@link StatusCode} with a payload.
     *
     * @param statusCode status providing code, summary, and display
     * @param data       optional payload
     * @param <T>        payload type
     * @return failure response
     */
    public static <T> R<T> fail(StatusCode statusCode, T data) {
        StatusCode code = Checks.notNull(statusCode, "statusCode");
        return new R<>(false, code.fullCode(), code.summary(), data, code.message());
    }

    /**
     * Maps a {@link NexusException} to a failure response.
     *
     * @param exception thrown platform exception
     * @param <T>       payload type
     * @return failure response with the exception code, message, and display
     */
    public static <T> R<T> from(NexusException exception) {
        NexusException error = Checks.notNull(exception, "exception");
        return new R<>(false, error.code(), error.getMessage(), null, error.display());
    }
}
