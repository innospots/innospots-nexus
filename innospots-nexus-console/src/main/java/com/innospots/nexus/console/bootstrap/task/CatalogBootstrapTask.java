package com.innospots.nexus.console.bootstrap.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage;
import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 启动引导第 1 阶段：把已激活插件的 console 贡献同步为权限目录索引。
 *
 * <p><b>为什么必须最先执行：</b>后续的角色授权阶段要通过
 * {@code ConsoleCatalogService#grantKeyTypes()} 校验授权键是否存在，
 * 该映射完全由本阶段写入的目录资源派生。目录为空时授权阶段会全部失败。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public final class CatalogBootstrapTask implements NexusStartupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogBootstrapTask.class);

    private final ConsoleCatalogSyncService syncService;

    /**
     * 创建目录同步阶段。
     *
     * @param syncService 目录同步服务
     */
    public CatalogBootstrapTask(ConsoleCatalogSyncService syncService) {
        if (syncService == null) {
            throw new IllegalArgumentException("syncService is required");
        }
        this.syncService = syncService;
    }

    /**
     * 返回名称。
     *
     * @return 任务名称
     */
    @Override
    public String name() {
        return BootstrapStage.CATALOG.taskName();
    }

    /**
     * 返回执行顺序。
     *
     * @return 排序值
     */
    @Override
    public int order() {
        return BootstrapStage.CATALOG.order();
    }

    /**
     * 执行目录同步。
     *
     * @param context 启动上下文
     */
    @Override
    public void run(NexusStartupContext context) {
        CatalogSyncResult result = syncService.sync();
        LOGGER.info(
                "Catalog bootstrap finished created={} updated={} disabled={}",
                result.createdResources(),
                result.updatedResources(),
                result.disabledResources());
    }
}
