package com.innospots.nexus.core.plugin.installation.bootstrap;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.domain.entity.PluginInstallationEntity;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginManagementView;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证宿主统一入口完成发现、对账与启动。 */
class PluginHostBootstrapTest {

    @TempDir
    Path classpathRoot;

    @Test
    void enableCompletesDiscoveryReconcileAndStartup() throws Exception {
        TestDao dao = new TestDao();
        ClassLoader classLoader = serviceClassLoader(BootstrapPlugin.class.getName());
        PluginInstallationManager manager = PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                dao.dao(),
                new PluginRuntimeConfig(Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), classLoader),
                new PluginInstallationConfig(true),
                PluginContributionDecoderRegistry.builder().build(),
                List.of(),
                PluginContributionSnapshotterRegistry.builder().build(),
                classLoader));

        PluginManagementView view = manager.plugin("com.example.bootstrap").orElseThrow();
        assertThat(view.installation().installed()).isTrue();
        assertThat(view.installation().desiredEnabled()).isTrue();
        assertThat(view.runtime()).get().extracting(runtime -> runtime.state())
                .isEqualTo(PluginState.ACTIVE);
        assertThat(manager.capabilities()).isNotNull();

        manager.close();
    }

    private ClassLoader serviceClassLoader(String... implementationClasses) throws Exception {
        Path serviceFile = classpathRoot.resolve(
                "META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin");
        Files.createDirectories(serviceFile.getParent());
        Files.writeString(serviceFile, String.join(System.lineSeparator(), implementationClasses));
        return new URLClassLoader(
                new java.net.URL[]{classpathRoot.toUri().toURL()},
                getClass().getClassLoader());
    }

    public static final class BootstrapPlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.bootstrap")
                    .name("Bootstrap Plugin")
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
