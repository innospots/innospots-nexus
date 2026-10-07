package com.innospots.nexus.spring.console.role;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.spring.console.role.support.RoleEndpointH2Support;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestApplication;
import com.innospots.nexus.spring.console.role.support.RoleEndpointIntegrationTestProperties;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestHttpClient;
import com.innospots.nexus.spring.console.role.support.RoleEndpointTestSessions;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = RoleEndpointIntegrationTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RoleEndpointIntegrationTestProperties
class RoleEndpointHttpIntegrationTest extends RoleEndpointH2Support {

    private static final Logger LOGGER = LogManager.getLogger(RoleEndpointHttpIntegrationTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleEndpoint roleEndpoint;

    private RoleEndpointTestHttpClient http;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM nx_role_binding");
        jdbcTemplate.execute("DELETE FROM nx_role");
        http = new RoleEndpointTestHttpClient(port);
        // 触发 @Lazy 端点 Bean 初始化，供 Jersey Spring 桥接解析
        assertThat(roleEndpoint).isNotNull();
    }

    @Test
    void getRoleOptionsReturnsJsonThroughJersey() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO nx_role (
                    role_id, role_name, role_code, owner_type, owner_id, security_realm,
                    status, sort_order, built_in, administrator)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "rol-http-1",
                "HTTP Role",
                "http-role",
                "WORKSPACE",
                RoleEndpointTestSessions.WORKSPACE_ID,
                "TENANT",
                "ENABLED",
                1,
                false,
                false);

        HttpResponse<String> options = http.get("/api/nexus/roles/options");
        LOGGER.info("GET /api/nexus/roles/options status={} body={}", options.statusCode(), options.body());

        assertThat(options.statusCode()).as(options.body()).isEqualTo(200);
        assertThat(options.body()).contains("\"success\":true");
        assertThat(options.body()).contains("http-role");
    }
}
