package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.model.CatalogGrantKeys;
import com.innospots.nexus.console.catalog.domain.model.CatalogPermissionGroupDeclarations;
import com.innospots.nexus.console.catalog.domain.vo.CatalogModuleGrantVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantDescriptor;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.EndpointModuleGrantCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;

/**
 * 组装 MODULE {@link CatalogModuleGrantVo}（catalog 分组元数据 + EndpointGrant 目录）。
 *
 * <p>浏览分组来自 MODULE {@code viewGroups} 声明（按组授权，不含 endpoint 子项）；操作项取自
 * {@link EndpointModuleGrantCatalog#operable()}。</p>
 */
public final class CatalogPermissionCatalogAssembler {

    private static final String DEFAULT_GROUP = "default";

    private final EndpointGrantManager endpointGrantManager;

    public CatalogPermissionCatalogAssembler(EndpointGrantManager endpointGrantManager) {
        this.endpointGrantManager = endpointGrantManager;
    }

    public CatalogModuleGrantVo assembleModuleGrants(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        return new CatalogModuleGrantVo(
                buildViewGroups(moduleRow, directMenus),
                buildOperationGroups(moduleRow, directMenus));
    }

    /**
     * 按 MODULE/MENU 的 {@code viewGroups} 声明组装浏览分组（组级 {@code resourceKey}，无 items）。
     */
    List<CatalogPermissionGroupVo> buildViewGroups(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        String groupsJson = resolveGroupsJson(moduleRow.getViewGroups(), directMenus,
                ConsoleCatalogResourceEntity::getViewGroups);
        List<ConsolePermissionGroupDeclaration> declarations =
                CatalogPermissionGroupDeclarations.parseGroups(groupsJson);
        List<CatalogPermissionGroupVo> groups = new ArrayList<>();
        for (ConsolePermissionGroupDeclaration declaration : declarations) {
            groups.add(new CatalogPermissionGroupVo(
                    declaration.group(),
                    declaration.label(),
                    null,
                    CatalogGrantKeys.viewGroupKey(
                            moduleRow.getDomainKey(),
                            moduleRow.getModuleKey(),
                            declaration.group()),
                    List.of()));
        }
        return List.copyOf(groups);
    }

    /**
     * 按 MODULE/MENU 的 {@code operationGroups} 声明与模块 {@code operable} grants 组装操作分组。
     */
    List<CatalogPermissionGroupVo> buildOperationGroups(
            ConsoleCatalogResourceEntity moduleRow,
            List<ConsoleCatalogResourceEntity> directMenus
    ) {
        return buildOperationGrantGroups(
                moduleRow,
                resolveGroupsJson(moduleRow.getOperationGroups(), directMenus,
                        ConsoleCatalogResourceEntity::getOperationGroups));
    }

    private List<CatalogPermissionGroupVo> buildOperationGrantGroups(
            ConsoleCatalogResourceEntity moduleRow,
            String groupsJson
    ) {
        List<ConsolePermissionGroupDeclaration> declarations =
                CatalogPermissionGroupDeclarations.parseGroups(groupsJson);
        EndpointModuleGrantCatalog catalog = endpointGrantManager
                .moduleCatalog(moduleRow.getModuleKey())
                .orElse(null);
        Map<String, List<CatalogViewItemVo>> itemsByGroup = new LinkedHashMap<>();
        for (ConsolePermissionGroupDeclaration declaration : declarations) {
            itemsByGroup.put(declaration.group(), new ArrayList<>());
        }
        if (catalog != null) {
            for (EndpointGrantDescriptor descriptor : catalog.operable()) {
                String group = hasText(descriptor.grantGroup()) ? descriptor.grantGroup() : DEFAULT_GROUP;
                itemsByGroup.computeIfAbsent(group, ignored -> new ArrayList<>())
                        .add(grantItem(moduleRow, descriptor));
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
                    null,
                    List.copyOf(items)));
        }
        return List.copyOf(groups);
    }

    private static CatalogViewItemVo grantItem(
            ConsoleCatalogResourceEntity moduleRow,
            EndpointGrantDescriptor descriptor
    ) {
        String domainKey = hasText(descriptor.domainKey())
                ? descriptor.domainKey()
                : moduleRow.getDomainKey();
        String grantName = descriptor.name();
        return new CatalogViewItemVo(
                CatalogGrantKeys.of(
                        grantName,
                        domainKey,
                        moduleRow.getModuleKey()),
                descriptor.name(),
                descriptor.label(),
                I18nObject.of(Map.of()),
                null);
    }

    private static String resolveGroupsJson(
            String moduleGroupsJson,
            List<ConsoleCatalogResourceEntity> directMenus,
            Function<ConsoleCatalogResourceEntity, String> menuGroups
    ) {
        if (hasText(moduleGroupsJson)) {
            return moduleGroupsJson;
        }
        if (directMenus == null || directMenus.isEmpty()) {
            return null;
        }
        return menuGroups.apply(directMenus.getFirst());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
