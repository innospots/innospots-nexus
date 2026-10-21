package com.innospots.nexus.sample.spring.platform.permission.support;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 从启动同步后的目录索引解析 platform 插件真实的页面资源 ID。
 *
 * <p>单页模块是携带 {@code entryPageKey} 的叶子 MODULE；多页模块仍可显式声明 MENU。</p>
 */
public final class SyncedCatalogResourceIds {

    private static final String USER_MENU_KEY = "module:user";

    private static final String ROLE_MENU_KEY = "module:role";

    private SyncedCatalogResourceIds() {
    }

    public static UserAndRoleMenus requireUserAndRoleMenus(JdbcTemplate jdbcTemplate) {
        ResourceRow user = requireResource(jdbcTemplate, USER_MENU_KEY);
        ResourceRow role = requireResource(jdbcTemplate, ROLE_MENU_KEY);
        return new UserAndRoleMenus(
                user.resourceId(),
                role.resourceId(),
                USER_MENU_KEY,
                ROLE_MENU_KEY,
                user.viewGrantKey(),
                role.viewGrantKey());
    }

    private static ResourceRow requireResource(JdbcTemplate jdbcTemplate, String resourceKey) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT resource_id, entry_page_key, domain_key, module_key
                FROM nx_console_catalog_resource
                WHERE resource_key = ? AND status = 'ENABLED'
                """,
                resourceKey);
        Object id = row.get("resource_id");
        if (id == null || id.toString().isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "catalog resource missing: " + resourceKey);
        }
        Object entryPageKey = row.get("entry_page_key");
        if (entryPageKey == null || entryPageKey.toString().isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "catalog resource has no page grant key: " + resourceKey);
        }
        return new ResourceRow(
                id.toString(),
                entryPageKey + ":" + row.get("domain_key") + "." + row.get("module_key"));
    }

    private record ResourceRow(String resourceId, String viewGrantKey) {
    }

    public record UserAndRoleMenus(
            String userMenuResourceId,
            String roleMenuResourceId,
            String userMenuResourceKey,
            String roleMenuResourceKey,
            String userMenuViewGrantKey,
            String roleMenuViewGrantKey
    ) {
    }
}
