package com.innospots.nexus.console.bootstrap.task;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage;
import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CatalogBootstrapTask} 契约测试（启动引导第 1 阶段）。
 */
class CatalogBootstrapTaskTest {

    @Test
    void nameAndOrderMatchStage() {
        CatalogBootstrapTask task =
                new CatalogBootstrapTask(mock(ConsoleCatalogSyncService.class));

        assertThat(task.name()).isEqualTo(BootstrapStage.CATALOG.taskName());
        assertThat(task.order()).isEqualTo(200);
    }

    @Test
    void runDelegatesToSyncService() {
        ConsoleCatalogSyncService syncService = mock(ConsoleCatalogSyncService.class);
        when(syncService.sync()).thenReturn(new CatalogSyncResult(12, 0, 0));

        new CatalogBootstrapTask(syncService).run(new NexusStartupContext());

        verify(syncService).sync();
    }
}
