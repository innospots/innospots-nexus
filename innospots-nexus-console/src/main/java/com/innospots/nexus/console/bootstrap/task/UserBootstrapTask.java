package com.innospots.nexus.console.bootstrap.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage;
import com.innospots.nexus.console.bootstrap.loader.InitialUserManifestLoader;
import com.innospots.nexus.console.bootstrap.loader.InitialUserManifestLoader.LoadedInitialUsers;
import com.innospots.nexus.console.bootstrap.service.InitialUserSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 启动引导第 4 阶段：按配置创建初始后台用户。
 *
 * <p>本阶段把解析后的用户清单写入 {@link NexusStartupContext}，供第 5 阶段复用。</p>
 *
 * <p><b>开关：</b>{@code console/bootstrap/users.yaml} 中任一文件声明
 * {@code enabled: false} 即整体跳过。关闭时同样会把空清单写入上下文，
 * 使第 5 阶段自然跳过绑定。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public final class UserBootstrapTask implements NexusStartupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserBootstrapTask.class);

    /** 初始用户清单在启动上下文中的键；第 5 阶段读取该值。 */
    public static final String CONTEXT_USERS = "console.bootstrap.users";

    private final InitialUserManifestLoader loader;
    private final InitialUserSyncService syncService;

    /**
     * 创建用户阶段。
     *
     * @param loader      初始用户清单加载器
     * @param syncService 用户同步服务
     */
    public UserBootstrapTask(InitialUserManifestLoader loader, InitialUserSyncService syncService) {
        if (loader == null || syncService == null) {
            throw new IllegalArgumentException("User bootstrap dependencies are required");
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
        return BootstrapStage.USER.taskName();
    }

    /**
     * 返回执行顺序。
     *
     * @return 排序值
     */
    @Override
    public int order() {
        return BootstrapStage.USER.order();
    }

    /**
     * 执行初始用户创建。
     *
     * @param context 启动上下文
     */
    @Override
    public void run(NexusStartupContext context) {
        LoadedInitialUsers loaded = loader.load();
        if (!loaded.enabled()) {
            LOGGER.info("Initial user sync is disabled by configuration; skip user bootstrap");
            context.putAttribute(CONTEXT_USERS, List.<InitialUser>of());
            return;
        }
        context.putAttribute(CONTEXT_USERS, loaded.users());
        if (loaded.users().isEmpty()) {
            LOGGER.info("No initial users configured; skip user bootstrap");
            return;
        }
        InitialUserSyncService.SyncResult result = syncService.sync(loaded.users());
        LOGGER.info(
                "User bootstrap finished created={} skipped={}",
                result.created(),
                result.skipped());
    }
}
