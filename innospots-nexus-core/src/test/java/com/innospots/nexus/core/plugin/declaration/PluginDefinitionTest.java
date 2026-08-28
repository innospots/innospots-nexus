package com.innospots.nexus.core.plugin.declaration;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.CapabilityType;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.config.ConfigDefinition;
import com.innospots.nexus.core.plugin.config.ConfigItemDefinition;
import com.innospots.nexus.core.plugin.config.ConfigType;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginDefinitionTest {

    private static final CapabilityType<AlphaProvider> ALPHA =
            CapabilityType.of("sample.alpha", 1, AlphaProvider.class);
    private static final CapabilityType<BetaProvider> BETA =
            CapabilityType.of("sample.beta", 1, BetaProvider.class);

    @Test
    void buildsImmutableDefinitionWithMultipleCapabilities() {
        PluginDefinition definition = PluginDefinition.builder("sample-plugin")
                .name("Sample Plugin")
                .version("1.0.0")
                .tags(Tags.of("provider", "sample"))
                .provide(ALPHA, AlphaProviderImpl::new)
                .provide(BETA, BetaProviderImpl::new)
                .require(ALPHA, false)
                .build();

        assertThat(definition.id()).isEqualTo("sample-plugin");
        assertThat(definition.capabilities())
                .extracting(contribution -> contribution.type().key())
                .containsExactly(ALPHA.key(), BETA.key());
        assertThat(definition.requirements()).hasSize(1);
        assertThatThrownBy(() -> definition.capabilities().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsDuplicateCapabilityKeyBeforeFactoriesRun() {
        int[] factoryCalls = {0};

        assertThatThrownBy(() -> PluginDefinition.builder("sample-plugin")
                .name("Sample Plugin")
                .version("1.0.0")
                .tags(Tags.of("provider", "sample"))
                .provide(ALPHA, () -> {
                    factoryCalls[0]++;
                    return new AlphaProviderImpl();
                })
                .provide(ALPHA, AlphaProviderImpl::new)
                .build())
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("duplicate capability");

        assertThat(factoryCalls[0]).isZero();
    }

    @Test
    void validatesStableIdentifiersAndTags() {
        assertThatThrownBy(() -> CapabilityType.of("Sample.Alpha", 1, AlphaProvider.class))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> Tags.of("Provider", "sample"))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> Tags.of(null, "sample"))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> PluginDefinition.builder("SamplePlugin")
                .name("Sample")
                .version("1.0.0")
                .tags(Tags.of("provider", "sample"))
                .build())
                .isInstanceOf(NexusException.class);
    }

    @Test
    void keepsTagsInDeterministicNameOrder() {
        Tags tags = Tags.of("zeta", "last")
                .and("alpha", "first");

        assertThat(tags.asMap().keySet()).containsExactly("alpha", "zeta");
    }

    @Test
    void snapshotsCustomConfigurationDefinitions() {
        List<ConfigItemDefinition> items = new ArrayList<>();
        items.add(new ConfigItemDefinition("endpoint", ConfigType.STRING, false, null, false, null));

        PluginDefinition definition = PluginDefinition.builder("sample-plugin")
                .name("Sample Plugin")
                .version("1.0.0")
                .tags(Tags.of("provider", "sample"))
                .config(() -> items)
                .build();
        items.clear();

        assertThat(definition.config().items()).hasSize(1);
    }

    @Test
    void rejectsNullDeclarationEntriesWithNexusExceptions() {
        List<CapabilityContribution<?>> contributions = new ArrayList<>();
        contributions.add(null);

        assertThatThrownBy(() -> new PluginDefinition(
                "sample-plugin",
                "Sample Plugin",
                "1.0.0",
                PluginDefinition.CURRENT_API_VERSION,
                Tags.of("provider", "sample"),
                contributions,
                List.of(),
                ConfigDefinition.empty()))
                .isInstanceOf(NexusException.class);
    }

    private interface AlphaProvider extends CapabilityProvider {
    }

    private interface BetaProvider extends CapabilityProvider {
    }

    private static final class AlphaProviderImpl implements AlphaProvider {
    }

    private static final class BetaProviderImpl implements BetaProvider {
    }
}
