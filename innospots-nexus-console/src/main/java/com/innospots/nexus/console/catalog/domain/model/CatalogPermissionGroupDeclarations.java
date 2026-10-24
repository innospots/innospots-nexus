package com.innospots.nexus.console.catalog.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;

/**
 * 从 catalog 持久化的分组 JSON 解析声明并生成浏览分组授权键。
 */
public final class CatalogPermissionGroupDeclarations {

    private CatalogPermissionGroupDeclarations() {
    }

    public static List<ConsolePermissionGroupDeclaration> parseGroups(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return Jsons.fromJson(json, new TypeReference<List<ConsolePermissionGroupDeclaration>>() {
        });
    }

    /**
     * 返回资源上声明的浏览分组授权键 {@code domain.module.groupName:view}。
     */
    public static List<String> viewGrantKeys(ConsoleCatalogResourceEntity resource) {
        if (resource == null) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (ConsolePermissionGroupDeclaration declaration : parseGroups(resource.getViewGroups())) {
            String key = CatalogGrantKeys.viewGroupKey(
                    resource.getDomainKey(),
                    resource.getModuleKey(),
                    declaration.group());
            if (key != null) {
                keys.add(key);
            }
        }
        return List.copyOf(keys);
    }
}
