package com.innospots.nexus.core.plugin.declaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
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
        PluginDefinition definition = PluginDefinition.builder("com.example.sample")
                .name("Sample Plugin")
                .version("1.0.0")
                .tags(Tags.of("provider", "sample"))
                .provide(ALPHA, AlphaProviderImpl::new)
                .provide(BETA, BetaProviderImpl::new)
                .require(ALPHA, false)
                .build();

        assertThat(definition.pluginId()).isEqualTo("com.example.sample");
        assertThat(definition.capabilities())
                .extracting(contribution -> contribution.type().key())
                .containsExactly(ALPHA.key(), BETA.key());
        assertThat(definition.requirements()).hasSize(1);
        assertThatThrownBy(() -> definition.capabilities().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void exposesReverseDomainPluginIdentityAndProviderIdentity() {
        PluginDefinition definition = PluginDefinition.builder("com.example.message")
                .displayName(I18nObject.of("zh-CN", "消息插件"))
                .description(I18nObject.of("zh-CN", "消息能力"))
                .version("1.0.0")
                .tags(Tags.of("channel", "message"))
                .provide(ALPHA, "wecom", Tags.of("provider", "wecom"),
                        ConfigDefinition.empty(), AlphaProviderImpl::new)
                .build();

        assertThat(definition.pluginId()).isEqualTo("com.example.message");
        assertThat(definition.displayName().cnValue()).isEqualTo("消息插件");
        assertThat(definition.capabilities().getFirst().providerId()).isEqualTo("wecom");
        assertThat(definition.capabilities().getFirst().tags().asMap())
                .containsExactly(Map.entry("provider", "wecom"));
    }

    @Test
    void rejectsDuplicateProviderIdAcrossCapabilityTypes() {
        assertThatThrownBy(() -> PluginDefinition.builder("com.example.message")
                .displayName(I18nObject.of("消息插件"))
                .version("1.0.0")
                .tags(Tags.empty())
                .provide(ALPHA, "shared", Tags.empty(), ConfigDefinition.empty(), AlphaProviderImpl::new)
                .provide(BETA, "shared", Tags.empty(), ConfigDefinition.empty(), BetaProviderImpl::new)
                .build())
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("providerId");
    }

    @Test
    void rejectsPluginIdsThatAreNotReverseDomainNames() {
        assertThatThrownBy(() -> PluginDefinition.builder("message-plugin")
                .displayName(I18nObject.of("消息插件"))
                .version("1.0.0")
                .build())
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("reverse-domain");
    }

    @Test
    void requirementTagsAreDefensivelyCopied() {
        CapabilityRequirement requirement = new CapabilityRequirement(ALPHA.key(), Tags.of("region", "cn"), true);
        assertThat(requirement.requiredTags()).isEqualTo(Tags.of("region", "cn"));
        assertThat(requirement.requiredTags().asMap()).isUnmodifiable();
    }

    @Test
    void rejectsDuplicateProviderIdentityBeforeFactoriesRun() {
        int[] factoryCalls = {0};

        assertThatThrownBy(() -> PluginDefinition.builder("com.example.sample")
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
                .hasMessageContaining("duplicate providerId");

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

        PluginDefinition definition = PluginDefinition.builder("com.example.sample")
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
                "com.example.sample",
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
