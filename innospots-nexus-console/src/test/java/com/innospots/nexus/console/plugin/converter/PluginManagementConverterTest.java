package com.innospots.nexus.console.plugin.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.plugin.domain.vo.PluginManagementVo;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.declaration.CapabilityRequirement;
import com.innospots.nexus.core.plugin.declaration.PluginSource;
import com.innospots.nexus.core.plugin.capability.CapabilityKey;
import com.innospots.nexus.core.plugin.dependency.DependencyResolution;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginSourceType;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginInstallation;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginManagementView;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;

import static org.assertj.core.api.Assertions.assertThat;

/** MapStruct 管理视图转换测试，确保安装事实和运行事实不混淆。 */
class PluginManagementConverterTest {

    @Test
    void mapsInstallationAndRuntimeDimensionsWithoutCollapsingThem() {
        LocalDateTime discoveredAt = LocalDateTime.of(2026, 9, 1, 12, 0);
        Instant runtimeDiscoveredAt = Instant.parse("2026-09-01T04:00:00Z");
        Instant runtimeStartedAt = Instant.parse("2026-09-01T04:00:01Z");
        PluginInstallation installation = new PluginInstallation(
                "plg-installation", "com.example.sales", "1.2.0", PluginSourceType.YAML,
                "classpath:/META-INF/nexus/plugin.yaml", PluginPresence.PRESENT,
                true, false, "{\"pluginId\":\"com.example.sales\"}",
                "ACTIVE", null, discoveredAt, discoveredAt, discoveredAt,
                discoveredAt, null, null);
        PluginRuntimeInfo runtime = new PluginRuntimeInfo(
                "com.example.sales", "Sales", "1.2.0", "example.SalesPlugin", PluginState.ACTIVE,
                "active", Tags.empty(), List.of(), List.<CapabilityRequirement>of(),
                Map.<CapabilityKey, DependencyResolution>of(), runtimeDiscoveredAt, runtimeStartedAt, null);

        PluginManagementVo result = PluginManagementConverter.INSTANCE.toVo(
                new PluginManagementView(installation, java.util.Optional.of(runtime)));

        assertThat(result.pluginId()).isEqualTo("com.example.sales");
        assertThat(result.presence()).isEqualTo(PluginPresence.PRESENT);
        assertThat(result.installed()).isTrue();
        assertThat(result.desiredEnabled()).isFalse();
        assertThat(result.runtimeState()).isEqualTo("ACTIVE");
        assertThat(result.runtimePhase()).isEqualTo("active");
        assertThat(result.runtimeDiscoveredAt()).isEqualTo(runtimeDiscoveredAt);
        assertThat(result.runtimeStartedAt()).isEqualTo(runtimeStartedAt);
    }
}
