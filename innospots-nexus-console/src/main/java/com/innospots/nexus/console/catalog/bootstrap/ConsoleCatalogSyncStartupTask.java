package com.innospots.nexus.console.catalog.bootstrap;

import com.innospots.nexus.core.plugin.contribution.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/** 启动后将 ACTIVE 插件贡献同步到宿主级目录索引。 */
public final class ConsoleCatalogSyncStartupTask implements NexusStartupTask {

    private static final int ORDER = 200;

    private final ConsoleCatalogSyncService syncService;

    /** 创建目录同步启动任务。 */
    public ConsoleCatalogSyncStartupTask(ConsoleCatalogSyncService syncService) {
        if (syncService == null) {
            throw new IllegalArgumentException("syncService is required");
        }
        this.syncService = syncService;
    }

    @Override
    public String name() {
        return "console-catalog-sync";
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void run(NexusStartupContext context) {
        syncService.sync();
    }
}
