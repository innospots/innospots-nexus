package com.innospots.nexus.base.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TLCTest {

    @AfterEach
    void clear() {
        TLC.clear();
    }

    @Test
    void storesAndRestoresThreadLocalContextScopes() {
        TLC.put("traceId", "root");

        try (TLC.Scope ignored = TLC.scope(Map.of("traceId", "child", "tenantId", "t1"))) {
            assertThat(TLC.getString("traceId")).isEqualTo("child");
            assertThat(TLC.getString("tenantId")).isEqualTo("t1");
        }

        assertThat(TLC.getString("traceId")).isEqualTo("root");
        assertThat(TLC.get("tenantId")).isNull();
    }

    @Test
    void snapshotsAreIndependentCopies() {
        TLC.put("traceId", "a");
        Map<String, Object> snapshot = TLC.snapshot();

        TLC.put("traceId", "b");
        TLC.restore(snapshot);

        assertThat(TLC.getString("traceId")).isEqualTo("a");
    }

    @Test
    void exposesProjectAndUserIdentityKeys() {
        TLC.projectId(1001L);
        TLC.userId(2002L);
        TLC.userName("alice");

        assertThat(TLC.projectId()).isEqualTo(1001L);
        assertThat(TLC.userId()).isEqualTo(2002L);
        assertThat(TLC.userName()).isEqualTo("alice");
        assertThat(TLC.snapshot()).containsEntry(TLC.PROJECT_ID, 1001L)
                .containsEntry(TLC.USER_ID, 2002L)
                .containsEntry(TLC.USER_NAME, "alice");
    }
}
