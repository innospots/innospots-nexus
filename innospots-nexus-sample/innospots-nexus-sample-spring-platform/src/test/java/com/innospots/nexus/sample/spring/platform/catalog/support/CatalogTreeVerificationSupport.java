package com.innospots.nexus.sample.spring.platform.catalog.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;

/**
 * 权限目录树 HTTP 响应解析与 {@link CatalogNodeVo} / grants 校验辅助。
 */
public final class CatalogTreeVerificationSupport {

    private CatalogTreeVerificationSupport() {
    }

    public static List<CatalogNodeVo> parseTreeResponse(String responseBody) {
        JsonNode data = CatalogTreeJsonSupport.parseDataArray(responseBody);
        if (data == null || data.isNull()) {
            return List.of();
        }
        return Jsons.mapper().convertValue(data, new TypeReference<List<CatalogNodeVo>>() {
        });
    }

    public static List<CatalogNodeVo> flattenNodes(List<CatalogNodeVo> roots) {
        List<CatalogNodeVo> flat = new ArrayList<>();
        for (CatalogNodeVo root : roots) {
            walk(root, flat);
        }
        return flat;
    }

    private static void walk(CatalogNodeVo node, List<CatalogNodeVo> flat) {
        flat.add(node);
        for (CatalogNodeVo child : node.children()) {
            walk(child, flat);
        }
    }

    public static Map<String, Set<String>> operableGrantNamesByModule(EndpointGrantManager endpointGrantManager) {
        Map<String, Set<String>> namesByModule = new LinkedHashMap<>();
        for (Map.Entry<String, EndpointModuleGrantCatalogVo> entry
                : endpointGrantManager.allModuleCatalogs().entrySet()) {
            Set<String> names = entry.getValue().operable().stream()
                    .map(EndpointGrantDescriptorVo::name)
                    .collect(Collectors.toCollection(TreeSet::new));
            namesByModule.put(entry.getKey(), names);
        }
        return namesByModule;
    }

    public static Map<String, Object> summarizeGrants(List<CatalogNodeVo> roots) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("rootModuleCount", roots.size());
        summary.put("totalNodeCount", flattenNodes(roots).size());
        Map<String, Integer> operationCountsByModule = new LinkedHashMap<>();
        for (CatalogNodeVo node : flattenNodes(roots)) {
            if (node.resourceType() == CatalogResourceType.MODULE && node.grants() != null) {
                int count = node.grants().operationGroups().stream()
                        .mapToInt(group -> group.items().size())
                        .sum();
                operationCountsByModule.put(node.moduleKey(), count);
            }
        }
        summary.put("operationGrantCountsByModuleKey", operationCountsByModule);
        return summary;
    }

    public static void assertModuleOperationGrantsMatchEndpointCatalog(
            CatalogNodeVo moduleNode,
            Map<String, Set<String>> operableNamesByModule
    ) {
        if (moduleNode.resourceType() != CatalogResourceType.MODULE || moduleNode.grants() == null) {
            return;
        }
        Set<String> expected = operableNamesByModule.getOrDefault(moduleNode.moduleKey(), Set.of());
        Set<String> actual = moduleNode.grants().operationGroups().stream()
                .flatMap(group -> group.items().stream())
                .map(CatalogViewItemVo::grantName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        org.assertj.core.api.Assertions.assertThat(actual)
                .as("operation grants for moduleKey=%s resourceKey=%s",
                        moduleNode.moduleKey(), moduleNode.resourceKey())
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    public static void assertOperationGrantsExcludeReadOnlyActions(
            CatalogNodeVo moduleNode,
            EndpointGrantManager endpointGrantManager
    ) {
        if (moduleNode.resourceType() != CatalogResourceType.MODULE || moduleNode.grants() == null) {
            return;
        }
        EndpointModuleGrantCatalogVo catalog = endpointGrantManager
                .moduleCatalog(moduleNode.moduleKey())
                .orElse(null);
        if (catalog == null) {
            return;
        }
        Map<String, EndpointGrantAction> actionByName = catalog.operable().stream()
                .collect(Collectors.toMap(EndpointGrantDescriptorVo::name, EndpointGrantDescriptorVo::action));
        for (CatalogViewItemVo item : moduleNode.grants().operationGroups().stream()
                .flatMap(group -> group.items().stream())
                .toList()) {
            EndpointGrantAction action = actionByName.get(item.grantName());
            org.assertj.core.api.Assertions.assertThat(action)
                    .as("operation grant %s", item.grantName())
                    .isNotIn(EndpointGrantAction.READ, EndpointGrantAction.EXPORT);
        }
    }
}
