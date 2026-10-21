package com.innospots.nexus.core.plugin.contribution.console;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.contribution.PluginContributionEntry;

class ConsolePluginContributionHandlerTest {

    private ConsolePluginContributionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ConsolePluginContributionHandler(
                new ConsoleContributionCatalog(),
                new ReservedPluginResourceCatalog(List.of()));
    }

    @Test
    void validatesConsoleV2Contribution() {
        ConsolePluginContribution contribution = ConsoleContributionTestSupport.sampleContribution(
                "nexus", "role", "nexus-role-main");
        ProviderRef owner = new ProviderRef("com.innospots.nexus.test-plugin", "main");
        assertThatCode(() -> handler.validate(
                null,
                List.of(new PluginContributionEntry<>(owner, contribution))))
                .doesNotThrowAnyException();
    }
}
