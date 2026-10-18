package com.innospots.nexus.platform.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.thread.TLC;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformRequestTraceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void currentTraceIdUsesTlcOrDash() {
        assertThat(PlatformRequestTrace.currentTraceId()).isEqualTo("-");
        TLC.put(TLC.TRACE_ID, "req-abc");
        assertThat(PlatformRequestTrace.currentTraceId()).isEqualTo("req-abc");
    }

    @Test
    void describeContactMasksEmailAndMobile() {
        assertThat(PlatformRequestTrace.describeContact("user@example.com", null)).isEqualTo("EMAIL:us***@example.com");
        assertThat(PlatformRequestTrace.describeContact(null, "13800138000")).isEqualTo("MOBILE:138****00");
    }

    @Test
    void describeContactValueDetectsChannel() {
        assertThat(PlatformRequestTrace.describeContactValue("a@b.co")).contains("EMAIL:");
        assertThat(PlatformRequestTrace.describeContactValue("13900001111")).contains("MOBILE:");
    }

}
