package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.model.CatalogGrantKeys;
import com.innospots.nexus.console.catalog.domain.vo.CatalogModuleGrantVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;

/**
 * 组装 MODULE {@link CatalogModuleGrantVo}（catalog 表 + EndpointGrant）。
 *
 * <p>访问项 {@code grantName} 恒为 MENU 的 {@code entryPageKey}（与 console@2 贡献一致）。</p>
 */
public final class CatalogPermissionCatalogAssembler {

    private final EndpointGrantManager endpointGrantManager;

    public CatalogPermissionCatalogAssembler(EndpointGrantManager endpointGrantManager) {
        this.endpointGrantManager = endpointGrantManager;
    }

    public CatalogModuleGrantVo assembleModuleGrants(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        List<CatalogViewItemVo> viewItems = new ArrayList<>();
        if (hasText(moduleRow.getEntryPageKey())) {
            viewItems.add(viewItem(moduleRow, moduleRow.getEntryPageKey()));
        }
        for (ConsoleCatalogResourceEntity menu : directMenus) {
            if (hasText(menu.getEntryPageKey())) {
                viewItems.add(viewItem(menu, menu.getEntryPageKey()));
            }
        }
        viewItems.sort(Comparator.comparing(CatalogViewItemVo::orderIndex,
                Comparator.nullsLast(Integer::compareTo)));

        List<CatalogPermissionGroupVo> operationGroups = buildOperationGroups(moduleRow, directMenus);
        return new CatalogModuleGrantVo(List.copyOf(viewItems), operationGroups);
    }

    private static CatalogViewItemVo viewItem(
            ConsoleCatalogResourceEntity resource,
            String grantName
    ) {
        return new CatalogViewItemVo(
                CatalogGrantKeys.of(grantName, resource.getDomainKey(), resource.getModuleKey()),
                grantName,
                parseI18n(resource.getDisplayName()),
                parseI18n(resource.getDescription()),
                resource.getSortOrder());
    }

    private List<CatalogPermissionGroupVo> buildOperationGroups(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        String groupsJson = hasText(moduleRow.getOperationGroups())
                ? moduleRow.getOperationGroups()
                : directMenus.isEmpty() ? null : directMenus.getFirst().getOperationGroups();
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
                                CatalogGrantKeys.of(
                                        descriptor.name(),
                                        moduleRow.getDomainKey(),
                                        moduleRow.getModuleKey()),
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

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
