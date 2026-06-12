package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityContractsTest {

    @Test
    void definesUserRoleAndGroupModels() {
        RoleInfo role = RoleInfo.of("r1", "Admin", "admin", BasicStatus.ENABLED);
        UserGroupInfo group = new UserGroupInfo(10L, "AI Team", "ai-team", null, 1L, List.of(2L, 3L), BasicStatus.ENABLED);
        UserInfo user = UserInfo.simple(7L, "yxy", "YXY")
                .email("yxy@example.com")
                .role(role)
                .group(group);

        assertThat(user.displayName()).isEqualTo("YXY");
        assertThat(user.roles()).containsExactly(role);
        assertThat(user.group()).isEqualTo(group);
        assertThat(group.assistantUserIds()).containsExactly(2L, 3L);
    }
}
