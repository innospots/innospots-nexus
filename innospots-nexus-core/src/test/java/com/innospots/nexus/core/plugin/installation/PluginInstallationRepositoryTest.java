package com.innospots.nexus.core.plugin.installation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.domain.entity.PluginInstallationEntity;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginSourceType;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginDefinitionSnapshot;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginInstallation;
import com.innospots.nexus.core.plugin.installation.repository.PluginInstallationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 安装仓储的首次登记、意图保持、MISSING 恢复和非法事实测试。 */
class PluginInstallationRepositoryTest {

    @Test
    void preservesAdministratorIntentAcrossRediscoveryAndMissingRecovery() {
        InMemoryDao store = new InMemoryDao();
        PluginInstallationRepository repository = new PluginInstallationRepository(store.dao);
        PluginDefinitionSnapshot snapshot = snapshot("1.0.0");

        PluginInstallation first = repository.register(snapshot, false);
        assertThat(first.installed()).isFalse();
        assertThat(first.desiredEnabled()).isFalse();

        PluginInstallation enabled = repository.setIntent(first.pluginId(), true, true);
        assertThat(enabled.desiredEnabled()).isTrue();
        PluginInstallation missing = repository.markMissing(List.of()).getFirst();
        assertThat(missing.presence()).isEqualTo(PluginPresence.MISSING);
        assertThat(missing.desiredEnabled()).isTrue();

        PluginInstallation restored = repository.register(snapshot("2.0.0"), true);
        assertThat(restored.presence()).isEqualTo(PluginPresence.PRESENT);
        assertThat(restored.pluginVersion()).isEqualTo("2.0.0");
        assertThat(restored.installed()).isTrue();
        assertThat(restored.desiredEnabled()).isTrue();
    }

    @Test
    void rejectsInvalidInstallationIntentAtTheDomainAndRepositoryBoundaries() {
        assertThatThrownBy(() -> new PluginInstallation(
                "plg-invalid", "com.example.invalid", "1.0.0", PluginSourceType.JAVA,
                "class:Example", PluginPresence.PRESENT, false, true, "{}", null, null,
                null, null, null, null, null, null))
                .isInstanceOf(NexusException.class);

        InMemoryDao store = new InMemoryDao();
        PluginInstallationRepository repository = new PluginInstallationRepository(store.dao);
        repository.register(snapshot("1.0.0"), false);
        assertThatThrownBy(() -> repository.setIntent("com.example.sales", false, true))
                .hasMessageContaining("uninstalled plugin");
    }

    private static PluginDefinitionSnapshot snapshot(String version) {
        return new PluginDefinitionSnapshot(
                "com.example.sales", version, 1, "YAML", "classpath:/plugin.yaml", List.of(),
                List.of(Map.of("type", "console@1", "modules", List.of())));
    }

    private static final class InMemoryDao {

        private final Map<String, PluginInstallationEntity> records = new LinkedHashMap<>();
        private final PluginInstallationDao dao = Mockito.mock(PluginInstallationDao.class);

        private InMemoryDao() {
            Mockito.when(dao.selectByPluginId(Mockito.anyString()))
                    .thenAnswer(invocation -> records.get(invocation.getArgument(0)));
            Mockito.when(dao.selectAll()).thenAnswer(ignored -> List.copyOf(records.values()));
            Mockito.when(dao.insert(Mockito.<PluginInstallationEntity>any())).thenAnswer(invocation -> {
                PluginInstallationEntity entity = invocation.getArgument(0);
                records.put(entity.getPluginId(), entity);
                return 1;
            });
            Mockito.when(dao.updateById(Mockito.<PluginInstallationEntity>any())).thenAnswer(invocation -> {
                PluginInstallationEntity entity = invocation.getArgument(0);
                records.put(entity.getPluginId(), entity);
                return 1;
            });
        }
    }
}
