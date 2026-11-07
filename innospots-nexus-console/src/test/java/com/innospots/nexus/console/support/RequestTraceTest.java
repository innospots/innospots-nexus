package com.innospots.nexus.console.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.thread.TLC;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTraceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void currentTraceIdUsesTlcOrDash() {
        assertThat(RequestTrace.currentTraceId()).isEqualTo("-");
        TLC.put(TLC.TRACE_ID, "req-abc");
        assertThat(RequestTrace.currentTraceId()).isEqualTo("req-abc");
    }

    @Test
    void describeContactMasksEmailAndMobile() {
        assertThat(RequestTrace.describeContact("user@example.com", null)).isEqualTo("EMAIL:us***@example.com");
        assertThat(RequestTrace.describeContact(null, "13800138000")).isEqualTo("MOBILE:138****00");
    }

    @Test
    void describeContactValueDetectsChannel() {
        assertThat(RequestTrace.describeContactValue("a@b.co")).contains("EMAIL:");
        assertThat(RequestTrace.describeContactValue("13900001111")).contains("MOBILE:");
    }

}
