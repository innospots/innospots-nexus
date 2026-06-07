package com.innospots.nexus.base.domain.response;

/**
 * Generic API response wrapper with success/failure status, result code,
 * message, and optional data payload.
 */
public record R<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static final String OK = "OK";

    /** Returns a success response with null data. */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /** Returns a success response wrapping the given data. */
    public static <T> R<T> ok(T data) {
        return new R<>(true, OK, OK, data);
    }

    /** Returns a failure response with an error code and message. */
    public static <T> R<T> fail(String code, String message) {
        return new R<>(false, code, message, null);
    }

    /** Returns a failure response with an error code, message, and data payload. */
    public static <T> R<T> fail(String code, String message, T data) {
        return new R<>(false, code, message, data);
    }
}
