package com.innospots.nexus.sample.spring.platform.permission.support;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
/**
 * 从启动同步后的目录索引解析 platform 插件真实资源 ID。
 */
public final class SyncedCatalogResourceIds {

    private static final String USER_MENU_KEY = "menu:user.user-main";

    private static final String USER_PAGE_KEY = "page:user."
            + ConsoleModuleDescriptor.compositePageKey(
                    BuiltinPlatformEntryPlugins.DOMAIN_KEY, "user", "main");

    private SyncedCatalogResourceIds() {
    }

    public static UserMainMenuPageIds requireUserMainMenuAndPage(JdbcTemplate jdbcTemplate) {
        String menuId = requireResourceId(jdbcTemplate, USER_MENU_KEY);
        String pageId = requireResourceId(jdbcTemplate, USER_PAGE_KEY);
        return new UserMainMenuPageIds(menuId, pageId, USER_MENU_KEY, USER_PAGE_KEY);
    }

    private static String requireResourceId(JdbcTemplate jdbcTemplate, String resourceKey) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                SELECT resource_id FROM nx_console_catalog_resource
                WHERE resource_key = ? AND status = 'ENABLED'
                """,
                resourceKey);
        Object id = row.get("resource_id");
        if (id == null || id.toString().isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "catalog resource missing: " + resourceKey);
        }
        return id.toString();
    }

    public record UserMainMenuPageIds(
            String menuResourceId,
            String pageResourceId,
            String menuResourceKey,
            String pageResourceKey
    ) {
    }
}
