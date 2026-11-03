package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.model.CatalogGrantKeys;
import com.innospots.nexus.console.catalog.domain.model.CatalogPermissionGroupDeclarations;
import com.innospots.nexus.console.catalog.domain.vo.CatalogModuleGrantVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantDescriptor;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.EndpointModuleGrantCatalog;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;

/**
 * 组装 MODULE {@link CatalogModuleGrantVo}（catalog 分组元数据 + EndpointGrant 目录）。
 *
 * <p>浏览分组来自 MODULE {@code viewGroups} 声明（按组授权，不含 endpoint 子项）；操作项取自
 * {@link EndpointModuleGrantCatalog#operable()}。</p>
 */
public final class CatalogPermissionCatalogAssembler {

    private static final String OPERATE_GROUP = ResolvedEndpointGrant.OPERATE_GRANT_GROUP;
    private static final String VIEW_GROUP = ResolvedEndpointGrant.VIEW_GRANT_GROUP;

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
        Set<String> declaredGroups = new java.util.LinkedHashSet<>();
        for (ConsolePermissionGroupDeclaration declaration : declarations) {
            declaredGroups.add(declaration.group());
        }
        EndpointModuleGrantCatalog catalog = endpointGrantManager
                .moduleCatalog(moduleRow.getModuleKey())
                .orElse(null);
        Map<String, List<CatalogViewItemVo>> itemsByGroup = new LinkedHashMap<>();
        for (ConsolePermissionGroupDeclaration declaration : declarations) {
            itemsByGroup.put(declaration.group(), new ArrayList<>());
        }
        if (catalog != null) {
            for (EndpointGrantDescriptor descriptor : catalog.operable()) {
                String group = resolveOperationGroupKey(descriptor, declaredGroups);
                if (group == null) {
                    continue;
                }
                itemsByGroup.get(group).add(grantItem(moduleRow, descriptor));
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
                    CatalogGrantKeys.of(
                            declaration.group(),
                            moduleRow.getDomainKey(),
                            moduleRow.getModuleKey()),
                    List.copyOf(items)));
        }
        return List.copyOf(groups);
    }

    /**
     * 将 operable grant 归入 catalog 声明的 {@code operationGroups}；未声明的分组不输出。
     *
     * <p>显式 {@link EndpointGrantDescriptor#grantGroup()}（非 {@code operate} 缺省）优先；否则按
     * {@link EndpointGrantAction} 映射 {@code create}/{@code update}/{@code delete}/{@code status}
     * 等；仍无匹配且声明含 {@code operate} 时归入 operate。</p>
     */
    private static String resolveOperationGroupKey(
            EndpointGrantDescriptor descriptor,
            Set<String> declaredGroups
    ) {
        if (descriptor == null || declaredGroups.isEmpty()) {
            return null;
        }
        if (hasText(descriptor.grantGroup()) && !OPERATE_GROUP.equals(descriptor.grantGroup())) {
            String explicit = descriptor.grantGroup();
            return declaredGroups.contains(explicit) ? explicit : null;
        }
        String actionGroup = operationGroupForAction(descriptor.action());
        if (declaredGroups.contains(actionGroup)) {
            return actionGroup;
        }
        if (declaredGroups.contains(OPERATE_GROUP)) {
            return OPERATE_GROUP;
        }
        return null;
    }

    private static String operationGroupForAction(EndpointGrantAction action) {
        if (action == null) {
            return OPERATE_GROUP;
        }
        return switch (action) {
            case CREATE -> "create";
            case UPDATE -> "update";
            case DELETE -> "delete";
            case STATUS -> "status";
            case EXECUTE -> "execute";
            case REPLACE -> "replace";
            case ASSIGN -> "assign";
            case REVOKE -> "revoke";
            case IMPORT -> "import";
            case WRITE -> "write";
            case READ, EXPORT -> VIEW_GROUP;
        };
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
