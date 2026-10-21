package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogModuleGrantVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;

/**
 * 组装 MODULE {@link CatalogModuleGrantVo}（DB + contribution + EndpointGrant）。
 */
public final class CatalogPermissionCatalogAssembler {

    private final ConsoleContributionCatalog contributionCatalog;
    private final EndpointGrantManager endpointGrantManager;

    public CatalogPermissionCatalogAssembler(
            ConsoleContributionCatalog contributionCatalog,
            EndpointGrantManager endpointGrantManager
    ) {
        this.contributionCatalog = contributionCatalog;
        this.endpointGrantManager = endpointGrantManager;
    }

    public CatalogModuleGrantVo assembleModuleGrants(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        List<CatalogViewItemVo> viewItems = new ArrayList<>();
        for (ConsoleCatalogResourceEntity menu : directMenus) {
            String grantName = resolveGrantName(moduleRow, menu);
            viewItems.add(new CatalogViewItemVo(
                    grantName + ":" + menu.getDomainKey() + "." + menu.getModuleKey(),
                    grantName,
                    parseI18n(menu.getDisplayName()),
                    parseI18n(menu.getDescription()),
                    menu.getSortOrder()));
        }
        viewItems.sort(Comparator.comparing(CatalogViewItemVo::orderIndex,
                Comparator.nullsLast(Integer::compareTo)));

        List<CatalogPermissionGroupVo> operationGroups = buildOperationGroups(moduleRow, directMenus);
        return new CatalogModuleGrantVo(List.copyOf(viewItems), operationGroups);
    }

    private List<CatalogPermissionGroupVo> buildOperationGroups(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        String groupsJson = directMenus.isEmpty() ? null : directMenus.getFirst().getOperationGroups();
        List<ConsolePermissionGroupDeclaration> declarations = parseGroups(groupsJson);
        EndpointModuleGrantCatalogVo catalog = endpointGrantManager
                .moduleCatalog(moduleRow.getModuleKey())
                .orElse(null);
        Map<String, List<CatalogViewItemVo>> itemsByGroup = new LinkedHashMap<>();
        for (ConsolePermissionGroupDeclaration declaration : declarations) {
            itemsByGroup.put(declaration.group(), new ArrayList<>());
        }
        if (catalog != null) {
            for (EndpointGrantDescriptorVo descriptor : catalog.operable()) {
                String group = "default";
                itemsByGroup.computeIfAbsent(group, ignored -> new ArrayList<>())
                        .add(new CatalogViewItemVo(
                                descriptor.name() + ":" + moduleRow.getDomainKey()
                                        + "." + moduleRow.getModuleKey(),
                                descriptor.name(),
                                descriptor.label(),
                                I18nObject.of(Map.of()),
                                null));
            }
        }
        List<CatalogPermissionGroupVo> groups = new ArrayList<>();
        for (ConsolePermissionGroupDeclaration declaration : declarations) {
            List<CatalogViewItemVo> items = itemsByGroup.getOrDefault(
                    declaration.group(), List.of());
            groups.add(new CatalogPermissionGroupVo(
                    declaration.group(),
                    declaration.label(),
                    null,
                    List.copyOf(items)));
        }
        return List.copyOf(groups);
    }

    private String resolveGrantName(
            ConsoleCatalogResourceEntity moduleRow,
            ConsoleCatalogResourceEntity menu
    ) {
        Optional<ConsoleContributeNode> node = findMenuNode(
                menu.getOwnerPluginId(),
                moduleRow.getDomainKey(),
                moduleRow.getModuleKey(),
                menu.getEntryPageKey());
        if (node.isPresent()) {
            return node.get().resolvedGrantName();
        }
        return "view." + menu.getEntryPageKey();
    }

    private Optional<ConsoleContributeNode> findMenuNode(
            String ownerPluginId,
            String domainKey,
            String moduleKey,
            String entryPageKey
    ) {
        for (ConsoleContributionCatalog.ActiveConsoleContribution active
                : contributionCatalog.activeContributions()) {
            if (!active.ownerPluginId().equals(ownerPluginId)) {
                continue;
            }
            ConsolePluginContribution contribution = active.contribution();
            if (!contribution.domainKey().equals(domainKey)) {
                continue;
            }
            for (ConsoleContributeNode module : contribution.modules()) {
                Optional<ConsoleContributeNode> found = findMenuInModule(
                        module, moduleKey, entryPageKey);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ConsoleContributeNode> findMenuInModule(
            ConsoleContributeNode module,
            String moduleKey,
            String entryPageKey
    ) {
        if (!module.isModule() || !module.moduleKey().equals(moduleKey)) {
            return Optional.empty();
        }
        for (ConsoleContributeNode child : module.children()) {
            if (child.isMenu() && child.entryPageKey().equals(entryPageKey)) {
                return Optional.of(child);
            }
            if (child.isModule()) {
                Optional<ConsoleContributeNode> nested = findMenuInModule(
                        child, moduleKey, entryPageKey);
                if (nested.isPresent()) {
                    return nested;
                }
            }
        }
        return Optional.empty();
    }

    private static List<ConsolePermissionGroupDeclaration> parseGroups(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return Jsons.fromJson(json, new TypeReference<List<ConsolePermissionGroupDeclaration>>() {
        });
    }

    private static I18nObject parseI18n(String json) {
        if (json == null || json.isBlank()) {
            return I18nObject.of(Map.of());
        }
        return Jsons.fromJson(json, I18nObject.class);
    }
}
