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
import com.innospots.nexus.console.catalog.domain.vo.CatalogDomainVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantDescriptor;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.EndpointModuleGrantCatalog;

/**
 * 权限目录 HTTP 响应解析与 {@link CatalogDomainVo} / {@link CatalogNodeVo} 校验辅助。
 */
public final class CatalogTreeVerificationSupport {

    private CatalogTreeVerificationSupport() {
    }

    public static List<CatalogDomainVo> parsePermissionCatalogResponse(String responseBody) {
        JsonNode data = CatalogTreeJsonSupport.parseDataArray(responseBody);
        if (data == null || data.isNull()) {
            return List.of();
        }
        return Jsons.mapper().convertValue(data, new TypeReference<List<CatalogDomainVo>>() {
        });
    }

    public static List<CatalogNodeVo> flattenModuleNodes(List<CatalogDomainVo> domains) {
        List<CatalogNodeVo> flat = new ArrayList<>();
        for (CatalogDomainVo domain : domains) {
            for (CatalogNodeVo module : domain.modules()) {
                walk(module, flat);
            }
        }
        return flat;
    }

    public static CatalogNodeVo findModuleByResourceKey(List<CatalogDomainVo> domains, String resourceKey) {
        return flattenModuleNodes(domains).stream()
                .filter(node -> node.resourceType() == CatalogResourceType.MODULE)
                .filter(node -> resourceKey.equals(node.resourceKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("module not found: " + resourceKey));
    }

    public static CatalogNodeVo findModuleByModuleKey(List<CatalogDomainVo> domains, String moduleKey) {
        return flattenModuleNodes(domains).stream()
                .filter(node -> node.resourceType() == CatalogResourceType.MODULE)
                .filter(node -> moduleKey.equals(node.moduleKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("module not found: " + moduleKey));
    }

    private static void walk(CatalogNodeVo node, List<CatalogNodeVo> flat) {
        flat.add(node);
        for (CatalogNodeVo child : node.children()) {
            walk(child, flat);
        }
    }

    public static Map<String, Set<String>> operableGrantNamesByModule(EndpointGrantManager endpointGrantManager) {
        Map<String, Set<String>> namesByModule = new LinkedHashMap<>();
        for (Map.Entry<String, EndpointModuleGrantCatalog> entry
                : endpointGrantManager.allModuleCatalogs().entrySet()) {
            Set<String> names = entry.getValue().operable().stream()
                    .map(EndpointGrantDescriptor::name)
                    .collect(Collectors.toCollection(TreeSet::new));
            namesByModule.put(entry.getKey(), names);
        }
        return namesByModule;
    }

    public static Map<String, Object> summarizeGrants(List<CatalogDomainVo> domains) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("domainCount", domains.size());
        summary.put("totalModuleMenuNodeCount", flattenModuleNodes(domains).size());
        Map<String, Integer> operationCountsByModule = new LinkedHashMap<>();
        for (CatalogNodeVo node : flattenModuleNodes(domains)) {
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
        EndpointModuleGrantCatalog catalog = endpointGrantManager
                .moduleCatalog(moduleNode.moduleKey())
                .orElse(null);
        if (catalog == null) {
            return;
        }
        Map<String, EndpointGrantAction> actionByName = catalog.operable().stream()
                .collect(Collectors.toMap(EndpointGrantDescriptor::name, EndpointGrantDescriptor::action));
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
