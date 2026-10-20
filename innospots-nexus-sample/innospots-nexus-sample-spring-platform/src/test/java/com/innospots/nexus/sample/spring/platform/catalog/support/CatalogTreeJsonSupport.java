package com.innospots.nexus.sample.spring.platform.catalog.support;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.innospots.nexus.base.json.Jsons;

/**
 * 从目录树 HTTP 响应 JSON 提取资源 key。
 */
public final class CatalogTreeJsonSupport {

    private CatalogTreeJsonSupport() {
    }

    public static Set<String> extractResourceKeys(String responseBody) {
        JsonNode root = parseRoot(responseBody);
        JsonNode data = root.get("data");
        Set<String> keys = new LinkedHashSet<>();
        if (data != null && data.isArray()) {
            for (JsonNode node : data) {
                collectResourceKeys(node, keys);
            }
        }
        return keys;
    }

    public static List<String> extractModuleKeys(String responseBody) {
        return extractModuleKeys(responseBody, null);
    }

    public static List<String> extractModuleKeys(String responseBody, String ownerPluginId) {
        JsonNode root = parseRoot(responseBody);
        JsonNode data = root.get("data");
        List<String> moduleKeys = new ArrayList<>();
        if (data != null && data.isArray()) {
            for (JsonNode node : data) {
                if (ownerPluginId != null) {
                    JsonNode owner = node.get("ownerPluginId");
                    if (owner == null || !ownerPluginId.equals(owner.asText())) {
                        continue;
                    }
                }
                JsonNode moduleKey = node.get("moduleKey");
                if (moduleKey != null && moduleKey.isTextual()) {
                    moduleKeys.add(moduleKey.asText());
                }
            }
        }
        return moduleKeys;
    }

    public static Set<String> extractResourceKeys(String responseBody, String ownerPluginId) {
        JsonNode root = parseRoot(responseBody);
        JsonNode data = root.get("data");
        Set<String> keys = new LinkedHashSet<>();
        if (data != null && data.isArray()) {
            for (JsonNode node : data) {
                collectResourceKeys(node, keys, ownerPluginId);
            }
        }
        return keys;
    }

    public static JsonNode parseDataArray(String responseBody) {
        JsonNode root = parseRoot(responseBody);
        return root == null ? null : root.get("data");
    }

    static JsonNode parseRoot(String responseBody) {
        try {
            return Jsons.mapper().readTree(responseBody);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid catalog tree JSON", ex);
        }
    }

    private static void collectResourceKeys(JsonNode node, Set<String> keys) {
        collectResourceKeys(node, keys, null);
    }

    private static void collectResourceKeys(JsonNode node, Set<String> keys, String ownerPluginId) {
        if (ownerPluginId != null) {
            JsonNode owner = node.get("ownerPluginId");
            if (owner == null || !ownerPluginId.equals(owner.asText())) {
                return;
            }
        }
        JsonNode resourceKey = node.get("resourceKey");
        if (resourceKey != null && resourceKey.isTextual()) {
            keys.add(resourceKey.asText());
        }
        JsonNode children = node.get("children");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                collectResourceKeys(child, keys, ownerPluginId);
            }
        }
    }
}
