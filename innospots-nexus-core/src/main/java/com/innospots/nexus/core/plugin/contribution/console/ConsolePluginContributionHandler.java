package com.innospots.nexus.core.plugin.contribution.console;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.capability.ProviderRef;
import com.innospots.nexus.core.plugin.contribution.PluginContributionContext;
import com.innospots.nexus.core.plugin.contribution.PluginContributionEntry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionType;
import com.innospots.nexus.core.plugin.contribution.PreparedPluginContribution;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** Console Contribution 的全局资源校验器和活动目录事务适配器。 */
public final class ConsolePluginContributionHandler
        implements PluginContributionHandler<ConsolePluginContribution> {

    private final ConsoleContributionCatalog catalog;
    private final ReservedPluginResourceCatalog reservedResources;

    /**
     * 创建 Console Handler。
     *
     * @param catalog           活动 Console 资源目录
     * @param reservedResources 平台保留资源表，用于拒绝插件抢占系统身份
     * @throws NexusException 任一目录为空时抛出
     */
    public ConsolePluginContributionHandler(
            ConsoleContributionCatalog catalog,
            ReservedPluginResourceCatalog reservedResources
    ) {
        if (catalog == null || reservedResources == null) {
            throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                    "console contribution catalogs are required");
        }
        this.catalog = catalog;
        this.reservedResources = reservedResources;
    }

    /** 返回 console@1 类型。 */
    @Override
    public PluginContributionType<ConsolePluginContribution> type() {
        return ConsolePluginContribution.TYPE;
    }

    /**
     * 对模块、页面、菜单、路径和历史归属执行无副作用全局校验。
     *
     * @param pluginCatalog 当前有效插件目录；单插件 prepare 时可传 {@code null}
     * @param entries       待校验的 console@1 贡献条目
     * @throws NexusException 资源冲突、路由冲突或菜单引用非法时抛出
     */
    @Override
    public void validate(
            PluginCatalog pluginCatalog,
            List<PluginContributionEntry<ConsolePluginContribution>> entries
    ) {
        if (entries == null) {
            invalid("console contribution entries are required");
        }
        Map<String, String> resourceOwners = new HashMap<>();
        Map<String, String> routeOwners = new HashMap<>();
        for (PluginContributionEntry<ConsolePluginContribution> entry : entries) {
            if (entry == null) {
                invalid("console contribution entry must not be null");
            }
            String owner = entry.owner().pluginId();
            for (ConsoleModuleDeclaration module : entry.contribution().modules()) {
                claim(resourceOwners, owner, "MODULE", module.resourceKey());
                validateModule(owner, module, resourceOwners, routeOwners);
            }
        }
    }

    /**
     * 准备一个插件的 Console 资源，提交前不进入活动目录。
     *
     * @param context      当前插件启动上下文
     * @param contribution 待准备的贡献声明
     * @return 可在提交或回滚时关闭的预备资源
     * @throws NexusException 全局校验失败或目录准备失败时抛出
     */
    @Override
    public PreparedPluginContribution prepare(
            PluginContributionContext context,
            ConsolePluginContribution contribution
    ) {
        if (context == null || contribution == null) {
            invalid("console contribution context and value are required");
        }
        validate(null, List.of(new PluginContributionEntry<>(context.owner(), contribution)));
        return catalog.prepare(context.owner(), contribution, context.availability());
    }

    /** 返回供权限同步使用的活动资源目录。 */
    public ConsoleContributionCatalog catalog() {
        return catalog;
    }

    private void validateModule(
            String owner,
            ConsoleModuleDeclaration module,
            Map<String, String> resourceOwners,
            Map<String, String> routeOwners
    ) {
        Map<String, UiSpecPageDeclaration> pages = new HashMap<>();
        collectPages(owner, module, module.pages(), pages, resourceOwners, routeOwners,
                new HashSet<>());
        Set<String> menuKeys = new HashSet<>();
        Set<String> referencedPages = new HashSet<>();
        collectMenus(owner, module, module.menuTree(), pages, menuKeys, referencedPages, resourceOwners);
    }

    private void collectPages(
            String owner,
            ConsoleModuleDeclaration module,
            List<UiSpecPageDeclaration> declarations,
            Map<String, UiSpecPageDeclaration> pages,
            Map<String, String> resourceOwners,
            Map<String, String> routeOwners,
            Set<String> ancestors
    ) {
        for (UiSpecPageDeclaration page : declarations) {
            // 沿祖先链检测环，避免嵌套 children 形成不可遍历的页面图。
            if (!pages.isEmpty() && ancestors.contains(page.pageKey())) {
                invalid("page declaration contains a cycle: " + page.pageKey());
            }
            if (pages.putIfAbsent(page.pageKey(), page) != null) {
                invalid("duplicate pageKey in module " + module.moduleKey() + ": " + page.pageKey());
            }
            claim(resourceOwners, owner, "PAGE", page.resourceKey(module.moduleKey()));
            // 路由匹配不携带模块身份，因此相同规范化路径在不同模块间也必须拒绝。
            String routeKey = page.normalizedRouteTemplate();
            String routeOwner = routeOwners.putIfAbsent(routeKey, owner);
            if (routeOwner != null) {
                invalid("pagePath conflicts with plugin " + routeOwner + ": " + page.pagePath());
            }
            Set<String> nextAncestors = new HashSet<>(ancestors);
            nextAncestors.add(page.pageKey());
            collectPages(owner, module, page.children(), pages, resourceOwners, routeOwners, nextAncestors);
        }
    }

    private void collectMenus(
            String owner,
            ConsoleModuleDeclaration module,
            List<MenuDeclaration> menus,
            Map<String, UiSpecPageDeclaration> pages,
            Set<String> menuKeys,
            Set<String> referencedPages,
            Map<String, String> resourceOwners
    ) {
        for (MenuDeclaration menu : menus) {
            if (!menuKeys.add(menu.menuKey())) {
                invalid("duplicate menuKey in module " + module.moduleKey() + ": " + menu.menuKey());
            }
            claim(resourceOwners, owner, "MENU", menu.resourceKey(module.moduleKey()));
            if (menu.pageKey() != null) {
                UiSpecPageDeclaration page = pages.get(menu.pageKey());
                if (page == null) {
                    invalid("menu references an unknown page: " + menu.pageKey());
                }
                if (page.hasRequiredPathVariables()) {
                    // 带必填路径变量的页面只能动态导航，不能作为静态菜单落点。
                    invalid("page with required path variables cannot be a static menu entry: "
                            + menu.pageKey());
                }
                if (!referencedPages.add(menu.pageKey())) {
                    // 一个页面只能被一个菜单项直接引用，避免权限和面包屑归属歧义。
                    invalid("page is referenced by multiple menu entries: " + menu.pageKey());
                }
            }
            collectMenus(owner, module, menu.children(), pages, menuKeys, referencedPages, resourceOwners);
        }
    }

    private void claim(Map<String, String> resourceOwners, String owner, String type, String resourceKey) {
        String reservedOwner = reservedResources.ownerOf(type, resourceKey).orElse(null);
        if (reservedOwner != null && !reservedOwner.equals(owner)) {
            invalid("resource identity is reserved by another plugin: " + resourceKey);
        }
        String identity = type + ":" + resourceKey;
        String previous = resourceOwners.putIfAbsent(identity, owner);
        if (previous != null && !previous.equals(owner)) {
            invalid("resource identity is owned by another plugin: " + resourceKey);
        }
        if (previous != null) {
            // 同一插件重复声明同一资源身份，说明模块/页面/菜单键冲突。
            invalid("duplicate resource identity: " + resourceKey);
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT, message);
    }
}
