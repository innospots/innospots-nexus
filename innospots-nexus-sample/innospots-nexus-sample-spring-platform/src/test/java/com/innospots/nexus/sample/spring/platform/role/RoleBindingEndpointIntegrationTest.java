package com.innospots.nexus.sample.spring.platform.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.role.endpoint.RoleBindingEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.role.endpoint.PlatformRoleBindingEndpoint;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.role.support.RoleBindingDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PlatformRoleBindingEndpoint} REST 集成测试：H2、PLATFORM 会话，仅 USER 主体绑定。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_ROLE_BINDING_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class RoleBindingEndpointIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(RoleBindingEndpointIntegrationTest.class);

    private static final String ROLES_BASE = PlatformConstant.ROLES_PATH;

    private static final String USER_ALPHA = "plt-user-alpha";
    private static final String USER_BETA = "plt-user-beta";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleBindingEndpoint roleBindingEndpoint;

    @Autowired
    private PlatformRoleBindingEndpoint platformRoleBindingEndpoint;

    private RoleEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_permission_grant");
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        http = new RoleEndpointHttpClient(port);
        assertThat(roleBindingEndpoint).isSameAs(platformRoleBindingEndpoint);
        RoleBindingDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "00-clean");
    }

    @Test
    void addPageAndRemoveUserRoleBindings() throws Exception {
        String roleId = createRole("binding-sample", "Binding Sample Role");
        String bindingsBase = ROLES_BASE + "/" + roleId + "/bindings";

        HttpResponse<String> emptyPage = http.get(bindingsBase + "?subjectType=USER&pageNo=1&pageSize=20");
        LOGGER.info("GET {} (empty) status={} body={}", bindingsBase, emptyPage.statusCode(), emptyPage.body());
        assertThat(emptyPage.statusCode()).as(emptyPage.body()).isEqualTo(200);
        assertThat(emptyPage.body()).contains("\"records\":[]");
        RoleBindingDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "01-role-created");

        String addBody = """
                {
                  "subjectType": "USER",
                  "subjectIds": ["%s", "%s"]
                }
                """.formatted(USER_ALPHA, USER_BETA);

        HttpResponse<String> added = http.postJson(bindingsBase, addBody);
        LOGGER.info("POST {} body={} status={} response={}", bindingsBase, addBody.trim(), added.statusCode(), added.body());
        assertThat(added.statusCode()).as(added.body()).isEqualTo(200);
        RoleBindingDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "02-after-add-two-users");

        Long bindingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_role_binding WHERE role_id = ? AND subject_type = 'USER'",
                Long.class,
                roleId);
        assertThat(bindingCount).isEqualTo(2L);

        List<Map<String, Object>> dbRows = jdbcTemplate.queryForList(
                """
                SELECT binding_id, role_id, subject_type, subject_id, owner_type, owner_id, security_realm
                FROM nx_role_binding WHERE role_id = ? ORDER BY subject_id
                """,
                roleId);
        LOGGER.info("nx_role_binding rows after add: {}", Jsons.toJson(dbRows));
        assertThat(dbRows).hasSize(2);
        for (Map<String, Object> row : dbRows) {
            assertThat(row.get("subject_type")).isEqualTo("USER");
            assertThat(row.get("owner_type")).isEqualTo("PLATFORM");
            assertThat(row.get("owner_id")).isNull();
            assertThat(row.get("security_realm")).isEqualTo("PLATFORM");
        }
        assertThat(dbRows.stream().map(r -> r.get("subject_id")).map(String::valueOf).toList())
                .containsExactly(USER_ALPHA, USER_BETA);

        HttpResponse<String> duplicateAdd = http.postJson(bindingsBase, addBody);
        LOGGER.info("POST duplicate add status={} body={}", duplicateAdd.statusCode(), duplicateAdd.body());
        assertThat(duplicateAdd.statusCode()).isEqualTo(200);
        Long countAfterDuplicate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_role_binding WHERE role_id = ?",
                Long.class,
                roleId);
        assertThat(countAfterDuplicate).isEqualTo(2L);
        RoleBindingDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "03-after-duplicate-add-ignored");

        HttpResponse<String> pageAllUsers = http.get(bindingsBase + "?subjectType=USER&pageNo=1&pageSize=20");
        LOGGER.info("GET page (all USER) status={} body={}", pageAllUsers.statusCode(), pageAllUsers.body());
        assertThat(pageAllUsers.statusCode()).isEqualTo(200);
        assertThat(pageAllUsers.body()).contains(USER_ALPHA);
        assertThat(pageAllUsers.body()).contains(USER_BETA);
        assertThat(pageAllUsers.body()).contains("\"subjectType\":\"USER\"");

        HttpResponse<String> pageFiltered = http.get(
                bindingsBase + "?subjectType=USER&input=" + USER_ALPHA + "&pageNo=1&pageSize=20");
        LOGGER.info("GET page (input filter) status={} body={}", pageFiltered.statusCode(), pageFiltered.body());
        assertThat(pageFiltered.statusCode()).isEqualTo(200);
        assertThat(pageFiltered.body()).contains(USER_ALPHA);
        assertThat(pageFiltered.body()).doesNotContain(USER_BETA);

        String bindingIdAlpha = jdbcTemplate.queryForObject(
                "SELECT binding_id FROM nx_role_binding WHERE role_id = ? AND subject_id = ?",
                String.class,
                roleId,
                USER_ALPHA);
        assertThat(bindingIdAlpha).isNotBlank();

        HttpResponse<String> removedAlpha = http.exchange(
                "DELETE",
                bindingsBase + "/" + bindingIdAlpha,
                Map.of(),
                null);
        LOGGER.info(
                "DELETE {}/{} status={} body={}",
                bindingsBase,
                bindingIdAlpha,
                removedAlpha.statusCode(),
                removedAlpha.body());
        assertThat(removedAlpha.statusCode()).as(removedAlpha.body()).isEqualTo(200);
        RoleBindingDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "04-after-remove-alpha");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_role_binding WHERE role_id = ?",
                Long.class,
                roleId)).isEqualTo(1L);

        HttpResponse<String> pageAfterOneRemoved = http.get(bindingsBase + "?subjectType=USER&pageNo=1&pageSize=20");
        LOGGER.info("GET page after one removed status={} body={}", pageAfterOneRemoved.statusCode(), pageAfterOneRemoved.body());
        assertThat(pageAfterOneRemoved.body()).doesNotContain(USER_ALPHA);
        assertThat(pageAfterOneRemoved.body()).contains(USER_BETA);

        String bindingIdBeta = jdbcTemplate.queryForObject(
                "SELECT binding_id FROM nx_role_binding WHERE role_id = ? AND subject_id = ?",
                String.class,
                roleId,
                USER_BETA);
        HttpResponse<String> removedBeta = http.exchange(
                "DELETE",
                bindingsBase + "/" + bindingIdBeta,
                Map.of(),
                null);
        assertThat(removedBeta.statusCode()).isEqualTo(200);
        RoleBindingDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "05-after-remove-beta");

        HttpResponse<String> finalPage = http.get(bindingsBase + "?subjectType=USER&pageNo=1&pageSize=20");
        LOGGER.info("GET final page status={} body={}", finalPage.statusCode(), finalPage.body());
        assertThat(finalPage.body()).contains("\"records\":[]");

        LOGGER.info(
                "final nx_role_binding rows: {}",
                Jsons.toJson(jdbcTemplate.queryForList("SELECT * FROM nx_role_binding ORDER BY binding_id")));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_role_binding", Long.class)).isZero();
    }

    private String createRole(String roleCode, String roleName) throws Exception {
        String createBody = """
                {
                  "roleName": "%s",
                  "roleCode": "%s",
                  "description": "role binding IT"
                }
                """.formatted(roleName, roleCode);

        HttpResponse<String> created = http.postJson(ROLES_BASE, createBody);
        LOGGER.info("POST role status={} body={}", created.statusCode(), created.body());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);

        return jdbcTemplate.queryForObject(
                "SELECT role_id FROM nx_role WHERE role_code = ?",
                String.class,
                roleCode);
    }
}
