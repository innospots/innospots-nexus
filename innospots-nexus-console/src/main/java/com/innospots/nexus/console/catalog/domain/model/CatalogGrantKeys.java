package com.innospots.nexus.console.catalog.domain.model;

import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;

/**
 * Console catalog 授权项的稳定键。
 *
 * <p>键格式为 {@code grantName:domainKey.moduleKey}；{@code grantName} 为
 * {@code EndpointGrant.name}（访问项对应模块 {@code readOnly}，操作项对应 {@code operable}）。</p>
 */
public final class CatalogGrantKeys {

    private CatalogGrantKeys() {
    }

    /**
     * 组装 catalog 授权项键。
     *
     * @param grantName EndpointGrant 稳定业务名
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
     * 返回资源所属模块的授权键后缀 {@code :domainKey.moduleKey}。
     *
     * @param resource catalog 资源
     * @return 后缀；必要字段缺失时返回 null
     */
    public static String moduleKeySuffix(ConsoleCatalogResourceEntity resource) {
        if (resource == null) {
            return null;
        }
        return moduleKeySuffix(resource.getDomainKey(), resource.getModuleKey());
    }

    /**
     * 返回模块授权键后缀 {@code :domainKey.moduleKey}。
     *
     * @param domainKey 领域键
     * @param moduleKey 模块键
     * @return 后缀；必要字段缺失时返回 null
     */
    public static String moduleKeySuffix(String domainKey, String moduleKey) {
        if (blank(domainKey) || blank(moduleKey)) {
            return null;
        }
        return ":" + domainKey + "." + moduleKey;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
