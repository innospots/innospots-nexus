package com.innospots.nexus.console.bootstrap.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.bootstrap.domain.model.BootstrapStage;
import com.innospots.nexus.console.bootstrap.loader.InitialUserManifestLoader;
import com.innospots.nexus.console.bootstrap.service.InitialUserRoleBindingService;
import com.innospots.nexus.core.bootstrap.NexusStartupContext;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 启动引导第 5 阶段：把初始用户绑定到配置声明的角色。
 *
 * <p><b>前置依赖：</b>第 2 阶段创建角色（绑定目标），第 4 阶段创建用户（绑定主体）。
 * 这是整条管线的最后一环，顺序固定为 240。</p>
 *
 * <p>用户清单优先从启动上下文读取（第 4 阶段已解析并写入）；
 * 上下文中不存在时回退到重新加载。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public final class UserRoleBindingBootstrapTask implements NexusStartupTask {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserRoleBindingBootstrapTask.class);

    private final InitialUserManifestLoader loader;
    private final InitialUserRoleBindingService bindingService;

    /**
     * 创建用户角色绑定阶段。
     *
     * @param loader         初始用户清单加载器，作为上下文缺失时的回退
     * @param bindingService 绑定服务
     */
    public UserRoleBindingBootstrapTask(
            InitialUserManifestLoader loader,
            InitialUserRoleBindingService bindingService
    ) {
        if (loader == null || bindingService == null) {
            throw new IllegalArgumentException("User role binding dependencies are required");
        }
        this.loader = loader;
        this.bindingService = bindingService;
    }

    /**
     * 返回名称。
     *
     * @return 任务名称
     */
    @Override
    public String name() {
        return BootstrapStage.USER_ROLE_BINDING.taskName();
    }

    /**
     * 返回执行顺序。
     *
     * @return 排序值
     */
    @Override
    public int order() {
        return BootstrapStage.USER_ROLE_BINDING.order();
    }

    /**
     * 执行用户角色绑定。
     *
     * @param context 启动上下文
     */
    @Override
    public void run(NexusStartupContext context) {
        List<InitialUser> users = resolveUsers(context);
        if (users.isEmpty()) {
            LOGGER.info("No initial users configured; skip user role binding bootstrap");
            return;
        }
        InitialUserRoleBindingService.SyncResult result = bindingService.sync(users);
        LOGGER.info(
                "User role binding bootstrap finished bound={} skipped={}",
                result.bound(),
                result.skipped());
    }

    /**
     * 读取用户清单：优先用第 4 阶段写入上下文的实例。
     */
    @SuppressWarnings("unchecked")
    private List<InitialUser> resolveUsers(NexusStartupContext context) {
        return context.getAttribute(UserBootstrapTask.CONTEXT_USERS, List.class)
                .map(value -> (List<InitialUser>) value)
                .orElseGet(() -> loader.load().users());
    }
}
