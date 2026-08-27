package com.innospots.nexus.base.util;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * Precondition checks that fail with {@link NexusException} and
 * {@link NexusStatusCode#INVALID_PARAMETER}.
 */
public final class Checks {

    private Checks() {
    }

    /**
     * Returns {@code value} when it is not null.
     *
     * @param value required value
     * @param name  parameter name used in the error message
     * @param <T>   value type
     * @return the same value
     * @throws NexusException when {@code value} is null
     */
    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, name + " must not be null");
        }
        return value;
    }

    /**
     * Returns {@code value} when it is not blank.
     *
     * @param value required text
     * @param name  parameter name used in the error message
     * @return the same value
     * @throws NexusException when {@code value} is null or blank
     */
    public static String notBlank(String value, String name) {
        if (StringUtils.isBlank(value)) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, name + " must not be blank");
        }
        return value;
    }

    /**
     * Accepts a true expression and rejects a false one.
     *
     * @param expression condition that must hold
     * @param message    error message when the condition is false
     * @throws NexusException when {@code expression} is false
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, message);
        }
    }
}
