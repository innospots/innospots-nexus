package com.innospots.nexus.service.runtime.cancellation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 写侧取消源首次生效与监听器单次通知行为测试。
 */
class CancellationSourceTest {

    @Test
    void firstCancelWinsAndLaterCallsReturnFalse() {
        CancellationSource source = new CancellationSource();

        assertThat(source.cancel(CancellationReason.CLIENT_DISCONNECTED)).isTrue();
        assertThat(source.cancel(CancellationReason.APPLICATION_CANCELLED)).isFalse();
        assertThat(source.token().isCancelled()).isTrue();
        assertThat(source.token().reason()).contains(CancellationReason.CLIENT_DISCONNECTED);
    }

    @Test
    void lateRegistrationIsNotifiedImmediatelyOnce() {
        CancellationSource source = new CancellationSource();
        AtomicInteger count = new AtomicInteger();
        source.cancel(CancellationReason.SERVER_SHUTDOWN);

        CancellationRegistration registration = source.token().onCancel(reason -> count.incrementAndGet());
        registration.close();
        registration.close();

        assertThat(count).hasValue(1);
        assertThat(source.token().reason()).contains(CancellationReason.SERVER_SHUTDOWN);
    }

    @Test
    void callbackExceptionDoesNotBlockOtherListeners() {
        CancellationSource source = new CancellationSource();
        List<String> order = new ArrayList<>();
        source.token().onCancel(reason -> {
            order.add("first");
            throw new IllegalStateException("listener failed");
        });
        source.token().onCancel(reason -> order.add("second"));

        assertThat(source.cancel(CancellationReason.APPLICATION_CANCELLED)).isTrue();
        assertThat(order).containsExactly("first", "second");
    }

    @Test
    void closedRegistrationIsNotNotified() {
        CancellationSource source = new CancellationSource();
        AtomicInteger count = new AtomicInteger();
        CancellationRegistration registration = source.token().onCancel(reason -> count.incrementAndGet());
        registration.close();

        assertThat(source.cancel(CancellationReason.DEADLINE_EXCEEDED)).isTrue();
        assertThat(count).hasValue(0);
    }

    @Test
    void errorFromListenerIsNotSwallowed() {
        CancellationSource source = new CancellationSource();
        source.token().onCancel(reason -> {
            throw new AssertionError("fatal");
        });

        assertThatThrownBy(() -> source.cancel(CancellationReason.APPLICATION_CANCELLED))
                .isInstanceOf(AssertionError.class)
                .hasMessage("fatal");
        assertThat(source.token().isCancelled()).isTrue();
    }
}
