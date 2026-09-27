package com.innospots.nexus.spring.console.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.spring.console.role.support.RoleEndpointH2Support;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestApplication;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestProperties;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestSessions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = RoleEndpointIntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RoleEndpointIntegrationTestProperties
class RoleEndpointDirectIntegrationTest extends RoleEndpointH2Support {

    private static final Logger LOGGER = LogManager.getLogger(RoleEndpointDirectIntegrationTest.class);

    @Autowired
    private RoleEndpoint roleEndpoint;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        RoleEndpointTestSessions.bindWorkspaceScope();
    }

    @AfterEach
    void tearDown() {
        RoleEndpointTestSessions.clear();
    }

    @Test
    void createRolePersistsAndGetReturnsSameRole() {
        RoleCreateRequest request = new RoleCreateRequest(
                "Operators",
                "operators",
                RoleOwnerType.WORKSPACE,
                RoleEndpointTestSessions.WORKSPACE_ID,
                SecurityRealm.TENANT,
                "integration",
                10);

        R<RoleVo> created = roleEndpoint.createRole(request);
        LOGGER.info("createRole response success={} roleId={} roleCode={}",
                created.success(), created.data().roleId(), created.data().roleCode());

        assertThat(created.success()).isTrue();
        assertThat(created.data().roleCode()).isEqualTo("operators");

        R<RoleVo> loaded = roleEndpoint.getRole(created.data().roleId());
        LOGGER.info("getRole response roleName={} memberCount={}",
                loaded.data().roleName(), loaded.data().memberCount());

        assertThat(loaded.data().roleName()).isEqualTo("Operators");
        assertThat(loaded.data().memberCount()).isZero();
    }

    @Test
    void createRoleRejectsDuplicateRoleCode() {
        RoleCreateRequest request = new RoleCreateRequest(
                "First",
                "dup-code",
                RoleOwnerType.WORKSPACE,
                RoleEndpointTestSessions.WORKSPACE_ID,
                SecurityRealm.TENANT,
                null,
                1);
        roleEndpoint.createRole(request);

        assertThatThrownBy(() -> roleEndpoint.createRole(request))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(RoleStatusCode.ROLE_CODE_DUPLICATED.fullCode());

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_role WHERE role_code = ?", Long.class, "dup-code");
        LOGGER.info("duplicate create rejected; persisted rows for dup-code={}", count);
        assertThat(count).isEqualTo(1L);
    }
}
