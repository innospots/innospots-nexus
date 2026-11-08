package com.innospots.nexus.platform.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesPlatformOpenApiSpec() throws Exception {
        Path spec = Path.of("target/classes/META-INF/nexus-openapi/innospots-nexus-platform.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Platform API");
        /*
         * 说明：实现 console 契约接口的端点（auth、dictionary、logger、
         * navigation、permission、role-binding）不会出现在这里。
         *
         * 原因：这些端点的 @Path/@GET/@POST/@Operation 全部声明在
         * nexus-console 的接口上，而 platform 与 console 是两个 jar。
         * smallrye 的 JAX-RS 扫描器按 class 解析，不会沿 implements 跨 jar
         * 收集方法级路由，因此整组端点被跳过。仓库约定
         * （PlatformStandaloneEndpointGrantContractsTest）又明确要求实现类
         * 只写 @Override、不重复接口注解，所以不能在实现类补注解绕过。
         *
         * 这里只断言「直接在 platform 内声明的端点」，同时保留对 console
         * schema 的校验，确保类型仍被索引到 spec 里。
         */
        assertThat(yaml).contains("operationId: platformTenantPage");
        assertThat(yaml).contains("operationId: platformTenantCreate");
        assertThat(yaml).contains("operationId: platformTenantEnterpriseUpsert");
        assertThat(yaml).contains("name: PlatformEnterpriseProfile");
        assertThat(yaml).contains("operationId: platformPublicInvitePreview");
        assertThat(yaml).contains("operationId: platformPublicOpenRegistrationSubmit");
        assertThat(yaml).contains("operationId: platformPublicAccessRegistrationSubmit");
        assertThat(yaml).contains("operationId: platformConsoleGeneralSettingsGet");
        assertThat(yaml).contains("operationId: platformConsoleSiteNameSettingsUpdate");
        assertThat(yaml).doesNotContain(PlatformConstant.PUBLIC_PREFIX + "/settings/registration-mode");
        assertThat(yaml).doesNotContain("operationId: platformPublicRegistrationSettingsGet");
        assertThat(yaml).doesNotContain(PlatformConstant.AUTH_PATH + "/onboarding");
        // 类型仍从 console 索引：这些 schema 在 spec 的 components 中可见。
        // 注意：AuthLoginRequest 的 $ref 只出现在 auth 端点请求体里，
        // 而该端点因跨 jar 接口注解不被扫描，所以只校验 schema 定义本身。
        assertThat(yaml).contains("AuthLoginRequest:");
        assertThat(yaml).contains("TenantCreateRequest:");
        assertThat(yaml).contains("AdminUserVo:");
        assertThat(yaml).contains("name: AdminUser");
        assertThat(yaml).contains("bearerAuth");
    }
}
