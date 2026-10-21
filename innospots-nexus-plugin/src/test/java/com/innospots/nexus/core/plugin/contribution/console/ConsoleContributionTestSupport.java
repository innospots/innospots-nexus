package com.innospots.nexus.core.plugin.contribution.console;

import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.i18n.I18nObject;

final class ConsoleContributionTestSupport {

    private ConsoleContributionTestSupport() {
    }

    static ConsolePluginContribution sampleContribution(String domainKey, String moduleKey, String entryPageKey) {
        ConsoleContributeNode menu = new ConsoleContributeNode(
                null,
                I18nObject.of(Map.of("en", "Menu")),
                I18nObject.of(Map.of()),
                0,
                entryPageKey,
                null,
                "default",
                null,
                List.of(),
                List.of(),
                List.of());
        ConsoleContributeNode module = new ConsoleContributeNode(
                moduleKey,
                I18nObject.of(Map.of("en", "Module")),
                I18nObject.of(Map.of()),
                0,
                null,
                null,
                null,
                null,
                defaultViewGroups(),
                defaultOperationGroups(),
                List.of(menu));
        return new ConsolePluginContribution(
                domainKey,
                I18nObject.of(Map.of("en", "Root")),
                I18nObject.of(Map.of()),
                0,
                List.of(module));
    }

    private static List<ConsolePermissionGroupDeclaration> defaultViewGroups() {
        return List.of(new ConsolePermissionGroupDeclaration(
                "default",
                I18nObject.of(Map.of("en", "Views"))));
    }

    private static List<ConsolePermissionGroupDeclaration> defaultOperationGroups() {
        return List.of(new ConsolePermissionGroupDeclaration(
                "default",
                I18nObject.of(Map.of("en", "Ops"))));
    }
}
