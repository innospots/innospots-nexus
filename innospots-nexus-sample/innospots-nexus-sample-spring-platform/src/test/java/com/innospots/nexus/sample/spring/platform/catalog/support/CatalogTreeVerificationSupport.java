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
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeOperationVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;

/**
 * 目录树 HTTP 响应解析与 {@link CatalogNodeVo} / operations 校验辅助。
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

    public static Map<String, Object> summarizeOperations(List<CatalogNodeVo> roots) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("rootModuleCount", roots.size());
        summary.put("totalNodeCount", flattenNodes(roots).size());
        Map<String, Integer> operationCountsByModule = new LinkedHashMap<>();
        for (CatalogNodeVo node : flattenNodes(roots)) {
            operationCountsByModule.put(
                    node.moduleKey(),
                    node.operations().size());
        }
        summary.put("operationCountsByModuleKey", operationCountsByModule);
        return summary;
    }

    public static void assertOperationsBelongToModule(
            CatalogNodeVo node,
            Map<String, Set<String>> operableNamesByModule
    ) {
        Set<String> expected = operableNamesByModule.getOrDefault(node.moduleKey(), Set.of());
        Set<String> actual = node.operations().stream()
                .map(CatalogNodeOperationVo::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        org.assertj.core.api.Assertions.assertThat(actual)
                .as("operations for moduleKey=%s resourceKey=%s", node.moduleKey(), node.resourceKey())
                .containsExactlyInAnyOrderElementsOf(expected);
        for (CatalogNodeOperationVo operation : node.operations()) {
            org.assertj.core.api.Assertions.assertThat(operation.action())
                    .as("operation %s should not be read-only", operation.name())
                    .isNotIn(EndpointGrantAction.READ, EndpointGrantAction.EXPORT);
        }
    }
}
