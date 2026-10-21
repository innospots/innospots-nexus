package com.innospots.nexus.core.plugin.installation.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginSource;
import com.innospots.nexus.core.plugin.declaration.Tags;

import static org.assertj.core.api.Assertions.assertThat;

class PluginDefinitionSnapshotMapperTest {

    @Test
    void storesContributionSnapshotsWithoutLegacyCapabilities() {
        PluginDefinition definition = PluginDefinition.builder("com.example.snapshot")
                .name("Snapshot")
                .version("1.0.0")
                .tags(Tags.of("env", "test"))
                .contribute(sampleContribution())
                .build();

        PluginDefinitionSnapshot snapshot = PluginDefinitionSnapshotMapper.from(
                definition,
                PluginSource.java("test", Instant.now()),
                PluginContributionSnapshotterRegistry.builder()
                        .register(new ConsolePluginContributionSnapshotter())
                        .build());

        assertThat(snapshot.capabilities()).isEmpty();
        assertThat(snapshot.contributions()).hasSize(1);
        assertThat(snapshot.contributions().getFirst()).containsKey("domainKey");
    }

    private static ConsolePluginContribution sampleContribution() {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                "snapshot-page",
                null,
                "default",
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                "role",
                I18nObject.of(Map.of("en", "Role")),
                I18nObject.of(Map.of()),
                0,
                null,
                null,
                null,
                null,
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Views")))),
                List.of(new ConsolePermissionGroupDeclaration(
                        "default", I18nObject.of(Map.of("en", "Ops")))),
                List.of(menu));
        return new ConsolePluginContribution(
                "nexus",
                I18nObject.of(Map.of("en", "Nexus")),
                I18nObject.of(Map.of()),
                0,
                List.of(module));
    }
}
