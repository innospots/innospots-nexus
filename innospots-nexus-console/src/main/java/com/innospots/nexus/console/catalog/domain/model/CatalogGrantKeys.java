package com.innospots.nexus.console.catalog.domain.model;

import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;

/**
 * Console catalog 授权项的稳定键。
 *
 * <p>键格式为 {@code grantName:domainKey.moduleKey}；页面访问项的 grantName 为
 * {@code entryPageKey}，操作项的 grantName 为 {@code EndpointGrant.name}。</p>
 */
public final class CatalogGrantKeys {

    private CatalogGrantKeys() {
    }

    /**
     * 组装 catalog 授权项键。
     *
     * @param grantName 页面入口键或操作名
     * @param domainKey 领域键
     * @param moduleKey 模块键
     * @return 授权项键；必要字段缺失时返回 null
     */
    public static String of(String grantName, String domainKey, String moduleKey) {
        if (blank(grantName) || blank(domainKey) || blank(moduleKey)) {
            return null;
        }
        return grantName + ":" + domainKey + "." + moduleKey;
    }

    /**
     * 返回 catalog 页面资源的访问授权键。
     *
     * @param resource catalog 资源
     * @return 授权键；资源没有入口页时返回 null
     */
    public static String viewKey(ConsoleCatalogResourceEntity resource) {
        if (resource == null) {
            return null;
        }
        return of(resource.getEntryPageKey(), resource.getDomainKey(), resource.getModuleKey());
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
