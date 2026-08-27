package com.innospots.nexus.platform;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformModuleTest {

    @Test
    void platformModuleIsALoadableMarker() {
        assertThat(PlatformModule.class.getPackageName())
                .isEqualTo("com.innospots.nexus.platform");
    }
}
