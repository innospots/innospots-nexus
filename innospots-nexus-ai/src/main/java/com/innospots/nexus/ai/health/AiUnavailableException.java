package com.innospots.nexus.ai.health;

import java.time.Duration;

import com.innospots.nexus.ai.runtime.AiRuntimeStatus;

/**
 * 运行时不可用异常，携带状态与重试建议。
 */
public final class AiUnavailableException extends RuntimeException {

    private final AiRuntimeStatus state;
    private final Duration retryAfter;
    private final String code;

    public AiUnavailableException(String code, String message, AiRuntimeStatus state, Duration retryAfter) {
        super(message);
        this.code = code;
        this.state = state;
        this.retryAfter = retryAfter;
    }

    public String code() {
        return code;
    }

    public AiRuntimeStatus state() {
        return state;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
