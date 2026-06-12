package com.innospots.nexus.base.domain.response;

import com.innospots.nexus.base.i18n.I18nObject;

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
}
