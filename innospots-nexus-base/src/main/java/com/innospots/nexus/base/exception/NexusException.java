package com.innospots.nexus.base.exception;

import com.innospots.nexus.base.status.StatusCode;

import java.util.Objects;

/**
 * Base runtime exception for the platform. Carries a machine-readable
 * error code (see {@link com.innospots.nexus.base.status.StatusCode})
 * and a human-readable message.
 */
public class NexusException extends RuntimeException {

    private final String code;

    /**
     * Constructs an exception with a machine-readable code and a human-readable message.
     */
    public NexusException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructs an exception with a code, message, and originating cause.
     */
    public NexusException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Builds an exception from a {@link StatusCode}, using its full code and summary message.
     */
    public static NexusException build(StatusCode statusCode) {
        StatusCode code = Objects.requireNonNull(statusCode, "statusCode must not be null");
        return new NexusException(code.fullCode(), code.summary());
    }

    /** Returns the machine-readable error code. */
    public String code() {
        return code;
    }
}
