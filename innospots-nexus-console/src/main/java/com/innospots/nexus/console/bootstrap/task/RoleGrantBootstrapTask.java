package com.innospots.nexus.console.bootstrap.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage;
import com.innospots.nexus.console.bootstrap.loader.RoleAuthorizationManifestLoader;
import com.innospots.nexus.console.bootstrap.service.RoleGrantSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 启动引导第 3 阶段：按配置写入角色资源授权。
 *
 * <p><b>前置依赖：</b>第 1 阶段建立权限目录（用于校验授权键），
 * 第 2 阶段创建角色（作为授权主体）。因此顺序固定为 200 → 210 → 220。</p>
 *
 * <p>角色清单优先从启动上下文读取（第 2 阶段已解析并写入）；
 * 若上下文中不存在（例如单独注册本阶段做测试），则回退到重新加载。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public final class RoleGrantBootstrapTask implements NexusStartupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleGrantBootstrapTask.class);

    private final RoleAuthorizationManifestLoader loader;
    private final RoleGrantSyncService syncService;

    /**
     * 创建角色授权阶段。
     *
     * @param loader      角色授权清单加载器，作为上下文缺失时的回退
     * @param syncService 角色授权同步服务
     */
    public RoleGrantBootstrapTask(
            RoleAuthorizationManifestLoader loader,
            RoleGrantSyncService syncService
    ) {
        if (loader == null || syncService == null) {
            throw new IllegalArgumentException("Role grant bootstrap dependencies are required");
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
        return BootstrapStage.ROLE_GRANT.taskName();
    }

    /**
     * 返回执行顺序。
     *
     * @return 排序值
     */
    @Override
    public int order() {
        return BootstrapStage.ROLE_GRANT.order();
    }

    /**
     * 执行角色授权同步。
     *
     * @param context 启动上下文
     */
    @Override
    public void run(NexusStartupContext context) {
        List<RoleDefinition> roles = resolveRoles(context);
        if (roles.isEmpty()) {
            LOGGER.info("No role definitions configured; skip role grant bootstrap");
            return;
        }
        RoleGrantSyncService.SyncResult result = syncService.sync(roles);
        LOGGER.info(
                "Role grant bootstrap finished created={} updated={} skipped={}",
                result.created(),
                result.updated(),
                result.skipped());
    }

    /**
     * 读取角色清单：优先用第 2 阶段写入上下文的实例。
     */
    @SuppressWarnings("unchecked")
    private List<RoleDefinition> resolveRoles(NexusStartupContext context) {
        return context.getAttribute(RoleBootstrapTask.CONTEXT_ROLES, List.class)
                .map(value -> (List<RoleDefinition>) value)
                .orElseGet(loader::loadRoles);
    }
}
