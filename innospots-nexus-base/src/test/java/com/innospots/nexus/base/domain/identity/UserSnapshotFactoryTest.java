package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.thread.TLC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSnapshotFactoryTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void buildsSnapshotFromTlcIdentityKeys() {
        TLC.userId(42L);
        TLC.userName("yxy");

        UserSnapshot user = UserSnapshot.fromContext();

        assertThat(user.userId()).isEqualTo(42L);
        assertThat(user.userName()).isEqualTo("yxy");
    }

    @Test
    void buildsSnapshotFromClaimsUsingTlcKeyNames() {
        UserSnapshot user = UserSnapshot.fromClaims(Map.of(
                TLC.USER_ID, "15",
                TLC.USER_NAME, "demo",
                UserSnapshot.CLAIM_REAL_NAME, "Demo User",
                UserSnapshot.CLAIM_EMAIL, "demo@example.com"
        ));

        assertThat(user.userId()).isEqualTo(15L);
        assertThat(user.userName()).isEqualTo("demo");
        assertThat(user.realName()).isEqualTo("Demo User");
        assertThat(user.email()).isEqualTo("demo@example.com");
    }

    @Test
    void rejectsClaimsWithoutUserId() {
        assertThatThrownBy(() -> UserSnapshot.fromClaims(Map.of(TLC.USER_NAME, "demo")))
                .isInstanceOf(NexusException.class);
    }
}
