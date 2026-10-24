package com.innospots.nexus.sample.spring.platform.support;

import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.service.CredentialService;

/**
 * 平台 sample 集成测试共享常量与清理/种子辅助。
 */
public final class PlatformIntegrationTestSupport {

    public static final String SPRING_CONFIG_IMPORT =
            "optional:classpath:nexus/console-auth.yaml,optional:classpath:nexus/console-credential.yaml,"
                    + "optional:classpath:nexus/console-registration.yaml,"
                    + "optional:classpath:nexus/console-web.yaml,optional:classpath:nexus/console-openapi.yaml,"
                    + "optional:classpath:nexus/console-plugins.yaml";

    public static final String DEV_PLATFORM_USER_ID = "1";

    private static final String H2_SUFFIX = ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    public static final String H2_URL_DICTIONARY_IT =
            "jdbc:h2:mem:nexus-sample-platform-dictionary-it" + H2_SUFFIX;

    public static final String H2_URL_DICTIONARY_AUDIT_IT =
            "jdbc:h2:mem:nexus-sample-platform-dictionary-audit-it" + H2_SUFFIX;

    public static final String H2_URL_ORGANIZATION_IT =
            "jdbc:h2:mem:nexus-sample-platform-organization-it" + H2_SUFFIX;

    public static final String H2_URL_SETTINGS_IT =
            "jdbc:h2:mem:nexus-sample-platform-settings-it" + H2_SUFFIX;

    public static final String H2_URL_USER_IT = "jdbc:h2:mem:nexus-sample-platform-user-it" + H2_SUFFIX;

    public static final String H2_URL_ACCESS_IT = "jdbc:h2:mem:nexus-sample-platform-access-it" + H2_SUFFIX;

    public static final String H2_URL_INVITE_IT = "jdbc:h2:mem:nexus-sample-platform-invite-it" + H2_SUFFIX;

    public static final String H2_URL_AUDIT_LOG_IT =
            "jdbc:h2:mem:nexus-sample-platform-audit-log-it" + H2_SUFFIX;

    public static final String H2_URL_AUTH_SESSION_IT =
            "jdbc:h2:mem:nexus-sample-platform-auth-session-it" + H2_SUFFIX;

    public static final String H2_URL_ROLE_IT = "jdbc:h2:mem:nexus-sample-platform-role-it" + H2_SUFFIX;

    public static final String H2_URL_ROLE_BINDING_IT =
            "jdbc:h2:mem:nexus-sample-platform-role-binding-it" + H2_SUFFIX;

    public static final String H2_URL_PERMISSION_IT =
            "jdbc:h2:mem:nexus-sample-platform-permission-endpoints-it" + H2_SUFFIX;

    public static final String H2_URL_NAVIGATION_IT =
            "jdbc:h2:mem:nexus-sample-platform-navigation-it" + H2_SUFFIX;

    public static final String H2_URL_PUBLIC_AUTH_IT =
            "jdbc:h2:mem:nexus-sample-platform-public-auth-it" + H2_SUFFIX;

    public static final String H2_URL_PUBLIC_INVITE_IT =
            "jdbc:h2:mem:nexus-sample-platform-public-invite-it" + H2_SUFFIX;

    public static final String H2_URL_PUBLIC_OPEN_REGISTRATION_IT =
            "jdbc:h2:mem:nexus-sample-platform-public-open-registration-it" + H2_SUFFIX;

    public static final String H2_URL_PUBLIC_ACCESS_REGISTRATION_IT =
            "jdbc:h2:mem:nexus-sample-platform-public-access-registration-it" + H2_SUFFIX;

    public static final String H2_URL_PUBLIC_PASSWORD_RESET_IT =
            "jdbc:h2:mem:nexus-sample-platform-public-password-reset-it" + H2_SUFFIX;

    public static final String H2_URL_ACCESS_APPROVAL_E2E_IT =
            "jdbc:h2:mem:nexus-sample-platform-access-approval-e2e-it" + H2_SUFFIX;

    public static final String H2_URL_ENDPOINT_GRANT_IT =
            "jdbc:h2:mem:nexus-sample-platform-endpoint-grant-it" + H2_SUFFIX;

    public static final String H2_URL_CATALOG_TREE_OPS_IT =
            "jdbc:h2:mem:nexus-sample-platform-catalog-tree-ops-it" + H2_SUFFIX;

    public static final String H2_URL_CATALOG_SYNC_DB_IT =
            "jdbc:h2:mem:nexus-sample-platform-catalog-sync-db-it" + H2_SUFFIX;

    public static final String H2_URL_PERMISSION_GRANT_FLOW_IT =
            "jdbc:h2:mem:nexus-sample-platform-permission-grant-flow-it" + H2_SUFFIX;

    private static final String PLATFORM_IT_ADMIN_ROLE_ID = "ROLITPLATFORMADMIN0001";

    private static final String PLATFORM_IT_ADMIN_ROLE_CODE = "it-platform-admin";

    private PlatformIntegrationTestSupport() {
    }

    /**
     * 插入平台管理员角色并绑定指定用户，供 security.enabled=true 时通过 HTTP 完成角色/授权配置。
     *
     * @return 管理员角色 ID
     */
    public static String ensurePlatformAdministratorRoleAndBindUser(
            JdbcTemplate jdbcTemplate,
            String platformUserId) {
        jdbcTemplate.update("DELETE FROM nx_role WHERE role_id = ?", PLATFORM_IT_ADMIN_ROLE_ID);
        jdbcTemplate.update(
                """
                INSERT INTO nx_role (
                    role_id, role_name, role_code, owner_type, owner_id, security_realm,
                    description, status, sort_order, built_in, administrator, created_at, updated_at
                ) VALUES (
                    ?, 'IT Platform Admin', ?, 'PLATFORM', NULL, 'PLATFORM',
                    'integration test bootstrap', 'ENABLED', 0, FALSE, TRUE,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                PLATFORM_IT_ADMIN_ROLE_ID,
                PLATFORM_IT_ADMIN_ROLE_CODE);
        replacePlatformUserRoleBindings(jdbcTemplate, platformUserId, PLATFORM_IT_ADMIN_ROLE_ID);
        return PLATFORM_IT_ADMIN_ROLE_ID;
    }

    public static void replacePlatformUserRoleBindings(
            JdbcTemplate jdbcTemplate,
            String platformUserId,
            String roleId) {
        jdbcTemplate.update(
                "DELETE FROM nx_role_binding WHERE subject_type = 'USER' AND subject_id = ?",
                platformUserId);
        jdbcTemplate.update(
                """
                INSERT INTO nx_role_binding (
                    binding_id, role_id, subject_type, subject_id, owner_type, owner_id,
                    security_realm, created_at, updated_at
                ) VALUES (
                    ?, ?, 'USER', ?, 'PLATFORM', NULL, 'PLATFORM',
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                "RBN" + roleId.substring(3),
                roleId,
                platformUserId);
    }

    public static void cleanRolePermissionGraph(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_permission_grant");
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
    }

    public static void cleanDictionary(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_dictionary_item");
        jdbcTemplate.execute("DELETE FROM nx_dictionary_type");
    }

    public static void cleanOrganization(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_pl_enterprise");
        jdbcTemplate.execute("DELETE FROM nx_pl_tenant_admin");
        jdbcTemplate.execute("DELETE FROM nx_pl_tenant");
    }

    public static void cleanRegistrationSettings(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_system_setting WHERE setting_domain = 'platform'");
    }

    public static void cleanPlatformUsers(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_user_credential WHERE security_realm = 'PLATFORM'");
        jdbcTemplate.execute("DELETE FROM nx_pl_user");
    }

    public static void cleanAccessRequests(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_pl_access_request");
        jdbcTemplate.execute("DELETE FROM nx_user_credential WHERE security_realm = 'PLATFORM'");
        jdbcTemplate.execute("DELETE FROM nx_pl_user");
    }

    public static void cleanInvites(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_pl_invite");
    }

    public static void cleanAuditLogs(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM nx_audit_log");
    }

    /**
     * 与 console-web dev-session {@code user-id: "1"} 对齐的平台用户及本地密码。
     */
    public static void ensureDevSessionPlatformUser(
            JdbcTemplate jdbcTemplate,
            CredentialService credentialService,
            String rawPassword) {
        jdbcTemplate.update(
                "DELETE FROM nx_user_credential WHERE subject_id = ? AND security_realm = 'PLATFORM'",
                DEV_PLATFORM_USER_ID);
        jdbcTemplate.update("DELETE FROM nx_pl_user WHERE platform_user_id = ?", DEV_PLATFORM_USER_ID);
        jdbcTemplate.update(
                """
                INSERT INTO nx_pl_user (
                    platform_user_id, login_name, display_name, status, created_at, updated_at
                ) VALUES (?, 'dev', 'Dev', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                DEV_PLATFORM_USER_ID);
        credentialService.enrollPassword(SecurityRealm.PLATFORM, DEV_PLATFORM_USER_ID, rawPassword);
    }

    public static void insertPendingApprovalUser(JdbcTemplate jdbcTemplate, String platformUserId, String loginName) {
        jdbcTemplate.update(
                """
                INSERT INTO nx_pl_user (
                    platform_user_id, login_name, display_name, status, created_at, updated_at
                ) VALUES (?, ?, ?, 'PENDING_APPROVAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                platformUserId,
                loginName,
                loginName);
    }

    public static void insertPendingAccessRequest(
            JdbcTemplate jdbcTemplate,
            String accessRequestId,
            String platformUserId,
            String loginName,
            String applicantName) {
        jdbcTemplate.update(
                """
                INSERT INTO nx_pl_access_request (
                    access_request_id, applicant_name, login_name, platform_user_id, status,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, 'PENDING_APPROVAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                accessRequestId,
                applicantName,
                loginName,
                platformUserId);
    }

    public static void seedAuditLog(JdbcTemplate jdbcTemplate, String auditLogId) {
        jdbcTemplate.update(
                """
                INSERT INTO nx_audit_log (
                    audit_log_id, action, target, path, operated_time, actor, message, status_code,
                    execution_result, owner_type, owner_id, security_realm, created_at, updated_at
                ) VALUES (
                    ?, 'READ', 'platform.test', '/api/d/platform/sample', CURRENT_TIMESTAMP, 'dev',
                    'seeded for IT', '200', 'SUCCESS', 'PLATFORM', NULL, 'PLATFORM',
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                auditLogId);
    }
}
