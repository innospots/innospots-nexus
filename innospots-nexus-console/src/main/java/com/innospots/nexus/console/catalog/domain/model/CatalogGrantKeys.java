package com.innospots.nexus.console.catalog.domain.model;

import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;

/**
 * Console catalog 授权项的稳定键。
 *
 * <p>统一格式为 {@code domainKey.moduleKey:grantName}；浏览分组 {@code grantName} 为
 * {@code viewGroups} 声明中的 {@code group}（通常为 {@code default}）。</p>
 */
public final class CatalogGrantKeys {

    private CatalogGrantKeys() {
    }

    /**
     * 组装 catalog / 授权表资源键 {@code domainKey.moduleKey:grantName}。
     *
     * @param grantName 浏览分组名或 EndpointGrant 稳定业务名
     * @param domainKey 领域键
     * @param moduleKey 模块键
     * @return 资源键；必要字段缺失时返回 null
     */
    public static String of(String grantName, String domainKey, String moduleKey) {
        if (blank(grantName) || blank(domainKey) || blank(moduleKey)) {
            return null;
        }
        return domainKey + "." + moduleKey + ":" + grantName;
    }

    /**
     * 浏览分组资源键（与 {@link #of} 相同，{@code grantName} 为分组键）。
     */
    public static String viewGroupKey(String domainKey, String moduleKey, String groupName) {
        return of(groupName, domainKey, moduleKey);
    }

    /**
     * 返回资源键前缀 {@code domainKey.moduleKey:}（用于按模块匹配）。
     */
    public static String moduleKeyPrefix(ConsoleCatalogResourceEntity resource) {
        if (resource == null) {
            return null;
        }
        return moduleKeyPrefix(resource.getDomainKey(), resource.getModuleKey());
    }

    /**
     * 返回模块资源键前缀 {@code domainKey.moduleKey:}。
     */
    public static String moduleKeyPrefix(String domainKey, String moduleKey) {
        if (blank(domainKey) || blank(moduleKey)) {
            return null;
        }
        return domainKey + "." + moduleKey + ":";
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
