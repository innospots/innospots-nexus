package com.innospots.nexus.base.exception;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCode;

import java.util.Objects;

/**
 * Base runtime exception for the platform. Carries a machine-readable
 * error code (see {@link com.innospots.nexus.base.status.StatusCode}),
 * a human-readable message, and an optional internationalized display
 * for frontend rendering.
 * <p>Use static factory methods ({@link #build(StatusCode)} and
 * {@link #build(String, String)}) instead of constructors.</p>
 */
public class NexusException extends RuntimeException {

    private final String code;
    private final I18nObject display;

    private NexusException(String code, String message) {
        super(message);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.display = null;
    }

    private NexusException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.display = null;
    }

    private NexusException(String code, String message, I18nObject display) {
        super(message);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.display = display;
    }

    private NexusException(String code, String message, I18nObject display, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.display = display;
    }

    /** Builds from a {@link StatusCode}, using its full code and summary message. */
    public static NexusException build(StatusCode statusCode) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary());
    }

    /** Builds from a {@link StatusCode} with an internationalized display message. */
    public static NexusException build(StatusCode statusCode, I18nObject display) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary(), display);
    }

    /** Builds from a {@link StatusCode} with a display message and originating cause. */
    public static NexusException build(StatusCode statusCode, I18nObject display, Throwable cause) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary(), display, cause);
    }

    /** Builds with a machine-readable code and a human-readable message. */
    public static NexusException build(String code, String message) {
        return new NexusException(code, message);
    }

    /** Builds with a code, message, and originating cause. */
    public static NexusException build(String code, String message, Throwable cause) {
        return new NexusException(code, message, cause);
    }

    /** Returns the machine-readable error code. */
    public String code() {
        return code;
    }

    /** Returns the internationalized display message, may be {@code null}. */
    public I18nObject display() {
        return display;
    }
}
