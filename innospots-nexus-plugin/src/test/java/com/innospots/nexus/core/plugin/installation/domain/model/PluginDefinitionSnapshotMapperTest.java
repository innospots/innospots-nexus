package com.innospots.nexus.core.plugin.installation.domain.model;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginSource;

import static org.assertj.core.api.Assertions.assertThat;

class PluginDefinitionSnapshotMapperTest {

    private static final CapabilityType<Provider> PROVIDER =
            CapabilityType.of("snapshot.fixture", 1, Provider.class);

    @Test
    void storesMergedPluginAndProviderTagsInCapabilitySnapshot() {
        PluginDefinition definition = PluginDefinition.builder("com.example.snapshot")
                .name("Snapshot")
                .version("1.0.0")
                .tags(Tags.of("tenant", "acme"))
                .provide(PROVIDER, "primary", Tags.of("channel", "wecom"), ConfigDefinition.empty(), ProviderImpl::new)
                .build();
        PluginSource source = PluginSource.java("com.example.SnapshotPlugin", Instant.parse("2026-01-01T00:00:00Z"));

        PluginDefinitionSnapshot snapshot = PluginDefinitionSnapshotMapper.from(
                definition, source, PluginContributionSnapshotterRegistry.builder().build());

        assertThat(snapshot.capabilities()).hasSize(1);
        assertThat(snapshot.capabilities().getFirst().tags())
                .containsExactlyEntriesOf(Map.of("tenant", "acme", "channel", "wecom"));
    }

    private interface Provider extends CapabilityProvider {
    }

    private static final class ProviderImpl implements Provider {
    }
}
