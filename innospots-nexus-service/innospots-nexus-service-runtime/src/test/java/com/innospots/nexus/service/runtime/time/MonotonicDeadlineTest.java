package com.innospots.nexus.service.runtime.time;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.time.Ticker;

import static org.assertj.core.api.Assertions.assertThat;

class MonotonicDeadlineTest {

    @Test
    void remainingIsNeverNegativeAndShortenDoesNotExtend() {
        AtomicLong nanos = new AtomicLong(1_000);
        Ticker ticker = nanos::get;
        Deadline deadline = Deadline.of(ticker, Duration.ofNanos(100));

        assertThat(deadline.remaining()).isEqualTo(Duration.ofNanos(100));
        nanos.set(1_200);
        assertThat(deadline.remaining()).isEqualTo(Duration.ZERO);
        assertThat(deadline.isExpired()).isTrue();

        nanos.set(1_000);
        Deadline shortened = deadline.shorten(Duration.ofNanos(40));
        assertThat(shortened.remaining()).isEqualTo(Duration.ofNanos(40));
        Deadline ignored = shortened.shorten(Duration.ofNanos(80));
        assertThat(ignored.remaining()).isEqualTo(Duration.ofNanos(40));
    }
}
