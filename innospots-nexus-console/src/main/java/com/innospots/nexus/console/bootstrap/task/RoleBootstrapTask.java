package com.innospots.nexus.console.bootstrap.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage;
import com.innospots.nexus.console.bootstrap.loader.RoleAuthorizationManifestLoader;
import com.innospots.nexus.console.bootstrap.service.RoleDefinitionSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 启动引导第 2 阶段：按配置创建或更新角色定义。
 *
 * <p>本阶段把解析后的角色清单写入 {@link NexusStartupContext}，
 * 供第 3 阶段复用，避免重复扫描 classpath 与重复解析 YAML。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public final class RoleBootstrapTask implements NexusStartupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleBootstrapTask.class);

    /** 角色清单在启动上下文中的键；第 3 阶段读取该值。 */
    public static final String CONTEXT_ROLES = "console.bootstrap.roles";

    private final RoleAuthorizationManifestLoader loader;
    private final RoleDefinitionSyncService syncService;

    /**
     * 创建角色阶段。
     *
     * @param loader      角色授权清单加载器
     * @param syncService 角色同步服务
     */
    public RoleBootstrapTask(
            RoleAuthorizationManifestLoader loader,
            RoleDefinitionSyncService syncService
    ) {
        if (loader == null || syncService == null) {
            throw new IllegalArgumentException("Role bootstrap dependencies are required");
        }
        this.loader = loader;
        this.syncService = syncService;
    }

    /**
     * 返回名称。
     *
     * @return 任务名称
     */
    @Override
    public String name() {
        return BootstrapStage.ROLE.taskName();
    }

    /**
     * 返回执行顺序。
     *
     * @return 排序值
     */
    @Override
    public int order() {
        return BootstrapStage.ROLE.order();
    }

    /**
     * 执行角色同步，并把清单写入启动上下文。
     *
     * @param context 启动上下文
     */
    @Override
    public void run(NexusStartupContext context) {
        List<RoleDefinition> roles = loader.loadRoles();
        context.putAttribute(CONTEXT_ROLES, roles);
        if (roles.isEmpty()) {
            LOGGER.info("No role definitions configured; skip role bootstrap");
            return;
        }
        RoleDefinitionSyncService.SyncResult result = syncService.sync(roles);
        LOGGER.info(
                "Role bootstrap finished created={} updated={}",
                result.created(),
                result.updated());
    }
}
