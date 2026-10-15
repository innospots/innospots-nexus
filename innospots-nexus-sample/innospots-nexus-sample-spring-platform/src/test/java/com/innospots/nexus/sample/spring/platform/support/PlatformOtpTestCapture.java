package com.innospots.nexus.sample.spring.platform.support;

import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.base.events.EventHandler;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent;

/**
 * 集成测试中捕获 {@link OtpSendRequestedEvent} 明文验证码（进程内 EventBus，非 mock）。
 */
public final class PlatformOtpTestCapture implements AutoCloseable {

    private final AtomicReference<OtpSendRequestedEvent> lastEvent = new AtomicReference<>();

    private final EventHandler<OtpSendRequestedEvent> handler = event -> {
        lastEvent.set(event);
        return null;
    };

    public PlatformOtpTestCapture() {
        EventBus.subscribe(OtpSendRequestedEvent.class, handler);
    }

    public OtpSendRequestedEvent lastEvent() {
        return lastEvent.get();
    }

    public String requirePlaintextCode(OtpPurpose expectedPurpose) {
        OtpSendRequestedEvent event = lastEvent.get();
        if (event == null) {
            throw new IllegalStateException("No OTP event captured");
        }
        if (event.purpose() != expectedPurpose) {
            throw new IllegalStateException(
                    "Expected OTP purpose " + expectedPurpose + " but got " + event.purpose());
        }
        if (event.plaintextCode() == null || event.plaintextCode().isBlank()) {
            throw new IllegalStateException("Captured OTP event has blank plaintextCode");
        }
        return event.plaintextCode();
    }

    public void clear() {
        lastEvent.set(null);
    }

    @Override
    public void close() {
        EventBus.unsubscribe(OtpSendRequestedEvent.class, handler);
        lastEvent.set(null);
    }
}
