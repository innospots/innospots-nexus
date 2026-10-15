package com.innospots.nexus.spring.console.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.spring.console.role.support.RoleEndpointH2Support;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestApplication;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestProperties;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestSessions;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = RoleEndpointIntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RoleEndpointIntegrationTestProperties
class RoleOperatorTransactionIntegrationTest extends RoleEndpointH2Support {

    private static final Logger LOGGER = LogManager.getLogger(RoleOperatorTransactionIntegrationTest.class);

    @Autowired
    private RoleOperator roleOperator;

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
    void createRolePersistsUnderExplicitOwnership() {
        RoleVo role = roleOperator.createRole(
                "Ops",
                "ops",
                "tx-it",
                RoleOwnerType.WORKSPACE,
                RoleEndpointTestSessions.WORKSPACE_ID);
        LOGGER.info("created roleId={} code={}", role.roleId(), role.roleCode());

        ConsoleOwnership ownership = new ConsoleOwnership(
                RoleOwnerType.WORKSPACE,
                RoleEndpointTestSessions.WORKSPACE_ID,
                SecurityRealm.TENANT.name());
        RoleVo loaded = roleOperator.getRole(
                role.roleId(),
                ownership,
                RoleEndpointTestSessions.TENANT_ID,
                RoleEndpointTestSessions.WORKSPACE_ID);

        assertThat(loaded.roleCode()).isEqualTo("ops");
        assertThat(loaded.ownerType()).isEqualTo(RoleOwnerType.WORKSPACE);
    }
}
