package com.innospots.nexus.spring.console.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.spring.console.role.support.RoleEndpointH2Support;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestApplication;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestProperties;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestSessions;

import java.util.List;

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
    private RoleBindingOperator roleBindingOperator;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RoleBindingDao roleBindingDao;

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
    void deleteRoleRemovesBindingsInSameTransaction() {
        RoleVo role = roleOperator.createRole(new RoleCreateRequest(
                "Txn Role",
                "txn-role",
                RoleOwnerType.WORKSPACE,
                RoleEndpointTestSessions.WORKSPACE_ID,
                SecurityRealm.TENANT,
                null,
                0));
        LOGGER.info("created role roleId={}", role.roleId());

        roleBindingOperator.addRoleBindings(
                role.roleId(),
                new RoleBindingAddRequest(RoleBindingSubjectType.USER, List.of("usr-1001")));
        long bindingsBeforeDelete = roleBindingDao.selectCount(new LambdaQueryWrapper<RoleBindingEntity>()
                .eq(RoleBindingEntity::getRoleId, role.roleId()));
        LOGGER.info("bindings before delete={}", bindingsBeforeDelete);
        assertThat(bindingsBeforeDelete).isEqualTo(1L);

        roleOperator.deleteRole(role.roleId());

        long rolesLeft = roleDao.selectCount(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleId, role.roleId()));
        long bindingsLeft = roleBindingDao.selectCount(new LambdaQueryWrapper<RoleBindingEntity>()
                .eq(RoleBindingEntity::getRoleId, role.roleId()));
        LOGGER.info("after deleteRole rolesLeft={} bindingsLeft={}", rolesLeft, bindingsLeft);

        assertThat(rolesLeft).isZero();
        assertThat(bindingsLeft).isZero();
    }
}
