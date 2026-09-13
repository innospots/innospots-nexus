package com.innospots.nexus.core.plugin.contribution.console;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotter;

/** 仅保存 Console 模块、页面、菜单稳定身份的安全快照器。 */
public final class ConsolePluginContributionSnapshotter
        implements PluginContributionSnapshotter<ConsolePluginContribution> {

    /** 返回 console@1 类型。 */
    @Override
    public com.innospots.nexus.core.plugin.contribution.PluginContributionType<ConsolePluginContribution> type() {
        return ConsolePluginContribution.TYPE;
    }

    /** 生成不含 UiSpec 正文、Class、Handler 和 Secret 的稳定资源摘要。 */
    @Override
    public Map<String, Object> snapshot(ConsolePluginContribution contribution) {
        List<Map<String, Object>> modules = contribution.modules().stream()
                .map(ConsolePluginContributionSnapshotter::module)
                .toList();
        return Map.of("type", ConsolePluginContribution.TYPE.toString(), "modules", modules);
    }

    private static Map<String, Object> module(ConsoleModuleDeclaration module) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("moduleKey", module.moduleKey());
        value.put("resourceKey", module.resourceKey());
        value.put("pages", module.pages().stream()
                .map(page -> page(module.moduleKey(), page)).toList());
        value.put("menuTree", module.menuTree().stream()
                .map(menu -> menu(module.moduleKey(), menu)).toList());
        return Map.copyOf(value);
    }

    private static Map<String, Object> page(String moduleKey, UiSpecPageDeclaration page) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("pageKey", page.pageKey());
        value.put("pagePath", page.pagePath());
        value.put("resourceKey", page.resourceKey(moduleKey));
        value.put("children", page.children().stream()
                .map(child -> page(moduleKey, child)).toList());
        return Map.copyOf(value);
    }

    private static Map<String, Object> menu(String moduleKey, MenuDeclaration menu) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("menuKey", menu.menuKey());
        if (menu.pageKey() != null) {
            value.put("pageKey", menu.pageKey());
        }
        value.put("resourceKey", menu.resourceKey(moduleKey));
        value.put("children", menu.children().stream()
                .map(child -> menu(moduleKey, child)).toList());
        return Map.copyOf(value);
    }
}
