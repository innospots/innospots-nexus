package com.innospots.nexus.console.catalog.bootstrap;

import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 启动后将 ACTIVE 插件贡献同步到宿主级目录索引。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ConsoleCatalogSyncStartupTask implements NexusStartupTask {

    private static final int ORDER = 200;

    private final ConsoleCatalogSyncService syncService;

    /**
     * 创建目录同步启动任务。
     */
    public ConsoleCatalogSyncStartupTask(ConsoleCatalogSyncService syncService) {
        if (syncService == null) {
            throw new IllegalArgumentException("syncService is required");
        }
        this.syncService = syncService;
    }
    /**
     * 返回名称。
     * @return 操作结果
     */

    
    @Override
    public String name() {
        return "console-catalog-sync";
    }
    /**
     * 执行order。
     * @return 操作结果
     */

    
    @Override
    public int order() {
        return ORDER;
    }
    /**
     * 执行run。
     * @param context 调用上下文
     */

    
    @Override
    public void run(NexusStartupContext context) {
        syncService.sync();
    }
}
