package com.innospots.nexus.core.plugin.installation;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.discovery.PluginDiscoveryReport;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.domain.entity.PluginInstallationEntity;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginManagementView;
import com.innospots.nexus.core.plugin.installation.repository.PluginInstallationRepository;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.installation.service.PluginRuntimeFactory;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

import static org.assertj.core.api.Assertions.assertThat;

/** 安装管理器的持久化意图、对账和运行时协作测试。 */
class PluginInstallationManagerTest {

    @Test
    void autoInstallFalseOnlyRegistersPlugin() {
        TestDao dao = new TestDao();
        PluginInstallationManager manager = manager(dao, false);

        manager.start();

        PluginManagementView view = manager.plugin("com.example.installation").orElseThrow();
        assertThat(view.installation().installed()).isFalse();
        assertThat(view.installation().desiredEnabled()).isFalse();
        assertThat(view.runtime()).get().extracting(runtime -> runtime.state())
                .isEqualTo(PluginState.DESCRIBED);
    }

    @Test
    void installAndStartCommitsIntentBeforeStartingRuntime() {
        TestDao dao = new TestDao();
        PluginInstallationManager manager = manager(dao, false);

        manager.start();
        PluginManagementView view = manager.installAndStart("com.example.installation");

        assertThat(view.installation().installed()).isTrue();
        assertThat(view.installation().desiredEnabled()).isTrue();
        assertThat(view.runtime()).get().extracting(runtime -> runtime.state())
                .isEqualTo(PluginState.ACTIVE);
    }

    private static PluginInstallationManager manager(TestDao dao, boolean autoInstall) {
        Plugin plugin = new InstallationPlugin();
        PluginCatalog catalog = PluginCatalog.of(List.of(new DiscoveredPlugin(
                plugin, plugin.definition(), Instant.now())));
        return new PluginInstallationManager(
                new PluginInstallationRepository(dao.dao()),
                new PluginRuntimeFactory(
                        new PluginRuntimeConfig(Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), null),
                        List.of(),
                        PluginContributionSnapshotterRegistry.builder().build()),
                new PluginInstallationConfig(autoInstall),
                new PluginDiscoveryReport(catalog, List.of()));
    }

    private static final class InstallationPlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.installation")
                    .name("Installation")
                    .version("1.0.0")
                    .build();
        }
    }

    private static final class TestDao {

        private final Map<String, PluginInstallationEntity> records = new LinkedHashMap<>();
        private final PluginInstallationDao dao = Mockito.mock(PluginInstallationDao.class);

        private TestDao() {
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

        private PluginInstallationDao dao() {
            return dao;
        }
    }
}
