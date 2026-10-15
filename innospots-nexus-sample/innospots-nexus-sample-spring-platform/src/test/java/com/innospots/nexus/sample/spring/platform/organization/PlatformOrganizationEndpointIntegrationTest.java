package com.innospots.nexus.sample.spring.platform.organization;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_ORGANIZATION_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformOrganizationEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-organization";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanOrganization(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_pl_tenant", "tenant_code", "nx_pl_enterprise", "tenant_id"));
    }

    @Test
    void tenantAndEnterpriseProfileEndpointsPersistRows() throws Exception {
        String createTenantBody = """
                {
                  "tenantName": "IT Enterprise Tenant",
                  "tenantCode": "it-ent-tenant",
                  "tenantType": "ENTERPRISE"
                }
                """;
        HttpResponse<String> created = http.postJson(PlatformConstant.TENANTS_PATH, createTenantBody);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        assertThat(created.body()).contains("it-ent-tenant");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-tenant-created",
                Map.of("nx_pl_tenant", "tenant_code"));

        String tenantId = jdbcTemplate.queryForObject(
                "SELECT tenant_id FROM nx_pl_tenant WHERE tenant_code = ?",
                String.class,
                "it-ent-tenant");

        HttpResponse<String> gotTenant = http.get(PlatformConstant.TENANTS_PATH + "/" + tenantId);
        assertThat(gotTenant.statusCode()).isEqualTo(200);
        assertThat(gotTenant.body()).contains("IT Enterprise Tenant");

        String enterprisePath = PlatformConstant.TENANT_ENTERPRISE_PATH.replace("{tenantId}", tenantId);
        String upsertBody = """
                {
                  "legalName": "IT Enterprise Legal Name",
                  "creditCode": "91310000IT000000",
                  "contactName": "Ops",
                  "contactEmail": "ops@example.com"
                }
                """;
        HttpResponse<String> upserted = http.putJson(enterprisePath, upsertBody);
        assertThat(upserted.statusCode()).as(upserted.body()).isEqualTo(200);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-enterprise-upserted",
                Map.of("nx_pl_tenant", "tenant_code", "nx_pl_enterprise", "tenant_id"));

        HttpResponse<String> gotEnterprise = http.get(enterprisePath);
        assertThat(gotEnterprise.statusCode()).isEqualTo(200);
        assertThat(gotEnterprise.body()).contains("IT Enterprise Legal Name");

        Long enterpriseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_pl_enterprise WHERE tenant_id = ?",
                Long.class,
                tenantId);
        assertThat(enterpriseCount).isEqualTo(1L);
    }
}
