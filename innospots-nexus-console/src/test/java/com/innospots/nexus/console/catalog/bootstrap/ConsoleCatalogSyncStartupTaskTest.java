package com.innospots.nexus.console.catalog.bootstrap;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleCatalogSyncStartupTaskTest {

    @Test
    void nameAndOrderMatchStartupPipelineContract() {
        ConsoleCatalogSyncStartupTask task =
                new ConsoleCatalogSyncStartupTask(mock(ConsoleCatalogSyncService.class));

        assertThat(task.name()).isEqualTo("console-catalog-sync");
        assertThat(task.order()).isEqualTo(200);
    }

    @Test
    void runDelegatesToSyncService() {
        ConsoleCatalogSyncService syncService = mock(ConsoleCatalogSyncService.class);
        when(syncService.sync()).thenReturn(new CatalogSyncResult(12, 0, 0));

        ConsoleCatalogSyncStartupTask task = new ConsoleCatalogSyncStartupTask(syncService);
        task.run(new NexusStartupContext());

        verify(syncService).sync();
    }
}
