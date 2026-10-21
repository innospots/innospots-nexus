package com.innospots.nexus.core.plugin.contribution.console;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PluginConsoleAssemblyTest {

    @Test
    void contributionTypeIsConsoleV2() {
        assertThat(ConsolePluginContribution.TYPE.majorVersion()).isEqualTo(2);
    }
}
